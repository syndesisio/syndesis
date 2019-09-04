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
package io.syndesis.integration.runtime.handlers;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import io.syndesis.common.model.choice.FlowOption;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.Json;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import io.syndesis.integration.runtime.logging.ActivityTracker;
import io.syndesis.integration.runtime.logging.IntegrationLoggingConstants;
import io.syndesis.integration.runtime.util.JsonSimplePredicate;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.util.ObjectHelper;

public class ChoiceStepHandler implements IntegrationStepHandler {

    @Override
    public boolean canHandle(Step step) {
        return StepKind.choice == step.getStepKind();
    }

    @SuppressWarnings({"PMD.AvoidReassigningParameters", "PMD.AvoidDeeplyNestedIfStmts"})
    @Override
    public Optional<ProcessorDefinition<?>> handle(Step step, ProcessorDefinition<?> route, IntegrationRouteBuilder builder, String flowIndex, String stepIndex) {
        ObjectHelper.notNull(route, "route");

        String routingScheme = step.getConfiguredProperties().getOrDefault("routingScheme", "direct");
        String defaultFlow = step.getConfiguredProperties().get("default");
        List<FlowOption> flows = extractFlows(step.getConfiguredProperties().get("flows"));

        if (!flows.isEmpty()) {
            ChoiceDefinition choice = route.choice();

            for (FlowOption flowOption : flows) {
                choice.when(getPredicate(flowOption.getConditionExpression(), builder.getContext()))
                        .description(flowOption.getConditionExpression())
                        .process(new EnrichActivityIdHeader())
                        .to(getEndpointUri(routingScheme, flowOption.getFlow()))
                        .end();
            }

            if (ObjectHelper.isNotEmpty(defaultFlow)) {
                choice.otherwise()
                        .process(new EnrichActivityIdHeader())
                        .to(getEndpointUri(routingScheme, defaultFlow))
                        .end();
            }

            route = choice.end();
        }

        return Optional.of(route);
    }

    private Predicate getPredicate(String conditionExpression, CamelContext context) {
        return new JsonSimplePredicate(conditionExpression, context);
    }

    // *******************************
    // Helpers
    // *******************************

    /**
     * Construct flow endpoint uri using given scheme and flow identifier. By default constructed uri is
     * using "scheme:flowId" pattern.
     *
     * @param routingScheme
     * @param flowId
     * @return
     */
    private String getEndpointUri(String routingScheme, String flowId) {
        return routingScheme + ":" + flowId;
    }

    private List<FlowOption> extractFlows(String flowMappings) {
        try {
            if (flowMappings == null || flowMappings.isEmpty()) {
                return Collections.emptyList();
            }

            return Json.reader().forType(new TypeReference<List<FlowOption>>(){}).readValue(flowMappings);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Failed to read flow mappings %s: %s", flowMappings, e.getMessage()),e);
        }
    }

    private static class EnrichActivityIdHeader implements Processor {
        @Override
        public void process(Exchange exchange) {
            String activityId = ActivityTracker.getActivityId(exchange);
            if (ObjectHelper.isNotEmpty(activityId)) {
                exchange.getIn().setHeader(IntegrationLoggingConstants.ACTIVITY_ID, activityId);
            }
        }
    }
}
