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

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.CollectionsUtils;
import io.syndesis.common.util.Optionals;
import io.syndesis.common.util.Predicates;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import org.apache.camel.Component;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.URISupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is needed until connectors are migrated to the new architecture.
 */
public class EndpointStepHandler implements IntegrationStepHandler, IntegrationStepHandler.Consumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointStepHandler.class);

    @Override
    public boolean canHandle(Step step) {
        if (StepKind.endpoint != step.getStepKind() && StepKind.connector != step.getStepKind()) {
            return false;
        }
        if (!step.getConnection().isPresent()) {
            return false;
        }
        if (!step.getConnection().get().getConnector().isPresent()) {
            return false;
        }
        if (!step.getActionAs(ConnectorAction.class).isPresent()) {
            return false;
        }

        return !Optionals.first(
            step.getActionAs(ConnectorAction.class).get().getDescriptor().getComponentScheme(),
            step.getConnection().get().getConnector().get().getComponentScheme()
        ).isPresent();
    }

    @SuppressWarnings({"unchecked", "PMD"})
    @Override
    public Optional<ProcessorDefinition<?>> handle(Step step, ProcessorDefinition<?> route, IntegrationRouteBuilder builder, final String flowIndex, final String stepIndex) {
        // Model
        final Connection connection = step.getConnection().get();
        final Connector connector = connection.getConnector().get();
        final ConnectorAction action = step.getActionAs(ConnectorAction.class).get();
        final ConnectorDescriptor descriptor = action.getDescriptor();

        // Camel
        final String componentScheme = Optionals.first(descriptor.getComponentScheme(), connector.getComponentScheme()).get();
        final Map<String, String> configuredProperties = CollectionsUtils.aggregate(connection.getConfiguredProperties(), step.getConfiguredProperties());
        final Map<String, String> properties = CollectionsUtils.aggregate(connector.filterEndpointProperties(configuredProperties), action.filterEndpointProperties(configuredProperties));

        properties.entrySet()
            .stream()
            .filter(Predicates.or(connector::isEndpointProperty, action::isEndpointProperty))
            .filter(Predicates.or(connector::isSecret, action::isSecret))
            .forEach(e -> e.setValue(String.format("{{flow-%s.%s-%s.%s}}", flowIndex, componentScheme, stepIndex, e.getKey())));

        // raw values.
        properties.entrySet()
            .stream()
            .filter(Predicates.or(connector::isRaw, action::isRaw))
            .forEach(e -> e.setValue(String.format("RAW(%s)", e.getValue())));

        // any configuredProperties on action descriptor are considered
        properties.putAll(descriptor.getConfiguredProperties());

        final String componentName = String.format("%s-%s-%s", componentScheme, flowIndex, stepIndex);
        String uri = componentName;

        if (ObjectHelper.isNotEmpty(uri) && ObjectHelper.isNotEmpty(properties)) {
            try {
                uri = URISupport.appendParametersToURI(uri, Map.class.cast(properties));
            } catch (UnsupportedEncodingException |URISyntaxException e) {
                throw ObjectHelper.wrapRuntimeCamelException(e);
            }
        }

        final ModelCamelContext context = builder.getContext();
        LOGGER.debug("Getting component with name: {}", componentName);
        final Component component = context.getComponent(componentName);
        LOGGER.debug("Got component: {}", component);
        HandlerCustomizer.customizeComponent(context, connector, descriptor, component, new HashMap<>(properties));

        if (route == null) {
            route = builder.from(uri);
        } else {
            route = route.to(uri);
        }

        return Optional.ofNullable(route);
    }

}
