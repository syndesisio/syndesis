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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30MediaType;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.apicurio.datamodels.openapi.v3.models.Oas30Parameter;
import io.apicurio.datamodels.openapi.v3.models.Oas30ParameterDefinition;
import io.syndesis.server.api.generator.openapi.DataShapeGenerator;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;

import static io.syndesis.server.api.generator.openapi.DataShapeGenerator.APPLICATION_XML;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

final class Oas30DataShapeGeneratorHelper {

    private Oas30DataShapeGeneratorHelper() {
        // utility class
    }

    static List<OasResponse> resolveResponses(Oas30Document openApiDoc, List<OasResponse> operationResponses) {
        if (openApiDoc.components == null || openApiDoc.components.responses == null) {
            return operationResponses;
        }

        List<OasResponse> responses = new ArrayList<>();

        for (OasResponse response : operationResponses) {
            if (response.$ref != null) {
                responses.add(openApiDoc.components.responses.get(OasModelHelper.getReferenceName(response.$ref)));
            } else {
                responses.add(response);
            }
        }

        return responses;
    }

    static Optional<DataShapeGenerator.NameAndSchema> findBodySchema(Oas30Operation operation) {
        if (operation.requestBody == null) {
            return empty();
        }

        Optional<Oas30MediaType> body = Oas30ModelHelper.getMediaType(operation.requestBody, APPLICATION_XML);
        if (body.isPresent()) {
            String name = ofNullable(body.get().getName()).orElse(operation.requestBody.description);
            return Optional.of(new DataShapeGenerator.NameAndSchema(name, body.get().schema));
        }

        return empty();
    }

    static List<Oas30Parameter> getOperationParameters(Oas30Document openApiDoc, Oas30Operation operation) {
        final List<Oas30Parameter> operationParameters = Oas30ModelHelper.getParameters(operation);

        OasPathItem parent = ofNullable(operation.parent())
            .filter(OasPathItem.class::isInstance)
            .map(OasPathItem.class::cast)
            .orElse(null);
        final List<Oas30Parameter> pathParameters = Oas30ModelHelper.getParameters(parent);
        operationParameters.addAll(pathParameters);

        final Map<String, Oas30ParameterDefinition> globalParameters = Oas30ModelHelper.getParameters(openApiDoc);
        operationParameters.addAll(globalParameters.values());

        return operationParameters;
    }
}
