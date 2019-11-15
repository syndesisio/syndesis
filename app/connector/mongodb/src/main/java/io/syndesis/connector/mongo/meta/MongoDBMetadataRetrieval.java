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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.connector.mongo.MongoCustomizersUtil;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.connector.support.verifier.api.ComponentMetadataRetrieval;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.component.extension.metadata.MetaDataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MongoDBMetadataRetrieval extends ComponentMetadataRetrieval {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBMetadataRetrieval.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
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

        return buildDatashape(actionId, schema, properties);
    }

    // The component may not return metadata if the collection has no validator associated
    @Override
    protected MetaDataExtension.MetaData fetchMetaData(MetaDataExtension extension, Map<String, Object> properties) {
        MongoCustomizersUtil.replaceAdminDBIfMissing(properties);
        Optional<MetaDataExtension.MetaData> meta = extension.meta(properties);
        return meta.orElse(defaultMeta);
    }

    public SyndesisMetadata buildDatashape(String actionId, DataShape schema, Map<String, Object> properties) {
        if (isFilterAction(actionId)) {
            return SyndesisMetadata.of(
                MongoDBMetadataRetrieval.criteria(ConnectorOptions.extractOption(properties,"filter")),
                schema);
        } else if ("io.syndesis.connector:connector-mongodb-insert".equals(actionId)) {
            return SyndesisMetadata.of(schema);
        } else if (actionId.contains("consumer")) {
            return SyndesisMetadata.of(none("N/A"), schema);
        } else {
            throw new IllegalArgumentException(String.format("Could not find any dynamic metadata adaptation for action %s", actionId));
        }
    }

    @SuppressWarnings("MethodCanBeStatic")
    private boolean isFilterAction(String actionId) {
        return "io.syndesis.connector:connector-mongodb-update".equals(actionId) ||
            "io.syndesis.connector:connector-mongodb-find".equals(actionId) ||
            "io.syndesis.connector:connector-mongodb-delete".equals(actionId) ||
            "io.syndesis.connector:connector-mongodb-upsert".equals(actionId) ||
            "io.syndesis.connector:connector-mongodb-count".equals(actionId);
    }

    public static DataShape any(String name) {
        return new DataShape.Builder()
            .name(name)
            .kind(DataShapeKinds.ANY)
            .build();
    }

    public static DataShape none(String name) {
        return new DataShape.Builder()
            .name(name)
            .kind(DataShapeKinds.NONE)
            .build();
    }

    public static DataShape criteria(String filter) {
        if (filter == null || !FilterUtil.hasAnyParameter(filter)){
            return none("Filter parameters");
        }
        return new DataShape.Builder()
            .name("Filter parameters")
            .kind(DataShapeKinds.JSON_SCHEMA)
            .specification(buildFilterJsonSpecification(filter))
            .build();
    }

    public static String buildFilterJsonSpecification(String filter) {
        final JsonSchemaFactory factory = new JsonSchemaFactory();
        final ObjectSchema builderIn = new ObjectSchema();
        List<String> parameters = FilterUtil.extractParameters(filter);
        builderIn.setTitle("Filter parameters");
        for(String param:parameters){
            builderIn.putProperty(param,factory.stringSchema());
        }
        String jsonSpecification = null;
        try {
            jsonSpecification = MAPPER.writeValueAsString(builderIn);
        } catch (JsonProcessingException e) {
            LOGGER.error("Issue while processing filter parameters", e);
        }
        return  jsonSpecification;
    }

}

