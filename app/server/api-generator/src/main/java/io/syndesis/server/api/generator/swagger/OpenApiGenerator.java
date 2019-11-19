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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.core.models.common.Info;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasPaths;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.models.OasResponses;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20PathItem;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ResourceIdentifier;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ActionsSummary;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.api.APISummary;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.api.generator.APIIntegration;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.ProvidedApiTemplate;
import io.syndesis.server.api.generator.swagger.util.Oas20ModelHelper;
import io.syndesis.server.api.generator.swagger.util.Oas20ModelParser;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

public class OpenApiGenerator implements APIGenerator {

    private static final String DEFAULT_RETURN_CODE_METADATA_KEY = "default-return-code";
    private static final String EXCERPT_METADATA_KEY = "excerpt";
    private static final String HTTP_RESPONSE_CODE_PROPERTY = "httpResponseCode";
    private static final String ERROR_RESPONSE_CODES_PROPERTY = "errorResponseCodes";
    private static final String ERROR_RESPONSE_BODY = "returnBody";

    private final DataShapeGenerator dataShapeGenerator;

    public OpenApiGenerator() {
        dataShapeGenerator = new UnifiedDataShapeGenerator();
    }

    @Override
    @SuppressWarnings({"PMD.ExcessiveMethodLength"})
    public APIIntegration generateIntegration(final String specification, final ProvidedApiTemplate template) {

        final OpenApiModelInfo info = Oas20ModelParser.parse(specification, APIValidationContext.NONE);
        final Oas20Document openApiDoc = info.getModel();

        final String name = Optional.ofNullable(openApiDoc.info)
                                .flatMap(i -> Optional.ofNullable(i.title))
                                .orElse("Untitled");

        final Integration.Builder integration = new Integration.Builder()
            .addTag("api-provider")
            .createdAt(System.currentTimeMillis())
            .name(name);

        final Set<String> alreadyUsedOperationIds = new HashSet<>();
        final OasPaths paths = Optional.ofNullable(openApiDoc.paths)
                                       .orElse(openApiDoc.createPaths());

        for (final Oas20PathItem pathEntry : Oas20ModelHelper.getPathItems(paths, Oas20PathItem.class)) {
            for (final Map.Entry<String, Oas20Operation> operationEntry : Oas20ModelHelper.getOperationMap(pathEntry).entrySet()) {
                final Oas20Operation operation = operationEntry.getValue();

                String operationName = operation.summary;
                final String operationDescription = operationEntry.getKey() + " " + pathEntry.getPath();

                final String operationId = requireUniqueOperationId(operation.operationId, alreadyUsedOperationIds);
                alreadyUsedOperationIds.add(operationId);
                operation.operationId = operationId; // Update open api spec

                final DataShape startDataShape = dataShapeGenerator.createShapeFromRequest(info.getResolvedJsonGraph(), openApiDoc, operation);
                final Action startAction = template.getStartAction().orElseThrow(() -> new IllegalStateException("cannot find start action"));
                final ConnectorAction.Builder modifiedStartActionBuilder = new ConnectorAction.Builder()
                    .createFrom(startAction)
                    .addTag("locked-action")
                    .descriptor(new ConnectorDescriptor.Builder()
                        .createFrom(startAction.getDescriptor())
                        .outputDataShape(startDataShape)
                        .build());

                final String basePath = openApiDoc.basePath;
                if (!Strings.isNullOrEmpty(basePath)) {
                    // pass the basePath so it gets picked up by
                    // EndpointController
                    modifiedStartActionBuilder.putMetadata("serverBasePath", basePath);
                }

                final Action modifiedStartAction = modifiedStartActionBuilder.build();

                final Step startStep = new Step.Builder()
                    .id(KeyGenerator.createKey())
                    .action(modifiedStartAction)
                    .connection(template.getConnection())
                    .stepKind(StepKind.endpoint)
                    .putConfiguredProperty("name", operationId)
                    .putMetadata("configured", "true")
                    .build();

                final DataShape endDataShape = dataShapeGenerator.createShapeFromResponse(info.getResolvedJsonGraph(), openApiDoc, operation);
                final Action endAction = template.getEndAction().orElseThrow(() -> new IllegalStateException("cannot find end action"));
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
                final Step endStep = new Step.Builder()
                    .id(KeyGenerator.createKey())
                    .action(modifiedEndAction)
                    .connection(template.getConnection())
                    .stepKind(StepKind.endpoint)
                    .putConfiguredProperty(HTTP_RESPONSE_CODE_PROPERTY, getResponseCode(operation))
                    .putConfiguredProperty(ERROR_RESPONSE_BODY, "false")
                    .putConfiguredProperty(ERROR_RESPONSE_CODES_PROPERTY, "{}")
                    .putMetadata("configured", "true")
                    .build();

                if (Strings.isNullOrEmpty(operationName)) {
                    operationName = Oas20ModelHelper.operationDescriptionOf(
                        openApiDoc,
                        operation,
                        (m, p) -> "Receiving " + m + " request on " + p).description;
                }

                String defaultCode = "200";
                final Optional<Pair<String, OasResponse>> defaultResponse = findResponseCode(operation);
                if (defaultResponse.isPresent() && NumberUtils.isDigits(defaultResponse.get().getKey())) {
                    defaultCode = defaultResponse.get().getKey();
                }

                final String flowId = KeyGenerator.createKey();

                final Flow flow = new Flow.Builder()
                    .id(flowId)
                    .type(Flow.FlowType.API_PROVIDER)
                    .putMetadata(OpenApi.OPERATION_ID, operationId)
                    .putMetadata(EXCERPT_METADATA_KEY, "501 Not Implemented")
                    .putMetadata(DEFAULT_RETURN_CODE_METADATA_KEY, defaultCode)
                    .addStep(startStep)
                    .addStep(endStep)
                    .name(operationName)
                    .description(operationDescription)
                    .build();

                integration.addFlow(flow);
            }

        }

        // TODO: evaluate what can be shrinked (e.g. Oas20Helper#minimalOpenApiUsedByComponent)
        final byte[] updatedSpecification = Library.writeDocumentToJSONString(openApiDoc).getBytes(StandardCharsets.UTF_8);

        // same check SwaggerParser is performing
        final String specificationContentType;
        if (specification.trim().startsWith("{")) {
            // means it's JSON (kinda)
            specificationContentType = "application/vnd.oai.openapi+json";
        } else {
            // YAML
            specificationContentType = "application/vnd.oai.openapi";
        }

        final String apiId = KeyGenerator.createKey();
        final OpenApi api = new OpenApi.Builder()
            .id(apiId)
            .name(name)
            .document(updatedSpecification)
            .putMetadata("Content-Type", specificationContentType)
            .build();

        integration.addResource(new ResourceIdentifier.Builder()
            .id(apiId)
            .kind(Kind.OpenApi)
            .build());

        return new APIIntegration(integration.build(), api);
    }

