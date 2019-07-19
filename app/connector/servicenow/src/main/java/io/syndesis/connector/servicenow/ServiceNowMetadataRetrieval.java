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
package io.syndesis.connector.servicenow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.util.Json;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.connector.support.verifier.api.ComponentMetadataRetrieval;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.component.servicenow.ServiceNowConstants;
import org.apache.camel.util.ObjectHelper;

public final class ServiceNowMetadataRetrieval extends ComponentMetadataRetrieval {

    /**
     * TODO: use local extension, remove when switching to camel 2.22.x
     */
    @Override
    protected MetaDataExtension resolveMetaDataExtension(CamelContext context, Class<? extends MetaDataExtension> metaDataExtensionClass, String componentId, String actionId) {
        return new ServiceNowMetaDataExtension(context);
    }

    @Override
    public SyndesisMetadata fetch(CamelContext context, String componentId, String actionId, Map<String, Object> properties) {
        final Object table = ConnectorOptions.extractOption(properties, "table");

        if (table == null) {
            Map<String, Object> props = new HashMap<>(properties);
            props.put("metaType", "list");

            //
            // We need a way to inject verifier/meta specific properties in action
            // definition so we do not need to discriminate the behavior according
            // to the action id and we can also set default values directly from
            // the action definition, something like:
            //
            // "verifierProperties": {
            //     ...
            // }
            //
            // "metaProperties": {
            //     "objectType": "table",
            //     "metaType": "list"
            // }
            //
            // UI should read them and inject them on the properties map (without
            // overriding properties with the same name provided by the user.
            //
            if (ObjectHelper.equal("io.syndesis:servicenow-action-retrieve-record", actionId)) {
                props.put("objectType", ServiceNowConstants.RESOURCE_TABLE);
            } else if (ObjectHelper.equal("io.syndesis:servicenow-action-create-record", actionId)) {
                props.put("objectType", ServiceNowConstants.RESOURCE_IMPORT);
            } else {
                throw new UnsupportedOperationException("Unsupported action: " + actionId);
            }

            return super.fetch(context, componentId, actionId, props);
        } else {
            Map<String, Object> props = new HashMap<>(properties);
            props.put("objectType", ServiceNowConstants.RESOURCE_TABLE);
            props.put("objectName", table);
            props.put("metaType", "definition");

            return super.fetch(context, componentId, actionId, props);
        }
    }

    @Override
    protected SyndesisMetadata adapt(CamelContext context, String componentId, String actionId, Map<String, Object> properties, MetaDataExtension.MetaData metadata) {
        if (metadata.getPayload() != null) {
            final String objectType = ConnectorOptions.extractOption(properties, "objectType");
            final String metaType = ConnectorOptions.extractOption(properties, "metaType");

            if (ServiceNowConstants.RESOURCE_TABLE.equalsIgnoreCase(objectType) && "definition".equals(metaType)) {
                return adaptTableDefinitionMetadata(actionId, properties, metadata);
            }
            if (ServiceNowConstants.RESOURCE_TABLE.equalsIgnoreCase(objectType) && "list".equals(metaType)) {
                return adaptTableListMetadata(metadata);
            }
            if (ServiceNowConstants.RESOURCE_IMPORT.equalsIgnoreCase(objectType) && "list".equals(metaType)) {
                return adaptTableListMetadata(metadata);
            }
        }

        return SyndesisMetadata.EMPTY;
    }

    private SyndesisMetadata adaptTableDefinitionMetadata(String actionId, Map<String, Object> properties, MetaDataExtension.MetaData metadata) {
        try {
            final Object table = ConnectorOptions.extractOption(properties, "table");
            final ObjectNode schema = (ObjectNode) metadata.getPayload();

            DataShape.Builder shapeBuilder = new DataShape.Builder().kind(DataShapeKinds.JSON_SCHEMA)
                .type("servicenow." + table)
                .name("ServiceNow Import Set (" + table + ")");

            if (ObjectHelper.equal("io.syndesis:servicenow-action-retrieve-record", actionId)) {
                ObjectNode collectionSchema = schema.objectNode();
                collectionSchema.put("$schema", "http://json-schema.org/schema#");
                collectionSchema.put("type", "array");
                collectionSchema.set("items", schema);

                shapeBuilder.specification(Json.writer().writeValueAsString(collectionSchema));
                return SyndesisMetadata.outOnly(shapeBuilder.build());
            }
            if (ObjectHelper.equal("io.syndesis:servicenow-action-create-record", actionId)) {
                shapeBuilder.specification(Json.writer().writeValueAsString(schema));
                return SyndesisMetadata.inOnly(shapeBuilder.build());
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return SyndesisMetadata.EMPTY;
    }

    private SyndesisMetadata adaptTableListMetadata(MetaDataExtension.MetaData metadata) {
        try {
            final JsonNode payload = metadata.getPayload(JsonNode.class);
            final List<PropertyPair> tables = new ArrayList<>();
            final Iterator<Map.Entry<String, JsonNode>> it = payload.fields();

            while (it.hasNext()) {
                final Map.Entry<String, JsonNode> entry = it.next();
                final String name = entry.getKey();
                final String displayName = entry.getValue().asText(name);

                tables.add(new PropertyPair(name, displayName));
            }

            return SyndesisMetadata.of(
                Collections.singletonMap(ServiceNowConstants.RESOURCE_TABLE, tables)
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
