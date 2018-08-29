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

import java.util.List;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.syndesis.common.model.DataShape;

import com.fasterxml.jackson.databind.node.ObjectNode;

public final class UnifiedDataShapeGenerator implements DataShapeGenerator {

    private static final String APPLICATION_JSON = "application/json";

    private static final String APPLICATION_XML = "application/xml";

    private static final DataShapeGenerator JSON = new UnifiedJsonDataShapeGenerator();

    private static final DataShapeGenerator XML = new UnifiedXmlDataShapeGenerator();

    @Override
    public DataShape createShapeFromRequest(final ObjectNode json, final Swagger swagger, final Operation operation) {
        if (supports(APPLICATION_JSON, swagger.getConsumes(), operation.getConsumes())) {
            return JSON.createShapeFromRequest(json, swagger, operation);
        } else if (supports(APPLICATION_XML, swagger.getConsumes(), operation.getConsumes())) {
            return XML.createShapeFromRequest(json, swagger, operation);
        } else {
            // most likely a body-less request, i.e. only with parameters, we'll
            // use JSON to define those parameters
            return JSON.createShapeFromRequest(json, swagger, operation);
        }
    }

    @Override
    public DataShape createShapeFromResponse(final ObjectNode json, final Swagger swagger, final Operation operation) {
        if (supports(APPLICATION_JSON, swagger.getProduces(), operation.getProduces())) {
            return JSON.createShapeFromResponse(json, swagger, operation);
        } else if (supports(APPLICATION_XML, swagger.getProduces(), operation.getProduces())) {
            return XML.createShapeFromResponse(json, swagger, operation);
        } else {
            // most likely a body-less request, i.e. only with parameters, we'll
            // use JSON to define those parameters
            return JSON.createShapeFromResponse(json, swagger, operation);
        }
    }

    static boolean supports(final String mime, final List<String> defaultMimes, final List<String> mimes) {
        boolean supports = false;
        if (mimes != null && !mimes.isEmpty()) {
            supports |= mimes.contains(mime);
        }

        if (defaultMimes != null && !defaultMimes.isEmpty()) {
            supports |= defaultMimes.contains(mime);
        }

        return supports;
    }

}
