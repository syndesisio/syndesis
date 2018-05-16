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

import java.util.Optional;

import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import org.apache.camel.Expression;
import org.apache.camel.builder.Builder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.aggregate.UseOriginalAggregationStrategy;
import org.apache.camel.spi.Language;
import org.apache.camel.util.ObjectHelper;

public class SplitStepHandler implements IntegrationStepHandler {
    @Override
    public boolean canHandle(Step step) {
        return StepKind.split == step.getStepKind();
    }

    @SuppressWarnings({"PMD.AvoidReassigningParameters", "PMD.AvoidDeeplyNestedIfStmts"})
    @Override
    public Optional<ProcessorDefinition<?>> handle(Step step, ProcessorDefinition<?> route, IntegrationRouteBuilder builder, String stepIndex) {
        ObjectHelper.notNull(route, "route");

        String languageName = step.getConfiguredProperties().get("language");
        String expressionDefinition = step.getConfiguredProperties().get("expression");

        if (ObjectHelper.isEmpty(languageName) && ObjectHelper.isEmpty(expressionDefinition)) {
            route = route.split(Builder.body());
        } else if (ObjectHelper.isNotEmpty(expressionDefinition)) {

            if (ObjectHelper.isEmpty(languageName)) {
                languageName = "simple";
            }

            // A small hack until https://issues.apache.org/jira/browse/CAMEL-12079
            // gets fixed so we can support the 'bean::method' annotation as done by
            // Function step definition
            if ("bean".equals(languageName) && expressionDefinition.contains("::")) {
                expressionDefinition = expressionDefinition.replace("::", "?method=");
            }

            final Language language = builder.getContext().resolveLanguage(languageName);
            final Expression expression = language.createExpression(expressionDefinition);

            route = route.split(expression).aggregationStrategy(new UseOriginalAggregationStrategy(null, false));
        }

        return Optional.of(route);
    }
}
