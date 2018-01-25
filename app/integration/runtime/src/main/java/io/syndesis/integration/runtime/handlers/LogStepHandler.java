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
import io.syndesis.model.log.LogStep;
import org.apache.camel.LoggingLevel;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class LogStepHandler implements IntegrationStepHandler {

    @Override
    public boolean canHandle(Step step) {
        return LogStep.STEP_KIND.equals(step.getStepKind());
    }

    @Override
    public Optional<ProcessorDefinition> handle(Step step, ProcessorDefinition route, IntegrationRouteBuilder builder) {
        return Optional.of(route.log(LoggingLevel.INFO, createMessage((LogStep)step)));
    }


    private static String createMessage(LogStep l) {
        StringBuilder sb = new StringBuilder(128);
        configureProperties(sb, l.getPropertyNames());
        configureHeaders(sb, HeaderKind.in, l.getInHeaderNames());
        configureHeaders(sb, HeaderKind.out, l.getOutHeaderNames());

        if (l.isBodyLoggingEnabled()) {
            sb.append("Body: [${body}] ");
        }

        if (l.getExpression() != null && !l.getExpression().isEmpty()) {
            sb.append(l.getExpression());
        }

        return sb.toString();
    }

    private static void configureProperties(StringBuilder sb, Collection<String> names) {
        if (names != null && !names.isEmpty()) {
            sb.append("Exchange properties: [");
            sb.append(names.stream().map(p -> "$property." + p).collect(Collectors.joining(" ")));
            sb.append("] ");
        }
    }


    private static void configureHeaders(StringBuilder sb, HeaderKind kind, Collection<String> names) {
        if (kind == null) {
            throw new NullPointerException("Kind should not be null. Please specify a valid value.");
        }
        if (names != null && !names.isEmpty()) {
            sb.append(kind.getLabel());
            sb.append(" Headers: [");
            sb.append(names.stream().map(h -> "$"+kind.name()+".header." + h).collect(Collectors.joining(" ")));
            sb.append("] ");
        }
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
