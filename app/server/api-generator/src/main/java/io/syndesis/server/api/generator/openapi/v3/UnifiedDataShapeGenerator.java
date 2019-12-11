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
package io.syndesis.server.api.generator.openapi.v3;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.apicurio.datamodels.openapi.v3.models.Oas30Response;
import io.syndesis.common.model.DataShape;
import io.syndesis.server.api.generator.openapi.DataShapeGenerator;

final class UnifiedDataShapeGenerator implements DataShapeGenerator<Oas30Document, Oas30Operation> {

    private static final DataShapeGenerator<Oas30Document, Oas30Operation> JSON = new UnifiedJsonDataShapeGenerator();

    private static final DataShapeGenerator<Oas30Document, Oas30Operation> XML = new UnifiedXmlDataShapeGenerator();

    @Override
    public DataShape createShapeFromRequest(final ObjectNode json, final Oas30Document openApiDoc, final Oas30Operation operation) {
        Set<String> consumes = Optional.ofNullable(operation.requestBody)
            .map(req -> req.content)
            .map(Map::keySet)
            .orElse(Collections.emptySet());

        if (supports(APPLICATION_JSON, consumes)) {
            return JSON.createShapeFromRequest(json, openApiDoc, operation);
        } else if (supports(APPLICATION_XML, consumes)) {
            return XML.createShapeFromRequest(json, openApiDoc, operation);
        } else {
            // most likely a body-less request, i.e. only with parameters, we'll
            // use JSON to define those parameters
            return JSON.createShapeFromRequest(json, openApiDoc, operation);
        }
    }

    @Override
    public DataShape createShapeFromResponse(final ObjectNode json, final Oas30Document openApiDoc, final Oas30Operation operation) {
        Optional<Oas30Response> response = findResponse(openApiDoc, operation, res -> Oas30ModelHelper.getSchema(res).isPresent(), Oas30Response.class);

        if (!response.isPresent()) {
            return DATA_SHAPE_NONE;
        }

        Set<String> produces = response.get().content.keySet();
        if (supports(APPLICATION_JSON, produces)) {
            return JSON.createShapeFromResponse(json, openApiDoc, operation);
        } else if (supports(APPLICATION_XML, produces)) {
            return XML.createShapeFromResponse(json, openApiDoc, operation);
        } else {
            // most likely a body-less request, i.e. only with parameters, we'll
            // use JSON to define those parameters
            return JSON.createShapeFromResponse(json, openApiDoc, operation);
        }
    }

    @Override
    public List<OasResponse> resolveResponses(Oas30Document openApiDoc, List<OasResponse> operationResponses) {
        return Oas30DataShapeGeneratorHelper.resolveResponses(openApiDoc, operationResponses);
    }

    static boolean supports(final String mime, final Set<String> mimes) {
        if (mimes != null && !mimes.isEmpty()) {
            return mimes.contains(mime);
        }

        return false;
    }

}
