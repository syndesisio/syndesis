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
package io.syndesis.server.api.generator.swagger;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.swagger.models.ArrayModel;
import io.swagger.models.HttpMethod;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.syndesis.common.model.Violation;
import io.syndesis.server.api.generator.APIValidationContext;

/**
 * This class contains Syndesis custom validation rules for swagger definitions.
 */
public final class SyndesisSwaggerValidationRules implements Function<SwaggerModelInfo, SwaggerModelInfo> {

    private static final Set<String> SUPPORTED_CONSUMED_AUTH_TYPES = new HashSet<>(Arrays.asList("apiKey", "basic", "oauth2"));

    private final List<Function<SwaggerModelInfo, SwaggerModelInfo>> rules = new ArrayList<>();

    private SyndesisSwaggerValidationRules(final APIValidationContext context) {
        switch (context) {
        case CONSUMED_API:
            rules.add(SyndesisSwaggerValidationRules::validateResponses);
            rules.add(SyndesisSwaggerValidationRules::validateConsumedAuthTypes);
            rules.add(SyndesisSwaggerValidationRules::validateScheme);
            rules.add(SyndesisSwaggerValidationRules::validateUniqueOperationIds);
            rules.add(SyndesisSwaggerValidationRules::validateCyclicReferences);
            rules.add(SyndesisSwaggerValidationRules::validateOperationsGiven);
            return;
        case PROVIDED_API:
            rules.add(SyndesisSwaggerValidationRules::validateResponses);
            rules.add(SyndesisSwaggerValidationRules::validateProvidedAuthTypes);
            rules.add(SyndesisSwaggerValidationRules::validateUniqueOperationIds);
            rules.add(SyndesisSwaggerValidationRules::validateNoMissingOperationIds);
            rules.add(SyndesisSwaggerValidationRules::validateCyclicReferences);
            rules.add(SyndesisSwaggerValidationRules::validateOperationsGiven);
            return;
        case NONE:
            return;
        default:
            throw new IllegalArgumentException("Unsupported validation context " + context);
        }
    }

    @Override
    public SwaggerModelInfo apply(final SwaggerModelInfo swaggerModelInfo) {
        return rules.stream().reduce(Function::compose).map(f -> f.apply(swaggerModelInfo)).orElse(swaggerModelInfo);
    }

    public static SyndesisSwaggerValidationRules get(final APIValidationContext context) {
        return new SyndesisSwaggerValidationRules(context);
    }

