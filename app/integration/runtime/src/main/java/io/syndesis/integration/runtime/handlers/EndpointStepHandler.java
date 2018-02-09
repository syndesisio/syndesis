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

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import io.syndesis.core.CollectionsUtils;
import io.syndesis.core.Optionals;
import io.syndesis.core.Predicates;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.model.WithConfigurationProperties;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.action.ConnectorDescriptor;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Step;
import io.syndesis.model.integration.StepKind;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.URISupport;

/**
 * This is needed until connectors are migrated to the new architecture.
 */
public class EndpointStepHandler extends AbstractEndpointStepHandler {
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
        if (!step.getAction().filter(ConnectorAction.class::isInstance).isPresent()) {
            return false;
        }

        return !Optionals.first(
            step.getAction().filter(ConnectorAction.class::isInstance).map(ConnectorAction.class::cast).get().getDescriptor().getComponentScheme(),
            step.getConnection().get().getConnector().get().getComponentScheme()
        ).isPresent();
    }

    @SuppressWarnings({"unchecked", "PMD"})
    @Override
    public Optional<ProcessorDefinition> handle(Step step, ProcessorDefinition route, IntegrationRouteBuilder builder, final String stepIndex) {
        // Model
        final Connection connection = step.getConnection().get();
        final Connector connector = connection.getConnector().get();
        final ConnectorAction action = step.getAction().filter(ConnectorAction.class::isInstance).map(ConnectorAction.class::cast).get();
        final ConnectorDescriptor descriptor = action.getDescriptor();

        // Camel
        final String componentScheme = action.getDescriptor().getCamelConnectorPrefix();
        final Map<String, String> configuredProperties = CollectionsUtils.aggregate(connection.getConfiguredProperties(), step.getConfiguredProperties());
        final Map<String, String> properties = CollectionsUtils.aggregate(connector.filterEndpointProperties(configuredProperties), action.filterEndpointProperties(configuredProperties));

        properties.entrySet()
            .stream()
            .filter(Predicates.or(connector::isEndpointProperty, action::isEndpointProperty))
            .filter(Predicates.or(connector::isSecret, action::isSecret))
            .forEach(e -> e.setValue(String.format("{{%s-%s.%s}}", componentScheme, stepIndex, e.getKey())));

        // raw values.
        properties.entrySet()
            .stream()
            .filter(Predicates.or(connector::isRaw, action::isRaw))
            .forEach(e -> e.setValue(String.format("RAW(%s)", e.getValue())));

        // any configuredProperties on action descriptor are considered
        properties.putAll(descriptor.getConfiguredProperties());

        String uri = componentScheme;

        if (hasComponentProperties(configuredProperties, connector, action)) {
            uri = String.format("%s-%s", componentScheme, stepIndex);
        }

        if (ObjectHelper.isNotEmpty(uri) && ObjectHelper.isNotEmpty(properties)) {
            try {
                uri = URISupport.appendParametersToURI(uri, Map.class.cast(properties));
            } catch (UnsupportedEncodingException |URISyntaxException e) {
                throw ObjectHelper.wrapRuntimeCamelException(e);
            }
        }

        if (route == null) {
            route = builder.from(uri);
        } else {
            route = route.to(uri);
        }

        // Handle split
        return handleSplit(descriptor, route, builder, stepIndex);
    }

    // *************************
    // Helpers
    // *************************


    private static boolean hasComponentProperties(Map<String, String> properties, WithConfigurationProperties... withConfigurationProperties) {
        for (WithConfigurationProperties wcp : withConfigurationProperties) {
            if (properties.entrySet().stream().anyMatch(wcp::isComponentProperty)) {
                return true;
            }
        }

        return false;
    }
}
