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

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.models.OasResponses;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20PathItem;
import io.apicurio.datamodels.openapi.v2.models.Oas20Response;
import io.apicurio.datamodels.openapi.v2.models.Oas20SecurityDefinitions;
import io.apicurio.datamodels.openapi.v2.models.Oas20SecurityScheme;
import io.syndesis.common.model.Violation;
import io.syndesis.server.api.generator.openapi.OpenApiModelInfo;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;

/**
 * @author Christoph Deppisch
 */
public final class Oas20ValidationRules {

    private static final Set<String> SUPPORTED_CONSUMED_AUTH_TYPES = new HashSet<>(Arrays.asList("apiKey", "basic", "oauth2"));

    private Oas20ValidationRules() {
        // utility class
    }

    /**
     * Check if all operations contains valid authentication types
     */
    private static OpenApiModelInfo validateAuthTypesIn(final OpenApiModelInfo modelInfo, final Set<String> validAuthTypes) {
        if (modelInfo.getV2Model() == null) {
            return modelInfo;
        }

        final OpenApiModelInfo.Builder withWarnings = new OpenApiModelInfo.Builder().createFrom(modelInfo);

        List<Oas20SecurityScheme> securitySchemes = Optional.ofNullable(modelInfo.getV2Model().securityDefinitions)
            .map(Oas20SecurityDefinitions::getItems)
            .orElse(Collections.emptyList());
        for (final Oas20SecurityScheme definitionEntry : securitySchemes) {
            final String authType = definitionEntry.type;
            if (!validAuthTypes.contains(authType)) {
                withWarnings.addWarning(new Violation.Builder()//
                    .property("")//
                    .error("unsupported-auth")//
                    .message("Authentication type " + authType + " is currently not supported")//
                    .build());
            }
        }

        return withWarnings.build();
    }

    /**
     * Check if all operations contains valid authentication types for consumed
     * APIs.
     */
    public static OpenApiModelInfo validateConsumedAuthTypes(final OpenApiModelInfo modelInfo) {
        return validateAuthTypesIn(modelInfo, SUPPORTED_CONSUMED_AUTH_TYPES);
    }

    public static OpenApiModelInfo validateCyclicReferences(final OpenApiModelInfo info) {
        if (info.getModel() == null) {
            return info;
        }

        if (CyclicValidationCheck.hasCyclicReferences(info.getV2Model())) {
            return new OpenApiModelInfo.Builder().createFrom(info)
                .addError(new Violation.Builder()
                    .error("cyclic-schema")
                    .message("Cyclic references are not suported")
                    .build())
                .build();
        }

        return info;
    }

    public static OpenApiModelInfo validateNoMissingOperationIds(final OpenApiModelInfo info) {
        final Oas20Document openApiDoc = info.getV2Model();
        if (openApiDoc == null || openApiDoc.paths == null) {
            return info;
        }

        final long countNoOpId = OasModelHelper.getPathItems(openApiDoc.paths, Oas20PathItem.class)
            .stream()
            .flatMap(p -> OasModelHelper.getOperationMap(p).values().stream())
            .filter(o -> o.operationId == null)
            .count();

        if (countNoOpId == 0) {
            return info;
        }

        final OpenApiModelInfo.Builder withWarnings = new OpenApiModelInfo.Builder().createFrom(info);
        withWarnings.addWarning(new Violation.Builder()//
            .error("missing-operation-ids")
            .message("Some operations (" + countNoOpId + ") have no operationId").build());

        return withWarnings.build();
    }

    public static OpenApiModelInfo validateOperationsGiven(final OpenApiModelInfo modelInfo) {
        final Oas20Document openApiDoc = modelInfo.getV2Model();
        if (openApiDoc == null) {
            return modelInfo;
        }

        final OpenApiModelInfo.Builder withErrors = new OpenApiModelInfo.Builder().createFrom(modelInfo);

        final List<Oas20PathItem> paths = OasModelHelper.getPathItems(openApiDoc.paths, Oas20PathItem.class);
        if (paths.isEmpty()) {
            withErrors.addError(new Violation.Builder()
                .property("paths")
                .error("missing-paths")
                .message("No paths defined")
                .build());
        } else if (paths.stream().allMatch(p -> OasModelHelper.getOperationMap(p).isEmpty())) {
            withErrors.addError(new Violation.Builder()
                .property("")
                .error("missing-operations")
                .message("No operations defined")
                .build());
        }

        return withErrors.build();
    }

    /**
     * Check if all operations contains valid authentication types for provided
     * APIs.
     */
    public static OpenApiModelInfo validateProvidedAuthTypes(final OpenApiModelInfo modelInfo) {
        return validateAuthTypesIn(modelInfo, Collections.emptySet());
    }

