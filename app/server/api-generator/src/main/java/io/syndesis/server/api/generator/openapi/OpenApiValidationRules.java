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
package io.syndesis.server.api.generator.openapi;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.apicurio.datamodels.core.models.common.SecurityScheme;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.syndesis.common.model.Violation;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;

/**
 * This class contains Syndesis custom validation rules for Open API specifications.
 */
public abstract class OpenApiValidationRules<T extends OasResponse, S extends SecurityScheme, D extends OasSchema> implements Function<OpenApiModelInfo, OpenApiModelInfo> {

    private final List<Function<OpenApiModelInfo, OpenApiModelInfo>> rules = new ArrayList<>();

    /**
     * Constructor initializes rules based on given validation context and specific rules for consumer and producer APIs.
     * Subclasses may provide special rules in addition to generic rules added in this base class.
     * @param context the validation context specifying the consumer or provider API
     * @param consumerRules specific rules to add for consumed APIs
     * @param providerRules specific rules to add for provided APIs
     */
    protected OpenApiValidationRules(final APIValidationContext context,
                                     final List<Function<OpenApiModelInfo, OpenApiModelInfo>> consumerRules,
                                     final List<Function<OpenApiModelInfo, OpenApiModelInfo>> providerRules) {
        switch (context) {
        case CONSUMED_API:
            rules.addAll(consumerRules);
            rules.add(this::validateRequestResponseBodySchemas);
            rules.add(this::validateConsumedAuthTypes);
            rules.add(this::validateScheme);
            rules.add(this::validateUniqueOperationIds);
            rules.add(this::validateCyclicReferences);
            rules.add(this::validateOperationsGiven);
            return;
        case PROVIDED_API:
            rules.addAll(providerRules);
            rules.add(this::validateRequestResponseBodySchemas);
            rules.add(this::validateProvidedAuthTypes);
            rules.add(this::validateUniqueOperationIds);
            rules.add(OpenApiValidationRules::validateNoMissingOperationIds);
            rules.add(this::validateCyclicReferences);
            rules.add(this::validateOperationsGiven);
            return;
        case NONE:
            return;
        default:
            throw new IllegalArgumentException("Unsupported validation context " + context);
        }
    }

    @Override
    public OpenApiModelInfo apply(final OpenApiModelInfo modelInfo) {
        return rules.stream().reduce(Function::compose).map(f -> f.apply(modelInfo)).orElse(modelInfo);
    }

    protected abstract List<T> getResponses(OasOperation operation);

    protected abstract boolean hasResponseSchema(T responseEntry);

    protected abstract Optional<Violation> validateRequestSchema(String operationId, String path, OasOperation operation);

    protected abstract Map<String, D> getSchemaDefinitions(OpenApiModelInfo info);

    protected abstract List<String> getSchemes(OpenApiModelInfo info);

    protected abstract Collection<S> getSecuritySchemes(OpenApiModelInfo info);

    /**
     * Check if all operations contains valid authentication types
     */
    private OpenApiModelInfo validateAuthTypesIn(final OpenApiModelInfo modelInfo, final Set<String> validAuthTypes) {
        if (modelInfo.getModel() == null) {
            return modelInfo;
        }

        final OpenApiModelInfo.Builder withWarnings = new OpenApiModelInfo.Builder().createFrom(modelInfo);

        Collection<S> securitySchemes = getSecuritySchemes(modelInfo);
        for (final S definitionEntry : securitySchemes) {
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
    public OpenApiModelInfo validateConsumedAuthTypes(final OpenApiModelInfo modelInfo) {
        return validateAuthTypesIn(modelInfo, OpenApiSecurityScheme.namesAndAliases());
    }

    public OpenApiModelInfo validateCyclicReferences(final OpenApiModelInfo info) {
        if (info.getModel() == null) {
            return info;
        }

        if (CyclicValidationCheck.hasCyclicReferences(getSchemaDefinitions(info))) {
            return new OpenApiModelInfo.Builder().createFrom(info)
                .addError(new Violation.Builder()
                    .error("cyclic-schema")
                    .message("Cyclic references are not supported")
                    .build())
                .build();
        }

        return info;
    }

    private static OpenApiModelInfo validateNoMissingOperationIds(final OpenApiModelInfo info) {
        final OasDocument openApiDoc = info.getModel();
        if (openApiDoc == null || openApiDoc.paths == null) {
            return info;
        }

        final long countNoOpId = OasModelHelper.getPathItems(openApiDoc.paths)
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

    public OpenApiModelInfo validateOperationsGiven(final OpenApiModelInfo modelInfo) {
        final OasDocument openApiDoc = modelInfo.getModel();
        if (openApiDoc == null) {
            return modelInfo;
        }

        final OpenApiModelInfo.Builder withErrors = new OpenApiModelInfo.Builder().createFrom(modelInfo);

        final List<OasPathItem> paths = OasModelHelper.getPathItems(openApiDoc.paths);
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
    public OpenApiModelInfo validateProvidedAuthTypes(final OpenApiModelInfo modelInfo) {
        return validateAuthTypesIn(modelInfo, Collections.emptySet());
    }

    /**
     * Check if a request/response JSON schema is present
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity"})
    public OpenApiModelInfo validateRequestResponseBodySchemas(final OpenApiModelInfo modelInfo) {
        final OasDocument openApiDoc = modelInfo.getModel();
        if (openApiDoc == null) {
            return modelInfo;
        }

        final OpenApiModelInfo.Builder withWarnings = new OpenApiModelInfo.Builder().createFrom(modelInfo);

        final List<OasPathItem> paths = OasModelHelper.getPathItems(openApiDoc.paths);
        for (final OasPathItem pathEntry : paths) {
            for (final Map.Entry<String, OasOperation> operationEntry : OasModelHelper.getOperationMap(pathEntry).entrySet()) {

                // Check request body schema
                Optional<Violation> missingRequestSchema = validateRequestSchema(operationEntry.getKey().toUpperCase(Locale.US),
                    pathEntry.getPath(), operationEntry.getValue());
                missingRequestSchema.ifPresent(withWarnings::addWarning);

                // Check response body schemas
                List<T> responses = getResponses(operationEntry.getValue());
                for (final T responseEntry : responses) {
                    if (responseEntry.getStatusCode() == null || responseEntry.getStatusCode().charAt(0) != '2') {
                        continue; // check only correct responses
                    }

                    if (!hasResponseSchema(responseEntry)) {
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

    private OpenApiModelInfo validateScheme(final OpenApiModelInfo info) {
        if (info.getModel() == null) {
            return info;
        }

        final OpenApiModelInfo.Builder withWarnings = new OpenApiModelInfo.Builder().createFrom(info);

        final URI specificationUrl = OasModelHelper.specificationUriFrom(info.getModel());

        final List<String> schemes = getSchemes(info);
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

    public OpenApiModelInfo validateUniqueOperationIds(final OpenApiModelInfo info) {
        final OasDocument openApiDoc = info.getModel();
        if (openApiDoc == null || openApiDoc.paths == null) {
            return info;
        }

        final Map<String, Long> operationIdCounts = OasModelHelper.getPathItems(openApiDoc.paths)
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
