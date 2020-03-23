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
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionNames;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionSpec;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.syndesis.connector.kafka.model.crd.Listener;
import io.syndesis.connector.kafka.model.crd.Address;
import io.syndesis.connector.kafka.model.crd.Kafka;
import io.syndesis.connector.kafka.model.crd.KafkaResourceDoneable;
import io.syndesis.connector.kafka.model.crd.KafkaResourceList;
import io.syndesis.connector.support.verifier.api.ComponentMetadataRetrieval;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import io.syndesis.connector.support.verifier.api.SyndesisMetadataProperties;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaMetaDataRetrieval extends ComponentMetadataRetrieval {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaMetaDataRetrieval.class);
    public static final String GROUP = "kafka.strimzi.io";
    public static final String PLURAL = "kafkas";

    /**
     * TODO: use local extension, remove when switching to camel 2.22.x
     */
    @Override
    protected MetaDataExtension resolveMetaDataExtension(CamelContext context,
                                                         Class<? extends MetaDataExtension> metaDataExtensionClass,
                                                         String componentId, String actionId) {
        return new KafkaMetaDataExtension(context);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected SyndesisMetadata adapt(CamelContext context, String componentId, String actionId,
                                     Map<String, Object> properties, MetaDataExtension.MetaData metadata) {
        Set<String> topicsNames = (Set<String>) metadata.getPayload();

        List<PropertyPair> topicsResult = new ArrayList<>();
        topicsNames.forEach(
            t -> topicsResult.add(new PropertyPair(t, t))
        );

        return SyndesisMetadata.of(
            Collections.singletonMap("topic", topicsResult)
        );
    }

    /**
     * Query the strimzi brokers available on this kubernetes environment
     * to suggest auto discovered urls.
     */
    @Override
    public SyndesisMetadataProperties fetchProperties(CamelContext context, String componentId,
                                                      Map<String, Object> properties) {
        List<PropertyPair> brokers = new ArrayList<>();
        try (KubernetesClient client = createKubernetesClient()) {
            client.customResourceDefinitions().list().getItems()
                .stream().filter(KafkaMetaDataRetrieval::isKafkaCustomResourceDefinition)
                .forEach(kafka -> processKafkaCustomResourceDefinition(brokers, client, kafka));
        } catch (Exception t) {
            LOG.warn("Couldn't auto discover any broker.");
            LOG.debug("Couldn't auto discover any broker.", t);
        }

        Map<String, List<PropertyPair>> dynamicProperties = new HashMap<>();
        dynamicProperties.put("brokers", brokers);
        return new SyndesisMetadataProperties(dynamicProperties);
    }

    KubernetesClient createKubernetesClient() {
        return new DefaultKubernetesClient();
    }

    /**
     * For each Kafka container definition found, process it.
     *
     * @param brokers
     * @param client
     * @param crd
     */
    private static void processKafkaCustomResourceDefinition(List<PropertyPair> brokers, KubernetesClient client, CustomResourceDefinition crd) {
        KafkaResourceList list = client.customResources(crd,
            Kafka.class,
            KafkaResourceList.class,
            KafkaResourceDoneable.class).inAnyNamespace().list();

        for (Kafka item : list.getItems()) {
            processKafkaResource(brokers, item);
        }
    }

    /**
     * For each Kafka resource found on Kubernetes, extract the listeners and
     * add them to the brokers list.
     *
     * @param brokers
     * @param item
     */
    private static void processKafkaResource(List<PropertyPair> brokers, Kafka item) {
        //Extract an identifier of this broker
        final ObjectMeta metadata = item.getMetadata();
        List<Listener> listeners = item.getStatus().getListeners();
        listeners.stream().filter(KafkaMetaDataRetrieval::typesAllowed).forEach(
            listener -> getAddress(
                                    listener,
                                    brokers,
                                    String.format("%s::%s (%s)", metadata.getNamespace(), metadata.getName(), listener.getType()))
        );
    }

    /**
     * Get the list of addresses for this connection and add it to the brokers list.
     *
     * @param listener metadata for this broker
     * @param brokers  list where all brokers are going to be added
     * @param name     identifier of this broker
     */
    private static void getAddress(final Listener listener,
                                   final List<PropertyPair> brokers,
                                   final String name) {
        StringBuilder add = new StringBuilder();

        List<Address> addresses = listener.getAddresses();
        for (Address a : addresses) {
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
     * Used to filter which types of connections are we interested in. Right now, only plain connections.
     */
    private static boolean typesAllowed(final Listener listener) {
        return "plain".equalsIgnoreCase(listener.getType()) ||
            "tls".equalsIgnoreCase(listener.getType());
    }

    /**
     * Used to filter brokers. Right now, based on GROUP and PLURAL.
     */
    static boolean isKafkaCustomResourceDefinition(final CustomResourceDefinition crd) {
        final CustomResourceDefinitionSpec spec = crd.getSpec();

        final String group = spec.getGroup();

        final CustomResourceDefinitionNames names = spec.getNames();
        final String plural = names.getPlural();

        return GROUP.equalsIgnoreCase(group)
            && PLURAL.equalsIgnoreCase(plural);
    }
}
