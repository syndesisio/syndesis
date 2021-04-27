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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import io.syndesis.common.model.Violation;
import io.syndesis.common.util.Resources;
import io.syndesis.common.util.json.JsonUtils;

import org.slf4j.LoggerFactory;

import static java.util.Optional.ofNullable;

public interface OpenApiSchemaValidator {

    /**
     * Provide the json schema for validation.
     * @return the schema that performs the validation.
     */
    JsonSchema getSchema();

    /**
     * Validates given specification Json and add validation errors and warnings to given open api info model builder.
     * @param specRoot the specification as Json root.
     * @param modelBuilder the model builder receiving all validation errors and warnings.
     */
    default void validateJSonSchema(JsonNode specRoot, OpenApiModelInfo.Builder modelBuilder) {
        try {
            final ProcessingReport report = getSchema().validate(specRoot);
            final List<Violation> errors = new ArrayList<>();
            final List<Violation> warnings = new ArrayList<>();
            for (final ProcessingMessage message : report) {
                final boolean added = append(errors, message, Optional.of("error"));
                if (!added) {
                    append(warnings, message, Optional.empty());
                }
            }

            modelBuilder.addAllErrors(errors);
            modelBuilder.addAllWarnings(warnings);

        } catch (ProcessingException ex) {
            LoggerFactory.getLogger(OpenApiSchemaValidator.class).error("Unable to load the schema file embedded in the artifact", ex);
            modelBuilder.addError(new Violation.Builder()
                .error("error").property("")
                .message("Unable to load the OpenAPI schema file embedded in the artifact")
                .build());
        }
    }

    /**
     * Append violations with level filtering.
     * @param violations the list of violations receiving new entries.
     * @param message the current processing message.
     * @param requiredLevel level of violation message.
     * @return true when violation has been added false when skipped.
     */
    default boolean append(final List<Violation> violations, final ProcessingMessage message, final Optional<String> requiredLevel) {
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

    static JsonSchema loadSchema(final String path, final String uri) {
        try {
            final JsonNode oas30Schema = JsonUtils.reader().readTree(Resources.getResourceAsText(path));
            final LoadingConfiguration loadingConfiguration = LoadingConfiguration.newBuilder()
                .preloadSchema(oas30Schema)
                .freeze();
            return JsonSchemaFactory.newBuilder().setLoadingConfiguration(loadingConfiguration).freeze().getJsonSchema(uri);
        } catch (final ProcessingException | IOException ex) {
            throw new IllegalStateException("Unable to load the schema file embedded in the artifact", ex);
        }
    }
}
