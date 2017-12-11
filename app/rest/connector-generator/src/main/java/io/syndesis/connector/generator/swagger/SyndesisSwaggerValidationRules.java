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

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.syndesis.model.Violation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * This class contains Syndesis custom validation rules for swagger definitions.
 */
public class SyndesisSwaggerValidationRules implements Function<SwaggerModelInfo, SwaggerModelInfo> {

    private static final SyndesisSwaggerValidationRules INSTANCE = new SyndesisSwaggerValidationRules();

    private static final String[] SUPPORTED_AUTH_TYPES = {"basic", "apiKey", "oauth2"};

    private List<Function<SwaggerModelInfo, SwaggerModelInfo>> rules = new ArrayList<>();

    private SyndesisSwaggerValidationRules() {
        rules.add(this::validateResponses);
        rules.add(this::validateAuthTypes);
    }

    public static SyndesisSwaggerValidationRules getInstance() {
        return INSTANCE;
    }

    /**
     * Check if a request/response JSON schema is present
     */
    private SwaggerModelInfo validateResponses(SwaggerModelInfo swaggerModelInfo) {

        if (swaggerModelInfo.getModel() != null) {

            for (Map.Entry<String, Path> pathEntry : notNull(swaggerModelInfo.getModel().getPaths()).entrySet()) {
                for (Map.Entry<HttpMethod, Operation> operationEntry : notNull(pathEntry.getValue().getOperationMap()).entrySet()) {

                    // Check requests
                    for (Parameter parameter : notNull(operationEntry.getValue().getParameters())) {
                        if (!(parameter instanceof BodyParameter)) {
                            continue;
                        }
                        BodyParameter bodyParameter = (BodyParameter) parameter;
                        if (bodyParameter.getSchema() == null) {
                            swaggerModelInfo = swaggerModelInfo.withWarning(new Violation.Builder()
                                .property("")
                                .error("missing-parameter-schema")
                                .message("Operation " + operationEntry.getKey() + " " + pathEntry.getKey() + " does not provide a schema for the body parameter")
                                .build());
                        }
                    }

                    // Check responses
                    for (Map.Entry<String, Response> responseEntry : notNull(operationEntry.getValue().getResponses()).entrySet()) {
                        if (!responseEntry.getKey().startsWith("2")) {
                            continue; // check only correct responses
                        }

                        if (responseEntry.getValue().getSchema() == null) {
                            swaggerModelInfo = swaggerModelInfo.withWarning(new Violation.Builder()
                                .property("")
                                .error("missing-response-schema")
                                .message("Operation " + operationEntry.getKey() + " " + pathEntry.getKey() + " does not provide a response schema for code " + responseEntry.getKey())
                                .build());
                        }
                    }
                    // Assume that operations without 2xx responses do not provide a response

                }
            }

        }

        return swaggerModelInfo;
    }

    /**
     * Check if all operations contains valid authentication types
     */
    private SwaggerModelInfo validateAuthTypes(SwaggerModelInfo swaggerModelInfo) {

        if (swaggerModelInfo.getModel() != null) {

            for (Map.Entry<String, SecuritySchemeDefinition> definitionEntry : notNull(swaggerModelInfo.getModel().getSecurityDefinitions()).entrySet()) {
                String authType = definitionEntry.getValue().getType();
                if (!Arrays.asList(SUPPORTED_AUTH_TYPES).contains(authType)) {
                    swaggerModelInfo = swaggerModelInfo.withWarning(new Violation.Builder()
                        .property("")
                        .error("unsupported-auth")
                        .message("Authentication type " + authType + " is currently not supported")
                        .build());
                }
            }
        }

        return swaggerModelInfo;
    }

    @Override
    public SwaggerModelInfo apply(SwaggerModelInfo swaggerModelInfo) {
        return rules.stream().reduce(Function::compose)
            .map(f -> f.apply(swaggerModelInfo))
            .orElse(swaggerModelInfo);
    }

    private <K,V> Map<K,V> notNull(Map<K,V> value) {
        return value != null ? value : Collections.emptyMap();
    }

    private <T> List<T> notNull(List<T> value) {
        return value != null ? value : Collections.emptyList();
    }

}
