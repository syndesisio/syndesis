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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30MediaType;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.apicurio.datamodels.openapi.v3.models.Oas30RequestBody;
import io.apicurio.datamodels.openapi.v3.models.Oas30Response;
import io.apicurio.datamodels.openapi.v3.models.Oas30SchemaDefinition;
import io.apicurio.datamodels.openapi.v3.models.Oas30SecurityScheme;
import io.syndesis.common.model.Violation;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.openapi.OpenApiModelInfo;
import io.syndesis.server.api.generator.openapi.OpenApiValidationRules;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;

public final class Oas30ValidationRules extends OpenApiValidationRules<Oas30Response, Oas30SecurityScheme, Oas30SchemaDefinition> {

    Oas30ValidationRules(APIValidationContext context) {
        super(context,
            Arrays.asList(
                Oas30ValidationRules::validateUnsupportedCallbacksFeature,
                Oas30ValidationRules::validateUnsupportedLinksFeature
            ),
            Arrays.asList(
                Oas30ValidationRules::validateUnsupportedCallbacksFeature,
                Oas30ValidationRules::validateUnsupportedLinksFeature,
                Oas30ValidationRules::validateServerBasePaths));
    }

    public static Oas30ValidationRules get(final APIValidationContext context) {
        return new Oas30ValidationRules(context);
    }

    /**
     * OpenAPI 3.x adds links feature which is not supported at the moment. Add warning when links are specified
     * either as component or in an individual response object as all links will be ignored.
     * @param info the info holding the OpenAPI document.
     * @return info with maybe a warning added due to the unsupported feature.
     */
    static OpenApiModelInfo validateUnsupportedLinksFeature(OpenApiModelInfo info) {
        if (info.getModel() == null) {
            return info;
        }
        final Oas30Document openApiDoc = info.getV3Model();
        final OpenApiModelInfo.Builder withWarnings = new OpenApiModelInfo.Builder().createFrom(info);

        if (openApiDoc.components != null &&
            openApiDoc.components.links != null &&
            !openApiDoc.components.links.isEmpty()) {
            withWarnings.addWarning(new Violation.Builder()
                    .error("unsupported-links-feature")
                    .message("Links component is not supported yet. This part of the OpenAPI specification will be ignored.")
                    .build());
        }

        final List<OasPathItem> paths = OasModelHelper.getPathItems(info.getModel().paths);
        for (final OasPathItem pathEntry : paths) {
            for (final Map.Entry<String, OasOperation> operationEntry : OasModelHelper.getOperationMap(pathEntry).entrySet()) {
                if (operationEntry.getValue().responses == null) {
                    continue;
                }

                // Check links usage on responses
                List<Oas30Response> responses = operationEntry.getValue().responses.getResponses().stream()
                                                                .filter(Oas30Response.class::isInstance)
                                                                .map(Oas30Response.class::cast)
                                                                .collect(Collectors.toList());
                for (final Oas30Response responseEntry : responses) {
                    if (responseEntry.links != null && !responseEntry.links.isEmpty()) {
                        final String message = "Operation " + operationEntry.getKey().toUpperCase(Locale.US) + " " + pathEntry.getPath()
                            + " uses unsupported links feature. All links will be ignored.";

                        withWarnings.addWarning(new Violation.Builder()//
                            .property("")//
                            .error("unsupported-links-feature")//
                            .message(message)//
                            .build());
                    }
                }
            }
        }

        return withWarnings.build();
    }

    /**
     * OpenAPI 3.x adds callbacks feature which is not supported at the moment. Add warning when callbacks are specified
     * either as component or in an individual operation as all callbacks will be ignored.
     * @param info the info holding the OpenAPI document.
     * @return info with maybe a warning added due to the unsupported feature.
     */
    static OpenApiModelInfo validateUnsupportedCallbacksFeature(OpenApiModelInfo info) {
        if (info.getModel() == null) {
            return info;
        }
        final Oas30Document openApiDoc = info.getV3Model();
        final OpenApiModelInfo.Builder withWarnings = new OpenApiModelInfo.Builder().createFrom(info);

        if (openApiDoc.components != null &&
            openApiDoc.components.callbacks != null &&
            !openApiDoc.components.callbacks.isEmpty()) {
            withWarnings.addWarning(new Violation.Builder()
                    .error("unsupported-callbacks-feature")
                    .message("Callbacks component is not supported yet. This part of the OpenAPI specification will be ignored.")
                    .build());
        }

        final List<OasPathItem> paths = OasModelHelper.getPathItems(info.getModel().paths);
        for (final OasPathItem pathEntry : paths) {
            for (final Map.Entry<String, Oas30Operation> operationEntry : Oas30ModelHelper.getOperationMap(pathEntry).entrySet()) {
                // Check callback usage on operation
                if (operationEntry.getValue().callbacks != null && !operationEntry.getValue().callbacks.isEmpty()) {
                    final String message = "Operation " + operationEntry.getKey().toUpperCase(Locale.US) + " " + pathEntry.getPath()
                        + " uses unsupported callbacks feature. All callbacks will be ignored.";

                    withWarnings.addWarning(new Violation.Builder()//
                        .property("")//
                        .error("unsupported-callbacks-feature")//
                        .message(message)//
                        .build());
                }
            }
        }

        return withWarnings.build();
    }

    /**
     * OpenAPI 3.x is able to specify multiple servers with host URLs. Validates that the specified server URLs
     * share the same base path. Generated REST endpoint can only use one single base path and in case
     * there are differing base paths defined in server URLs we raise a warning.
     * @param info the info holding the OpenAPI document.
     * @return info with maybe a warning added due to differing base paths in servers.
     */
    static OpenApiModelInfo validateServerBasePaths(OpenApiModelInfo info) {
        if (info.getModel() == null) {
            return info;
        }

        final Oas30Document openApiDoc = info.getV3Model();
        if (openApiDoc.servers == null || openApiDoc.servers.isEmpty()) {
            return info;
        }

        List<String> basePaths = openApiDoc.servers.stream().map(Oas30ModelHelper::getBasePath).collect(Collectors.toList());
        if (basePaths.size() > 1 && new HashSet<>(basePaths).size() == basePaths.size()) {
            return new OpenApiModelInfo.Builder().createFrom(info)
                .addWarning(new Violation.Builder()
                    .error("differing-base-paths")
                    .message(String.format("Specified servers do not share the same base path. " +
                        "REST endpoint will use '%s' as base path.", Oas30ModelHelper.getBasePath(openApiDoc)))
                    .build())
                .build();
        }

        return info;
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
    protected Optional<Violation> validateRequestSchema(String operationId, String path, OasOperation operation) {
        if (operation instanceof Oas30Operation) {
            Oas30RequestBody requestBody = ((Oas30Operation) operation).requestBody;
            if (requestBody == null || requestBody.content == null) {
                return Optional.empty();
            }

            for (final Map.Entry<String, Oas30MediaType> mediaType : requestBody.content.entrySet()) {
                final OasSchema schema = mediaType.getValue().schema;
                if (OasModelHelper.schemaIsNotSpecified(schema)) {
                    final String message = "Operation " + operationId + " " + path
                        + " does not provide a schema for the request body on media type " + mediaType.getKey();

                    return Optional.of(new Violation.Builder()//
                        .property("")//
                        .error("missing-request-schema")//
                        .message(message)//
                        .build());
                }
            }
        }

        return Optional.empty();
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
