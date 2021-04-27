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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Response;
import io.apicurio.datamodels.openapi.v2.models.Oas20SchemaDefinition;
import io.apicurio.datamodels.openapi.v2.models.Oas20SecurityDefinitions;
import io.apicurio.datamodels.openapi.v2.models.Oas20SecurityScheme;
import io.syndesis.common.model.Violation;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.openapi.OpenApiModelInfo;
import io.syndesis.server.api.generator.openapi.OpenApiValidationRules;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;

public class Oas20ValidationRules extends OpenApiValidationRules<Oas20Response, Oas20SecurityScheme, Oas20SchemaDefinition> {

    Oas20ValidationRules(APIValidationContext context) {
        super(context, Collections.emptyList(), Collections.emptyList());
    }

    public static Oas20ValidationRules get(final APIValidationContext context) {
        return new Oas20ValidationRules(context);
    }

    @Override
    protected List<Oas20Response> getResponses(OasOperation operation) {
        if (operation.responses == null) {
            return Collections.emptyList();
        }

        return operation.responses.getResponses().stream()
            .filter(Oas20Response.class::isInstance)
            .map(Oas20Response.class::cast)
            .collect(Collectors.toList());
    }

    @Override
    protected boolean hasResponseSchema(Oas20Response response) {
        return response.schema != null;
    }

    @Override
    protected Optional<Violation> validateRequestSchema(String operationId, String path, OasOperation operation) {
        for (final OasParameter parameter : OasModelHelper.getParameters(operation)) {
            if (!OasModelHelper.isBody(parameter)) {
                continue;
            }
            final OasSchema schema = (OasSchema) parameter.schema;
            if (OasModelHelper.schemaIsNotSpecified(schema)) {
                final String message = "Operation " + operationId + " " + path
                    + " does not provide a schema for the body parameter";

                return Optional.of(new Violation.Builder()//
                    .property("")//
                    .error("missing-request-schema")//
                    .message(message)//
                    .build());
            }
        }

        return Optional.empty();
    }

    @Override
    protected Map<String, Oas20SchemaDefinition> getSchemaDefinitions(OpenApiModelInfo info) {
        Oas20Document openApiDoc = info.getV2Model();

        if (openApiDoc == null
            || openApiDoc.definitions == null) {
            return Collections.emptyMap();
        }

        return openApiDoc.definitions.getDefinitions().stream().collect(Collectors.toMap(Oas20SchemaDefinition::getName, definition -> definition));
    }

    @Override
    protected List<String> getSchemes(OpenApiModelInfo info) {
        return info.getV2Model().schemes;
    }

    @Override
    protected Collection<Oas20SecurityScheme> getSecuritySchemes(OpenApiModelInfo info) {
        return Optional.ofNullable(info.getV2Model().securityDefinitions)
            .map(Oas20SecurityDefinitions::getItems)
            .orElse(Collections.emptyList());
    }
}