    @Override
    public APISummary info(final String specification, final APIValidationContext validation) {
        final OpenApiModelInfo swaggerInfo = Oas20ModelParser.parse(specification, validation);

        final Oas20Document model = swaggerInfo.getModel();
        if (model == null) {
            return new APISummary.Builder().errors(swaggerInfo.getErrors()).warnings(swaggerInfo.getWarnings()).build();
        }

        final OasPaths paths = model.paths;
        final ActionsSummary actionsSummary = determineSummaryFrom(Oas20ModelHelper.getPathItems(paths, Oas20PathItem.class));

        final Info info = model.info;
        final String title = Optional.ofNullable(info).map(i -> i.title).orElse("unspecified");
        final String description = Optional.ofNullable(info).map(i -> i.description).orElse("unspecified");

        return new APISummary.Builder()//
            .name(title)//
            .description(description)//
            .actionsSummary(actionsSummary)//
            .errors(swaggerInfo.getErrors())//
            .warnings(swaggerInfo.getWarnings())//
            // needed if the user wants to change it
            .putConfiguredProperty("specification", swaggerInfo.getResolvedSpecification())
            .build();
    }

    @Override
    public Integration updateFlowExcerpts(final Integration integration) {
        // Update excerpt for api provider endpoints only
        if (integration == null || !integration.getTags().contains("api-provider")) {
            return integration;
        }

        return new Integration.Builder()
            .createFrom(integration)
            .flows(integration.getFlows().stream()
                .map(this::flowWithExcerpts)
                .collect(Collectors.toList()))
            .build();
    }

