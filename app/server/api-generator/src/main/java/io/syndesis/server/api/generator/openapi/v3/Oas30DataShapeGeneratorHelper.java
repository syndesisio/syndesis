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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30MediaType;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.apicurio.datamodels.openapi.v3.models.Oas30Parameter;
import io.apicurio.datamodels.openapi.v3.models.Oas30RequestBody;
import io.syndesis.server.api.generator.openapi.DataShapeGenerator;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

final class Oas30DataShapeGeneratorHelper {

    private Oas30DataShapeGeneratorHelper() {
        // utility class
    }

    /**
     * Method resolves response references in given list of operation responses. Each response can reference a reusable
     * response definition on the OpenAPI document. When such a reference is found in the list of responses resolve the reference to
     * the definition object. So the resulting list of responses does only container real response objects and no references anymore.
     * @param openApiDoc the OpenAPI document.
     * @param operationResponses the responses for an operation.
     * @return list of real response objects where references to reusable response definitions are resolved.
     */
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

    /**
     * Find schema that is specified to define the body if any.
     * @param openApiDoc the OpenAPI document.
     * @param operation maybe holding a body schema.
     * @return the body schema.
     */
    static Optional<DataShapeGenerator.NameAndSchema> findBodySchema(Oas30Document openApiDoc,
                                                                     Oas30Operation operation, String mediaType) {
        if (operation.requestBody == null) {
            return empty();
        }

        Oas30RequestBody bodyToUse = resolveRequestBody(openApiDoc, operation.requestBody);
        Optional<Oas30MediaType> body = Oas30ModelHelper.getMediaType(bodyToUse, mediaType);
        if (body.isPresent()) {
            String name = ofNullable(body.get().getName()).orElse(bodyToUse.description);
            return Optional.of(new DataShapeGenerator.NameAndSchema(name, body.get().schema));
        }

        return empty();
    }

    /**
     * Resolves request body when given body is a reference to a global request body definition.
     * @param openApiDoc the OpenAPI document with global request body definitions.
     * @param requestBody the body maybe referencing a global request body definition.
     * @return the request body itself or a resolved global request body definition.
     */
    private static Oas30RequestBody resolveRequestBody(Oas30Document openApiDoc, Oas30RequestBody requestBody) {
        if (openApiDoc.components == null || openApiDoc.components.requestBodies == null) {
            return requestBody;
        }

        if (requestBody.$ref != null) {
            return openApiDoc.components.requestBodies.get(OasModelHelper.getReferenceName(requestBody.$ref));
        }

        return requestBody;
    }

    static List<Oas30Parameter> getOperationParameters(Oas30Document openApiDoc, Oas30Operation operation) {
        final List<Oas30Parameter> operationParameters = getParameters(openApiDoc, operation);

        OasPathItem parent = ofNullable(operation.parent())
            .filter(OasPathItem.class::isInstance)
            .map(OasPathItem.class::cast)
            .orElse(null);
        final List<Oas30Parameter> pathParameters = getParameters(openApiDoc, parent);
        operationParameters.addAll(pathParameters);

        return operationParameters
                    .stream()
                    .distinct()
                    .collect(Collectors.toList());
    }

    /**
     * Iterate through list of generic path parameters on the given operation and collect those of given type.
     * @param openApiDoc the OpenAPI document.
     * @param operation given path item.
     * @return typed list of path parameters.
     */
    private static List<Oas30Parameter> getParameters(Oas30Document openApiDoc, Oas30Operation operation) {
        List<Oas30Parameter> parameters = OasModelHelper.getParameters(operation)
            .stream()
            .filter(Oas30Parameter.class::isInstance)
            .map(Oas30Parameter.class::cast)
            .map(p -> resolveParameter(openApiDoc, p))
            .collect(Collectors.toList());

        if (Oas30FormDataHelper.hasFormDataBody(operation.requestBody)) {
            //add form urlencoded properties as we handle those as parameters
            Optional<Oas30MediaType> formDataContent = Oas30FormDataHelper.getFormDataContent(operation.requestBody.content);
            formDataContent.ifPresent(oas30MediaType -> ofNullable(oas30MediaType.schema.properties).orElse(Collections.emptyMap()).forEach((name, property) -> {
                Oas30Parameter formParameter = new Oas30Parameter(name);
                formParameter.schema = property;
                formParameter.in = "formData";
                formParameter.$ref = property.$ref;
                formParameter.description = property.description;
                parameters.add(formParameter);
            }));
        }

        return parameters;
    }

    /**
     * Iterate through list of generic path parameters on the given path item and collect those of given type.
     * @param openApiDoc the OpenAPI document.
     * @param pathItem given path item.
     * @return typed list of path parameters.
     */
    private static List<Oas30Parameter> getParameters(Oas30Document openApiDoc, OasPathItem pathItem) {
        return OasModelHelper.getParameters(pathItem)
            .stream()
            .filter(Oas30Parameter.class::isInstance)
            .map(Oas30Parameter.class::cast)
            .map(p -> resolveParameter(openApiDoc, p))
            .collect(Collectors.toList());
    }

    /**
     * Resolve parameter with potential reference to global parameter definition.
     * @param openApiDoc the OpenAPI document with global parameter definitions.
     * @param parameter the parameter maybe referencing a global parameter definition.
     * @return the parameter itself or a resolved global parameter definition.
     */
    private static Oas30Parameter resolveParameter(Oas30Document openApiDoc, Oas30Parameter parameter) {
        if (openApiDoc.components == null || openApiDoc.components.parameters == null) {
            return parameter;
        }

        if (parameter.$ref != null) {
            return openApiDoc.components.parameters.get(OasModelHelper.getReferenceName(parameter.$ref));
        }

        return parameter;
    }
}
