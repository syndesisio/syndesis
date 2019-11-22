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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Items;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20Parameter;
import io.apicurio.datamodels.openapi.v2.models.Oas20ParameterDefinition;
import io.apicurio.datamodels.openapi.v2.models.Oas20ParameterDefinitions;
import io.apicurio.datamodels.openapi.v2.models.Oas20Response;
import io.apicurio.datamodels.openapi.v2.models.Oas20Schema;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.DataShapeMetaData;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;
import io.syndesis.server.api.generator.swagger.util.JsonSchemaHelper;
import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.trimToNull;

@SuppressWarnings("PMD.GodClass")
public class UnifiedJsonDataShapeGenerator extends BaseDataShapeGenerator {

    private static final List<String> PROPERTIES_TO_REMOVE_ON_MERGE = Arrays.asList("$schema", "title");

    @Override
    public DataShape createShapeFromRequest(final ObjectNode json, final Oas20Document openApiDoc, final Oas20Operation operation) {
        final ObjectNode bodySchema = createJsonSchemaForBodyOf(json, operation);

        final ObjectNode parametersSchema = createJsonSchemaForParametersOf(openApiDoc, operation);

        return unifiedJsonSchema("Request", "API request payload", bodySchema, parametersSchema);
    }

    @Override
    public DataShape createShapeFromResponse(final ObjectNode json, final Oas20Document openApiDoc, final Oas20Operation operation) {
        final Optional<Oas20Response> maybeResponse = findResponse(operation);

        if (!maybeResponse.isPresent()) {
            return DATA_SHAPE_NONE;
        }

        final Oas20Response response = maybeResponse.get();
        final Oas20Schema responseSchema = response.schema;
        final String description = response.description;

        final ObjectNode bodySchema = createSchemaFromModel(json, description, responseSchema);

        return unifiedJsonSchema("Response", "API response payload", bodySchema, null);
    }

    private static void addEnumsTo(final ObjectNode parameterParameter, final Oas20Parameter parameter) {
        if (parameter.items != null) {
            final Oas20Items items = parameter.items;

            List<String> enums = Optional.ofNullable(items.enum_).orElse(Collections.emptyList());
            final ObjectNode itemsNode = parameterParameter.putObject("items");
            final String itemType = items.type;
            if (StringUtils.isNotBlank(itemType)) {
                itemsNode.put("type", itemType);
            }

            if (!enums.isEmpty()) {
                final ArrayNode enumArray = itemsNode.putArray("enum");
                enums.forEach(e -> enumArray.add(String.valueOf(e)));
            }
        } else {
            final List<String> enums = parameter.enum_;

            if (enums != null && !enums.isEmpty()) {
                final ArrayNode enumArray = parameterParameter.putArray("enum");
                enums.forEach(enumArray::add);
            }
        }
    }

    private static ObjectNode createJsonSchemaForBodyOf(final ObjectNode json, final Oas20Operation operation) {
        final Optional<OasParameter> maybeRequestBody = findBodyParameter(operation);

        if (!maybeRequestBody.isPresent()) {
            return null;
        }

        final OasParameter requestBody = maybeRequestBody.get();

        final Oas20Schema requestSchema = (Oas20Schema) requestBody.schema;
        final String name = Optional.ofNullable(requestBody.getName()).orElse(requestBody.description);

        return createSchemaFromModel(json, name, requestSchema);
    }

    private static ObjectNode createJsonSchemaForParametersOf(final Oas20Document openApiDoc, final Oas20Operation operation) {
        final List<Oas20Parameter> operationParameters = OasModelHelper.getParameters(operation, Oas20Parameter.class);

        OasPathItem parent = Optional.of(operation.parent())
            .filter(OasPathItem.class::isInstance)
            .map(OasPathItem.class::cast)
            .orElse(null);
        final List<Oas20Parameter> pathParameters = OasModelHelper.getParameters(parent, Oas20Parameter.class);
        operationParameters.addAll(pathParameters);

        final List<Oas20ParameterDefinition> globalParameters = Optional.ofNullable(openApiDoc.parameters)
                .map(Oas20ParameterDefinitions::getItems)
                .orElse(Collections.emptyList());
        operationParameters.addAll(globalParameters);

        return createSchemaFor(operationParameters.stream()
            .filter(p -> p.type != null)
            .filter(OasModelHelper::isSerializable)
            .collect(Collectors.toList()));
    }

