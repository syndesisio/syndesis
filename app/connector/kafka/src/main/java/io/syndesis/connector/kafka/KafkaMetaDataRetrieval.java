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
import java.util.function.Predicate;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.syndesis.connector.kafka.model.crd.KafkaResource;
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
     * Used to filter which types of connections are we interested in. Right now, only plain connections.
     */
    private final Predicate<? super Map<?, ?>>
        typesAllowed = listener -> listener.get("type").toString().equalsIgnoreCase("plain");

    /**
     * Used to filter brokers. Right now, based on GROUP and PLURAL.
     */
    private final Predicate<? super CustomResourceDefinition> isKafkaBroker =
        crd -> crd.getSpec().getGroup().equalsIgnoreCase(GROUP)
                   && crd.getSpec().getNames().getPlural().equalsIgnoreCase(PLURAL);

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
        try {
            Set<String> topicsNames = (Set<String>) metadata.getPayload();

            List<PropertyPair> topicsResult = new ArrayList<>();
            topicsNames.forEach(
                t -> topicsResult.add(new PropertyPair(t, t))
            );

            return SyndesisMetadata.of(
                Collections.singletonMap("topic", topicsResult)
            );
        } catch (Exception e) {
            return SyndesisMetadata.EMPTY;
        }
    }

    /**
     * Query the strimzi brokers available on this kubernetes environment
     * to suggest auto discovered urls.
     */
    @Override
    public SyndesisMetadataProperties fetchProperties(CamelContext context, String componentId,
                                                      Map<String, Object> properties) {
        List<PropertyPair> brokers = new ArrayList<>();
        try (KubernetesClient client = new DefaultKubernetesClient()) {
            client.customResourceDefinitions().list().getItems()
                .stream().filter(isKafkaBroker)
                .forEach(kafka -> processKafkaCRD(brokers, client, kafka));
        } catch (Throwable t) {
            LOG.warn("Couldn't auto discover any broker.");
            LOG.debug("Couldn't auto discover any broker.", t);
        }

        Map<String, List<PropertyPair>> dynamicProperties = new HashMap<>();
        dynamicProperties.put("brokers", brokers);
        return new SyndesisMetadataProperties(dynamicProperties);
    }

    /**
     * For each Kafka container definition found, process it.
     *
     * @param brokers
     * @param client
     * @param crd
     */
    private void processKafkaCRD(List<PropertyPair> brokers, KubernetesClient client, CustomResourceDefinition crd) {
        KafkaResourceList list = client.customResources(crd,
            KafkaResource.class,
            KafkaResourceList.class,
            KafkaResourceDoneable.class).inAnyNamespace().list();

        for (KafkaResource item : list.getItems()) {
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
    @SuppressWarnings("unchecked")
    private void processKafkaResource(List<PropertyPair> brokers, KafkaResource item) {
        //Extract an identifier of this broker
        final ObjectMeta metadata = item.getMetadata();
        String id = metadata.getNamespace() + "::" + metadata.getName();

        List<Map<String, List<Map<String, Object>>>> listeners =
            (List<Map<String, List<Map<String, Object>>>>) item.getStatus().get("listeners");

        listeners.stream().filter(typesAllowed).forEach(
            listener -> getAddress(listener, brokers, id));
    }

    /**
     * Get the list of addresses for this connection and add it to the brokers list.
     *
     * @param listener metadata for this broker
     * @param brokers  list where all brokers are going to be added
     * @param name     identifier of this broker
     */
    private static void getAddress(final Map<String, List<Map<String, Object>>> listener,
                                   final List<PropertyPair> brokers,
                                   final String name) {
        StringBuilder add = new StringBuilder();

        List<Map<String, Object>> addresses = listener.get("addresses");
        for (Map<String, Object> a : addresses) {
            if (add.length() > 0) {
                add.append(',');
            }
            add.append(a.get("host"));
            add.append(':');
            add.append(a.get("port"));
        }

        brokers.add(new PropertyPair(add.toString(), name));
    }
}
