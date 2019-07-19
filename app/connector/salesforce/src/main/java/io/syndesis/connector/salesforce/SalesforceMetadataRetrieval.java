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
package io.syndesis.connector.salesforce;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.connector.support.verifier.api.ComponentMetadataRetrieval;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;

import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension.MetaData;
import org.apache.camel.component.salesforce.SalesforceEndpointConfig;
import org.apache.camel.component.salesforce.api.SalesforceException;
import org.apache.camel.component.salesforce.api.utils.JsonUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.SimpleTypeSchema;

public final class SalesforceMetadataRetrieval extends ComponentMetadataRetrieval {

    @Override
    public RuntimeException handle(final Exception e) {
        Throwable current = e;
        while (current != null && current != current.getCause() && !(current instanceof SalesforceException)) {
            current = current.getCause();
        }

        if (current instanceof SalesforceException) {
            final SalesforceException salesforceException = (SalesforceException) current;
            final String message = salesforceException.getErrors().stream().map(error -> error.getErrorCode() + ": " + error.getMessage())
                .collect(Collectors.joining(". "));

            return new SyndesisServerException("Salesforce: " + message, e);
        }

        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }

        return new SyndesisServerException("Salesforce", e);
    }

    @Override
    protected SyndesisMetadata adapt(final CamelContext context, final String componentId, final String actionId,
        final Map<String, Object> properties, final MetaData metadata) {
        final ObjectSchema schema = schemaPayload(metadata);

        Set<ObjectSchema> schemasToConsider;
        if (isPresentAndNonNull(properties, SalesforceEndpointConfig.SOBJECT_NAME)) {
            schemasToConsider = Collections.singleton(objectSchemaFrom(schema));
        } else {
            schemasToConsider = schema.getOneOf().stream().filter(SalesforceMetadataRetrieval::isObjectSchema)//
                .map(ObjectSchema.class::cast).collect(Collectors.toSet());
        }

        final Map<String, List<PropertyPair>> enrichedProperties = new HashMap<>();
        enrichedProperties.put(SalesforceEndpointConfig.SOBJECT_NAME, schemasToConsider.stream()//
            .map(SalesforceMetadataRetrieval::nameAndTitlePropertyPairOf).collect(Collectors.toList()));

        if (isPresent(properties, SalesforceEndpointConfig.SOBJECT_EXT_ID_NAME)) {
            enrichedProperties.put(SalesforceEndpointConfig.SOBJECT_EXT_ID_NAME,
                schemasToConsider.stream()//
                    .flatMap(s -> s.getProperties().entrySet().stream()).filter(e -> isIdLookup(e.getValue()))//
                    .map(SalesforceMetadataRetrieval::createFieldPairPropertyFromSchemaEntry).collect(Collectors.toList()));
        }

        if (isPresentAndNonNull(properties, SalesforceEndpointConfig.SOBJECT_NAME)) {
            try {
                final String objectName = ConnectorOptions.extractOption(properties, SalesforceEndpointConfig.SOBJECT_NAME);
                final ObjectSchema inputOutputSchema = inputOutputSchemaFor(schemasToConsider, objectName);
                final String specification = Json.writer().writeValueAsString(inputOutputSchema);

                return new SyndesisMetadata(//
                    enrichedProperties, //
                    new DataShape.Builder().kind(DataShapeKinds.JSON_SCHEMA)//
                        .type(inputOutputSchema.getTitle())//
                        .name("Salesforce " + objectName)//
                        .description("Salesforce " + objectName)//
                        .specification(specification).build(), //
                    new DataShape.Builder().kind(DataShapeKinds.JSON_SCHEMA)//
                        .type(inputOutputSchema.getTitle())//
                        .name("Salesforce " + objectName)//
                        .description("Salesforce " + objectName)//
                        .specification(specification).build());
            } catch (final JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        }

        return new SyndesisMetadata(enrichedProperties, null, null);
    }

    static ObjectSchema adaptSchema(final ObjectSchema schema) {
        schema.set$schema(JsonUtils.SCHEMA4);

        return schema;
    }

    static ObjectSchema convertSalesforceGlobalObjectJsonToSchema(final JsonNode payload) {
        final Set<Object> allSchemas = new HashSet<>();

        for (final JsonNode sobject : payload) {
            // generate SObject schema from description
            final ObjectSchema sobjectSchema = new ObjectSchema();
            sobjectSchema.setId(JsonUtils.DEFAULT_ID_PREFIX + ":" + sobject.get("name").asText());
            sobjectSchema.setTitle(sobject.get("label").asText());

            allSchemas.add(sobjectSchema);
        }

        final ObjectSchema schema = new ObjectSchema();
        schema.setOneOf(allSchemas);

        return schema;
    }

    static PropertyPair createFieldPairPropertyFromSchemaEntry(final Entry<String, JsonSchema> e) {
        return new PropertyPair(e.getKey(), ((SimpleTypeSchema) e.getValue()).getTitle());
    }

    static PropertyPair createObjectPairPropertyFromNode(final JsonNode node) {
        final String value = node.get("name").asText();
        final String displayValue = node.get("label").asText();

        return new PropertyPair(value, displayValue);
    }

    static ObjectSchema inputOutputSchemaFor(final Set<ObjectSchema> schemasToConsider, final String objectName) {
        for (final ObjectSchema schema : schemasToConsider) {
            if (schema.getId().contains(":" + objectName)) {
                return adaptSchema(schema);
            }
        }

        throw new IllegalArgumentException("Unable to find object schema for: " + objectName);
    }

    static boolean isIdLookup(final JsonSchema property) {
        final String description = property.getDescription();

        if (description == null) {
            return false;
        }

        return description.contains("idLookup");
    }

    static boolean isObjectSchema(final Object obj) {
        final ObjectSchema schema = (ObjectSchema) obj;
        final String id = schema.getId();

        return !id.contains(":QueryRecords");
    }

    static boolean isPresent(final Map<String, Object> properties, final String property) {
        return properties != null && properties.containsKey(property);
    }

    static boolean isPresentAndNonNull(final Map<String, Object> properties, final String property) {
        return ConnectorOptions.extractOption(properties, property) != null;
    }

    static PropertyPair nameAndTitlePropertyPairOf(final ObjectSchema schema) {
        final String id = schema.getId();
        final String objectName = id.substring(id.lastIndexOf(':') + 1);
        final String objectLabel = schema.getTitle();

        return new PropertyPair(objectName, objectLabel);
    }

    static ObjectSchema objectSchemaFrom(final ObjectSchema schema) {
        if (schema.getOneOf().isEmpty()) {
            return schema;
        }

        return (ObjectSchema) schema.getOneOf().stream().filter(ObjectSchema.class::isInstance)
            .filter(SalesforceMetadataRetrieval::isObjectSchema).findFirst().orElseThrow(
                () -> new IllegalStateException("The resulting schema does not contain an non query records object schema in `oneOf`"));
    }

    static ObjectSchema schemaPayload(final MetaData metadata) {
        final Object payload = metadata.getPayload();

        if (payload instanceof ObjectSchema) {
            return (ObjectSchema) payload;
        }

        if (payload instanceof JsonNode) {
            return convertSalesforceGlobalObjectJsonToSchema((JsonNode) payload);
        }

        throw new IllegalArgumentException("Unsupported metadata payload: " + payload);
    }
}
