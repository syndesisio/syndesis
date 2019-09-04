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
import io.swagger.models.HttpMethod;
import io.swagger.models.Info;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
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
import io.syndesis.common.util.Json;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.common.util.openapi.OpenApiHelper;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.api.generator.APIIntegration;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.ProvidedApiTemplate;
import io.syndesis.server.api.generator.swagger.util.SwaggerHelper;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

import static java.util.Optional.ofNullable;

public class SwaggerAPIGenerator implements APIGenerator {

    private static final String DEFAULT_RETURN_CODE_METADATA_KEY = "default-return-code";
    private static final String EXCERPT_METADATA_KEY = "excerpt";
    private static final String HTTP_RESPONSE_CODE_PROPERTY = "httpResponseCode";
    private static final String ERROR_RESPONSE_CODES_PROPERTY = "errorResponseCodes";
    private static final String ERROR_RESPONSE_BODY = "returnBody";

    private final DataShapeGenerator dataShapeGenerator;

    public SwaggerAPIGenerator() {
        dataShapeGenerator = new UnifiedDataShapeGenerator();
    }

    @Override
    @SuppressWarnings({"PMD.ExcessiveMethodLength"})
    public APIIntegration generateIntegration(final String specification, final ProvidedApiTemplate template) {

        final SwaggerModelInfo info = SwaggerHelper.parse(specification, APIValidationContext.NONE);
        final Swagger swagger = info.getModel();

        final String name = ofNullable(swagger.getInfo())
            .flatMap(i -> ofNullable(i.getTitle()))
            .orElse(null);

        final Integration.Builder integration = new Integration.Builder()
            .addTag("api-provider")
            .createdAt(System.currentTimeMillis())
            .name(name);

        final Set<String> alreadyUsedOperationIds = new HashSet<>();
        final Map<String, Path> paths = swagger.getPaths();

        for (final Map.Entry<String, Path> pathEntry : paths.entrySet()) {
            final Path path = pathEntry.getValue();

            for (final Map.Entry<HttpMethod, Operation> operationEntry : path.getOperationMap().entrySet()) {
                final Operation operation = operationEntry.getValue();

                String operationName = operation.getSummary();
                final String operationDescription = operationEntry.getKey() + " " + pathEntry.getKey();

                final String operationId = requireUniqueOperationId(operation.getOperationId(), alreadyUsedOperationIds);
                alreadyUsedOperationIds.add(operationId);
                operation.setOperationId(operationId); // Update swagger spec

                final DataShape startDataShape = dataShapeGenerator.createShapeFromRequest(info.getResolvedJsonGraph(), swagger, operation);
                final Action startAction = template.getStartAction().orElseThrow(() -> new IllegalStateException("cannot find start action"));
                final ConnectorAction.Builder modifiedStartActionBuilder = new ConnectorAction.Builder()
                    .createFrom(startAction)
                    .addTag("locked-action")
                    .descriptor(new ConnectorDescriptor.Builder()
                        .createFrom(startAction.getDescriptor())
                        .outputDataShape(startDataShape)
                        .build());

                final String basePath = swagger.getBasePath();
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

                final DataShape endDataShape = dataShapeGenerator.createShapeFromResponse(info.getResolvedJsonGraph(), swagger, operation);
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
                    operationName = SwaggerHelper.operationDescriptionOf(
                        swagger,
                        operation,
                        (m, p) -> "Receiving " + m + " request on " + p).description;
                }

                String defaultCode = "200";
                final Optional<Pair<String, Response>> defaultResponse = findResponseCode(operation);
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

        // TODO: evaluate what can be shrinked (e.g.
        // SwaggerHelper#minimalSwaggerUsedByComponent)
        final byte[] updatedSwagger = OpenApiHelper.serialize(swagger).getBytes(StandardCharsets.UTF_8);

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
            .document(updatedSwagger)
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
        final SwaggerModelInfo swaggerInfo = SwaggerHelper.parse(specification, validation);

        final Swagger model = swaggerInfo.getModel();
        if (model == null) {
            return new APISummary.Builder().errors(swaggerInfo.getErrors()).warnings(swaggerInfo.getWarnings()).build();
        }

        final Map<String, Path> paths = model.getPaths();

        final ActionsSummary actionsSummary = determineSummaryFrom(paths);

        final Info info = model.getInfo();
        final String title = ofNullable(info).map(Info::getTitle).orElse("unspecified");
        final String description = ofNullable(info).map(Info::getDescription).orElse("unspecified");

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
            .flatMap(a -> ofNullable(a.getProperties().get(HTTP_RESPONSE_CODE_PROPERTY)))
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

    static ActionsSummary determineSummaryFrom(final Map<String, Path> paths) {
        if (paths == null || paths.isEmpty()) {
            return new ActionsSummary.Builder().build();
        }

        final AtomicInteger total = new AtomicInteger(0);

        final Map<String, Integer> tagCounts = paths.entrySet().stream()//
            .flatMap(p -> p.getValue().getOperations().stream())//
            .peek(o -> total.incrementAndGet())//
            .flatMap(o -> SwaggerHelper.sanitizeTags(o.getTags()))//
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

    static String extendedPropertiesMapSet(final Operation operation) {
        List<ConfigurationProperty.PropertyValue> statusList = httpStatusList(operation);
        String enumJson = "[]";
        if (! statusList.isEmpty()) {
            enumJson = Json.toString(statusList);
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

    Optional<Pair<String, Response>> findResponseCode(final Operation operation) {
        // Return the Response object related to the first 2xx return code found
        Optional<Pair<String, Response>> responseOk = operation.getResponses().entrySet().stream()
            .map(e -> Pair.of(e.getKey(), e.getValue()))
            .filter(p -> p.getKey().startsWith("2"))
            .findFirst();

        if (responseOk.isPresent()) {
            return responseOk;
        }

        return operation.getResponses().entrySet().stream()
            .map(e -> Pair.of(e.getKey(), e.getValue()))
            .findFirst();
    }

    String getResponseCode(final Operation operation) {
        return findResponseCode(operation).get().getKey();
    }

    static List<ConfigurationProperty.PropertyValue> httpStatusList(final Operation operation) {
        List<ConfigurationProperty.PropertyValue> httpStatusList = new ArrayList<>();
        operation.getResponses()
        .keySet()
        .stream()
        .filter(NumberUtils::isDigits)
        .forEach(statusCode -> httpStatusList.add(
                ConfigurationProperty.PropertyValue.Builder.of(statusCode, getMessage(operation, statusCode))));
        return httpStatusList;
    }

    /**
     * Obtains the description for this statusCode set in the Swagger API, or defaults
     * to the HttpStatus description if not set.
     * @param operation Swagger fragment
     * @param statusCode for which we want the message
     * @return HttpStatus message
     */
    private static String getMessage(final Operation operation, String statusCode) {
        Response response = operation.getResponses().get(statusCode);
        if (response!=null && response.getDescription()!=null && !response.getDescription().isEmpty()) {
            return statusCode + " " + response.getDescription();
        } else {
            return HttpStatus.message(Integer.valueOf(statusCode));
        }
    }
}
