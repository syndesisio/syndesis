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

import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import io.syndesis.model.integration.Step;
import io.syndesis.model.integration.StepKind;
import org.apache.camel.LoggingLevel;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LogStepHandler implements IntegrationStepHandler {

    @Override
    public boolean canHandle(Step step) {
        return step.getStepKind() == StepKind.log;
    }

    @Override
    public Optional<ProcessorDefinition> handle(Step step, ProcessorDefinition route, IntegrationRouteBuilder builder, String stepIndex) {
        return Optional.of(route.log(LoggingLevel.INFO, createMessage(step)));
    }


    private static String createMessage(Step l) {
        StringBuilder sb = new StringBuilder(128);
        String expression = getExpression(l.getConfiguredProperties());
        Boolean isBodyLoggingEnabled = isBodyLoggingEnabled(l.getConfiguredProperties());
        List<String> inHeaderNames =  asList("inHeaderNames", l.getConfiguredProperties());
        List<String> outHeaderNames =  asList("outHeaderNames", l.getConfiguredProperties());
        List<String> propertyNames =  asList("propertyNames", l.getConfiguredProperties());

        if (expression != null &&
                !expression.isEmpty() &&
                !expression.equals("null")) { //TODO: the null thing should be handled by the ui.
            sb.append(expression);
        }

        if (isBodyLoggingEnabled) {
            sb.append("Body: [${body}] ");
        }

        configureHeaders(sb, HeaderKind.in, inHeaderNames);
        configureHeaders(sb, HeaderKind.out, outHeaderNames);
        configureProperties(sb, propertyNames);
        return sb.toString();
    }

    private static void configureProperties(StringBuilder sb, Collection<String> names) {
        if (names == null) {
            return;
        }

        List<String> filtered = names
                .stream()
                .filter(n -> !"null".equals(n))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return;
        }

        sb.append(filtered.stream().map(p->"p = $property."+p)
                .collect(Collectors.joining(" ", "Properties[", "] ")));
    }

    private static List<String> asList(String propertyName, Map<String, String> props) {
        if (props == null || props.isEmpty()) {
            return null;
        }
        String names = props.get(propertyName);
        if (names == null || names.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(names.split("[ \\n,]+"));
    }

    private static void configureHeaders(StringBuilder sb, HeaderKind kind, Collection<String> names) {
        if (kind == null) {
            throw new IllegalArgumentException("Kind should not be null. Please specify a valid value.");
        }

        if (names == null) {
            return;
        }

        List<String> filtered = names
                .stream()
                .filter(n -> !"null".equals(n))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return;
        }

        sb.append(filtered.stream().map(p->"h = $"+kind.name()+"header."+p)
                .collect(Collectors.joining(" ", kind.getLabel()+ " Headers[", "] ")));
    }

    private static boolean isBodyLoggingEnabled(Map<String, String> props) {
        if (props == null || props.isEmpty()) {
            return false;
        }
        return Boolean.parseBoolean(props.getOrDefault("bodyLoggingEnabled", "false"));
    }

    private static String getExpression(Map<String, String> props) {
        if (props == null || props.isEmpty()) {
            return null;
        }
        return props.get("expression");
    }
    private enum HeaderKind {
        in("Input"),
        out("Output");

        private final String label;

        HeaderKind(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