    protected String decodeHttpReturnCode(final List<Step> steps, final String code) {
        if (code == null || steps.isEmpty()) {
            return code;
        }
        final Step lastStep = steps.get(steps.size() - 1);
        final Optional<Action> lastAction = lastStep.getAction();
        if (!lastAction.isPresent()) {
            return code;
        }
        final Optional<String> httpCodeDescription = lastAction
            .flatMap(a -> Optional.ofNullable(a.getProperties().get(HTTP_RESPONSE_CODE_PROPERTY)))
            .flatMap(prop -> prop.getEnum().stream()
                .filter(e -> code.equals(e.getValue()))
                .map(ConfigurationProperty.PropertyValue::getLabel)
                .findFirst());
        return httpCodeDescription.orElse(code);
    }

    protected Flow flowWithExcerpts(final Flow flow) {
        final List<Step> steps = flow.getSteps();
        if (steps == null || steps.isEmpty()) {
            return flow;
        }

        final Step last = steps.get(steps.size() - 1);
        if (last.getConfiguredProperties().containsKey(HTTP_RESPONSE_CODE_PROPERTY)) {
            final String responseCode = last.getConfiguredProperties().get(HTTP_RESPONSE_CODE_PROPERTY);
            final String responseDesc = decodeHttpReturnCode(steps, responseCode);
            return new Flow.Builder()
                .createFrom(flow)
                .putMetadata(EXCERPT_METADATA_KEY, responseDesc)
                .build();
        } else if (flow.getMetadata(EXCERPT_METADATA_KEY).isPresent()) {
            final Map<String, String> newMetadata = new HashMap<>(flow.getMetadata());
            newMetadata.remove(EXCERPT_METADATA_KEY);
            return new Flow.Builder()
                .createFrom(flow)
                .metadata(newMetadata)
                .build();
        }
        return flow;
    }

    protected String requireUniqueOperationId(final String preferredOperationId, final Set<String> alreadyUsedOperationIds) {
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

    static ActionsSummary determineSummaryFrom(final List<Oas20PathItem> paths) {
        if (paths == null || paths.isEmpty()) {
            return new ActionsSummary.Builder().build();
        }

        final AtomicInteger total = new AtomicInteger(0);

        final Map<String, Integer> tagCounts = paths.stream()//
            .flatMap(p -> Oas20ModelHelper.getOperationMap(p).values().stream())//
            .peek(o -> total.incrementAndGet())//
            .flatMap(o -> Oas20ModelHelper.sanitizeTags(o.tags))//
            .collect(//
                Collectors.groupingBy(//
                    Function.identity(), //
                    Collectors.reducing(0, (e) -> 1, Integer::sum)//
                ));

        return new ActionsSummary.Builder()//
            .totalActions(total.intValue())//
            .actionCountByTags(tagCounts)//
            .build();
    }

    static String extendedPropertiesMapSet(final OasOperation operation) {
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

    Optional<Pair<String, OasResponse>> findResponseCode(final OasOperation operation) {
        List<OasResponse> responses = Optional.ofNullable(operation.responses)
                                              .map(OasResponses::getResponses)
                                              .orElse(Collections.emptyList());

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

    String getResponseCode(final OasOperation operation) {
        return findResponseCode(operation).get().getKey();
    }

    static List<ConfigurationProperty.PropertyValue> httpStatusList(final OasOperation operation) {
        List<OasResponse> responses = Optional.ofNullable(operation.responses)
            .map(OasResponses::getResponses)
            .orElse(Collections.emptyList());

        List<ConfigurationProperty.PropertyValue> httpStatusList = new ArrayList<>();
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
}
