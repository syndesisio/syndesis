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
package io.syndesis.server.api.generator.openapi.v2;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import io.syndesis.server.api.generator.openapi.DataShapeGenerator;
import io.syndesis.server.api.generator.openapi.UnifiedJsonDataShapeSupport;
import io.syndesis.server.api.generator.openapi.util.JsonSchemaHelper;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;
import org.apache.commons.lang3.StringUtils;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.trimToNull;

class UnifiedJsonDataShapeGenerator extends UnifiedJsonDataShapeSupport<Oas20Document, Oas20Operation> implements DataShapeGenerator<Oas20Document, Oas20Operation> {

    private static final Predicate<Oas20Response> RESPONSE_HAS_SCHEMA = response -> response.schema != null;

    @Override
    public DataShape createShapeFromRequest(final ObjectNode json, final Oas20Document openApiDoc, final Oas20Operation operation) {
        final ObjectNode bodySchema = createJsonSchemaForBodyOf(json, operation);

        final ObjectNode parametersSchema = createJsonSchemaForParametersOf(openApiDoc, operation);

        return unifiedJsonSchema("Request", "API request payload", bodySchema, parametersSchema);
    }

    @Override
    public DataShape createShapeFromResponse(final ObjectNode json, final Oas20Document openApiDoc, final Oas20Operation operation) {
        final Optional<Oas20Response> maybeResponse = findResponse(operation, RESPONSE_HAS_SCHEMA, Oas20Response.class);

        if (!maybeResponse.isPresent()) {
            return DATA_SHAPE_NONE;
        }

        final Oas20Response response = maybeResponse.get();
        final Oas20Schema responseSchema = response.schema;
        final String description = response.description;

        final ObjectNode bodySchema = createSchemaFromModel(json, description, responseSchema);

        return unifiedJsonSchema("Response", "API response payload", bodySchema, null);
    }

    @Override
    public Optional<NameAndSchema> findBodySchema(Oas20Operation operation) {
        Optional<OasParameter> maybeBody = Oas20ModelHelper.findBodyParameter(operation);

        if (maybeBody.isPresent()) {
            OasParameter body = maybeBody.get();
            String name = ofNullable(body.getName()).orElse(body.description);
            return Optional.of(new NameAndSchema(name, (OasSchema) body.schema));
        }

        return empty();
    }

    private static void addEnumsTo(final ObjectNode parameterParameter, final Oas20Parameter parameter) {
        if (parameter.items != null) {
            final Oas20Items items = parameter.items;

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
            final List<String> enums = parameter.enum_;

            if (enums != null && !enums.isEmpty()) {
                final ArrayNode enumArray = parameterParameter.putArray("enum");
                enums.forEach(enumArray::add);
            }
        }
    }

    private static ObjectNode createJsonSchemaForParametersOf(final Oas20Document openApiDoc, final Oas20Operation operation) {
        final List<Oas20Parameter> operationParameters = Oas20ModelHelper.getParameters(operation);

        OasPathItem parent = ofNullable(operation.parent())
            .filter(OasPathItem.class::isInstance)
            .map(OasPathItem.class::cast)
            .orElse(null);
        final List<Oas20Parameter> pathParameters = Oas20ModelHelper.getParameters(parent);
        operationParameters.addAll(pathParameters);

        final List<Oas20ParameterDefinition> globalParameters = ofNullable(openApiDoc.parameters)
                .map(Oas20ParameterDefinitions::getItems)
                .orElse(Collections.emptyList());
        operationParameters.addAll(globalParameters);

        return createSchemaFor(operationParameters.stream()
            .filter(p -> p.type != null)
            .filter(OasModelHelper::isSerializable)
            .collect(Collectors.toList()));
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
}
