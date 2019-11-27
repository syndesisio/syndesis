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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.ProvidedApiTemplate;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Generates integration flows from given Open API specification where the type parameter defines the Open API document implementation
 * representing Open API version 2.x or version 3.x
 */
public interface OpenApiFlowGenerator<T extends OasDocument> {

    String DEFAULT_RETURN_CODE_METADATA_KEY = "default-return-code";
    String ERROR_RESPONSE_CODES_PROPERTY = "errorResponseCodes";
    String ERROR_RESPONSE_BODY = "returnBody";

    String HTTP_RESPONSE_CODE_PROPERTY = "httpResponseCode";

    /**
     * Generate integration flows from given Open API document.
     * @param openApiDoc the open api document.
     * @param integration the integration builder
     * @param info the open api model info.
     * @param template the provided api template.
     */
    void generateFlows(T openApiDoc, Integration.Builder integration,
                       OpenApiModelInfo info, ProvidedApiTemplate template);

    default String requireUniqueOperationId(final String preferredOperationId, final Set<String> alreadyUsedOperationIds) {
        String baseId = preferredOperationId;
        if (baseId == null) {
            baseId = UUID.randomUUID().toString();
        }
        // Sanitize for using it in direct
        baseId = baseId.replaceAll("[^A-Za-z0-9-_]", "");

        int counter = 0;
        String newId = baseId;
        while (alreadyUsedOperationIds.contains(newId)) {
            newId = baseId + "-" + ++counter;
        }
        return newId;
    }

    default String extendedPropertiesMapSet(final OasOperation operation) {
        List<ConfigurationProperty.PropertyValue> statusList = httpStatusList(operation);
        String enumJson = "[]";
        if (! statusList.isEmpty()) {
            enumJson = JsonUtils.toString(statusList);
        }
        String mapsetJsonTemplate =
            "{ "
                + "\"mapsetValueDefinition\": {"
                + "    \"enum\" : @enum@,"
                + "    \"type\" : \"select\" },"
                + "\"mapsetOptions\": {"
                + "    \"i18nKeyColumnTitle\": \"When the error message is\","
                + "    \"i18nValueColumnTitle\": \"Return this HTTP response code\" }"
                + "}";
        return mapsetJsonTemplate.replace("@enum@", enumJson);
    }

    default String getResponseCode(final OasOperation operation) {
        return findResponseCode(operation).get().getKey();
    }

    default List<ConfigurationProperty.PropertyValue> httpStatusList(final OasOperation operation) {
        List<ConfigurationProperty.PropertyValue> httpStatusList = new ArrayList<>();

        if (operation.responses == null) {
            return httpStatusList;
        }

        List<OasResponse> responses = operation.responses.getResponses();
        responses.stream()
            .filter(r -> NumberUtils.isDigits(r.getStatusCode()))
            .forEach(r -> httpStatusList.add(
                ConfigurationProperty.PropertyValue.Builder.of(r.getStatusCode(), getMessage(r))));
        return httpStatusList;
    }

    /**
     * Obtains the description for this statusCode set in the Swagger API, or defaults
     * to the HttpStatus description if not set.
     * @param response the operation response
     * @return HttpStatus message
     */
    default String getMessage(final OasResponse response) {
        if (response.description != null && !response.description.isEmpty()) {
            return response.getStatusCode() + " " + response.description;
        } else {
            return HttpStatus.message(Integer.valueOf(response.getStatusCode()));
        }
    }

    default Optional<Pair<String, OasResponse>> findResponseCode(final OasOperation operation) {
        if (operation.responses == null) {
            return Optional.empty();
        }

        List<OasResponse> responses = operation.responses.getResponses();

        // Return the Response object related to the first 2xx return code found
        Optional<Pair<String, OasResponse>> responseOk = responses.stream()
            .filter(response -> response.getStatusCode() != null && response.getStatusCode().startsWith("2"))
            .map(response -> Pair.of(response.getStatusCode(), response))
            .findFirst();

        if (responseOk.isPresent()) {
            return responseOk;
        }

        return responses.stream()
            .map(response -> Pair.of(response.getStatusCode(), response))
            .findFirst();
    }
}
