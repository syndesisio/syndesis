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
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.syndesis.connector.support.verifier.api.ComponentMetadataRetrieval;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import io.syndesis.connector.support.verifier.api.SyndesisMetadataProperties;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;

public class KafkaMetaDataRetrieval extends ComponentMetadataRetrieval {

    /**
     * Used to filter which types of connections are we interested in. Right now, only plain connections.
     */
    private final Predicate<? super Map<?, ?>>
        typesAllowed = listener -> listener.get("type").toString().equalsIgnoreCase("plain");

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
     * Query the strimzi brokers available on this kubernetes environment to suggest auto discovered urls.
     */
    @Override
    @SuppressWarnings("unchecked")
    public SyndesisMetadataProperties fetchProperties(CamelContext context, String componentId,
                                                      Map<String, Object> properties) {
        List<PropertyPair> brokers = new ArrayList<>();
        try (KubernetesClient client = new DefaultKubernetesClient()) {

            //Filter by strimzi resources
            final CustomResourceDefinitionContext build = new CustomResourceDefinitionContext.Builder()
                                                              .withGroup("kafka.strimzi.io")
                                                              .withName("kafkas.kafka.strimzi.io")
                                                              .withPlural("kafkas")
                                                              .withScope("Namespaced")
                                                              .withVersion("v1beta1")
                                                              .build();

            final List<Map<String, ?>> items = (List<Map<String, ?>>) client.customResource(build).list().get("items");

            for (Map<String, ?> item : items) {
                //Extract an identifier of this broker
                final Map<?, ?> metadata = (Map<?, ?>) item.get("metadata");
                String id = metadata.get("namespace") + "::" + metadata.get("name");

                //Add the list of addresses for this item
                Map<?, ?> status = (Map<?, ?>) item.get("status");
                List<Map<String,
                            List<Map<String, Object>>>> listeners =
                    (List<Map<String, List<Map<String, Object>>>>) status.get("listeners");
                listeners.stream().filter(typesAllowed).forEach(
                    listener -> getAddress(listener, brokers, id));
            }
        }

        Map<String, List<PropertyPair>> dynamicProperties = new HashMap<>();
        dynamicProperties.put("brokers", brokers);
        return new SyndesisMetadataProperties(dynamicProperties);
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
