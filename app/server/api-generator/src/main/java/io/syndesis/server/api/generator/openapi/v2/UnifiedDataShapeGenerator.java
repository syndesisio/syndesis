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
package io.syndesis.server.api.generator.openapi.v2;

import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.syndesis.common.model.DataShape;
import io.syndesis.server.api.generator.openapi.DataShapeGenerator;

final class UnifiedDataShapeGenerator implements DataShapeGenerator<Oas20Document, Oas20Operation> {

    private static final DataShapeGenerator<Oas20Document, Oas20Operation> JSON = new UnifiedJsonDataShapeGenerator();

    private static final DataShapeGenerator<Oas20Document, Oas20Operation> XML = new UnifiedXmlDataShapeGenerator();

    @Override
    public DataShape createShapeFromRequest(final ObjectNode json, final Oas20Document openApiDoc, final Oas20Operation operation) {
        if (supports(APPLICATION_JSON, openApiDoc.consumes, operation.consumes)) {
            return JSON.createShapeFromRequest(json, openApiDoc, operation);
        } else if (supports(APPLICATION_XML, openApiDoc.consumes, operation.consumes)) {
            return XML.createShapeFromRequest(json, openApiDoc, operation);
        } else {
            // most likely a body-less request, i.e. only with parameters, we'll
            // use JSON to define those parameters
            return JSON.createShapeFromRequest(json, openApiDoc, operation);
        }
    }

    @Override
    public DataShape createShapeFromResponse(final ObjectNode json, final Oas20Document openApiDoc, final Oas20Operation operation) {
        if (supports(APPLICATION_JSON, openApiDoc.produces, operation.produces)) {
            return JSON.createShapeFromResponse(json, openApiDoc, operation);
        } else if (supports(APPLICATION_XML, openApiDoc.produces, operation.produces)) {
            return XML.createShapeFromResponse(json, openApiDoc, operation);
        } else {
            // most likely a body-less request, i.e. only with parameters, we'll
            // use JSON to define those parameters
            return JSON.createShapeFromResponse(json, openApiDoc, operation);
        }
    }

    static boolean supports(final String mime, final List<String> defaultMimes, final List<String> mimes) {
        boolean supports = false;
        if (mimes != null && !mimes.isEmpty()) {
            supports = mimes.contains(mime);
        }

        if (defaultMimes != null && !defaultMimes.isEmpty()) {
            supports |= defaultMimes.contains(mime);
        }

        return supports;
    }

}
