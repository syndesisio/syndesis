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

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import io.syndesis.common.model.Violation;

import static java.util.Optional.ofNullable;

/**
 * @author Christoph Deppisch
 */
public interface OpenApiSchemaValidator {

    /**
     * Validates given specification Json and add validation errors and warnings to given open api info model builder.
     * @param specRoot the specification as Json root.
     * @param modelBuilder the model builder receiving all validation errors and warnings.
     */
    void validateJSonSchema(JsonNode specRoot, OpenApiModelInfo.Builder modelBuilder);

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
}
