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
package io.syndesis.verifier.v1;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import io.syndesis.verifier.api.MetadataAdapter;
import io.syndesis.verifier.api.SyndesisMetadata;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.component.extension.MetaDataExtension.MetaData;

class MetadataEndpoint {

    private final CamelContext camelContext;
    private final MetadataAdapter adapter;
    private final String connectorId;

    private static final Set<String> CONNECTORS_WITH_DIFFERENT_METADATA_PER_ACTION =
            new HashSet<>(Arrays.asList("sql"));

    MetadataEndpoint(final CamelContext camelContext, final String connectorId, final MetadataAdapter adapter) {
        this.camelContext = camelContext;
        this.connectorId = connectorId;
        this.adapter = adapter;
    }

    protected final SyndesisMetadata fetchMetadata(final String actionId, final Map<String, Object> properties) {
        try {
            //TODO Kurt
            String componentId = connectorId;
            if (CONNECTORS_WITH_DIFFERENT_METADATA_PER_ACTION.contains(connectorId)) {
                componentId = actionId;
            }
            final MetaDataExtension metadataExtension = camelContext.getComponent(componentId, true, false)
                .getExtension(MetaDataExtension.class)
                .orElseThrow(() -> new IllegalArgumentException("No Metadata extension present for action: " + actionId));

            final Map<String, Object> propertiesForMetadataExtension = properties.entrySet().stream()
                .filter(e -> e.getValue() != null).collect(Collectors.toMap(Entry::getKey, Entry::getValue));

            final MetaData metaData = metadataExtension.meta(propertiesForMetadataExtension)
                .orElseThrow(() -> new IllegalArgumentException("No Metadata returned by the metadata extension"));

            return adapter.adapt(actionId, properties, metaData);
        } catch (final Exception e) {
            throw new IllegalStateException("Unable to fetch and process metadata", e);
        }
    }
}
