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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.core.models.Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.syndesis.common.model.Violation;
import io.syndesis.common.util.IOStreams;
import io.syndesis.common.util.Resources;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.swagger.OpenApiModelInfo;
import io.syndesis.server.api.generator.swagger.OpenApiValidationRules;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import static java.util.Optional.ofNullable;

/**
 * @author Christoph Deppisch
 */
public final class Oas20ModelParser {

    private static final Pattern JSON_TEST = Pattern.compile("^\\s*\\{.*");

    private static final Logger LOG = LoggerFactory.getLogger(Oas20ModelParser.class);

    private static final JsonSchema SWAGGER_2_0_SCHEMA;

    private static final String SWAGGER_2_0_SCHEMA_FILE = "schema/swagger-2.0-schema.json";

    private static final String SWAGGER_IO_V2_SCHEMA_URI = "http://swagger.io/v2/schema.json#";

    private static final Yaml YAML_PARSER = new Yaml();

    static {
        try {
            final JsonNode swagger20Schema = JsonUtils.reader().readTree(Resources.getResourceAsText(SWAGGER_2_0_SCHEMA_FILE));
            final LoadingConfiguration loadingConfiguration = LoadingConfiguration.newBuilder()
                .preloadSchema(SWAGGER_IO_V2_SCHEMA_URI, swagger20Schema)
                .freeze();
            SWAGGER_2_0_SCHEMA = JsonSchemaFactory.newBuilder().setLoadingConfiguration(loadingConfiguration).freeze().getJsonSchema(SWAGGER_IO_V2_SCHEMA_URI);
        } catch (final ProcessingException | IOException ex) {
            throw new IllegalStateException("Unable to load the schema file embedded in the artifact", ex);
        }
    }

    private Oas20ModelParser() {
        // utility class
    }

    public static OpenApiModelInfo parse(final String specification, final APIValidationContext validationContext) {
        final OpenApiModelInfo.Builder resultBuilder = new OpenApiModelInfo.Builder();

        final String resolvedSpecification;
        try {
            resolvedSpecification = resolve(specification);
            resultBuilder.resolvedSpecification(resolvedSpecification);
        } catch (final Exception e) {
            LOG.debug("Unable to resolve OpenAPI document\n{}\n", specification, e);
            return resultBuilder
                .addError(new Violation.Builder().error("error").property("").message("Unable to resolve OpenAPI document from: "
                    + ofNullable(specification).map(s -> StringUtils.abbreviate(s, 100)).orElse("")).build())
                .build();
        }

        final JsonNode tree;
        try {
            tree = JsonUtils.reader().readTree(resolvedSpecification);
        } catch (final IOException e) {
            return new OpenApiModelInfo.Builder()
                .addError(new Violation.Builder()
                    .property("")
                    .error("ureadable-document")
                    .message("Unable to read OpenAPI document: " + e.getMessage())
                    .build())
                .build();
        }

        final JsonNode swaggerVersion = tree.get("swagger");
        if (swaggerVersion == null || swaggerVersion.isNull() || !"2.0".equals(swaggerVersion.textValue())) {
            return new OpenApiModelInfo.Builder()
                .addError(new Violation.Builder()
                    .property("")
                    .error("unsupported-version")
                    .message("This document cannot be uploaded. Provide an OpenAPI 2.0 document.")
                    .build())
                .build();
        }

        final Document parsed = Library.readDocumentFromJSONString(resolvedSpecification);
        if (!(parsed instanceof Oas20Document)) {
            LOG.debug("Unable to read OpenAPI document\n{}\n", specification);
            return resultBuilder
                .addError(new Violation.Builder().error("error").property("").message("Unable to read OpenAPI document from: '"
                    + StringUtils.abbreviate(specification, 100)).build())
                .build();
        }

        if (validationContext != APIValidationContext.NONE) {
            final OpenApiModelInfo swaggerModelInfo = validateJSonSchema(resolvedSpecification, (Oas20Document) parsed);
            return OpenApiValidationRules.get(validationContext).apply(swaggerModelInfo);
        }
        return resultBuilder.model((Oas20Document) parsed).build();
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    static String resolve(final String specification) throws Exception {
        final String specificationToUse;
        if (specification.toLowerCase(Locale.US).startsWith("http")) {
            specificationToUse = resolve(new URL(specification));
        } else {
            specificationToUse = specification;
        }

        final JsonNode node = convertToJson(specificationToUse);

        return JsonUtils.writer().writeValueAsString(node);
    }

    static String resolve(URL url) {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int status = con.getResponseCode();
            if (status > 299) {
                throw new IllegalStateException(String.format("Failed to retrieve Open API specification from %s", url),
                    new IOException(IOStreams.readText(con.getErrorStream())));
            } else {
                return IOStreams.readText(con.getInputStream());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to retrieve Swagger Open API specification: " + url.toString(), e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    private static OpenApiModelInfo validateJSonSchema(final String specification, final Oas20Document model) {
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

            return new OpenApiModelInfo.Builder().errors(errors).warnings(warnings).model(model).resolvedSpecification(specification)
                .build();

        } catch (IOException | ProcessingException ex) {
            LOG.error("Unable to load the schema file embedded in the artifact", ex);
            return new OpenApiModelInfo.Builder().addError(new Violation.Builder().error("error").property("")
                .message("Unable to load the OpenAPI schema file embedded in the artifact").build()).build();
        }
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

    static JsonNode convertToJson(final String specification) throws IOException {
        final JsonNode specRoot;
        if (JSON_TEST.matcher(specification).matches()) {
            specRoot = JsonUtils.reader().readTree(specification);
        } else {
            specRoot = JsonUtils.convertValue(YAML_PARSER.load(specification), JsonNode.class);
        }
        return specRoot;
    }
}
