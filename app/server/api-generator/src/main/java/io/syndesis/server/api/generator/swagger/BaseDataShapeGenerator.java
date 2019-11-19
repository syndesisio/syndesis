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
import java.util.Optional;

import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20Response;
import org.apache.commons.lang3.tuple.Pair;

abstract class BaseDataShapeGenerator implements DataShapeGenerator {

    static Optional<OasParameter> findBodyParameter(final OasOperation operation) {
        if (operation.parameters == null) {
            return Optional.empty();
        }

        final List<OasParameter> operationParameters = operation.parameters;

        return operationParameters.stream()
            .filter(p -> "body".equals(p.in) && p.schema != null)
            .findFirst();
    }

    static Optional<Oas20Response> findResponse(final OasOperation operation) {
        return findResponseCodeAndSchema(operation).map(Pair::getValue);
    }

    private static Optional<Pair<String, Oas20Response>> findResponseCodeAndSchema(final OasOperation operation) {
        if (operation.responses == null) {
            return Optional.empty();
        }

        List<OasResponse> responses = operation.responses.getResponses();

        // Return the Response object related to the first 2xx return code found
        Optional<Pair<String, Oas20Response>> responseOk = responses.stream()
            .filter(r -> r instanceof Oas20Response)
            .map(r -> Pair.of(r.getStatusCode(), (Oas20Response) r))
            .filter(p -> p.getKey() != null && p.getKey().startsWith("2"))
            .filter(p -> p.getValue().schema != null)
            .findFirst();

        if (responseOk.isPresent()) {
            return responseOk;
        }

        return responses.stream()
            .filter(r -> r instanceof Oas20Response)
            .map(r -> Pair.of(r.getStatusCode(), (Oas20Response) r))
            .filter(p -> p.getValue().schema != null)
            .findFirst();
    }

    static String getResponseSpecification(final Oas20Operation operation) {
        return findResponseCodeAndSchema(operation)
                    .map(Pair::getKey)
                    .orElse(null);
    }
}
