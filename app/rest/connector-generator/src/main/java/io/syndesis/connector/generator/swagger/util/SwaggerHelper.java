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
package io.syndesis.connector.generator.swagger.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Optional;

import static java.util.Optional.ofNullable;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.RemoteUrl;
import io.syndesis.connector.generator.swagger.SwaggerModelInfo;
import io.syndesis.connector.generator.swagger.SyndesisSwaggerValidationRules;
import io.syndesis.core.Json;
import io.syndesis.model.Violation;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import static org.apache.commons.lang3.StringUtils.trimToNull;

public final class SwaggerHelper {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private static final Logger LOG = LoggerFactory.getLogger(SwaggerHelper.class);

    private static final JsonSchema SWAGGER_2_0_SCHEMA;

    private static final String SWAGGER_2_0_SCHEMA_FILE = "/schema/swagger-2.0-schema.json";

    private static final Yaml YAML_PARSER = new Yaml();

    static {
        try {
            SWAGGER_2_0_SCHEMA = JsonSchemaFactory.byDefault().getJsonSchema("resource:" + SWAGGER_2_0_SCHEMA_FILE);
        } catch (final ProcessingException ex) {
            throw new IllegalStateException("Unable to load the schema file embedded in the artifact", ex);
        }
    }

    private SwaggerHelper() {
        // utility class
    }

    public static OperationDescription operationDescriptionOf(final Swagger swagger, final Operation operation) {
        final Entry<String, Path> pathEntry = swagger.getPaths().entrySet().stream()
            .filter(e -> e.getValue().getOperations().contains(operation)).findFirst().get();
        final String path = pathEntry.getKey();

        final Entry<HttpMethod, Operation> operationEntry = pathEntry.getValue().getOperationMap().entrySet().stream()
            .filter(e -> e.getValue().equals(operation)).findFirst().get();
        final HttpMethod method = operationEntry.getKey();

        final String specifiedSummary = trimToNull(operation.getSummary());
        final String specifiedDescription = trimToNull(operation.getDescription());

        final String name = ofNullable(specifiedSummary).orElseGet(() -> method + " " + path);
        final String description = ofNullable(specifiedDescription).orElseGet(() -> "Send " + method + " request to " + path);

        return new OperationDescription(name, description);
    }

    public static SwaggerModelInfo parse(final String specification, final boolean validate) {
        final SwaggerModelInfo.Builder resultBuilder = new SwaggerModelInfo.Builder();

        final String resolvedSpecification;
        try {
            resolvedSpecification = resolve(specification);
            resultBuilder.resolvedSpecification(resolvedSpecification);
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") final Exception e) {
            LOG.debug("Unable to resolve Swagger specification\n{}\n", specification, e);
            return resultBuilder
                .addError(new Violation.Builder().error("error").property("").message("Unable to resolve Swagger specification from: "
                    + ofNullable(specification).map(s -> StringUtils.abbreviate(s, 100)).orElse("")).build())
                .build();
        }

        final SwaggerParser parser = new SwaggerParser();
        final Swagger swagger = parser.parse(resolvedSpecification);
        if (swagger == null) {
            LOG.debug("Unable to read Swagger specification\n{}\n", specification);
            return resultBuilder
                .addError(new Violation.Builder().error("error").property("").message("Unable to read Swagger specification from: "
                    + ofNullable(specification).map(s -> StringUtils.abbreviate(s, 100)).orElse("")).build())
                .build();
        }

        if (validate) {
            final SwaggerModelInfo swaggerModelInfo = validateJSonSchema(resolvedSpecification, swagger);
            return SyndesisSwaggerValidationRules.getInstance().apply(swaggerModelInfo);
        }
        return resultBuilder.model(swagger).build();
    }

    public static String serialize(final Swagger swagger) {
        try {
            return Json.writer().writeValueAsString(swagger);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize Swagger specification", e);
        }
    }

    static JsonNode convertToJson(final String specification) throws IOException, JsonProcessingException {
        final JsonNode specRoot;
        if (specification.matches("\\s+\\{")) {
            specRoot = JSON_MAPPER.readTree(specification);
        } else {
            specRoot = JSON_MAPPER.convertValue(YAML_PARSER.load(specification), JsonNode.class);
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

        return Json.writer().writeValueAsString(node);
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
                .message("Unable to load the swagger schema file embedded in the artifact").build()).build();
        }
    }

}
