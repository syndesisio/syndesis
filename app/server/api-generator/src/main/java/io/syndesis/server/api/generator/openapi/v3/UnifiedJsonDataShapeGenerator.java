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
package io.syndesis.server.api.generator.openapi.v3;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30MediaType;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.apicurio.datamodels.openapi.v3.models.Oas30Parameter;
import io.apicurio.datamodels.openapi.v3.models.Oas30ParameterDefinition;
import io.apicurio.datamodels.openapi.v3.models.Oas30Response;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import io.syndesis.common.model.DataShape;
import io.syndesis.server.api.generator.openapi.DataShapeGenerator;
import io.syndesis.server.api.generator.openapi.UnifiedJsonDataShapeSupport;
import io.syndesis.server.api.generator.openapi.util.JsonSchemaHelper;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;
import org.apache.commons.lang3.StringUtils;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.trimToNull;

class UnifiedJsonDataShapeGenerator extends UnifiedJsonDataShapeSupport<Oas30Document, Oas30Operation> implements DataShapeGenerator<Oas30Document, Oas30Operation> {

    private static final Predicate<Oas30Response> RESPONSE_HAS_SCHEMA = response -> Oas30ModelHelper.getSchema(response, APPLICATION_JSON) != null;

    @Override
    public DataShape createShapeFromRequest(final ObjectNode json, final Oas30Document openApiDoc, final Oas30Operation operation) {
        final ObjectNode bodySchema = createJsonSchemaForBodyOf(json, operation);

        final ObjectNode parametersSchema = createJsonSchemaForParametersOf(openApiDoc, operation);

        return unifiedJsonSchema("Request", "API request payload", bodySchema, parametersSchema);
    }

    @Override
    public DataShape createShapeFromResponse(final ObjectNode json, final Oas30Document openApiDoc, final Oas30Operation operation) {
        final Optional<Oas30Response> maybeResponse = findResponse(operation, RESPONSE_HAS_SCHEMA, Oas30Response.class);

        if (!maybeResponse.isPresent()) {
            return DATA_SHAPE_NONE;
        }

        final Oas30Response response = maybeResponse.get();
        final Oas30Schema responseSchema = Oas30ModelHelper.getSchema(response, APPLICATION_JSON);
        final String description = response.description;

        final ObjectNode bodySchema = createSchemaFromModel(json, description, responseSchema);

        return unifiedJsonSchema("Response", "API response payload", bodySchema, null);
    }

    @Override
    public Optional<NameAndSchema> findBodySchema(Oas30Operation operation) {
        if (operation.requestBody == null) {
            return empty();
        }

        Oas30MediaType body = Oas30ModelHelper.getMediaType(operation.requestBody, APPLICATION_JSON);
        if (body != null) {
            String name = ofNullable(body.getName()).orElse(operation.requestBody.description);
            return Optional.of(new NameAndSchema(name, body.schema));
        }

        return empty();
    }

    private static void addEnumsTo(final ObjectNode parameterParameter, final Oas30Schema schema) {
        if (schema.items != null) {
            final Oas30Schema items = (Oas30Schema) schema.items;

            List<String> enums = ofNullable(items.enum_).orElse(Collections.emptyList());
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
            final List<String> enums = schema.enum_;

            if (enums != null && !enums.isEmpty()) {
                final ArrayNode enumArray = parameterParameter.putArray("enum");
                enums.forEach(enumArray::add);
            }
        }
    }

    private static ObjectNode createJsonSchemaForParametersOf(final Oas30Document openApiDoc, final Oas30Operation operation) {
        final List<Oas30Parameter> operationParameters = Oas30ModelHelper.getParameters(operation);

        OasPathItem parent = ofNullable(operation.parent())
            .filter(OasPathItem.class::isInstance)
            .map(OasPathItem.class::cast)
            .orElse(null);
        final List<Oas30Parameter> pathParameters = Oas30ModelHelper.getParameters(parent);
        operationParameters.addAll(pathParameters);

        final Map<String, Oas30ParameterDefinition> globalParameters = Oas30ModelHelper.getParameters(openApiDoc);
        operationParameters.addAll(globalParameters.values());

        return createSchemaFor(operationParameters.stream()
            .filter(p -> p.schema instanceof Oas30Schema && ((Oas30Schema) p.schema).type != null)
            .filter(OasModelHelper::isSerializable)
            .collect(Collectors.toList()));
    }

    private static ObjectNode createSchemaFor(final List<Oas30Parameter> parameterList) {
        if (parameterList.isEmpty()) {
            return null;
        }

        final ObjectNode schema = JsonSchemaHelper.newJsonObjectSchema();
        final ObjectNode properties = schema.putObject("properties");

        final ObjectNode parameters = properties.putObject("parameters");
        parameters.put("type", "object");
        final ObjectNode parametersProperties = parameters.putObject("properties");

        for (final Oas30Parameter parameter : parameterList) {
            final Oas30Schema parameterSchema = Oas30ModelHelper.getSchema(parameter);

            if (parameterSchema == null) {
                continue;
            }

            final String type = parameterSchema.type;
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

            final Object defaultValue = parameterSchema.default_;
            if (defaultValue != null) {
                parameterParameter.put("default", String.valueOf(defaultValue));
            }

            addEnumsTo(parameterParameter, parameterSchema);
        }
        return schema;
    }
}