    /**
     * Check if a request/response JSON schema is present
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity"})
    public static OpenApiModelInfo validateResponses(final OpenApiModelInfo modelInfo) {
        final Oas20Document openApiDoc = modelInfo.getV2Model();
        if (openApiDoc == null) {
            return modelInfo;
        }

        final OpenApiModelInfo.Builder withWarnings = new OpenApiModelInfo.Builder().createFrom(modelInfo);

        final List<Oas20PathItem> paths = OasModelHelper.getPathItems(openApiDoc.paths, Oas20PathItem.class);
        for (final Oas20PathItem pathEntry : paths) {
            for (final Map.Entry<String, Oas20Operation> operationEntry : OasModelHelper.getOperationMap(pathEntry, Oas20Operation.class).entrySet()) {

                // Check requests
                for (final OasParameter parameter : OasModelHelper.getParameters(operationEntry.getValue())) {
                    if (!OasModelHelper.isBody(parameter)) {
                        continue;
                    }
                    final OasSchema schema = (OasSchema) parameter.schema;
                    if (OasModelHelper.schemaIsNotSpecified(schema)) {
                        final String message = "Operation " + operationEntry.getKey() + " " + pathEntry.getPath()
                            + " does not provide a schema for the body parameter";

                        withWarnings.addWarning(new Violation.Builder()//
                            .property("")//
                            .error("missing-parameter-schema")//
                            .message(message)//
                            .build());
                    }
                }

                // Check responses
                List<OasResponse> responses = Optional.ofNullable(operationEntry.getValue().responses)
                    .map(OasResponses::getResponses)
                    .orElse(Collections.emptyList());

                for (final OasResponse responseEntry : responses) {
                    if (responseEntry.getStatusCode() == null || responseEntry.getStatusCode().charAt(0) != '2') {
                        continue; // check only correct responses
                    }

                    if (responseEntry instanceof Oas20Response && ((Oas20Response)responseEntry).schema == null) {
                        final String message = "Operation " + operationEntry.getKey().toUpperCase(Locale.US) + " " + pathEntry.getPath()
                            + " does not provide a response schema for code " + responseEntry.getStatusCode();

                        withWarnings.addWarning(new Violation.Builder()//
                            .property("")//
                            .error("missing-response-schema")//
                            .message(message)//
                            .build());
                    }
                }
                // Assume that operations without 2xx responses do not provide a
                // response
            }
        }

        return withWarnings.build();
    }

    public static OpenApiModelInfo validateScheme(final OpenApiModelInfo info) {
        final Oas20Document openApiDoc = info.getV2Model();
        if (openApiDoc == null) {
            return info;
        }

        final OpenApiModelInfo.Builder withWarnings = new OpenApiModelInfo.Builder().createFrom(info);

        final URI specificationUrl = OasModelHelper.specificationUriFrom(openApiDoc);

        final List<String> schemes = openApiDoc.schemes;
        if (schemes == null || schemes.isEmpty()) {
            if (specificationUrl == null) {
                withWarnings.addWarning(new Violation.Builder()//
                    .property("/schemes")//
                    .error("missing-schemes")
                    .message("Unable to determine the scheme to use: OpenAPI document does not provide a `schemes` definition "
                        + "and the document was uploaded so the originating URL is lost.")
                    .build());
            }
        } else {
            final boolean hasHttpSchemes = schemes.stream().anyMatch(s -> "http".equals(s) || "https".equals(s));
            if (!hasHttpSchemes) {
                withWarnings.addWarning(new Violation.Builder()//
                    .property("/schemes")//
                    .error("missing-schemes")
                    .message("Unable to determine the scheme to use: no supported scheme found within the OpenAPI document. "
                        + "Schemes given in the document: "
                        + String.join(", ", schemes))
                    .build());
            }

        }

        return withWarnings.build();
    }

    public static OpenApiModelInfo validateUniqueOperationIds(final OpenApiModelInfo info) {
        final Oas20Document openApiDoc = info.getV2Model();
        if (openApiDoc == null || openApiDoc.paths == null) {
            return info;
        }

        final Map<String, Long> operationIdCounts = OasModelHelper.getPathItems(openApiDoc.paths, Oas20PathItem.class)
            .stream()
            .flatMap(p -> OasModelHelper.getOperationMap(p).values().stream())//
            .map(o -> o.operationId)//
            .filter(Objects::nonNull)//
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        final Map<String, Long> nonUnique = operationIdCounts.entrySet().stream().filter(e -> e.getValue() > 1)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (nonUnique.isEmpty()) {
            return info;
        }

        final OpenApiModelInfo.Builder withWarnings = new OpenApiModelInfo.Builder().createFrom(info);
        withWarnings.addWarning(new Violation.Builder()//
            .error("non-unique-operation-ids")
            .message("Found operations with non unique operationIds: " + String.join(", ", nonUnique.keySet())).build());

        return withWarnings.build();
    }
}
