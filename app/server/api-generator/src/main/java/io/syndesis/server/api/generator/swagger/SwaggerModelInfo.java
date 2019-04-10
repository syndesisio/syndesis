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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import io.swagger.models.Swagger;
import io.syndesis.common.model.Violation;
import io.syndesis.common.util.Json;
import io.syndesis.server.api.generator.swagger.util.JsonSchemaHelper;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Class holding information about a swagger model and related validations.
 */
@Value.Immutable
@JsonDeserialize(builder = SwaggerModelInfo.Builder.class)
public interface SwaggerModelInfo {

    class Builder extends ImmutableSwaggerModelInfo.Builder {
        // make ImmutableSwaggerModelInfo.Builder accessible
    }

    @Value.Default
    default List<Violation> getErrors() {
        return Collections.emptyList();
    }

    Swagger getModel();

    @Value.Lazy
    default ObjectNode getResolvedJsonGraph() {
        try {
            final ObjectNode json = (ObjectNode) Json.reader().readTree(getResolvedSpecification());

            return JsonSchemaHelper.resolvableNodeForSpecification(json);
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to parse OpenAPI document resolved as JSON", e);
        }
    }

    String getResolvedSpecification();

    @Value.Default
    default List<Violation> getWarnings() {
        return Collections.emptyList();
    }
}
