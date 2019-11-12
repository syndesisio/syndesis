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
package io.syndesis.connector.mongo.meta;

import java.util.Map;
import java.util.Optional;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.connector.mongo.MongoCustomizersUtil;
import io.syndesis.connector.support.verifier.api.ComponentMetadataRetrieval;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.component.extension.metadata.MetaDataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MongoDBMetadataRetrieval extends ComponentMetadataRetrieval {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBMetadataRetrieval.class);
    private final MetaDataExtension.MetaData defaultMeta = MetaDataBuilder.on(null).build();

    @Override
    protected SyndesisMetadata adapt(CamelContext context, String componentId, String actionId, Map<String, Object> properties, MetaDataExtension.MetaData metadata) {
        String datashapeName = String.format("%s.%s", properties.get("database"), properties.get("collection"));
        DataShape schema;
        if (defaultMeta.equals(metadata)) {
            // no metadata provided, just return a ANY datashape to allow user add his own
            schema = MongoDBMetadataRetrieval.any(datashapeName);
        } else {
            String jsonPayload = metadata.getPayload(String.class);
            LOGGER.debug("Adapting meta retrieved by platform component {}", jsonPayload);
            schema = new DataShape.Builder()
                .name(datashapeName)
                .description(String.format("Schema validator for %s collection", properties.get("collection")))
                .kind(DataShapeKinds.JSON_SCHEMA)
                .specification(jsonPayload)
                .build();
        }

        return MongoDBMetadataRetrieval.buildDatashape(actionId, schema);
    }

    // The component may not return metadata if the collection has no validator associated
    @Override
    protected MetaDataExtension.MetaData fetchMetaData(MetaDataExtension extension, Map<String, Object> properties) {
        MongoCustomizersUtil.replaceAdminDBIfMissing(properties);
        Optional<MetaDataExtension.MetaData> meta = extension.meta(properties);
        return meta.orElse(defaultMeta);
    }

    private static SyndesisMetadata buildDatashape(String actionId, DataShape schema) {
        if (actionId.endsWith("find")) {
            return SyndesisMetadata.of(MongoDBMetadataRetrieval.criteria(), schema);
        } else if (actionId.endsWith("insert")) {
            return SyndesisMetadata.of(schema);
        } else if (actionId.contains("consumer")) {
            return SyndesisMetadata.outOnly(schema);
        } else {
            throw new IllegalArgumentException(String.format("Could not find any dynamic metadata adaptation for action %s", actionId));
        }
    }

    private static DataShape any(String name) {
        return new DataShape.Builder()
            .name(name)
            .kind(DataShapeKinds.ANY)
            .build();
    }

    private static DataShape criteria() {
        return new DataShape.Builder()
            .name("Raw text criteria")
            .description("Text criteria")
            .kind(DataShapeKinds.JAVA)
            .type("java.lang.String")
            .build();
    }

}

