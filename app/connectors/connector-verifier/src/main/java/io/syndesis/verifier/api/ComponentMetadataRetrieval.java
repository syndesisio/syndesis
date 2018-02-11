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
package io.syndesis.verifier.api;

import java.util.Map;

import io.syndesis.core.CollectionsUtils;
import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.component.extension.MetaDataExtension;

public abstract class ComponentMetadataRetrieval implements MetadataRetrieval {
    private final Class<? extends MetaDataExtension> metaDataExtensionClass;

    @SuppressWarnings("PMD.AvoidUsingVolatile")
    private volatile MetaDataExtension metaDataExtension;


    public ComponentMetadataRetrieval() {
        this(MetaDataExtension.class);
    }

    public ComponentMetadataRetrieval(Class<? extends MetaDataExtension> metaDataExtensionClass) {
        this.metaDataExtensionClass = metaDataExtensionClass;
    }

    @Override
    public SyndesisMetadata fetch(CamelContext context, String componentId, String actionId, Map<String, Object> properties) {
        try {
            if (metaDataExtension == null) {
                synchronized (this) {
                    if (metaDataExtension == null) {
                        metaDataExtension = resolveMetaDataExtension(context, componentId, actionId);
                    }
                }
            }

            final Map<String, Object> extensionOptions = CollectionsUtils.removeNullValues(properties);
            final MetaDataExtension.MetaData metaData = metaDataExtension.meta(extensionOptions)
                .orElseThrow(() -> new IllegalArgumentException("No Metadata returned by the metadata extension"));

            return adapt(context, componentId, actionId, properties, metaData);
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") final Exception e) {
            throw new IllegalStateException("Unable to fetch and process metadata", e);
        }
    }

    @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
    protected MetaDataExtension resolveMetaDataExtension(CamelContext context, String componentId, String actionId) {
        Component component = context.getComponent(componentId, true, false);
        if (component == null) {
            throw new IllegalArgumentException(
                String.format("Component %s does not exists", componentId)
            );
        }

        MetaDataExtension extension = component.getExtension(metaDataExtensionClass).orElse(null);
        if (extension == null) {
            throw  new IllegalArgumentException(
                String.format("Component %s does not support meta-data extension for action %s", componentId, actionId)
            );
        }

        return extension;
    }

    protected abstract SyndesisMetadata adapt(CamelContext context, String componentId, String actionId, Map<String, Object> properties, MetaDataExtension.MetaData metadata);
}
