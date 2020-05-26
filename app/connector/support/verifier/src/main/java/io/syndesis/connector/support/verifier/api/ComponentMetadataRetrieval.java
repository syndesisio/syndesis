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
package io.syndesis.connector.support.verifier.api;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import io.syndesis.common.util.CollectionsUtils;
import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.component.extension.MetaDataExtension;

public abstract class ComponentMetadataRetrieval implements MetadataRetrieval {
    private final Class<? extends MetaDataExtension> metaDataExtensionClass;
    private final Map<String, MetaDataExtension> metaDataExtensions;

    protected ComponentMetadataRetrieval() {
        this(MetaDataExtension.class);
    }

    protected ComponentMetadataRetrieval(Class<? extends MetaDataExtension> metaDataExtensionClass) {
        this.metaDataExtensionClass = metaDataExtensionClass;
        this.metaDataExtensions = new ConcurrentHashMap<>();
    }

    @Override
    public SyndesisMetadata fetch(CamelContext context, String componentId, String actionId, Map<String, Object> properties) {
        Objects.requireNonNull(componentId, "ComponentID must not be null");
        Objects.requireNonNull(actionId, "ActionID must not be null");

        MetaDataExtension extension = getOrCreateMetaDataExtension(context, componentId, actionId);
        Map<String, Object> extensionOptions = prepareProperties(context, componentId, actionId, properties);
        MetaDataExtension.MetaData metaData = fetchMetaData(extension, extensionOptions);

        return adapt(context, componentId, actionId, properties, metaData);
    }

    protected Map<String, Object> prepareProperties(CamelContext context, String componentId, String actionId, Map<String, Object> properties) {
        return CollectionsUtils.removeNullValues(properties);
    }

    protected MetaDataExtension.MetaData fetchMetaData(MetaDataExtension extension, Map<String, Object> properties) {
        Optional<MetaDataExtension.MetaData> meta = extension.meta(properties);
        if (!meta.isPresent()) {
            throw new IllegalArgumentException("No Metadata returned by the metadata extension");
        }

        return meta.get();
    }

    protected MetaDataExtension resolveMetaDataExtension(CamelContext context, Class<? extends MetaDataExtension> metaDataExtensionClass, String componentId, String actionId) {
        Component component = context.getComponent(componentId, true, false);
        if (component == null) {
            throw new IllegalArgumentException(
                String.format("Component %s does not exists", componentId)
            );
        }

        return component.getExtension(metaDataExtensionClass).orElse(null);
    }

    protected abstract SyndesisMetadata adapt(CamelContext context, String componentId, String actionId, Map<String, Object> properties, MetaDataExtension.MetaData metadata);

    // ***************************
    // Helpers
    // ***************************

    private MetaDataExtension getOrCreateMetaDataExtension(CamelContext context, String componentId, String actionId) {
        return metaDataExtensions.computeIfAbsent(
            componentId + ":" + actionId,
            key -> {
                MetaDataExtension answer = resolveMetaDataExtension(context, metaDataExtensionClass, componentId, actionId);

                if (answer == null) {
                    throw new IllegalArgumentException(
                        String.format("Component %s does not support meta-data extension for action %s", componentId, actionId)
                    );
                }

                return answer;
            }
        );
    }
}
