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
import java.util.Set;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.util.ObjectHelper;
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
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.connector.support.verifier.api.ComponentMetadataRetrieval;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;

public class ODataMetaDataRetrieval extends ComponentMetadataRetrieval implements ODataConstants {

    // This is the schema supported by current jackson API for {@link ObjectSchema}
    private static final String JSON_SCHEMA_URI = "http://json-schema.org/draft-03/schema#";

    private JsonSchemaFactory factory = new JsonSchemaFactory();

    @Override
    protected MetaDataExtension resolveMetaDataExtension(CamelContext context, Class<? extends MetaDataExtension> metaDataExtensionClass, String componentId, String actionId) {
        return new ODataMetaDataExtension(context);
    }

    @SuppressWarnings({"PMD"})
    @Override
    protected SyndesisMetadata adapt(CamelContext context, String componentId, String actionId, Map<String, Object> properties, MetaDataExtension.MetaData metadata) {
        ODataMetadata odataMetadata = (ODataMetadata) metadata.getPayload();
        Map<String, List<PropertyPair>> enrichedProperties = new HashMap<>();

        if (odataMetadata.hasEntityNames()) {
            List<PropertyPair> resourcesResult = new ArrayList<>();
            odataMetadata.getEntityNames().stream().forEach(
                t -> resourcesResult.add(new PropertyPair(t, t))
            );
            enrichedProperties.put(RESOURCE_PATH, resourcesResult);
        }

        //
        // Do things differently depending on which action is being sought
        //
        Methods method = Methods.methodForAction(actionId);
        switch (method) {
            case READ:
                if (actionId.endsWith(FROM)) {
                    return genReadFromDataShape(odataMetadata, properties, enrichedProperties);
                } else {
                    return genReadToShape(odataMetadata, enrichedProperties);
                }
            case CREATE:
                return genCreateDataShape(odataMetadata, enrichedProperties);
            case DELETE:
                return genDeleteDataShape(enrichedProperties, actionId);
            case PATCH:
                return genPatchDataShape(odataMetadata, enrichedProperties, actionId);
        }

        return SyndesisMetadata.of(enrichedProperties);
    }

    private SyndesisMetadata createSyndesisMetadata(
                                       Map<String, List<PropertyPair>> enrichedProperties,
                                       DataShape.Builder inDataShapeBuilder,
                                       DataShape.Builder outDataShapeBuilder) {
        return new SyndesisMetadata(enrichedProperties,
                                    inDataShapeBuilder.build(), outDataShapeBuilder.build());
    }

    private ObjectSchema createEntitySchema() {
        ObjectSchema entitySchema = new ObjectSchema();
        entitySchema.setTitle("ODATA_ENTITY_PROPERTIES");
        entitySchema.set$schema(JSON_SCHEMA_URI);
        return entitySchema;
    }

    private void populateEntitySchema(ODataMetadata odataMetadata, ObjectSchema entitySchema) {
        if (! odataMetadata.hasEntityProperties()) {
            return;
        }

        for (PropertyMetadata entityProperty : odataMetadata.getEntityProperties()) {
            schemaFor(entityProperty, entitySchema);
        }
    }

    private boolean isSplit(Map<String, Object> properties) {
        Object splitProp = ConnectorOptions.extractOption(properties, SPLIT_RESULT);
        return splitProp != null && Boolean.parseBoolean(splitProp.toString());
    }

    private String serializeSpecification(ContainerTypeSchema schema) {
        try {
            return Json.writer().writeValueAsString(schema);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize schema", e);
        }
    }

    private void applyEntitySchemaSpecification(ContainerTypeSchema schema, DataShape.Builder dataShapeBuilder) {
        final String specification = serializeSpecification(schema);

        dataShapeBuilder
                .kind(DataShapeKinds.JSON_SCHEMA)
                .name("Entity Schema")
                .description("Schema of OData result entities")
                .specification(specification);

        if (schema instanceof ArraySchema) {
            dataShapeBuilder.putMetadata("variant", "collection");
        }
    }

    @SuppressWarnings("PMD")
    private void schemaFor(PropertyMetadata propertyMetadata, ObjectSchema parentSchema) {
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
                ObjectSchema objectSchema = factory.objectSchema();
                Set<PropertyMetadata> childProperties = propertyMetadata.getChilldProperties();
                if (childProperties != null) {
                    for (PropertyMetadata childProperty : childProperties) {
                        schemaFor(childProperty, objectSchema);
                    }
                }
                schema = objectSchema;
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
        // Use #putOptionalProperty() as it does not override required flag
        parentSchema.putOptionalProperty(propertyMetadata.getName(), schema);
    }

    /*
     * READ
     * - In has NO shape
     * - Out has the json entity schema
     */
    private SyndesisMetadata genReadFromDataShape(ODataMetadata odataMetadata,
                                              Map<String, Object> basicProperties,
                                              Map<String, List<PropertyPair>> enrichedProperties) {
        ObjectSchema entitySchema = createEntitySchema();
        DataShape.Builder inDataShapeBuilder = new DataShape.Builder()
            .kind(DataShapeKinds.NONE);
        DataShape.Builder outDataShapeBuilder = new DataShape.Builder()
            .kind(DataShapeKinds.NONE)
            .type(entitySchema.getTitle());

        populateEntitySchema(odataMetadata, entitySchema);
        //
        // If a key predicate is used then only one entity is expected to be returned
        // hence an array schema is not required.
        //
        Object keyPredicate = ConnectorOptions.extractOption(basicProperties, KEY_PREDICATE);
        boolean isSplit = isSplit(basicProperties);

        if (! entitySchema.getProperties().isEmpty()) {
            if (ObjectHelper.isNotEmpty(keyPredicate) || isSplit) {
                //
                // A split will mean that the schema is no longer an array schema
                //
                applyEntitySchemaSpecification(entitySchema,  outDataShapeBuilder);
            } else {
                ArraySchema collectionSchema = new ArraySchema();
                collectionSchema.set$schema(JSON_SCHEMA_URI);
                collectionSchema.setItemsSchema(entitySchema);
                applyEntitySchemaSpecification(collectionSchema, outDataShapeBuilder);
            }
        }
        return createSyndesisMetadata(enrichedProperties, inDataShapeBuilder, outDataShapeBuilder);
    }

