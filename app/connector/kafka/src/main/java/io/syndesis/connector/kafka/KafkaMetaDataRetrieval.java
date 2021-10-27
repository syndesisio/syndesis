/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.kafka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.V1ApiextensionAPIGroupDSL;
import io.fabric8.kubernetes.client.V1beta1ApiextensionAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.ApiextensionsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.syndesis.connector.kafka.model.crd.Address;
import io.syndesis.connector.kafka.model.crd.Kafka;
import io.syndesis.connector.kafka.model.crd.KafkaResourceDoneable;
import io.syndesis.connector.kafka.model.crd.KafkaResourceList;
import io.syndesis.connector.kafka.model.crd.Listener;
import io.syndesis.connector.support.verifier.api.ComponentMetadataRetrieval;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import io.syndesis.connector.support.verifier.api.SyndesisMetadataProperties;

import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaMetaDataRetrieval extends ComponentMetadataRetrieval {

    // sort versions, newest first
    static final CustomResourceDefinitionContext[] KAFKA_CRDS = Stream.of("v1beta2", "v1beta1")
        .map(v -> new CustomResourceDefinitionContext.Builder()
            .withGroup("kafka.strimzi.io")
            .withVersion(v)
            .withScope("Namespaced")
            .withName("kafkas.kafka.strimzi.io")
            .withPlural("kafkas")
            .withKind("Kafka")
            .build())
        .toArray(CustomResourceDefinitionContext[]::new);

    private static final Logger LOG = LoggerFactory.getLogger(KafkaMetaDataRetrieval.class);

    /**
     * Query the strimzi brokers available on this kubernetes environment to
     * suggest auto discovered urls.
     */
    @Override
    public SyndesisMetadataProperties fetchProperties(final CamelContext context, final String componentId, final Map<String, Object> properties) {
        final List<PropertyPair> brokers = new ArrayList<>();

        try (KubernetesClient client = createKubernetesClient();
            ApiextensionsAPIGroupDSL apiextensions = client.apiextensions();
            V1beta1ApiextensionAPIGroupDSL v1beta1 = apiextensions.v1beta1();
            V1ApiextensionAPIGroupDSL v1 = apiextensions.v1()) {

            final CustomResourceDefinition v1beta1CRD = v1beta1.customResourceDefinitions().withName("kafkas.kafka.strimzi.io").get();
            final io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition v1CRD = v1.customResourceDefinitions()
                .withName("kafkas.kafka.strimzi.io").get();

            if (v1beta1CRD != null || v1CRD != null) {
                for (final CustomResourceDefinitionContext crd : KAFKA_CRDS) {
                    try {
                        final KafkaResourceList kafkaList = client.customResources(crd, Kafka.class, KafkaResourceList.class, KafkaResourceDoneable.class)
                            .inAnyNamespace().list();
                        kafkaList.getItems().forEach(kafka -> processKafkaResource(brokers, kafka));
                    } catch (KubernetesClientException e) {
                        LOG.debug("Could not list: " + crd.getGroup() + "/" + crd.getName(), e);
                    }
                }
            }

        } catch (final Exception t) {
            LOG.warn("Couldn't auto discover any kafka broker.", t);
        }

        final Map<String, List<PropertyPair>> dynamicProperties = new HashMap<>();
        dynamicProperties.put("brokers", brokers);
        return new SyndesisMetadataProperties(dynamicProperties);
    }

    KubernetesClient createKubernetesClient() {
        return new DefaultKubernetesClient();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected SyndesisMetadata adapt(final CamelContext context, final String componentId, final String actionId,
        final Map<String, Object> properties, final MetaDataExtension.MetaData metadata) {
        final Set<String> topicsNames = (Set<String>) metadata.getPayload();

        final List<PropertyPair> topicsResult = new ArrayList<>();
        topicsNames.forEach(
            t -> topicsResult.add(new PropertyPair(t, t)));

        return SyndesisMetadata.of(
            Collections.singletonMap("topic", topicsResult));
    }

    /**
     * TODO: use local extension, remove when switching to camel 2.22.x
     */
    @Override
    protected MetaDataExtension resolveMetaDataExtension(final CamelContext context,
        final Class<? extends MetaDataExtension> metaDataExtensionClass,
        final String componentId, final String actionId) {
        return new KafkaMetaDataExtension(context);
    }

    /**
     * Get the list of addresses for this connection and add it to the brokers
     * list.
     *
     * @param listener metadata for this broker
     * @param brokers list where all brokers are going to be added
     * @param name identifier of this broker
     */
    private static void getAddress(final Listener listener,
        final List<PropertyPair> brokers,
        final String name) {
        final StringBuilder add = new StringBuilder();

        final List<Address> addresses = listener.getAddresses();
        for (final Address a : addresses) {
            if (add.length() > 0) {
                add.append(',');
            }
            add.append(a.getHost());
            add.append(':');
            add.append(a.getPort());
        }

        brokers.add(new PropertyPair(add.toString(), name));
    }

    /**
     * For each Kafka resource found on Kubernetes, extract the listeners and
     * add them to the brokers list.
     */
    private static void processKafkaResource(final List<PropertyPair> brokers, final Kafka item) {
        // Extract an identifier of this broker
        final ObjectMeta metadata = item.getMetadata();
        final List<Listener> listeners = item.getStatus().getListeners();
        listeners.stream().filter(KafkaMetaDataRetrieval::typesAllowed).forEach(
            listener -> getAddress(
                listener,
                brokers,
                String.format("%s::%s (%s)", metadata.getNamespace(), metadata.getName(), listener.getType())));
    }

    /**
     * Used to filter which types of connections are we interested in. Right
     * now, only plain connections.
     */
    private static boolean typesAllowed(final Listener listener) {
        return "plain".equalsIgnoreCase(listener.getType()) ||
            "tls".equalsIgnoreCase(listener.getType());
    }
}
