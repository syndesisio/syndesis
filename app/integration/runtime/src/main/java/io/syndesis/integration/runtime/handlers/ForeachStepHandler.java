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

import javax.annotation.concurrent.Immutable;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.builder.Builder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;
import org.apache.camel.processor.aggregate.UseLatestAggregationStrategy;
import org.apache.camel.processor.aggregate.UseOriginalAggregationStrategy;
import org.apache.camel.spi.Language;
import org.apache.camel.util.ObjectHelper;

public class ForeachStepHandler implements IntegrationStepHandler {
    @Override
    public boolean canHandle(Step step) {
        return StepKind.foreach == step.getStepKind() || StepKind.split == step.getStepKind();
    }

    @SuppressWarnings({"PMD.AvoidReassigningParameters", "PMD.AvoidDeeplyNestedIfStmts"})
    @Override
    public Optional<ProcessorDefinition<?>> handle(Step step, ProcessorDefinition<?> route, IntegrationRouteBuilder builder, String flowIndex, String stepIndex) {
        ObjectHelper.notNull(route, "route");

        String languageName = step.getConfiguredProperties().get("language");
        String expressionDefinition = step.getConfiguredProperties().get("expression");
        AggregationOption aggregation = Optional.ofNullable(step.getConfiguredProperties().get("aggregationStrategy"))
                                                        .map(AggregationOption::valueOf)
                                                        .orElse(StepKind.split == step.getStepKind() ? AggregationOption.original : AggregationOption.body);

        if (ObjectHelper.isEmpty(languageName) && ObjectHelper.isEmpty(expressionDefinition)) {
            route = route.split(Builder.body()).aggregationStrategy(aggregation.getStrategy(step.getConfiguredProperties()));
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

            route = route.split(expression).aggregationStrategy(aggregation.getStrategy(step.getConfiguredProperties()));
        }

        return Optional.of(route);
    }

    public static class EndHandler implements IntegrationStepHandler {
        @Override
        public boolean canHandle(Step step) {
            return StepKind.endForeach == step.getStepKind();
        }

        @Override
        public Optional<ProcessorDefinition<?>> handle(Step step, ProcessorDefinition<?> route, IntegrationRouteBuilder builder, String flowIndex, String stepIndex) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public enum AggregationOption {
        body(GroupedBodyAggregationStrategy::new),
        latest(UseLatestAggregationStrategy::new),
        original(() -> new UseOriginalAggregationStrategy(null, true)),
        script(ScriptAggregationStrategy::new, (strategy, stepProperties) -> {
            Optional.ofNullable(stepProperties.get("aggregationScriptLanguage")).ifPresent(strategy::setLanguage);
            Optional.ofNullable(stepProperties.get("aggregationScript")).ifPresent(strategy::setScript);
            return strategy;
        });

        private final StrategySupplier<AggregationStrategy> strategySupplier;
        private final StrategyConfigurer<AggregationStrategy> configurer;

        @Immutable
        interface StrategySupplier<T extends AggregationStrategy> extends Supplier<T> {}
        @Immutable
        interface StrategyConfigurer<T extends AggregationStrategy> extends BiFunction<T, Map<String, String>, T> {}

        AggregationOption(StrategySupplier<AggregationStrategy> strategySupplier) {
            this(strategySupplier, (strategy, stepProperties) -> strategy);
        }

        <T extends AggregationStrategy> AggregationOption(StrategySupplier<T> strategySupplier, StrategyConfigurer<T> configurer) {
            this.strategySupplier = (StrategySupplier<AggregationStrategy>) strategySupplier;
            this.configurer = (StrategyConfigurer<AggregationStrategy>) configurer;
        }

        public AggregationStrategy getStrategy(Map<String, String> stepProperties) {
            return configurer.apply(strategySupplier.get(), stepProperties);
        }
    }

    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    private static class ScriptAggregationStrategy implements AggregationStrategy {
        private String language;
        private String script;
        private ScriptEngine engine;

        ScriptAggregationStrategy() {
            this("nashorn", "oldExchange;");
        }

        ScriptAggregationStrategy(String language, String script) {
            this.language = language;
            this.script = script;
            this.engine = new ScriptEngineManager().getEngineByName(this.language);
        }

        @Override
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            Bindings bindings = new SimpleBindings();
            bindings.put("oldExchange", oldExchange);
            bindings.put("newExchange", newExchange);

            try {
                return (Exchange) engine.eval(script, bindings);
            } catch (ScriptException e) {
                throw new RuntimeException("Script aggregation strategy failed", e);
            }
        }

        /**
         * Specifies the script.
         *
         * @param script
         */
        public void setScript(String script) {
            this.script = script;
        }

        /**
         * Specifies the language.
         *
         * @param language
         */
        public void setLanguage(String language) {
            this.language = language;
            this.engine = new ScriptEngineManager().getEngineByName(this.language);
        }
    }
}