    /**
     * Check if all operations contains valid authentication types
     */
    static SwaggerModelInfo validateAuthTypesIn(final SwaggerModelInfo swaggerModelInfo, final Set<String> validAuthTypes) {

        if (swaggerModelInfo.getModel() == null) {
            return swaggerModelInfo;
        }

        final SwaggerModelInfo.Builder withWarnings = new SwaggerModelInfo.Builder().createFrom(swaggerModelInfo);

        for (final Map.Entry<String, SecuritySchemeDefinition> definitionEntry : notNull(
            swaggerModelInfo.getModel().getSecurityDefinitions()).entrySet()) {
            final String authType = definitionEntry.getValue().getType();
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
    static SwaggerModelInfo validateConsumedAuthTypes(final SwaggerModelInfo swaggerModelInfo) {
        return validateAuthTypesIn(swaggerModelInfo, SUPPORTED_CONSUMED_AUTH_TYPES);
    }

    static SwaggerModelInfo validateCyclicReferences(final SwaggerModelInfo info) {
        if (CyclicValidationCheck.hasCyclicReferences(info.getModel())) {
            return new SwaggerModelInfo.Builder().createFrom(info)
                .addError(new Violation.Builder()
                    .error("cyclic-schema")
                    .message("Cyclic references are not suported")
                    .build())
                .build();
        }

        return info;
    }

    static SwaggerModelInfo validateNoMissingOperationIds(final SwaggerModelInfo info) {
        final Swagger swagger = info.getModel();
        if (swagger == null || swagger.getPaths() == null) {
            return info;
        }

        final long countNoOpId = swagger.getPaths().values().stream()
            .flatMap(p -> p.getOperationMap().values().stream())
            .filter(o -> o.getOperationId() == null)
            .count();

        if (countNoOpId == 0) {
            return info;
        }

        final SwaggerModelInfo.Builder withWarnings = new SwaggerModelInfo.Builder().createFrom(info);
        withWarnings.addWarning(new Violation.Builder()//
            .error("missing-operation-ids")
            .message("Some operations (" + countNoOpId + ") have no operationId").build());

        return withWarnings.build();
    }

    static SwaggerModelInfo validateOperationsGiven(final SwaggerModelInfo swaggerModelInfo) {
        final Swagger swagger = swaggerModelInfo.getModel();
        if (swagger == null) {
            return swaggerModelInfo;
        }

        final SwaggerModelInfo.Builder withErrors = new SwaggerModelInfo.Builder().createFrom(swaggerModelInfo);
        final Map<String, Path> paths = swagger.getPaths();
        if (paths == null || paths.isEmpty()) {
            withErrors.addError(new Violation.Builder()
                .property("paths")
                .error("missing-paths")
                .message("No paths defined")
                .build());
        } else if (paths.values().stream().allMatch(p -> p.getOperations() == null || p.getOperations().isEmpty())) {
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
    static SwaggerModelInfo validateProvidedAuthTypes(final SwaggerModelInfo swaggerModelInfo) {
        return validateAuthTypesIn(swaggerModelInfo, Collections.emptySet());
    }

    /**
     * Check if a request/response JSON schema is present
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity"})
    static SwaggerModelInfo validateResponses(final SwaggerModelInfo swaggerModelInfo) {
        if (swaggerModelInfo.getModel() == null) {
            return swaggerModelInfo;
        }

        final SwaggerModelInfo.Builder withWarnings = new SwaggerModelInfo.Builder().createFrom(swaggerModelInfo);

        for (final Map.Entry<String, Path> pathEntry : notNull(swaggerModelInfo.getModel().getPaths()).entrySet()) {
            for (final Map.Entry<HttpMethod, Operation> operationEntry : notNull(pathEntry.getValue().getOperationMap()).entrySet()) {

                // Check requests
                for (final Parameter parameter : notNull(operationEntry.getValue().getParameters())) {
                    if (!(parameter instanceof BodyParameter)) {
                        continue;
                    }
                    final BodyParameter bodyParameter = (BodyParameter) parameter;
                    final Model schema = bodyParameter.getSchema();
                    if (schemaIsNotSpecified(schema)) {
                        final String message = "Operation " + operationEntry.getKey() + " " + pathEntry.getKey()
                            + " does not provide a schema for the body parameter";

                        withWarnings.addWarning(new Violation.Builder()//
                            .property("")//
                            .error("missing-parameter-schema")//
                            .message(message)//
                            .build());
                    }
                }

                // Check responses
                for (final Map.Entry<String, Response> responseEntry : notNull(operationEntry.getValue().getResponses()).entrySet()) {
                    if (responseEntry.getKey().charAt(0) != '2') {
                        continue; // check only correct responses
                    }

                    if (responseEntry.getValue().getSchema() == null) {
                        final String message = "Operation " + operationEntry.getKey() + " " + pathEntry.getKey()
                            + " does not provide a response schema for code " + responseEntry.getKey();

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

    static SwaggerModelInfo validateScheme(final SwaggerModelInfo info) {
        final Swagger swagger = info.getModel();
        if (swagger == null) {
            return info;
        }

        final SwaggerModelInfo.Builder withWarnings = new SwaggerModelInfo.Builder().createFrom(info);

        final URI specificationUrl = specificationUriFrom(swagger);

        final List<Scheme> schemes = swagger.getSchemes();
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
            final boolean hasHttpSchemes = schemes.stream()//
                .filter(s -> s.toValue().startsWith("http"))//
                .findFirst().isPresent();
            if (!hasHttpSchemes) {
                withWarnings.addWarning(new Violation.Builder()//
                    .property("/schemes")//
                    .error("missing-schemes")
                    .message("Unable to determine the scheme to use: no supported scheme found within the OpenAPI document. "
                        + "Schemes given in the document: "
                        + schemes.stream().map(s -> s.toValue()).collect(Collectors.joining(", ")))
                    .build());
            }

        }

        return withWarnings.build();
    }

    static SwaggerModelInfo validateUniqueOperationIds(final SwaggerModelInfo info) {
        final Swagger swagger = info.getModel();
        if (swagger == null || swagger.getPaths() == null) {
            return info;
        }

        final Map<String, Long> operationIdCounts = swagger.getPaths().values().stream()//
            .flatMap(p -> p.getOperationMap().values().stream())//
            .map(Operation::getOperationId)//
            .filter(Objects::nonNull)//
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        final Map<String, Long> nonUnique = operationIdCounts.entrySet().stream().filter(e -> e.getValue() > 1)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        if (nonUnique.isEmpty()) {
            return info;
        }

        final SwaggerModelInfo.Builder withWarnings = new SwaggerModelInfo.Builder().createFrom(info);
        withWarnings.addWarning(new Violation.Builder()//
            .error("non-unique-operation-ids")
            .message("Found operations with non unique operationIds: " + String.join(", ", nonUnique.keySet())).build());

        return withWarnings.build();
    }

    private static <T> List<T> notNull(final List<T> value) {
        return value != null ? value : Collections.emptyList();
    }

    private static <K, V> Map<K, V> notNull(final Map<K, V> value) {
        return value != null ? value : Collections.emptyMap();
    }

    private static boolean schemaIsNotSpecified(final Model schema) {
        if (schema == null) {
            return true;
        }

        if (schema instanceof ArrayModel) {
            return ((ArrayModel) schema).getItems() == null;
        }

        final Map<String, Property> properties = schema.getProperties();

        final boolean noProperties = properties == null || properties.isEmpty();

        final boolean noReference = schema.getReference() == null;

        return noProperties && noReference;
    }

    private static URI specificationUriFrom(final Swagger swagger) {
        final Map<String, Object> vendorExtensions = Optional.ofNullable(swagger.getVendorExtensions()).orElse(Collections.emptyMap());
        return (URI) vendorExtensions.get(BaseSwaggerConnectorGenerator.URL_EXTENSION);
    }

}
