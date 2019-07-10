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
package io.syndesis.server.api.generator.swagger.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.swagger.models.HttpMethod;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Swagger;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.util.RemoteUrl;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.syndesis.common.model.Violation;
import io.syndesis.common.util.Resources;
import io.syndesis.common.util.openapi.OpenApiHelper;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.swagger.SwaggerModelInfo;
import io.syndesis.server.api.generator.swagger.SyndesisSwaggerValidationRules;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.base.Joiner;

import static java.util.Optional.ofNullable;

import static org.apache.commons.lang3.StringUtils.trimToNull;

public final class SwaggerHelper {

    private static final Pattern JSON_TEST = Pattern.compile("^\\s*\\{.*");

    private static final Pattern JSONDB_DISALLOWED_KEY_CHARS = Pattern.compile("[^ -\"&-\\-0-Z\\^-\u007E\u0080-\u10FFFF]");

    private static final Logger LOG = LoggerFactory.getLogger(SwaggerHelper.class);

    private static final JsonSchema SWAGGER_2_0_SCHEMA;

    private static final String SWAGGER_2_0_SCHEMA_FILE = "schema/swagger-2.0-schema.json";

    private static final String SWAGGER_IO_V2_SCHEMA_URI = "http://swagger.io/v2/schema.json#";

    private static final Yaml YAML_PARSER = new Yaml();

    static {
        try {
            final JsonNode swagger20Schema = OpenApiHelper.mapper().readTree(Resources.getResourceAsText(SWAGGER_2_0_SCHEMA_FILE));
            final LoadingConfiguration loadingConfiguration = LoadingConfiguration.newBuilder()
                .preloadSchema(SWAGGER_IO_V2_SCHEMA_URI, swagger20Schema)
                .freeze();
            SWAGGER_2_0_SCHEMA = JsonSchemaFactory.newBuilder().setLoadingConfiguration(loadingConfiguration).freeze().getJsonSchema(SWAGGER_IO_V2_SCHEMA_URI);
        } catch (final ProcessingException | IOException ex) {
            throw new IllegalStateException("Unable to load the schema file embedded in the artifact", ex);
        }
    }

    private SwaggerHelper() {
        // utility class
    }

    public static ModelImpl dereference(final RefModel reference, final Swagger swagger) {
        return (ModelImpl) swagger.getDefinitions().get(reference.getSimpleRef());
    }

    public static ModelImpl dereference(final RefProperty property, final Swagger swagger) {
        return (ModelImpl) swagger.getDefinitions().get(property.getSimpleRef());
    }

    /**
     * Removes all properties from the given Swagger document that are not used
     * by the REST Swagger Camel component in order to minimize the amount of
     * data stored in the configured properties.
     */
    public static String minimalSwaggerUsedByComponent(final Swagger swagger) {
        final ObjectNode json = OpenApiHelper.mapper().convertValue(swagger, ObjectNode.class);
        json.remove(Arrays.asList("info", "tags", "definitions", "externalDocs"));

        final JsonNode paths = json.get("paths");

        if (paths != null) {
            paths.forEach(path -> {
                path.forEach(operation -> {
                    final ObjectNode operationNode = (ObjectNode) operation;
                    operationNode.remove(Arrays.asList("tags", "summary", "description"));
                    final ArrayNode parameters = (ArrayNode) operation.get("parameters");

                    if (parameters != null) {
                        final List<JsonNode> parametersList = new ArrayList<>(
                            StreamSupport.stream(parameters.spliterator(), false).collect(Collectors.toList()));

                        for (final ListIterator<JsonNode> i = parametersList.listIterator(); i.hasNext();) {
                            final ObjectNode param = (ObjectNode) i.next();
                            param.remove(Arrays.asList("description", "type", "required", "format"));

                            if (!"path".equals(param.get("in").textValue()) && !"query".equals(param.get("in").textValue())) {
                                i.remove();
                            }
                        }

                        if (parameters.size() == 0) {
                            operationNode.remove("parameters");
                        }

                        if (parametersList.isEmpty()) {
                            operationNode.remove("parameters");
                        } else {
                            parameters.removeAll();
                            parameters.addAll(parametersList);
                        }
                    }

                    operationNode.remove("responses");
                });
            });
        }

        try {
            return OpenApiHelper.mapper().writeValueAsString(json);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize minified OpenAPI document", e);
        }
    }

