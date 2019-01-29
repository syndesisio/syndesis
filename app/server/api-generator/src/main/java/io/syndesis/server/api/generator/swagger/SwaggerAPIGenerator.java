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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

import com.google.common.base.Strings;
import io.swagger.models.HttpMethod;
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
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.api.generator.APIIntegration;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.ProvidedApiTemplate;
import io.syndesis.server.api.generator.swagger.util.SwaggerHelper;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwaggerAPIGenerator implements APIGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(SwaggerAPIGenerator.class);

    private static final String EXCERPT_METADATA_KEY = "excerpt";
    private static final String DEFAULT_RETURN_CODE_METADATA_KEY = "default-return-code";

    private final DataShapeGenerator dataShapeGenerator;

    public SwaggerAPIGenerator() {
        this.dataShapeGenerator = new UnifiedDataShapeGenerator();
    }

    @Override
    public APISummary info(String specification, APIValidationContext validation) {
        final SwaggerModelInfo swaggerInfo = SwaggerHelper.parse(specification, validation);
        try {
            // No matter if the validation fails, try to process the swagger
            final Map<String, Path> paths = swaggerInfo.getModel().getPaths();

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

            final ActionsSummary actionsSummary = new ActionsSummary.Builder()//
                .totalActions(total.intValue())//
                .actionCountByTags(tagCounts)//
                .build();

            return new APISummary.Builder()//
                .name(swaggerInfo.getModel().getInfo().getTitle())//
                .description(swaggerInfo.getModel().getInfo().getDescription())//
                .actionsSummary(actionsSummary)//
                .errors(swaggerInfo.getErrors())//
                .warnings(swaggerInfo.getWarnings())//
                .putConfiguredProperty("specification", swaggerInfo.getResolvedSpecification())// needed if the user wants to change it
                .build();
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") final Exception ex) {
            if (!swaggerInfo.getErrors().isEmpty()) {
                // Just log and return the validation errors if any
                LOG.error("An error occurred while trying to validate a API", ex);
                return new APISummary.Builder().errors(swaggerInfo.getErrors()).warnings(swaggerInfo.getWarnings()).build();
            }

            throw SyndesisServerException.launderThrowable("An error occurred while trying to validate a API", ex);
        }
    }

    @Override
    @SuppressWarnings({"PMD.ExcessiveMethodLength"})
    public APIIntegration generateIntegration(String specification, ProvidedApiTemplate template) {
        SwaggerModelInfo info = SwaggerHelper.parse(specification, APIValidationContext.NONE);
        Swagger swagger = info.getModel();

        String name = ofNullable(swagger.getInfo())
            .flatMap(i -> ofNullable(i.getTitle()))
            .orElse(null);

        String integrationId = KeyGenerator.createKey();

        Integration.Builder integration = new Integration.Builder()
            .id(integrationId)
            .addTag("api-provider")
            .createdAt(System.currentTimeMillis())
            .name(name);

        Set<String> alreadyUsedOperationIds = new HashSet<>();
        Map<String, Path> paths = swagger.getPaths();

        for (Map.Entry<String, Path> pathEntry : paths.entrySet()) {
            Path path = pathEntry.getValue();

            for (Map.Entry<HttpMethod, Operation> operationEntry : path.getOperationMap().entrySet()) {
                Operation operation = operationEntry.getValue();

                String operationName = operation.getSummary();
                String operationDescription = operationEntry.getKey() + " " + pathEntry.getKey();

                String operationId = requireUniqueOperationId(operation.getOperationId(), alreadyUsedOperationIds);
                alreadyUsedOperationIds.add(operationId);
                operation.setOperationId(operationId); // Update swagger spec

                DataShape startDataShape = dataShapeGenerator.createShapeFromRequest(info.getResolvedJsonGraph(), swagger, operation);
                Action startAction = template.getStartAction().orElseThrow(() -> new IllegalStateException("cannot find start action"));
                Action modifiedStartAction = new ConnectorAction.Builder()
                    .createFrom(startAction)
                    .addTag("locked-action")
                    .descriptor(new ConnectorDescriptor.Builder()
                        .createFrom(startAction.getDescriptor())
                        .outputDataShape(startDataShape)
                        .build())
                    .build();
                Step startStep = new Step.Builder()
                    .id(KeyGenerator.createKey())
                    .action(modifiedStartAction)
                    .connection(template.getConnection())
                    .stepKind(StepKind.endpoint)
                    .putConfiguredProperty("name", operationId)
                    .putMetadata("configured", "true")
                    .build();

                DataShape endDataShape = dataShapeGenerator.createShapeFromResponse(info.getResolvedJsonGraph(), swagger, operation);
                Action endAction = template.getEndAction().orElseThrow(() -> new IllegalStateException("cannot find end action"));
                Action modifiedEndAction = new ConnectorAction.Builder()
                    .createFrom(endAction)
                    .addTag("locked-action")
                    .descriptor(new ConnectorDescriptor.Builder()
                        .createFrom(endAction.getDescriptor())
                        .inputDataShape(endDataShape)
                        .addConnectorCustomizer("io.syndesis.connector.rest.swagger.ResponseCustomizer")
                        .build())
                    .build();
                Step endStep = new Step.Builder()
                    .id(KeyGenerator.createKey())
                    .action(modifiedEndAction)
                    .connection(template.getConnection())
                    .stepKind(StepKind.endpoint)
                    .putConfiguredProperty("httpResponseCode", "501")
                    .putMetadata("configured", "true")
                    .build();

                if (Strings.isNullOrEmpty(operationName)) {
                    operationName = SwaggerHelper.operationDescriptionOf(
                        swagger,
                        operation,
                        (m, p) -> "Receiving " + m + " request on " + p
                    ).description;
                }

                String defaultCode = "200";
                Optional<Pair<String, Response>> defaultResponse = BaseDataShapeGenerator.findResponseCodeValue(operation);
                if (defaultResponse.isPresent() && NumberUtils.isDigits(defaultResponse.get().getKey())) {
                    defaultCode = defaultResponse.get().getKey();
                }


                Flow flow = new Flow.Builder()
                    .id(String.format("%s:flows:%s", integrationId, operationId))
                    .putMetadata(EXCERPT_METADATA_KEY, "501 Not Implemented")
                    .putMetadata(DEFAULT_RETURN_CODE_METADATA_KEY, defaultCode)
                    .addStep(startStep)
                    .addStep(endStep)
                    .name(operationName)
                    .description(operationDescription)
                    .build();

                integration = integration.addFlow(flow);
            }

        }

        // TODO: evaluate what can be shrinked (e.g. SwaggerHelper#minimalSwaggerUsedByComponent)
        byte[] updatedSwagger = Json.toString(swagger).getBytes(StandardCharsets.UTF_8);

        // same check SwaggerParser is performing
        final String specificationContentType;
        if (specification.trim().startsWith("{")) {
            // means it's JSON (kinda)
            specificationContentType = "application/vnd.oai.openapi+json";
        } else {
            // YAML
            specificationContentType = "application/vnd.oai.openapi";
        }

        String apiId = KeyGenerator.createKey();
        OpenApi api = new OpenApi.Builder()
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
    public Integration updateFlowExcerpts(Integration integration) {
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

    protected String requireUniqueOperationId(String preferredOperationId, Set<String> alreadyUsedOperationIds) {
        String baseId = preferredOperationId;
        if (baseId == null) {
            baseId = "operation";
        }
        // Sanitize for using it in direct
        baseId = baseId.replaceAll("[^A-Za-z0-9-_]", "");

        int counter = 0;
        String newId = baseId;
        while (alreadyUsedOperationIds.contains(newId)) {
            newId = baseId + "-" + (++counter);
        }
        return newId;
    }

    protected Flow flowWithExcerpts(Flow flow) {
        List<Step> steps = flow.getSteps();
        if (steps == null || steps.isEmpty()) {
            return flow;
        }

        Step last = steps.get(steps.size() - 1);
        if (last.getConfiguredProperties().containsKey("httpResponseCode")) {
            String responseCode = last.getConfiguredProperties().get("httpResponseCode");
            String responseDesc = decodeHttpReturnCode(steps, responseCode);
            return new Flow.Builder()
                .createFrom(flow)
                .putMetadata(EXCERPT_METADATA_KEY, responseDesc)
                .build();
        } else if (flow.getMetadata(EXCERPT_METADATA_KEY).isPresent()) {
            Map<String, String> newMetadata = new HashMap<>(flow.getMetadata());
            newMetadata.remove(EXCERPT_METADATA_KEY);
            return new Flow.Builder()
                .createFrom(flow)
                .metadata(newMetadata)
                .build();
        }
        return flow;
    }

    protected String decodeHttpReturnCode(List<Step> steps, String code) {
        if (code == null || steps.isEmpty()) {
            return code;
        }
        Step lastStep = steps.get(steps.size() - 1);
        Optional<Action> lastAction = lastStep.getAction();
        if (!lastAction.isPresent()) {
            return code;
        }
        Optional<String> httpCodeDescription = lastAction
            .flatMap(a -> ofNullable(a.getProperties().get("httpResponseCode")))
            .flatMap(prop -> prop.getEnum().stream()
                .filter(e -> code.equals(e.getValue()))
                .map(ConfigurationProperty.PropertyValue::getLabel)
                .findFirst()
            );
        return httpCodeDescription.orElse(code);
    }

}
