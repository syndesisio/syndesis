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
package io.syndesis.controllers.integration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import io.syndesis.core.Optionals;
import io.syndesis.core.Predicates;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.dao.manager.EncryptionComponent;
import io.syndesis.model.WithConfiguredProperties;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.action.ConnectorDescriptor;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.Step;

public final class IntegrationSupport {
    private IntegrationSupport() {
    }

    public static Properties buildApplicationProperties(IntegrationDeployment integrationDeployment, DataManager dataManager, EncryptionComponent encryptionSupport) {
        final Properties properties = new Properties();
        final AtomicInteger counter = new AtomicInteger();
        final Map<Step, Integer> indices = new HashMap<>();

        // Compute step index
        for (Step step : integrationDeployment.getSpec().getSteps()) {
            indices.put(step, counter.incrementAndGet());
        }

        // ****************************************
        //
        // Connectors
        //
        // ****************************************

        integrationDeployment.getSpec().getSteps().stream()
            .filter(step -> step.getStepKind().equals(io.syndesis.integration.model.steps.Endpoint.KIND))
            .filter(step -> step.getAction().filter(ConnectorAction.class::isInstance).isPresent())
            .filter(step -> step.getConnection().isPresent())
            .forEach(step -> {
                final Integer index = indices.get(step);
                final Connection connection = step.getConnection().get();
                final ConnectorAction action = ConnectorAction.class.cast(step.getAction().get());
                final ConnectorDescriptor descriptor = action.getDescriptor();
                final Connector connector = resolveConnector(connection, dataManager);

                if (connector.getComponentScheme().isPresent() || descriptor.getComponentScheme().isPresent()) {
                    // Grab the component scheme from the component descriptor or
                    // from the connector
                    final String componentScheme = Optionals.first(descriptor.getComponentScheme(), connector.getComponentScheme()).get();

                    Stream.of(connector, connection, step)
                        .filter(WithConfiguredProperties.class::isInstance)
                        .map(WithConfiguredProperties.class::cast)
                        .map(WithConfiguredProperties::getConfiguredProperties)
                        .flatMap(map -> map.entrySet().stream())
                        .filter(Predicates.or(connector::isSecret, action::isSecret))
                        .distinct()
                        .forEach(
                            e -> {
                                String key = String.format("%s-%d.%s", componentScheme, index, e.getKey());
                                String val = encryptionSupport.decrypt(e.getValue());

                                properties.put(key, val);
                            }
                        );
                } else {
                    // The component scheme is defined as camel connector prefix
                    // for 'old' style connectors.
                    final String componentScheme = descriptor.getCamelConnectorPrefix();

                    // endpoint secrets
                    Stream.of(connector, connection, step)
                        .filter(WithConfiguredProperties.class::isInstance)
                        .map(WithConfiguredProperties.class::cast)
                        .map(WithConfiguredProperties::getConfiguredProperties)
                        .flatMap(map -> map.entrySet().stream())
                        .filter(Predicates.or(connector::isEndpointProperty, action::isEndpointProperty))
                        .filter(Predicates.or(connector::isSecret, action::isSecret))
                        .distinct()
                        .forEach(
                            e -> {
                                String key = String.format("%s-%d.%s", componentScheme, index, e.getKey());
                                String val = encryptionSupport.decrypt(e.getValue());

                                properties.put(key, val);
                            }
                        );

                    // Component properties triggers connectors aliasing so we
                    // can have multiple instances of the same connectors
                    Stream.of(connector, connection, step)
                        .filter(WithConfiguredProperties.class::isInstance)
                        .map(WithConfiguredProperties.class::cast)
                        .map(WithConfiguredProperties::getConfiguredProperties)
                        .flatMap(map -> map.entrySet().stream())
                        .filter(Predicates.or(connector::isComponentProperty, action::isComponentProperty))
                        .distinct()
                        .forEach(
                            e -> {
                                String key = String.format("%s.configurations.%s-%d.%s", componentScheme, componentScheme, index, e.getKey());
                                String val = encryptionSupport.decrypt(e.getValue());

                                properties.put(key, val);
                            }
                        );
                }
            });

        return properties;
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