    private static ObjectNode createPropertySchema(final String name, final OasSchema schema) {
        final ObjectNode jsonSchema = JsonNodeFactory.instance.objectNode();
        final String format = schema.format;
        if (JsonSchemaHelper.isKnownFormat(format)) {
            jsonSchema.put("format", format);
        }
        final String type = schema.type;
        if (type != null) {
            jsonSchema.put("type", type);
        }
        final String title = schema.title;
        if (title != null) {
            jsonSchema.put("title", title);
        } else {
            jsonSchema.put("title", name);
        }
        final String description = schema.description;
        if (description != null) {
            jsonSchema.put("description", description);
        }

        return jsonSchema;
    }

    private static ObjectNode createSchemaFor(final List<Oas20Parameter> parameterList) {
        if (parameterList.isEmpty()) {
            return null;
        }

        final ObjectNode schema = JsonSchemaHelper.newJsonObjectSchema();
        final ObjectNode properties = schema.putObject("properties");

        final ObjectNode parameters = properties.putObject("parameters");
        parameters.put("type", "object");
        final ObjectNode parametersProperties = parameters.putObject("properties");

        for (final Oas20Parameter parameter : parameterList) {
            final String type = parameter.type;
            final String name = trimToNull(parameter.getName());
            final String description = trimToNull(parameter.description);

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

            final Object defaultValue = parameter.default_;
            if (defaultValue != null) {
                parameterParameter.put("default", String.valueOf(defaultValue));
            }

            addEnumsTo(parameterParameter, parameter);
        }
        return schema;
    }

    private static ObjectNode createSchemaFromModel(final ObjectNode json, final String name, final Oas20Schema schema) {
        if (OasModelHelper.isArrayType(schema)) {
            final Oas20Schema items = (Oas20Schema) schema.items;

            final ObjectNode itemSchema = createSchemaFromProperty(json, name, items);
            itemSchema.remove(Arrays.asList("$schema", "title"));

            final ObjectNode jsonSchema = JsonNodeFactory.instance.objectNode();
            jsonSchema.put("type", "array");
            jsonSchema.set("items", itemSchema);

            return jsonSchema;
        } else if (OasModelHelper.isReferenceType(schema)) {
            final String title = determineTitleOf(name, schema);
            return JsonSchemaHelper.resolveSchemaForReference(json, title, schema.$ref);
        }

        return createSchemaFromModelImpl(name, schema);
    }

    private static ObjectNode createSchemaFromModelImpl(final String name, final Oas20Schema model) {
        final ObjectNode schema = (ObjectNode) Library.writeNode(model);
        JsonSchemaHelper.sanitize(schema);

        final String title = determineTitleOf(name, model);
        return JsonSchemaHelper.createJsonSchema(title, schema);
    }

    private static ObjectNode createSchemaFromProperty(final ObjectNode json, final String name, final OasSchema schema) {
        if ("object".equals(schema.type)) {
            try {
                return (ObjectNode) Library.writeNode(schema);
            } catch (final Exception e) {
                throw new IllegalStateException("Unable to serialize/read given JSON specification in response schema: " + schema, e);
            }
        } else if (OasModelHelper.isReferenceType(schema) || OasModelHelper.isArrayType(schema)) {
            final String reference = JsonSchemaHelper.determineSchemaReference(schema)
                                                     .orElseThrow(() -> new IllegalArgumentException("Only references to schemas are supported"));
            final String title = determineTitleOf(name, schema);
            return JsonSchemaHelper.resolveSchemaForReference(json, title, reference);
        }

        return createPropertySchema(name, schema);
    }

    private static String determineTitleOf(final String name, final OasSchema schema) {
        final String title = schema.title;
        if (title != null) {
            return title;
        }

        final String description = schema.description;
        if (description != null) {
            return description;
        }

        final Optional<String> reference = JsonSchemaHelper.determineSchemaReference(schema);
        return reference.map(OasModelHelper::getReferenceName)
                        .orElse(name);
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
