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
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.syndesis.common.model.Violation;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.openapi.util.JsonSchemaHelper;
import org.immutables.value.Value;

/**
 * Class holding information about a open api model and related validations.
 */
@Value.Immutable
@JsonDeserialize(builder = OpenApiModelInfo.Builder.class)
public interface OpenApiModelInfo {

    class Builder extends ImmutableOpenApiModelInfo.Builder {
        // make ImmutableOpenApiModelInfo.Builder accessible
    }

    @Value.Default
    default List<Violation> getErrors() {
        return Collections.emptyList();
    }

    OasDocument getModel();

    @Value.Lazy
    default OpenApiVersion getApiVersion() {
        return OpenApiVersion.fromDocumentType(getModel());
    }

    @JsonIgnore
    default Oas20Document getV2Model() {
        if (getApiVersion() != OpenApiVersion.V2) {
            throw new IllegalStateException("Unable to retrieve OpenAPI 2.x document.");
        }

        return (Oas20Document) getModel();
    }

    @JsonIgnore
    default Oas30Document getV3Model() {
        if (getApiVersion() != OpenApiVersion.V3) {
            throw new IllegalStateException("Unable to retrieve OpenAPI 3.x document.");
        }

        return (Oas30Document) getModel();
    }

    @Value.Lazy
    default ObjectNode getResolvedJsonGraph() {
        try {
            final ObjectNode json = (ObjectNode) JsonUtils.reader().readTree(getResolvedSpecification());

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
