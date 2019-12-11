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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30Response;
import io.apicurio.datamodels.openapi.v3.models.Oas30SchemaDefinition;
import io.apicurio.datamodels.openapi.v3.models.Oas30SecurityScheme;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.openapi.OpenApiModelInfo;
import io.syndesis.server.api.generator.openapi.OpenApiValidationRules;

/**
 * @author Christoph Deppisch
 */
public final class Oas30ValidationRules extends OpenApiValidationRules<Oas30Response, Oas30SecurityScheme, Oas30SchemaDefinition> {

    Oas30ValidationRules(APIValidationContext context) {
        super(context);
    }

    public static Oas30ValidationRules get(final APIValidationContext context) {
        return new Oas30ValidationRules(context);
    }

    @Override
    protected List<Oas30Response> getResponses(OasOperation operation) {
        if (operation.responses == null) {
            return Collections.emptyList();
        }

        return operation.responses.getResponses().stream()
            .filter(Oas30Response.class::isInstance)
            .map(Oas30Response.class::cast)
            .collect(Collectors.toList());
    }

    @Override
    protected boolean hasResponseSchema(Oas30Response response) {
        return Oas30ModelHelper.getSchema(response).isPresent();
    }

    @Override
    protected Map<String, Oas30SchemaDefinition> getSchemaDefinitions(OpenApiModelInfo info) {
        Oas30Document openApiDoc = info.getV3Model();

        if (openApiDoc == null
            || openApiDoc.components == null
            || openApiDoc.components.schemas == null) {
            return Collections.emptyMap();
        }

        return openApiDoc.components.schemas;
    }

    @Override
    protected List<String> getSchemes(OpenApiModelInfo info) {
        if (info.getV3Model().servers == null) {
            return null;
        }

        return info.getV3Model().servers.stream().map(Oas30ModelHelper::getScheme).collect(Collectors.toList());
    }

    @Override
    protected Collection<Oas30SecurityScheme> getSecuritySchemes(OpenApiModelInfo info) {
        if (info.getV3Model().components == null || info.getV3Model().components.securitySchemes == null) {
            return Collections.emptyList();
        }

        final Map<String, Oas30SecurityScheme> securitySchemes = info.getV3Model().components.securitySchemes;
        return securitySchemes.values();
    }
}