    public static OperationDescription operationDescriptionOf(final Swagger swagger, final Operation operation,
        final BiFunction<String, String, String> consumer) {
        final Entry<String, Path> pathEntry = swagger.getPaths().entrySet().stream()
            .filter(e -> e.getValue().getOperations().contains(operation)).findFirst().get();
        final String path = pathEntry.getKey();

        final Entry<HttpMethod, Operation> operationEntry = pathEntry.getValue().getOperationMap().entrySet().stream()
            .filter(e -> e.getValue().equals(operation)).findFirst().get();
        final HttpMethod method = operationEntry.getKey();

        final String specifiedSummary = trimToNull(operation.getSummary());
        final String specifiedDescription = trimToNull(operation.getDescription());

        final String name = ofNullable(toLiteralNull(specifiedSummary)).orElseGet(() -> method + " " + path);
        final String description = ofNullable(toLiteralNull(specifiedDescription))
            .orElseGet(() -> consumer.apply(method.name(), path));

        return new OperationDescription(name, description);
    }

    public static SwaggerModelInfo parse(final String specification, final APIValidationContext validationContext) {
        final SwaggerModelInfo.Builder resultBuilder = new SwaggerModelInfo.Builder();

        final String resolvedSpecification;
        try {
            resolvedSpecification = resolve(specification);
            resultBuilder.resolvedSpecification(resolvedSpecification);
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") final Exception e) {
            LOG.debug("Unable to resolve OpenAPI document\n{}\n", specification, e);
            return resultBuilder
                .addError(new Violation.Builder().error("error").property("").message("Unable to resolve OpenAPI document from: "
                    + ofNullable(specification).map(s -> StringUtils.abbreviate(s, 100)).orElse("")).build())
                .build();
        }

        final JsonNode tree;
        try {
            tree = OpenApiHelper.mapper().readTree(resolvedSpecification);
        } catch (final IOException e) {
            return new SwaggerModelInfo.Builder()
                .addError(new Violation.Builder()
                    .property("")
                    .error("ureadable-document")
                    .message("Unable to read OpenAPI document: " + e.getMessage())
                    .build())
                .build();
        }

        final JsonNode swaggerVersion = tree.get("swagger");
        if (swaggerVersion == null || swaggerVersion.isNull() || !"2.0".equals(swaggerVersion.textValue())) {
            return new SwaggerModelInfo.Builder()
                .addError(new Violation.Builder()
                    .property("")
                    .error("unsupported-version")
                    .message("This document cannot be uploaded. Provide an OpenAPI 2.0 document.")
                    .build())
                .build();
        }

        final SwaggerDeserializationResult parsed = OpenApiHelper.parseWithResult(resolvedSpecification);
        final Swagger swagger = parsed.getSwagger();
        if (swagger == null) {
            LOG.debug("Unable to read OpenAPI document\n{}\n", specification);
            return resultBuilder
                .addError(new Violation.Builder().error("error").property("").message("Unable to read OpenAPI document from: '"
                    + ofNullable(specification).map(s -> StringUtils.abbreviate(s, 100)).orElse("")
                    + "': "
                    + Joiner.on(',').join(parsed.getMessages())).build())
                .build();
        }

        if (validationContext != APIValidationContext.NONE) {
            final SwaggerModelInfo swaggerModelInfo = validateJSonSchema(resolvedSpecification, swagger);
            return SyndesisSwaggerValidationRules.get(validationContext).apply(swaggerModelInfo);
        }
        return resultBuilder.model(swagger).build();
    }

