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
package io.syndesis.project.converter.visitor;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.syndesis.core.Optionals;
import io.syndesis.core.Predicates;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.WithConfigurationProperties;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Step;

public class ConnectorStepVisitor implements StepVisitor {

    // ******************************
    // Factories
    // ******************************

    public static class EndpointFactory implements StepVisitorFactory<ConnectorStepVisitor> {
        @Override
        public String getStepKind() {
            return io.syndesis.integration.model.steps.Endpoint.KIND;
        }

        @Override
        public ConnectorStepVisitor create() {
            return new ConnectorStepVisitor();
        }
    }

    public static class ConnectorFactory implements StepVisitorFactory<ConnectorStepVisitor> {
        @Override
        public String getStepKind() {
            return io.syndesis.camel.component.proxy.runtime.Connector.KIND;
        }

        @Override
        public ConnectorStepVisitor create() {
            return new ConnectorStepVisitor();
        }
    }

    // ******************************
    // Handler
    // ******************************

    @Override
    public Collection<io.syndesis.integration.model.steps.Step> visit(StepVisitorContext visitorContext) {
        final Step step = visitorContext.getStep();
        final Connection connection = step.getConnection().orElseThrow(() -> new IllegalArgumentException("Missing connection for step:" + step));
        final Connector connector = resolveConnector(connection, visitorContext.getGeneratorContext().getDataManager());
        final ConnectorAction action = step.getAction()
            .filter(ConnectorAction.class::isInstance)
            .map(ConnectorAction.class::cast)
            .orElseThrow(() -> new IllegalArgumentException("Missing action for step:" + step));

        if (Optionals.first(action.getDescriptor().getComponentScheme(), connector.getComponentScheme()).isPresent()) {
            return Collections.singletonList(
                createConnector(
                    visitorContext,
                    step,
                    connection,
                    connector,
                    action
                )
            );
        } else {
            return Collections.singletonList(
                createEndpoint(
                    visitorContext,
                    step,
                    connection,
                    connector,
                    action
                )
            );
        }
    }

    // ***************************
    // Connector
    // ***************************

    @SuppressWarnings("unchecked")
    private io.syndesis.camel.component.proxy.runtime.Connector createConnector(StepVisitorContext visitorContext, Step step, Connection connection, Connector connector, ConnectorAction action) {
        final Map<String, String> properties = aggregate(connection.getConfiguredProperties(), step.getConfiguredProperties());
        final String scheme = Optionals.first(action.getDescriptor().getComponentScheme(), connector.getComponentScheme()).get();

        // if the option is marked as secret use property placeholder as the
        // value is added to the integration secret.
        if (visitorContext.getGeneratorContext().getGeneratorProperties().isSecretMaskingEnabled()) {
            properties.entrySet()
                .stream()
                .filter(Predicates.or(connector::isSecret, action::isSecret))
                .forEach(e -> e.setValue(String.format("{{%s-%d.%s}}", scheme, visitorContext.getIndex(), e.getKey())));
        }

        //Connector/Action properties have the precedence
        connector.getConfiguredProperties().forEach(properties::put);
        action.getDescriptor().getConfiguredProperties().forEach(properties::put);

        return new io.syndesis.camel.component.proxy.runtime.Connector(
            scheme + "-" + visitorContext.getIndex(),
            scheme,
            Map.class.cast(properties),
            Optionals.first(action.getDescriptor().getConnectorFactory(), connector.getConnectorFactory()).orElse(null),
            action.getDescriptor().getConnectorCustomizers()
        );
    }

    // ***************************
    // Endpoint (camel connector)
    // ***************************

    @SuppressWarnings("unchecked")
    private io.syndesis.integration.model.steps.Endpoint createEndpoint(StepVisitorContext visitorContext, Step step, Connection connection, Connector connector, ConnectorAction action) {
        final Map<String, String> configuredProperties = aggregate(connection.getConfiguredProperties(), step.getConfiguredProperties());
        final Map<String, String> properties = aggregate(connector.filterEndpointProperties(configuredProperties), action.filterEndpointProperties(configuredProperties));
        final String componentScheme = action.getDescriptor().getCamelConnectorPrefix();

        // if the option is marked as secret use property placeholder as the
        // value is added to the integration secret.
        if (visitorContext.getGeneratorContext().getGeneratorProperties().isSecretMaskingEnabled()) {
            properties.entrySet()
                .stream()
                .filter(Predicates.or(connector::isEndpointProperty, action::isEndpointProperty))
                .filter(Predicates.or(connector::isSecret, action::isSecret))
                .forEach(e -> e.setValue(String.format("{{%s-%d.%s}}", componentScheme, visitorContext.getIndex(), e.getKey())));
        }

        if (hasComponentProperties(configuredProperties, connector, action)) {
            return new io.syndesis.integration.model.steps.Endpoint(String.format("%s-%d", componentScheme, visitorContext.getIndex()), Map.class.cast(properties));
        } else {
            return new io.syndesis.integration.model.steps.Endpoint(componentScheme, Map.class.cast(properties));
        }
    }

    // ***************************
    // Helpers
    // ***************************

    @SafeVarargs
    @SuppressWarnings("varargs")
    private static Map<String, String> aggregate(Map<String, String>... maps) {
        return Stream.of(maps)
            .flatMap(map -> map.entrySet().stream())
            .filter(entry -> entry.getValue() != null)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue));
    }

    private static boolean hasComponentProperties(Map<String, String> properties, WithConfigurationProperties... withConfigurationProperties) {
        for (WithConfigurationProperties wcp : withConfigurationProperties) {
            if (properties.entrySet().stream().anyMatch(wcp::isComponentProperty)) {
                return true;
            }
        }

        return false;
    }

    private static Connector resolveConnector(Connection connection, DataManager dataManager) {
        final Connector connector;

        if (connection.getConnector().isPresent()) {
            connector = connection.getConnector().get();
        } else {
            connector = dataManager.fetch(Connector.class, connection.getConnectorId().get());
            if (connector == null) {
                throw new IllegalArgumentException("No connector with id: " + connection.getConnectorId().get());
            }
        }

        return connector;
    }
}
