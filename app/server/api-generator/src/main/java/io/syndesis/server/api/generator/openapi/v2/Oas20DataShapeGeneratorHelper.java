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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20Parameter;
import io.apicurio.datamodels.openapi.v2.models.Oas20ParameterDefinition;
import io.apicurio.datamodels.openapi.v2.models.Oas20ParameterDefinitions;
import io.syndesis.server.api.generator.openapi.DataShapeGenerator;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

final class Oas20DataShapeGeneratorHelper {

    private Oas20DataShapeGeneratorHelper() {
        // utility class
    }

    static List<OasResponse> resolveResponses(Oas20Document openApiDoc, List<OasResponse> operationResponses) {
        if (openApiDoc.responses == null) {
            return operationResponses;
        }

        List<OasResponse> responses = new ArrayList<>();

        for (OasResponse response : operationResponses) {
            if (response.$ref != null) {
                responses.add(openApiDoc.responses.getResponse(OasModelHelper.getReferenceName(response.$ref)));
            } else {
                responses.add(response);
            }
        }

        return responses;
    }

    static Optional<DataShapeGenerator.NameAndSchema> findBodySchema(Oas20Operation operation) {
        Optional<OasParameter> maybeBody = Oas20ModelHelper.findBodyParameter(operation);

        if (maybeBody.isPresent()) {
            OasParameter body = maybeBody.get();
            String name = ofNullable(body.getName()).orElse(body.description);
            return Optional.of(new DataShapeGenerator.NameAndSchema(name, (OasSchema) body.schema));
        }

        return empty();
    }

    static List<Oas20Parameter> getOperationParameters(Oas20Document openApiDoc, Oas20Operation operation) {
        final List<Oas20Parameter> operationParameters = Oas20ModelHelper.getParameters(operation);

        OasPathItem parent = ofNullable(operation.parent())
            .filter(OasPathItem.class::isInstance)
            .map(OasPathItem.class::cast)
            .orElse(null);
        final List<Oas20Parameter> pathParameters = Oas20ModelHelper.getParameters(parent);
        operationParameters.addAll(pathParameters);

        final List<Oas20ParameterDefinition> globalParameters = ofNullable(openApiDoc.parameters)
            .map(Oas20ParameterDefinitions::getItems)
            .orElse(Collections.emptyList());
        operationParameters.addAll(globalParameters);

        return operationParameters;
    }
}
