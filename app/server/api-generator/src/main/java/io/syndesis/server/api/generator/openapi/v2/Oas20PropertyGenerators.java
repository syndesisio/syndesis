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
import java.util.function.Function;
import java.util.stream.Collectors;

import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Scopes;
import io.apicurio.datamodels.openapi.v2.models.Oas20SecurityDefinitions;
import io.apicurio.datamodels.openapi.v2.models.Oas20SecurityScheme;
import io.syndesis.server.api.generator.openapi.OpenApiModelInfo;
import io.syndesis.server.api.generator.openapi.OpenApiPropertyGenerator;

import static java.util.Optional.ofNullable;

public class Oas20PropertyGenerators extends OpenApiPropertyGenerator<Oas20Document, Oas20SecurityScheme> {

    @Override
    protected Function<Oas20SecurityScheme, String> authorizationUrl() {
        return scheme -> scheme.authorizationUrl;
    }

    @Override
    protected String basePath(OpenApiModelInfo info) {
        return info.getV2Model().basePath;
    }

    @Override
    protected Function<Oas20SecurityScheme, String> tokenUrl() {
        return scheme -> scheme.tokenUrl;
    }

    @Override
    protected Function<Oas20SecurityScheme, String> scopes() {
        return scheme -> ofNullable(scheme.scopes).map(Oas20Scopes::getScopeNames).map(scopes -> scopes.stream().sorted().collect(Collectors.joining(" "))).orElse(null);
    }

    @Override
    protected Collection<Oas20SecurityScheme> getSecuritySchemes(OpenApiModelInfo info) {
        return ofNullable(info.getV2Model().securityDefinitions)
            .map(Oas20SecurityDefinitions::getSecuritySchemes)
            .orElse(Collections.emptyList());
    }

    @Override
    protected String getFlow(Oas20SecurityScheme scheme) {
        return scheme.flow;
    }

    @Override
    protected String getHost(OpenApiModelInfo info) {
        return info.getV2Model().host;
    }

    @Override
    protected List<String> getSchemes(OpenApiModelInfo info) {
        return info.getV2Model().schemes;
    }
}
