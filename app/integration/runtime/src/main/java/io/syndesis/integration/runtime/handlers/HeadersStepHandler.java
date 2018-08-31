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

import java.util.Map;
import java.util.Optional;

import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.util.ObjectHelper;

public class HeadersStepHandler implements IntegrationStepHandler {
    @Override
    public boolean canHandle(Step step) {
        return StepKind.headers == step.getStepKind();
    }

    @SuppressWarnings({"PMD.AvoidReassigningParameters", "PMD.AvoidDeeplyNestedIfStmts"})
    @Override
    public Optional<ProcessorDefinition<?>> handle(Step step, final ProcessorDefinition<?> route, IntegrationRouteBuilder builder, String flowIndex, String stepIndex) {
        ObjectHelper.notNull(route, "route");

        final Map<String, String> props = step.getConfiguredProperties();
        final String action = props.getOrDefault("action", "set");

        if (ObjectHelper.equal(action, "set", true)) {
            props.entrySet().stream()
                .filter(e -> !"action".equalsIgnoreCase(e.getKey()))
                .forEach(e-> route.setHeader(e.getKey()).constant(e.getValue()));
        } else if (ObjectHelper.equal(action, "remove", true)) {
            props.entrySet().stream()
                .filter(e -> !"action".equalsIgnoreCase(e.getKey()))
                .forEach(e-> route.removeHeaders(e.getKey()));
        } else {
            throw new IllegalArgumentException("Unknown action:" + action);
        }

        return Optional.of(route);
    }
}
