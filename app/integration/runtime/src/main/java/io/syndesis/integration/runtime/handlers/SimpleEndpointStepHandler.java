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

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.runtimecatalog.RuntimeCamelCatalog;
import org.apache.camel.util.ObjectHelper;

/**
 * This is needed until connectors are migrated to the new architecture.
 */
public class SimpleEndpointStepHandler implements IntegrationStepHandler, IntegrationStepHandler.Consumer {
    @Override
    public boolean canHandle(Step step) {
        if (StepKind.endpoint != step.getStepKind() && StepKind.connector != step.getStepKind()) {
            return false;
        }
        if (step.getConnection().isPresent()) {
            return false;
        }
        if (!step.getActionAs(ConnectorAction.class).isPresent()) {
            return false;
        }

        ConnectorAction action = step.getActionAs(ConnectorAction.class).get();
        if (action.getDescriptor() == null) {
            return false;
        }

        return action.getDescriptor().getComponentScheme().isPresent();
    }

    @SuppressWarnings({"unchecked", "PMD"})
    @Override
    public Optional<ProcessorDefinition<?>> handle(Step step, ProcessorDefinition<?> route, IntegrationRouteBuilder builder, final String flowIndex, final String stepIndex) {
        // Model
        final ConnectorAction action = step.getActionAs(ConnectorAction.class).get();
        final ConnectorDescriptor descriptor = action.getDescriptor();

        // Camel
        final String componentScheme = action.getDescriptor().getComponentScheme().get();
        final Map<String, String> configuredProperties = step.getConfiguredProperties();
        final Map<String, String> properties = action.filterEndpointProperties(configuredProperties);

        properties.entrySet()
            .stream()
            .filter(action::isEndpointProperty)
            .filter(action::isSecret)
            .forEach(e -> e.setValue(String.format("{{flow-%s.%s-%s.%s}}", flowIndex, componentScheme, stepIndex, e.getKey())));

        // raw values.
        properties.entrySet()
            .stream()
            .filter(action::isRaw)
            .forEach(e -> e.setValue(String.format("RAW(%s)", e.getValue())));

        // any configuredProperties on action descriptor are considered
        properties.putAll(descriptor.getConfiguredProperties());

        try {
            final RuntimeCamelCatalog catalog = builder.getContext().getRuntimeCamelCatalog();
            final String uri = catalog.asEndpointUri(componentScheme, Map.class.cast(properties), false);

            if (route == null) {
                route = builder.from(uri);
            } else {
                route = route.to(uri);
            }
        } catch (URISyntaxException e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }

        return Optional.ofNullable(route);
    }
}
