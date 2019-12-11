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
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;

/**
 * Data shape generator creates request and response data shapes from OpenAPI operations.
 * @param <T> the OpenAPI document type representing version 2.x or 3.x.
 * @param <O> the OpenAPI operation type.
 */
public interface DataShapeGenerator<T extends OasDocument, O extends OasOperation> {

    String APPLICATION_JSON = "application/json";

    String APPLICATION_XML = "application/xml";

    DataShape DATA_SHAPE_NONE = new DataShape.Builder().kind(DataShapeKinds.NONE).build();

    DataShape createShapeFromRequest(ObjectNode json, T openApiDoc, O operation);

    DataShape createShapeFromResponse(ObjectNode json, T openApiDoc, O operation);

    /**
     * Find schema that is specified to define the body if any.
     * @param openApiDoc the OpenAPI document.
     * @param operation maybe holding a body schema.
     * @return the body schema.
     */
    default Optional<NameAndSchema> findBodySchema(final T openApiDoc, final O operation) {
        return Optional.empty();
    }

    /**
     * Combination of schema and name for request body.
     */
    class NameAndSchema {
        protected final String name;
        protected final OasSchema schema;

        public NameAndSchema(String name, OasSchema schema) {
            this.name = name;
            this.schema = schema;
        }
    }

    /**
     * Find response for given operation. Favors positive responses with status code 2xx and a body schema.
     * Only in case no positive response is present pick the first response with a schema present.
     * @param openApiDoc the OpenAPI document.
     * @param operation the operation holding some response definitions.
     * @param hasSchema predicate checks that response has a schema defined.
     * @param responseType the target response type.
     * @param <R> type of the response to return.
     * @return a response on the given operation that has a schema or empty.
     */
    default <R extends OasResponse> Optional<R> findResponse(final T openApiDoc, final O operation,
                                                             final Predicate<R> hasSchema, Class<R> responseType) {
        if (operation.responses == null) {
            return Optional.empty();
        }

        List<OasResponse> responses = resolveResponses(openApiDoc, operation.responses.getResponses());

        // Return the Response object related to the first 2xx return code found
        Optional<R> responseOk = responses.stream()
            .filter(responseType::isInstance)
            .filter(r -> r.getStatusCode() != null && r.getStatusCode().startsWith("2"))
            .map(responseType::cast)
            .filter(hasSchema)
            .findFirst();

        if (responseOk.isPresent()) {
            return responseOk;
        }

        return responses.stream()
            .filter(responseType::isInstance)
            .map(responseType::cast)
            .filter(hasSchema)
            .findFirst();
    }

    /**
     * Method resolves response references in given list of operation responses. Each response can reference a reusable
     * response definition on the OpenAPI document. When such a reference is found in the list of responses resolve the reference to
     * the definition object. So the resulting list of responses does only container real response objects and no references anymore.
     * @param openApiDoc the OpenAPI document.
     * @param operationResponses the responses for an operation.
     * @return list of real response objects where references to reusable response definitions are resolved.
     */
    default List<OasResponse> resolveResponses(T openApiDoc, List<OasResponse> operationResponses) {
        return operationResponses;
    }
}
