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

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.syndesis.core.Json;
import io.syndesis.model.Violation;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

public final class SwaggerHelper {

    private static final Logger LOG = LoggerFactory.getLogger(SwaggerHelper.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String SWAGGER_2_0_SCHEMA_FILE = "/schema/swagger-2.0-schema.json";

    private static final JsonSchema SWAGGER_2_0_SCHEMA;

    static {
        try {
            SWAGGER_2_0_SCHEMA = JsonSchemaFactory.byDefault().getJsonSchema("resource:" + SWAGGER_2_0_SCHEMA_FILE);
        } catch (ProcessingException ex) {
            throw new IllegalStateException("Unable to load the schema file embedded in the artifact", ex);
        }
    }

    private SwaggerHelper() {
        // utility class
    }

    /* default */ static String serialize(final Swagger swagger) {
        try {
            return Json.mapper().writeValueAsString(swagger);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize Swagger specification", e);
        }
    }

    /* default */ static SwaggerModelInfo parse(final String specification, boolean validate) {
        final SwaggerParser parser = new SwaggerParser();
        final Swagger swagger = ofNullable(parser.read(specification)).orElseGet(() -> parser.parse(specification));
        if (swagger == null) {
            LOG.debug("Unable to read Swagger specification\n{}\n", specification);
            return new SwaggerModelInfo.Builder()
                .addError(new Violation.Builder()
                    .error("error")
                    .property("")
                    .message("Unable to read Swagger specification from: " + ofNullable(specification).map(s -> StringUtils.abbreviate(s, 100)).orElse(""))
                    .build())
                .build();
        }

        if (validate) {
            SwaggerModelInfo swaggerModelInfo = validateJSonSchema(specification, swagger);
            return SyndesisSwaggerValidationRules.getInstance().apply(swaggerModelInfo);
        }
        return new SwaggerModelInfo.Builder().model(swagger).build();
    }

    private static SwaggerModelInfo validateJSonSchema(String specification, Swagger model) {
        try {
            JsonNode specRoot = OBJECT_MAPPER.readTree(specification);
            ProcessingReport report = SWAGGER_2_0_SCHEMA.validate(specRoot);
            List<Violation> errors = new ArrayList<>();
            List<Violation> warnings = new ArrayList<>();
            for (ProcessingMessage message : report) {
                boolean added = append(errors, message, Optional.of("error"));
                if (!added) {
                    append(warnings, message, Optional.empty());
                }
            }

            return new SwaggerModelInfo.Builder()
                .errors(errors)
                .warnings(warnings)
                .model(model)
                .build();

        } catch (IOException | ProcessingException ex) {
            LOG.error("Unable to load the schema file embedded in the artifact", ex);
            return new SwaggerModelInfo.Builder()
                .addError(new Violation.Builder()
                    .error("error")
                    .property("")
                    .message("Unable to load the swagger schema file embedded in the artifact")
                    .build())
                .build();
        }
    }

    private static boolean append(List<Violation> violations, ProcessingMessage message, Optional<String> requiredLevel) {
        if (requiredLevel.isPresent()) {
            Optional<String> level = ofNullable(message.asJson())
                .flatMap(node -> ofNullable(node.get("level")))
                .flatMap(node -> ofNullable(node.textValue()));

            if (!level.equals(requiredLevel)) {
                return false; // skip
            }
        }

        Optional<String> property = ofNullable(message.asJson())
            .flatMap(node -> ofNullable(node.get("instance")))
            .flatMap(node -> ofNullable(node.get("pointer")))
            .flatMap(node -> ofNullable(node.textValue()));

        Optional<String> error = ofNullable(message.asJson())
            .flatMap(node -> ofNullable(node.get("domain")))
            .flatMap(node -> ofNullable(node.textValue()));

        violations.add(new Violation.Builder()
            .error(error.orElse(""))
            .message(message.getMessage())
            .property(property.orElse(""))
            .build());

        return true;
    }
}
