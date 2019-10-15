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
package io.syndesis.server.api.generator.swagger;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.DataShapeMetaData;
import io.syndesis.common.util.Json;
import io.syndesis.server.api.generator.swagger.util.JsonSchemaHelper;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.trimToNull;

@SuppressWarnings("PMD.GodClass")
public class UnifiedJsonDataShapeGenerator extends BaseDataShapeGenerator {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Class<AbstractSerializableParameter<?>> PARAM_CLASS = (Class) AbstractSerializableParameter.class;
    private static final List<String> PROPERTIES_TO_REMOVE_ON_MERGE = Arrays.asList("$schema", "title");

    @Override
    public DataShape createShapeFromRequest(final ObjectNode json, final Swagger swagger, final Operation operation) {
        final ObjectNode bodySchema = createJsonSchemaForBodyOf(json, operation);

        final ObjectNode parametersSchema = createJsonSchemaForParametersOf(operation);

        return unifiedJsonSchema("Request", "API request payload", bodySchema, parametersSchema);
    }

    @Override
    public DataShape createShapeFromResponse(final ObjectNode json, final Swagger swagger, final Operation operation) {
        final Optional<Response> maybeResponse = findResponse(operation);

        if (!maybeResponse.isPresent()) {
            return DATA_SHAPE_NONE;
        }

        final Response response = maybeResponse.get();
        final Model responseSchema = response.getResponseSchema();
        final String description = response.getDescription();

        final ObjectNode bodySchema = createSchemaFromModel(json, description, responseSchema);

        return unifiedJsonSchema("Response", "API response payload", bodySchema, null);
    }

    private static void addEnumsTo(final ObjectNode parameterParameter, final AbstractSerializableParameter<?> serializableParameter) {
        if (serializableParameter.getItems() != null) {
            final Property items = serializableParameter.getItems();

            List<?> enums;
            try {
                final Method method = ClassUtils.getPublicMethod(items.getClass(), "getEnum");
                enums = (List<?>) method.invoke(items);
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
                enums.forEach(e -> enumArray.add(String.valueOf(e)));
            }
        } else {
            final List<String> enums = serializableParameter.getEnum();

            if (enums != null && !enums.isEmpty()) {
                final ArrayNode enumArray = parameterParameter.putArray("enum");
                enums.forEach(enumArray::add);
            }
        }
    }

    private static ObjectNode createJsonSchemaForBodyOf(final ObjectNode json, final Operation operation) {
        final Optional<BodyParameter> maybeRequestBody = findBodyParameter(operation);

        if (!maybeRequestBody.isPresent()) {
            return null;
        }

        final BodyParameter requestBody = maybeRequestBody.get();

        final Model requestSchema = requestBody.getSchema();
        final String name = Optional.ofNullable(requestBody.getName()).orElse(requestBody.getDescription());

        return createSchemaFromModel(json, name, requestSchema);
    }

    private static ObjectNode createJsonSchemaForParametersOf(final Operation operation) {
        final List<Parameter> operationParameters = operation.getParameters();

        final List<AbstractSerializableParameter<?>> serializableParameters = operationParameters.stream()//
            .filter(PARAM_CLASS::isInstance)//
            .map(PARAM_CLASS::cast)//
            .collect(Collectors.toList());

        if (serializableParameters.isEmpty()) {
            return null;
        }

        return createSchemaFor(serializableParameters);
    }

    private static ObjectNode createPropertySchema(final String name, final Property schema) {
        final ObjectNode jsonSchema = JsonNodeFactory.instance.objectNode();
        final String format = schema.getFormat();
        if (JsonSchemaHelper.isKnownFormat(format)) {
            jsonSchema.put("format", format);
        }
        final String type = schema.getType();
        if (type != null) {
            jsonSchema.put("type", type);
        }
        final String title = schema.getName();
        if (title != null) {
            jsonSchema.put("title", title);
        } else {
            jsonSchema.put("title", name);
        }
        final String description = schema.getDescription();
        if (description != null) {
            jsonSchema.put("description", description);
        }

        return jsonSchema;
    }

