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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ContainerTypeSchema;
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

    private JsonSchemaFactory factory = new JsonSchemaFactory();

    @Override
    protected MetaDataExtension resolveMetaDataExtension(CamelContext context, Class<? extends MetaDataExtension> metaDataExtensionClass, String componentId, String actionId) {
        return new ODataMetaDataExtension(context);
    }

    @SuppressWarnings({"unchecked", "PMD"})
    @Override
    protected SyndesisMetadata adapt(CamelContext context, String componentId, String actionId, Map<String, Object> properties, MetaDataExtension.MetaData metadata) {
            ODataMetadata odataMetadata = (ODataMetadata) metadata.getPayload();
            Map<String, List<PropertyPair>> enrichedProperties = new HashMap<>();
            DataShape inDataShape = null;
            DataShape outDataShape = null;

            if (odataMetadata.hasEntityNames()) {
                List<PropertyPair> resourcesResult = new ArrayList<>();
                odataMetadata.getEntityNames().stream().forEach(
                    t -> resourcesResult.add(new PropertyPair(t, t))
                );
                enrichedProperties.put(RESOURCE_PATH, resourcesResult);
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
            if (actionId.endsWith(Methods.READ.connectorId())) {
                //
                // READ
                // - In has NO shape
                // - Out has the json entity schema
                //
                inDataShapeBuilder.kind(DataShapeKinds.NONE);
                outDataShapeBuilder.type(entitySchema.getTitle());
                if (entitySchema.getProperties().isEmpty()) {
                    outDataShapeBuilder.kind(DataShapeKinds.NONE);
                } else {
                    ArraySchema collectionSchema = new ArraySchema();
                    collectionSchema.set$schema("http://json-schema.org/schema#");
                    collectionSchema.setItemsSchema(entitySchema);
                    applySchemaSpecification(collectionSchema, outDataShapeBuilder);
                }

                inDataShape = inDataShapeBuilder.build();
                outDataShape = outDataShapeBuilder.build();
            } else if(actionId.endsWith(Methods.CREATE.connectorId())) {
                //
                // CREATE
                // - In has the json entity schema
                // - Out has the same json entity schema (since create returns the new entity)
                //
                inDataShapeBuilder.type(entitySchema.getTitle());
                outDataShapeBuilder.type(entitySchema.getTitle());
                if (entitySchema.getProperties().isEmpty()) {
                    inDataShapeBuilder.kind(DataShapeKinds.NONE);
                    outDataShapeBuilder.kind(DataShapeKinds.NONE);
                } else {
                    applySchemaSpecification(entitySchema,  inDataShapeBuilder);
                    applySchemaSpecification(entitySchema, outDataShapeBuilder);
                }

                inDataShape = inDataShapeBuilder.build();
                outDataShape = outDataShapeBuilder.build();
            } else if (actionId.endsWith(Methods.DELETE.connectorId())) {
                //
                // DELETE
                // - In has the java object ODataDeleteResource
                // - Out has the json instance representing a status outcome
                //
                inDataShape = inDataShapeBuilder
                    .kind(DataShapeKinds.JAVA)
                    .type(String.class.getName())
                    .description("OData " + actionId)
                    .name(actionId).build();
                outDataShape = outDataShapeBuilder
                    .kind(DataShapeKinds.JSON_INSTANCE)
                    .description("OData " + actionId)
                    .name(actionId).build();
            } else if (actionId.endsWith(Methods.PATCH.connectorId())) {
                //
                // PATCH
                // - In has the json entity schema
                // - Out has the json instance representing a status outcome
                //

                //
                // Need to add a KEY_PREDICATE to the json schema to allow identification
                // of the entity to be patched.
                //
                entitySchema.putProperty(KEY_PREDICATE, factory.stringSchema());

                inDataShapeBuilder.type(entitySchema.getTitle());
                if (entitySchema.getProperties().isEmpty()) {
                    inDataShapeBuilder.kind(DataShapeKinds.NONE);
                } else {
                    applySchemaSpecification(entitySchema,  inDataShapeBuilder);
                }

                inDataShape = inDataShapeBuilder.build();
                outDataShape = outDataShapeBuilder
                    .kind(DataShapeKinds.JSON_INSTANCE)
                    .description("OData " + actionId)
                    .name(actionId).build();
            }

            return new SyndesisMetadata(enrichedProperties, inDataShape, outDataShape);
    }

    private void applySchemaSpecification(ContainerTypeSchema schema, DataShape.Builder dataShapeBuilder) {
        final String specification;
        try {
            specification = Json.writer().writeValueAsString(schema);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize schema", e);
        }

        dataShapeBuilder.kind(DataShapeKinds.JSON_SCHEMA)
                .name("Entity Schema")
                .description("Schema of OData result entities")
                .specification(specification);

        if (schema instanceof ArraySchema) {
            dataShapeBuilder.putMetadata("variant", "collection");
        }
    }

    @SuppressWarnings("PMD")
    private JsonSchema schemaFor(PropertyMetadata propertyMetadata) {
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
