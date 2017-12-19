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
package io.syndesis.integration.runtime.stephandlers;

import java.util.Objects;
import java.util.Optional;

import com.google.auto.service.AutoService;
import io.syndesis.integration.model.steps.SplitInline;
import io.syndesis.integration.model.steps.Step;
import io.syndesis.integration.runtime.StepHandler;
import io.syndesis.integration.runtime.SyndesisRouteBuilder;
import org.apache.camel.CamelContext;
import org.apache.camel.Expression;
import org.apache.camel.builder.Builder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.spi.Language;
import org.apache.camel.util.ObjectHelper;

@AutoService(StepHandler.class)
public class SplitInlineHandler implements StepHandler<SplitInline> {
    @Override
    public boolean canHandle(Step step) {
        return step.getClass().equals(SplitInline.class);
    }

    @Override
    public Optional<ProcessorDefinition> handle(SplitInline step, ProcessorDefinition route, SyndesisRouteBuilder routeBuilder) {
        String languageName = step.getLanguage();
        String expressionDefinition = step.getExpression();

        if (ObjectHelper.isEmpty(languageName) && ObjectHelper.isEmpty(expressionDefinition)) {
            return Optional.of(route.split(Builder.body()));
        } else {
            Objects.requireNonNull(languageName, "Expression language should not be null");
            Objects.requireNonNull(expressionDefinition, "Expression should not be null");

            // A small hack until https://issues.apache.org/jira/browse/CAMEL-12079
            // gets fixed so we can support the 'bean::method' annotation as done by
            // Function step definition
            if ("bean".equals(languageName)) {
                if (expressionDefinition.contains("::")) {
                    expressionDefinition = expressionDefinition.replace("::", "?method=");
                }
            }

            final CamelContext context = routeBuilder.getContext();
            final Language language = context.resolveLanguage(languageName);
            final Expression expression = language.createExpression(expressionDefinition);
            final ProcessorDefinition split = route.split(expression);

            return Optional.of(split);
        }
    }
}