    private static ObjectNode createSchemaFor(final List<AbstractSerializableParameter<?>> serializableParameters) {
        final ObjectNode schema = JsonSchemaHelper.newJsonObjectSchema();
        final ObjectNode properties = schema.putObject("properties");

        final ObjectNode parameters = properties.putObject("parameters");
        parameters.put("type", "object");
        final ObjectNode parametersProperties = parameters.putObject("properties");

        for (final AbstractSerializableParameter<?> serializableParameter : serializableParameters) {
            final String type = serializableParameter.getType();
            final String name = trimToNull(serializableParameter.getName());
            final String description = trimToNull(serializableParameter.getDescription());

            if ("file".equals(type)) {
                // 'file' type is not allowed in JSON schema
                continue;
            }

            final ObjectNode parameterParameter = parametersProperties.putObject(name);
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

            addEnumsTo(parameterParameter, serializableParameter);
        }
        return schema;
    }

    private static ObjectNode createSchemaFromModel(final ObjectNode json, final String name, final Model schema) {
        if (schema instanceof ArrayModel) {
            final Property items = ((ArrayModel) schema).getItems();

            final ObjectNode itemSchema = createSchemaFromProperty(json, name, items);
            itemSchema.remove(Arrays.asList("$schema", "title"));

            final ObjectNode jsonSchema = JsonNodeFactory.instance.objectNode();
            jsonSchema.put("type", "array");
            jsonSchema.set("items", itemSchema);

            return jsonSchema;
        } else if (schema instanceof ModelImpl) {
            return createSchemaFromModelImpl(name, schema);
        }

        final String title = determineTitleOf(name, schema);

        return JsonSchemaHelper.resolveSchemaForReference(json, title, schema.getReference());
    }

    private static ObjectNode createSchemaFromModelImpl(final String name, final Model model) {
        final ObjectNode schema = Json.convertValue(model, ObjectNode.class);
        JsonSchemaHelper.sanitize(schema);

        final String title = determineTitleOf(name, model);

        return JsonSchemaHelper.createJsonSchema(title, schema);
    }

    private static ObjectNode createSchemaFromProperty(final ObjectNode json, final String name, final Property schema) {
        if (schema instanceof MapProperty || schema instanceof ObjectProperty) {
            try {
                final String schemaString = Json.writer().writeValueAsString(schema);

                return JsonSchemaHelper.parseJsonSchema(schemaString);
            } catch (final JsonProcessingException e) {
                throw new IllegalStateException("Unable to serialize/read given JSON specification in response schema: " + schema, e);
            }
        } else if (schema instanceof RefProperty || schema instanceof ArrayProperty) {
            final String reference = JsonSchemaHelper.determineSchemaReference(schema);

            final String title = determineTitleOf(name, schema);

            return JsonSchemaHelper.resolveSchemaForReference(json, title, reference);
        }

        return createPropertySchema(name, schema);
    }

    private static String determineTitleOf(final String name, final Model schema) {
        final String title = schema.getTitle();
        if (title != null) {
            return title;
        }

        final String description = schema.getDescription();
        if (description != null) {
            return description;
        }

        final String reference = schema.getReference();
        if (reference != null) {
            return reference.replaceAll("^.*/", "");
        }

        return name;
    }

    private static String determineTitleOf(final String name, final Property schema) {
        final String title = schema.getTitle();
        if (title != null) {
            return title;
        }

        final String description = schema.getDescription();
        if (description != null) {
            return description;
        }

        final String reference = JsonSchemaHelper.determineSchemaReference(schema);
        if (reference != null) {
            return reference.replaceAll("^.*/", "");
        }

        return name;
    }

    private static DataShape unifiedJsonSchema(final String name, final String description, final ObjectNode bodySchema, final ObjectNode parametersSchema) {
        if (bodySchema == null && parametersSchema == null) {
            return DATA_SHAPE_NONE;
        }

        final ObjectNode unifiedSchema = JsonSchemaHelper.newJsonObjectSchema();
        unifiedSchema.put("$id", "io:syndesis:wrapped");
        final ObjectNode properties = unifiedSchema.putObject("properties");

        if (parametersSchema != null) {
            properties.remove(PROPERTIES_TO_REMOVE_ON_MERGE);
            properties.set("parameters", parametersSchema.get("properties").get("parameters"));
        }

        if (bodySchema != null) {
            bodySchema.remove(PROPERTIES_TO_REMOVE_ON_MERGE);
            properties.set("body", bodySchema);
        }

        return new DataShape.Builder()//
            .name(name)//
            .description(description)//
            .kind(DataShapeKinds.JSON_SCHEMA)//
            .specification(JsonSchemaHelper.serializeJson(unifiedSchema))//
            .putMetadata(DataShapeMetaData.UNIFIED, "true")
            .build();
    }

}
