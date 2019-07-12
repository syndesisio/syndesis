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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;
import org.apache.camel.processor.aggregate.UseLatestAggregationStrategy;
import org.apache.camel.processor.aggregate.UseOriginalAggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggregateStepHandler implements IntegrationStepHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AggregateStepHandler.class);

    @Override
    public boolean canHandle(Step step) {
        return StepKind.aggregate == step.getStepKind();
    }

    @Override
    public Optional<ProcessorDefinition<?>> handle(Step step, ProcessorDefinition<?> route, IntegrationRouteBuilder builder, String flowIndex, String stepIndex) {
        if (step.hasUnifiedJsonSchemaOutputShape()) {
            route.process(new UnifiedJsonAggregationPostProcessor());
        }

        return Optional.of(route);
    }

    @SuppressWarnings("unchecked")
    public enum AggregationOption {
        body(() -> new GroupedBodyAggregationStrategy() {
            @Override
            public Object getValue(Exchange exchange) {
                // Account for filter match indicator and only aggregate those values that actually matched the filter.
                // When filter match indicator is not present aggregate all.
                if (exchange.getProperty(Exchange.FILTER_MATCHED, true, Boolean.class)) {
                    return super.getValue(exchange);
                } else {
                    return null;
                }

            }
        }),
        latest(UseLatestAggregationStrategy::new),
        original(() -> new UseOriginalAggregationStrategy(null, true)),
        script(AggregateStepHandler.ScriptAggregationStrategy::new, (strategy, stepProperties) -> {
            Optional.ofNullable(stepProperties.get("aggregationScriptLanguage")).ifPresent(strategy::setLanguage);
            Optional.ofNullable(stepProperties.get("aggregationScript")).ifPresent(strategy::setScript);
            return strategy;
        });

        private final StrategySupplier<AggregationStrategy> strategySupplier;
        private final StrategyConfigurer<AggregationStrategy> configurer;

        @Immutable
        @com.google.errorprone.annotations.Immutable
        interface StrategySupplier<T extends AggregationStrategy> extends Supplier<T> {}
        @Immutable
        @com.google.errorprone.annotations.Immutable
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

    /**
     * Special unified json aggregation post processor takes care of multiple unified json body elements and aggregates
     * those to a single unified body element.
     */
    private static class UnifiedJsonAggregationPostProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            final Message message = exchange.hasOut() ? exchange.getOut() : exchange.getIn();

            if (message != null && message.getBody() instanceof List) {
                try {
                    List<String> unifiedBodies = new ArrayList<>();
                    List<?> jsonBeans = message.getBody(List.class);
                    for (Object unifiedJsonBean : jsonBeans) {
                        JsonNode unifiedJson = Json.reader().readTree(String.valueOf(unifiedJsonBean));
                        if (unifiedJson.isObject()) {
                            JsonNode body = unifiedJson.get("body");
                            if (body != null) {
                                unifiedBodies.add(Json.writer().writeValueAsString(body));
                            }
                        }
                    }

                    message.setBody("{\"body\":" + JsonUtils.jsonBeansToArray(unifiedBodies) + "}");
                } catch (JsonParseException e) {
                    LOG.warn("Unable to aggregate unified json array type", e);
                }
            }
        }
    }
}
