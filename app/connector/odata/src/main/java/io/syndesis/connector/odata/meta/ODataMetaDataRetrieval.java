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
package io.syndesis.connector.odata.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.util.Json;
import io.syndesis.connector.odata.ODataConstants;
import io.syndesis.connector.odata.meta.ODataMetadata.PropertyMetadata;
import io.syndesis.connector.odata.meta.ODataMetadata.PropertyMetadata.TypeClass;
import io.syndesis.connector.support.verifier.api.ComponentMetadataRetrieval;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;

public class ODataMetaDataRetrieval extends ComponentMetadataRetrieval implements ODataConstants {

    @Override
    protected MetaDataExtension resolveMetaDataExtension(CamelContext context, Class<? extends MetaDataExtension> metaDataExtensionClass, String componentId, String actionId) {
        return new ODataMetaDataExtension(context);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected SyndesisMetadata adapt(CamelContext context, String componentId, String actionId, Map<String, Object> properties, MetaDataExtension.MetaData metadata) {
        try {
            ODataMetadata odataMetadata = (ODataMetadata) metadata.getPayload();
            Map<String, List<PropertyPair>> enrichedProperties = new HashMap<>();
            DataShape inDataShape = null;
            DataShape outDataShape = null;

            if (odataMetadata.hasEntityNames()) {
                List<PropertyPair> resourcesResult = new ArrayList<>();
                odataMetadata.getEntityNames().stream().forEach(
                    t -> resourcesResult.add(new PropertyPair(t, t))
                );
                enrichedProperties.put(METHOD_NAME, resourcesResult);
            }

            ObjectSchema entitySchema = new ObjectSchema();
            entitySchema.setTitle("ODATA_ENTITY_PROPERTIES");
            entitySchema.set$schema("http://json-schema.org/schema#");

            if (odataMetadata.hasEntityProperties()) {
                for (PropertyMetadata entityProperty : odataMetadata.getEntityProperties()) {
                    JsonSchema propSchema = schemaFor(entityProperty);
                    boolean required = propSchema.getRequired();
                    entitySchema.putProperty(entityProperty.getName(), propSchema);
                    //
                    // Workaround oddity where ObjectSchema#putProperty sets required to true
                    //
                    propSchema.setRequired(required);
                }
            }

            DataShape.Builder inDataShapeBuilder = new DataShape.Builder();
            DataShape.Builder outDataShapeBuilder = new DataShape.Builder();

            //
            // Do things differently depending on which action is being sought
            //
            if (actionId.endsWith("odata-read-connector" )) {
                inDataShapeBuilder.kind(DataShapeKinds.NONE);

                outDataShapeBuilder.type(entitySchema.getTitle());
                if (entitySchema.getProperties().isEmpty()) {
                    outDataShapeBuilder.kind(DataShapeKinds.NONE);
                } else {
                    outDataShapeBuilder.kind(DataShapeKinds.JSON_SCHEMA)
                            .name("Entity Schema")
                            .description("Schema of OData result entities")
                            .specification(Json.writer().writeValueAsString(entitySchema));
                }

                inDataShape = inDataShapeBuilder.build();
                outDataShape = outDataShapeBuilder.build();
            } else {
                    throw new UnsupportedOperationException();
            }

            return new SyndesisMetadata(enrichedProperties, inDataShape, outDataShape);
        } catch ( Exception e) {
            return SyndesisMetadata.EMPTY;
        }
    }

    @SuppressWarnings("PMD")
    private JsonSchema schemaFor(PropertyMetadata propertyMetadata) {
        JsonSchemaFactory factory = new JsonSchemaFactory();
        JsonSchema schema;

        TypeClass type = propertyMetadata.getType();
        switch (type) {
            case STRING:
                schema = factory.stringSchema();
                break;
            case BOOLEAN:
                schema = factory.booleanSchema();
                break;
            case NUMBER:
                schema = factory.numberSchema();
                break;
            case OBJECT:
                schema = factory.objectSchema();
                break;
            default:
                schema = factory.anySchema();
        }

        if (propertyMetadata.isArray()) {
            ArraySchema arraySchema = factory.arraySchema();
            arraySchema.setItemsSchema(schema);
            schema = arraySchema;
        }

        schema.setRequired(propertyMetadata.isRequired());
        return schema;
    }
}
