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
package io.syndesis.connector.generator.swagger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import io.syndesis.connector.generator.swagger.util.DataShapeHelper;
import io.syndesis.connector.generator.swagger.util.JsonSchemaHelper;
import io.syndesis.core.Json;
import io.syndesis.model.DataShape;
import io.syndesis.model.DataShapeKinds;
import io.syndesis.model.action.ConnectorDescriptor;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static io.syndesis.connector.generator.swagger.util.JsonSchemaHelper.determineSchemaReference;

import static org.apache.commons.lang3.StringUtils.trimToNull;

public final class SwaggerUnifiedShapeConnectorGenerator extends BaseSwaggerConnectorGenerator {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Class<AbstractSerializableParameter<?>> PARAM_CLASS = (Class) AbstractSerializableParameter.class;

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.NPathComplexity", "PMD.ExcessiveMethodLength"})
    ConnectorDescriptor.Builder createDescriptor(final String specification, final Operation operation) {
        final ObjectNode unifiedSchema = JsonNodeFactory.instance.objectNode();
        unifiedSchema.put("$schema", "http://json-schema.org/draft-04/schema#");
        unifiedSchema.put("type", "object");
        final ObjectNode unifiedProperties = unifiedSchema.putObject("properties");

        final List<Parameter> operationParameters = operation.getParameters();
        final List<AbstractSerializableParameter<?>> serializableParameters = operationParameters.stream()//
            .filter(PARAM_CLASS::isInstance)//
            .map(PARAM_CLASS::cast)//
            .collect(Collectors.toList());

        if (!serializableParameters.isEmpty()) {
            final ObjectNode parameters = unifiedProperties.putObject("parameters");
            parameters.put("type", "object");
            final ObjectNode parameterProperties = parameters.putObject("properties");

            for (final AbstractSerializableParameter<?> serializableParameter : serializableParameters) {
                final String type = serializableParameter.getType();
                final String name = trimToNull(serializableParameter.getName());
                final String description = trimToNull(serializableParameter.getDescription());

                if ("file".equals(type)) {
                    // 'file' type is not allowed in JSON schema
                    continue;
                }

                final ObjectNode parameterParameter = parameterProperties.putObject(name);
                if (type != null) {
                    parameterParameter.put("type", type);
                }

                if (name != null) {
                    parameterParameter.put("title", name);
                }

                if (description != null) {
                    parameterParameter.put("description", description);
                }

                final Object defaultValue = serializableParameter.getDefault();
                if (defaultValue != null) {
                    parameterParameter.put("default", String.valueOf(defaultValue));
                }

                if (serializableParameter.getItems() != null) {
                    final Property items = serializableParameter.getItems();

                    List<String> enums;
                    try {
                        final Method method = ClassUtils.getPublicMethod(items.getClass(), "getEnum");
                        @SuppressWarnings("unchecked")
                        final List<String> tmp = (List<String>) method.invoke(items);

                        enums = tmp;
                    } catch (@SuppressWarnings("unused") final ReflectiveOperationException ignored) {
                        enums = Collections.emptyList();
                    }

                    final ObjectNode itemsNode = parameterParameter.putObject("items");
                    final String itemType = items.getType();
                    if (StringUtils.isNotBlank(itemType)) {
                        itemsNode.put("type", itemType);
                    }

                    if (enums != null && !enums.isEmpty()) {
                        final ArrayNode enumArray = itemsNode.putArray("enum");
                        enums.forEach(enumArray::add);
                    }
                } else {
                    final List<String> enums = serializableParameter.getEnum();

                    if (enums != null && !enums.isEmpty()) {
                        final ArrayNode enumArray = parameterParameter.putArray("enum");
                        enums.forEach(enumArray::add);
                    }
                }

            }
        }

        final Optional<BodyParameter> maybeRequestBody = operationParameters.stream()
            .filter(p -> p instanceof BodyParameter && ((BodyParameter) p).getSchema() != null).map(BodyParameter.class::cast).findFirst();
        maybeRequestBody.map(requestBody -> createSchemaFromModel(specification, requestBody.getSchema()))
            .ifPresent(bodySchema -> unifiedProperties.set("body", bodySchema));

        final ConnectorDescriptor.Builder actionDescriptor = new ConnectorDescriptor.Builder();

        if (unifiedProperties.size() == 0) {
            actionDescriptor.inputDataShape(new DataShape.Builder()//
                .kind(DataShapeKinds.NONE)//
                .build());
        } else {
            String schema;
            try {
                schema = Json.writer().writeValueAsString(unifiedSchema);
            } catch (final JsonProcessingException e) {
                throw new IllegalStateException("Unable to serialize given JSON schema", e);
            }

            actionDescriptor.inputDataShape(new DataShape.Builder()//
                .kind(DataShapeKinds.JSON_SCHEMA)//
                .specification(schema)//
                .build());
        }

        final Optional<Response> maybeResponse = operation.getResponses().values().stream().filter(r -> r.getSchema() != null).findFirst();
        final DataShape outputDataShape = maybeResponse.map(response -> DataShapeHelper.createShapeFromResponse(specification, response))
            .orElse(DATA_SHAPE_NONE);
        actionDescriptor.outputDataShape(outputDataShape);

        actionDescriptor.putConfiguredProperty("operationId", operation.getOperationId());

        return actionDescriptor;
    }

    private static JsonNode createSchemaFromModel(final String specification, final Model schema) {
        if (schema instanceof ArrayModel) {
            final Property items = ((ArrayModel) schema).getItems();

            return createSchemaFromProperty(specification, items);
        } else if (schema instanceof ModelImpl) {
            return createSchemaFromModelImpl(schema);
        }

        final String title = Optional.ofNullable(schema.getTitle()).orElse(schema.getReference().replaceAll("^.*/", ""));

        return createSchemaFromReference(specification, title, schema.getReference());
    }

    private static JsonNode createSchemaFromModelImpl(final Model schema) {
        try {
            final String schemaString = Json.writer().writeValueAsString(schema);

            return parseJsonSchema(schemaString);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize given JSON specification in response schema: " + schema, e);
        }
    }

    private static JsonNode createSchemaFromProperty(final String specification, final Property schema) {
        if (schema instanceof MapProperty) {
            try {
                final String schemaString = Json.writer().writeValueAsString(schema);

                return parseJsonSchema(schemaString);
            } catch (final JsonProcessingException e) {
                throw new IllegalStateException("Unable to serialize/read given JSON specification in response schema: " + schema, e);
            }
        } else if (schema instanceof StringProperty) {
            return null;
        }

        final String reference = determineSchemaReference(schema);

        final String title = Optional.ofNullable(schema.getTitle()).orElse(reference.replaceAll("^.*/", ""));

        return createSchemaFromReference(specification, title, reference);
    }

    private static JsonNode createSchemaFromReference(final String specification, final String title, final String reference) {
        final String jsonSchema = JsonSchemaHelper.resolveSchemaForReference(specification, title, reference);

        return parseJsonSchema(jsonSchema);
    }

    private static JsonNode parseJsonSchema(final String schema) {
        try {
            return Json.reader().readTree(schema);
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to parse given JSON schema: " + StringUtils.abbreviate(schema, 100), e);
        }
    }

}