    /**
     * Makes sure that the tag used as a key in a JSON object is a valid key
     * determined by
     * io.syndesis.server.jsondb.impl.JsonRecordSupport::validateKey.
     */
    public static String sanitizeTag(final String tag) {
        if (StringUtils.isEmpty(tag)) {
            return null;
        }

        final String sanitized = JSONDB_DISALLOWED_KEY_CHARS.matcher(tag).replaceAll("").trim();
        // 768 is maximum length for keys JSONDB supports
        if (sanitized.length() > 768) {
            return sanitized.substring(0, Math.min(tag.length(), 768));
        }

        return sanitized;
    }

    public static Stream<String> sanitizeTags(final List<String> list) {
        if (list == null || list.isEmpty()) {
            return Stream.empty();
        }

        return list.stream().map(SwaggerHelper::sanitizeTag).filter(Objects::nonNull).distinct();
    }

    static JsonNode convertToJson(final String specification) throws IOException, JsonProcessingException {
        final JsonNode specRoot;
        if (JSON_TEST.matcher(specification).matches()) {
            specRoot = OpenApiHelper.mapper().readTree(specification);
        } else {
            specRoot = OpenApiHelper.mapper().convertValue(YAML_PARSER.load(specification), JsonNode.class);
        }
        return specRoot;
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    static String resolve(final String specification) throws Exception {
        final String specificationToUse;
        if (specification.toLowerCase(Locale.US).startsWith("http")) {
            specificationToUse = RemoteUrl.urlToString(specification, null);
        } else {
            specificationToUse = specification;
        }

        final JsonNode node = convertToJson(specificationToUse);

        return OpenApiHelper.mapper().writeValueAsString(node);
    }

    private static boolean append(final List<Violation> violations, final ProcessingMessage message, final Optional<String> requiredLevel) {
        if (requiredLevel.isPresent()) {
            final Optional<String> level = ofNullable(message.asJson()).flatMap(node -> ofNullable(node.get("level")))
                .flatMap(node -> ofNullable(node.textValue()));

            if (!level.equals(requiredLevel)) {
                return false; // skip
            }
        }

        final Optional<String> property = ofNullable(message.asJson()).flatMap(node -> ofNullable(node.get("instance")))
            .flatMap(node -> ofNullable(node.get("pointer"))).flatMap(node -> ofNullable(node.textValue()));

        final Optional<String> error = ofNullable(message.asJson()).flatMap(node -> ofNullable(node.get("domain")))
            .flatMap(node -> ofNullable(node.textValue()));

        violations.add(new Violation.Builder().error(error.orElse("")).message(message.getMessage()).property(property.orElse("")).build());

        return true;
    }

    private static String toLiteralNull(final String given) {
        if (given == null) {
            return null;
        }

        // Swagger parser sometimes interprets empty strings as literal `"null"`
        // strings
        if ("null".equals(given)) {
            return null;
        }

        return given;
    }

    private static SwaggerModelInfo validateJSonSchema(final String specification, final Swagger model) {
        try {
            final JsonNode specRoot = convertToJson(specification);
            final ProcessingReport report = SWAGGER_2_0_SCHEMA.validate(specRoot);
            final List<Violation> errors = new ArrayList<>();
            final List<Violation> warnings = new ArrayList<>();
            for (final ProcessingMessage message : report) {
                final boolean added = append(errors, message, Optional.of("error"));
                if (!added) {
                    append(warnings, message, Optional.empty());
                }
            }

            return new SwaggerModelInfo.Builder().errors(errors).warnings(warnings).model(model).resolvedSpecification(specification)
                .build();

        } catch (IOException | ProcessingException ex) {
            LOG.error("Unable to load the schema file embedded in the artifact", ex);
            return new SwaggerModelInfo.Builder().addError(new Violation.Builder().error("error").property("")
                .message("Unable to load the OpenAPI schema file embedded in the artifact").build()).build();
        }
    }

}
