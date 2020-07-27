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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Strings;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasPaths;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.FlowMetadata;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.ProvidedApiTemplate;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Generates integration flows from given Open API specification where the type parameter defines the Open API document implementation
 * representing Open API version 2.x or version 3.x
 */
public abstract class OpenApiFlowGenerator<T extends OasDocument, O extends OasOperation> {

    private static final String DEFAULT_RETURN_CODE_METADATA_KEY = "default-return-code";
    private static final String ERROR_RESPONSE_CODES_PROPERTY = "errorResponseCodes";
    private static final String ERROR_RESPONSE_BODY = "returnBody";

    private static final String HTTP_RESPONSE_CODE_PROPERTY = "httpResponseCode";

    private final DataShapeGenerator<T, O> dataShapeGenerator;

    protected OpenApiFlowGenerator(DataShapeGenerator<T, O> dataShapeGenerator) {
        this.dataShapeGenerator = dataShapeGenerator;
    }

    /**
     * Generate integration flows from given Open API document.
     * @param openApiDoc the open api document.
     * @param integration the integration builder
     * @param info the open api model info.
     * @param template the provided api template.
     */
    public void generateFlows(T openApiDoc, Integration.Builder integration,
                       OpenApiModelInfo info, ProvidedApiTemplate template) {
        final Set<String> alreadyUsedOperationIds = new HashSet<>();
        final OasPaths paths = Optional.ofNullable(openApiDoc.paths)
            .orElse(openApiDoc.createPaths());

        for (final OasPathItem pathEntry : OasModelHelper.getPathItems(paths)) {
            for (final Map.Entry<String, O> operationEntry : getOperationsMap(pathEntry).entrySet()) {
                final O operation = operationEntry.getValue();

                final String operationDescription = operationEntry.getKey().toUpperCase(Locale.US) + " " + pathEntry.getPath();

                final String operationId = requireUniqueOperationId(operation.operationId, alreadyUsedOperationIds);
                alreadyUsedOperationIds.add(operationId);
                operation.operationId = operationId; // Update open api spec

                final DataShape startDataShape = dataShapeGenerator.createShapeFromRequest(info.getResolvedJsonGraph(), openApiDoc, operation);
                final Action startAction = template.getStartAction().orElseThrow(() -> new IllegalStateException("cannot find start action"));
                final Step startStep = createStartStep(operationId, getBasePath(openApiDoc), startAction, startDataShape, template.getConnection());

                final DataShape endDataShape = dataShapeGenerator.createShapeFromResponse(info.getResolvedJsonGraph(), openApiDoc, operation);
                final Action endAction = template.getEndAction().orElseThrow(() -> new IllegalStateException("cannot find end action"));
                final Step endStep = createEndStep(operation, endAction, endDataShape, template.getConnection());

                final String flowId = KeyGenerator.createKey();

                final Flow flow = new Flow.Builder()
                    .id(flowId)
                    .type(Flow.FlowType.API_PROVIDER)
                    .putMetadata(OpenApi.OPERATION_ID, operationId)
                    .putMetadata(FlowMetadata.EXCERPT, "501 Not Implemented")
                    .putMetadata(DEFAULT_RETURN_CODE_METADATA_KEY, getDefaultCode(operation))
                    .addStep(startStep)
                    .addStep(endStep)
                    .name(getOperationName(openApiDoc, operation))
                    .description(operationDescription)
                    .build();

                integration.addFlow(flow);
            }
        }
    }

    protected abstract String getBasePath(T openApiDoc);

    protected abstract Map<String, O> getOperationsMap(OasPathItem pathItem);

    private static String requireUniqueOperationId(final String preferredOperationId, final Set<String> alreadyUsedOperationIds) {
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

    private static String extendedPropertiesMapSet(final OasOperation operation) {
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

    private static String getResponseCode(final OasOperation operation) {
        return findResponseCode(operation).map(Pair::getKey).orElse("200");
    }

    private static List<ConfigurationProperty.PropertyValue> httpStatusList(final OasOperation operation) {
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
    private static String getMessage(final OasResponse response) {
        if (response.description != null && !response.description.isEmpty()) {
            return response.getStatusCode() + " " + response.description;
        } else {
            return HttpStatus.message(Integer.valueOf(response.getStatusCode()));
        }
    }

    private static Optional<Pair<String, OasResponse>> findResponseCode(final OasOperation operation) {
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

    private static String getDefaultCode(OasOperation operation) {
        String defaultCode = "200";
        final Optional<Pair<String, OasResponse>> defaultResponse = findResponseCode(operation);
        if (defaultResponse.isPresent() && NumberUtils.isDigits(defaultResponse.get().getKey())) {
            defaultCode = defaultResponse.get().getKey();
        }

        return defaultCode;
    }

    private static String getOperationName(OasDocument openApiDoc, OasOperation operation) {
        String operationName = operation.summary;
        if (Strings.isNullOrEmpty(operationName)) {
            operationName = OasModelHelper.operationDescriptionOf(openApiDoc, operation,
                (m, p) -> "Receiving " + m + " request on " + p).description;
        }

        return operationName;
    }

    private static Step createStartStep(String operationId, String basePath, Action startAction, DataShape startDataShape, Connection connection) {
        final ConnectorAction.Builder modifiedStartActionBuilder = new ConnectorAction.Builder()
            .createFrom(startAction)
            .addTag("locked-action")
            .descriptor(new ConnectorDescriptor.Builder()
                .createFrom(startAction.getDescriptor())
                .outputDataShape(startDataShape)
                .build());

        if (!Strings.isNullOrEmpty(basePath)) {
            // pass the basePath so it gets picked up by
            // EndpointController
            modifiedStartActionBuilder.putMetadata("serverBasePath", basePath);
        }

        final Action modifiedStartAction = modifiedStartActionBuilder.build();

        return new Step.Builder()
            .id(KeyGenerator.createKey())
            .action(modifiedStartAction)
            .connection(connection)
            .stepKind(StepKind.endpoint)
            .putConfiguredProperty("name", operationId)
            .putMetadata("configured", "true")
            .build();
    }

    private static Step createEndStep(OasOperation operation, Action endAction, DataShape endDataShape, Connection connection) {
        final Action modifiedEndAction = new ConnectorAction.Builder()
            .createFrom(endAction)
            .addTag("locked-action")
            .descriptor(new ConnectorDescriptor.Builder()
                .createFrom(endAction.getDescriptor())
                .inputDataShape(endDataShape)
                .replaceConfigurationProperty(ERROR_RESPONSE_CODES_PROPERTY,
                    builder -> builder.extendedProperties(extendedPropertiesMapSet(operation)))
                .replaceConfigurationProperty(HTTP_RESPONSE_CODE_PROPERTY,
                    builder -> builder.addAllEnum(httpStatusList(operation)))
                .build())
            .build();

        return new Step.Builder()
            .id(KeyGenerator.createKey())
            .action(modifiedEndAction)
            .connection(connection)
            .stepKind(StepKind.endpoint)
            .putConfiguredProperty(HTTP_RESPONSE_CODE_PROPERTY, getResponseCode(operation))
            .putConfiguredProperty(ERROR_RESPONSE_BODY, "true")
            .putConfiguredProperty(ERROR_RESPONSE_CODES_PROPERTY, "{}")
            .putMetadata("configured", "true")
            .build();
    }
}
