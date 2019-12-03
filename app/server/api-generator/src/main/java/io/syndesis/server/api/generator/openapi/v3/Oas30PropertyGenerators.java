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
import java.util.function.Function;
import java.util.stream.Collectors;

import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30SecurityScheme;
import io.syndesis.server.api.generator.openapi.OpenApiModelInfo;
import io.syndesis.server.api.generator.openapi.OpenApiPropertyGenerator;

import static java.util.Optional.ofNullable;

public class Oas30PropertyGenerators extends OpenApiPropertyGenerator<Oas30Document, Oas30SecurityScheme> {

    @Override
    protected Function<Oas30SecurityScheme, String> authorizationUrl() {
        return scheme -> {
            if (scheme.flows == null || scheme.flows.authorizationCode == null) {
                return null;
            }

            return scheme.flows.authorizationCode.authorizationUrl;
        };
    }

    @Override
    protected String basePath(OpenApiModelInfo info) {
        return Oas30ModelHelper.getBasePath(info.getV3Model());
    }

    @Override
    protected Function<Oas30SecurityScheme, String> tokenUrl() {
        return scheme -> {
            if (scheme.flows == null || scheme.flows.authorizationCode == null) {
                return null;
            }

            return scheme.flows.authorizationCode.tokenUrl;
        };
    }

    @Override
    protected Function<Oas30SecurityScheme, String> scopes() {
        return scheme -> {
            if (scheme.flows == null || scheme.flows.authorizationCode == null) {
                return null;
            }

            return ofNullable(scheme.flows.authorizationCode.scopes).map(Map::keySet).map(scopes -> scopes.stream().sorted().collect(Collectors.joining(" "))).orElse(null);
        };
    }

    @Override
    protected Collection<Oas30SecurityScheme> getSecuritySchemes(OpenApiModelInfo info) {
        if (info.getV3Model().components == null || info.getV3Model().components.securitySchemes == null) {
            return Collections.emptyList();
        }

        final Map<String, Oas30SecurityScheme> securitySchemes = info.getV3Model().components.securitySchemes;
        return securitySchemes.values();
    }

    @Override
    protected String getFlow(Oas30SecurityScheme scheme) {
        return Oas30ModelHelper.getAuthFlow(scheme);
    }

    @Override
    protected String getHost(OpenApiModelInfo info) {
        return Oas30ModelHelper.getHost(info.getV3Model());
    }

    @Override
    protected List<String> getSchemes(OpenApiModelInfo info) {
        if (info.getV3Model().servers == null) {
            return null;
        }

        return info.getV3Model().servers.stream().map(Oas30ModelHelper::getScheme).collect(Collectors.toList());
    }
}