    /*
     * Producer-version of READ
     */
    private SyndesisMetadata genReadToShape(ODataMetadata odataMetadata, Map<String, List<PropertyPair>> enrichedProperties) {
        //
        // Need to add a KEY_PREDICATE to the json schema to allow identification
        // of the entity to be patched.
        //
        ObjectSchema entityInSchema = createEntitySchema();
        entityInSchema.putProperty(KEY_PREDICATE, factory.stringSchema());

        DataShape.Builder inDataShapeBuilder = new DataShape.Builder()
            .kind(DataShapeKinds.JSON_SCHEMA)
            .type(entityInSchema.getTitle())
            .name("Entity Properties")
            .specification(serializeSpecification(entityInSchema));

        ObjectSchema entityOutSchema = createEntitySchema();
        populateEntitySchema(odataMetadata, entityOutSchema);

        DataShape.Builder outDataShapeBuilder = new DataShape.Builder()
            .kind(DataShapeKinds.JSON_SCHEMA)
            .type(entityOutSchema.getTitle());

        applyEntitySchemaSpecification(entityOutSchema,  outDataShapeBuilder);

        return createSyndesisMetadata(enrichedProperties, inDataShapeBuilder, outDataShapeBuilder);
    }

    /*
     *CREATE
     * - In has the json entity schema
     * - Out has the same json entity schema (since create returns the new entity)
     */
    private SyndesisMetadata genCreateDataShape(ODataMetadata odataMetadata,
                                                Map<String, List<PropertyPair>> enrichedProperties) {
        ObjectSchema entitySchema = createEntitySchema();
        populateEntitySchema(odataMetadata, entitySchema);

        DataShape.Builder inDataShapeBuilder = new DataShape.Builder()
            .kind(DataShapeKinds.NONE)
            .type(entitySchema.getTitle());
        DataShape.Builder outDataShapeBuilder = new DataShape.Builder()
            .kind(DataShapeKinds.NONE)
            .type(entitySchema.getTitle());

        if (! entitySchema.getProperties().isEmpty()) {
            applyEntitySchemaSpecification(entitySchema,  inDataShapeBuilder);
            applyEntitySchemaSpecification(entitySchema, outDataShapeBuilder);
        }

        return createSyndesisMetadata(enrichedProperties, inDataShapeBuilder, outDataShapeBuilder);
    }

    /*
     * PATCH
     * - In has the json entity schema
     * - Out has the json instance representing a status outcome
     */
    private SyndesisMetadata genPatchDataShape(ODataMetadata odataMetadata,
                                               Map<String, List<PropertyPair>> enrichedProperties,
                                               String actionId) {
        ObjectSchema entitySchema = createEntitySchema();
        populateEntitySchema(odataMetadata, entitySchema);

        //
        // Need to add a KEY_PREDICATE to the json schema to allow identification
        // of the entity to be patched.
        //
        entitySchema.putProperty(KEY_PREDICATE, factory.stringSchema());

        DataShape.Builder inDataShapeBuilder = new DataShape.Builder()
            .kind(DataShapeKinds.NONE)
            .type(entitySchema.getTitle())
            .name("Entity Properties");

        if (! entitySchema.getProperties().isEmpty()) {
            applyEntitySchemaSpecification(entitySchema,  inDataShapeBuilder);
        }

        DataShape.Builder outDataShapeBuilder = new DataShape.Builder()
            .kind(DataShapeKinds.JSON_INSTANCE)
            .description("OData " + actionId)
            .name(actionId);

        return createSyndesisMetadata(enrichedProperties, inDataShapeBuilder, outDataShapeBuilder);
    }

    /*
     * DELETE
     * - In has the json object with the key predicate in it
     * - Out has the json instance representing a status outcome
     */
    private SyndesisMetadata genDeleteDataShape(Map<String, List<PropertyPair>> enrichedProperties,
                                                String actionId) {
        //
        // Need to add a KEY_PREDICATE to the json schema to allow identification
        // of the entity to be patched.
        //
        ObjectSchema entitySchema = createEntitySchema();
        entitySchema.putProperty(KEY_PREDICATE, factory.stringSchema());

        DataShape.Builder inDataShapeBuilder = new DataShape.Builder()
            .kind(DataShapeKinds.JSON_SCHEMA)
            .type(entitySchema.getTitle())
            .name("Entity Properties")
            .specification(serializeSpecification(entitySchema));

        DataShape.Builder outDataShapeBuilder = new DataShape.Builder()
            .kind(DataShapeKinds.JSON_INSTANCE)
            .description("OData " + actionId)
            .name(actionId);

        return createSyndesisMetadata(enrichedProperties, inDataShapeBuilder, outDataShapeBuilder);
    }
}
