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
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.TypeConverter;
import org.apache.camel.builder.Builder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.spi.Language;
import org.apache.camel.support.ExpressionAdapter;
import org.apache.camel.util.ObjectHelper;

public class SplitStepHandler implements IntegrationStepHandler {

    /* Types that need conversion to String in order to perform a split operation */
    private enum AutoConvertTypes {
        INPUT_STREAM(InputStream.class),
        REMOTE_FILE("org.apache.camel.component.file.remote.RemoteFile") // ftp connector
        ;

        private final Class<?> type;
        private final String typeName;

        AutoConvertTypes(Class<?> type) {
            this.type = type;
            this.typeName = type.getName();
        }

        AutoConvertTypes(String typeName) {
            this.type = null;
            this.typeName = typeName;
        }

        boolean isInstance(Object value) {
            if (type != null) {
                return type.isInstance(value);
            }

            return typeName.equals(value.getClass().getName());
        }
    }

    @Override
    public boolean canHandle(Step step) {
        return StepKind.split == step.getStepKind();
    }

    @SuppressWarnings({"PMD.AvoidReassigningParameters", "PMD.AvoidDeeplyNestedIfStmts"})
    @Override
    public Optional<ProcessorDefinition<?>> handle(Step step, ProcessorDefinition<?> route, IntegrationRouteBuilder builder, String flowIndex, String stepIndex) {
        ObjectHelper.notNull(route, "route");

        SplitExpression splitExpression;
        String languageName = step.getConfiguredProperties().get("language");
        String expressionDefinition = step.getConfiguredProperties().get("expression");

        if (step.hasUnifiedJsonSchemaOutputShape()) {
            // we have to split the nested unified body property by default.
            splitExpression = new SplitExpression(new UnifiedJsonBodyExpression(Builder.body()));
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
            splitExpression = new SplitExpression(language.createExpression(expressionDefinition));
        } else {
            splitExpression = new SplitExpression(Builder.body());
        }

        AggregateStepHandler.AggregationOption aggregation = Optional.ofNullable(step.getConfiguredProperties().get("aggregationStrategy"))
                .map(AggregateStepHandler.AggregationOption::valueOf)
                .orElse(AggregateStepHandler.AggregationOption.body);

        route = route.split(splitExpression).aggregationStrategy(aggregation.getStrategy(step.getConfiguredProperties()));

        return Optional.of(route);
    }

    /**
     * Split expression that is aware of special list of Json beans representation provided by some connectors such as
     * SQL. Also handles Json array String representation and splits its array elements accordingly.
     *
     * Expression receives a delegate expression that usually evaluates the part of the body or header that should be split. By
     * default this is a simple body expression.
     *
     * When delegate expression evaluates to something else that a Json array or list of Json beans nothing is performed on top of
     * the delegate expression.
     */
    private static class SplitExpression extends ExpressionAdapter {
        private final Expression delegate;

        SplitExpression(Expression delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object evaluate(Exchange exchange) {
            try {
                Object value = convert(delegate.evaluate(exchange, Object.class), exchange);

                if (value instanceof String && JsonUtils.isJson(value.toString())) {
                    JsonNode json = Json.reader().readTree(value.toString());
                    if (json.isArray()) {
                        return JsonUtils.arrayToJsonBeans(json);
                    }
                }

                return value;
            } catch (IOException e) {
                throw SyndesisServerException.launderThrowable(e);
            }
        }
    }

    /**
     * Expression extracts body property from unified Json schema typed input. The unified Json holds the actual body in
     * a nested property. This property is extracted and set as expression result so follow up expressions can operate on the body.
     *
     * Expression receives a delegate expression that usually evaluates the part of the body or header that should be
     * treated as unified Json. By default this is a simple body expression.
     */
    private static class UnifiedJsonBodyExpression extends ExpressionAdapter {
        private final Expression delegate;

        UnifiedJsonBodyExpression(Expression delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object evaluate(Exchange exchange) {
            try {
                Object value = convert(delegate.evaluate(exchange, Object.class), exchange);

                if (value instanceof String && JsonUtils.isJson(value.toString())) {
                    JsonNode json = Json.reader().readTree(value.toString());
                    JsonNode body = json.get("body");
                    if (body != null) {
                        return Json.writer().writeValueAsString(body);
                    }
                }

                return value;
            } catch (IOException e) {
                throw SyndesisServerException.launderThrowable(e);
            }
        }
    }

    /**
     * Helper tries to convert given value to a String representation that can be split. For instance a remote file object
     * with Json array as content is converted to String and then split using the elements in that Json array.
     *
     * Some types are already supported by Camel's splitter such as List or Scanner. Do not touch these types and skip conversion.
     * @param value the value to convert.
     * @param exchange the current exchange.
     * @return converted value or original value itself if conversion failed or is not applicable.
     */
    private static Object convert(Object value, Exchange exchange) {
        if (Arrays.stream(AutoConvertTypes.values()).noneMatch(type -> type.isInstance(value))) {
            return value;
        }

        TypeConverter converter = exchange.getContext().getTypeConverter();
        String answer = converter.convertTo(String.class, exchange, value);
        if (answer != null) {
            return answer;
        }

        answer = converter.tryConvertTo(String.class, exchange, value);
        if (answer != null) {
            return answer;
        }

        return value;
    }
}
