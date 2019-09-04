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

import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;

import org.apache.commons.lang3.tuple.Pair;

abstract class BaseDataShapeGenerator implements DataShapeGenerator {

    static Optional<BodyParameter> findBodyParameter(final Operation operation) {
        final List<Parameter> operationParameters = operation.getParameters();

        return operationParameters.stream().filter(p -> p instanceof BodyParameter && ((BodyParameter) p).getSchema() != null)
            .map(BodyParameter.class::cast).findFirst();
    }

    static Optional<Response> findResponse(final Operation operation) {
        return findResponseCodeAndSchema(operation).map(Pair::getValue);
    }

    static Optional<Pair<String, Response>> findResponseCodeAndSchema(final Operation operation) {
        // Return the Response object related to the first 2xx return code found
        Optional<Pair<String, Response>> responseOk = operation.getResponses().entrySet().stream()
            .map(e -> Pair.of(e.getKey(), e.getValue()))
            .filter(p -> p.getKey().startsWith("2"))
            .filter(p -> p.getValue().getResponseSchema() != null).findFirst();

        if (responseOk.isPresent()) {
            return responseOk;
        }

        return operation.getResponses().entrySet().stream()
            .map(e -> Pair.of(e.getKey(), e.getValue()))
            .filter(p -> p.getValue().getResponseSchema() != null)
            .findFirst();
    }

    static String getResponseSpecification(final Operation operation) {
        return findResponseCodeAndSchema(operation).get().getKey();
    }
}
