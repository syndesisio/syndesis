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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
import io.syndesis.model.filter.ExpressionFilterStep;
import io.syndesis.model.filter.RuleFilterStep;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.IntegrationDeploymentSpec;
import io.syndesis.model.integration.SimpleStep;
import io.syndesis.model.integration.Step;

public final class IntegrationSupport {
    private IntegrationSupport() {
    }

    public static IntegrationDeployment sanitize(IntegrationDeployment integrationDeployment, DataManager dataManager, EncryptionComponent encryptionSupport) {
        final int stepCount = integrationDeployment.getSpec().getSteps().size();
        final List<Step> steps = new ArrayList<>(stepCount);
        final IntegrationDeploymentSpec.Builder builder = new IntegrationDeploymentSpec.Builder().createFrom(integrationDeployment.getSpec());

        for (int i = 1; i <= stepCount; i++) {
            final Step source = integrationDeployment.getSpec().getSteps().get(i - 1);
            final Step target;

            if (ExpressionFilterStep.STEP_KIND.equals(source.getStepKind())) {
                target = new ExpressionFilterStep.Builder()
                    .createFrom(source)
                    .putMetadata(Step.METADATA_STEP_INDEX, Integer.toString(i))
                    .build();
            } else if (RuleFilterStep.STEP_KIND.equals(source.getStepKind())) {
                target = new RuleFilterStep.Builder()
                    .createFrom(source)
                    .putMetadata(Step.METADATA_STEP_INDEX, Integer.toString(i))
                    .build();
            } else {
                SimpleStep.Builder stepBuilder = new SimpleStep.Builder();
                stepBuilder.createFrom(source);
                stepBuilder.putMetadata(Step.METADATA_STEP_INDEX, Integer.toString(i));

                source.getConnection().ifPresent(connection -> {
                    // If connector is not set, fetch it from data source and update connection
                    if (connection.getConnectorId().isPresent() && !connection.getConnector().isPresent()) {
                        Connector connector = dataManager.fetch(Connector.class, connection.getConnectorId().get());

                        if (connector != null) {
                            stepBuilder.connection(
                                new Connection.Builder()
                                    .createFrom(connection)
                                    .connector(connector)
                                    .build()
                            );
                        } else {
                            throw new IllegalArgumentException("Unable to fetch connector: " + connection.getConnectorId().get());
                        }
                    }
                });

                target = stepBuilder.build();
            }

            steps.add(target);
        }

        return new IntegrationDeployment.Builder()
            .createFrom(integrationDeployment)
            .spec(builder.steps(steps).build())
            .build();
    }

    public static Properties buildApplicationProperties(IntegrationDeployment integrationDeployment, DataManager dataManager, EncryptionComponent encryptionSupport) {
        final Properties properties = new Properties();
        final List<? extends Step> steps = integrationDeployment.getSpec().getSteps();

        // ****************************************
        //
        // Connectors
        //
        // ****************************************

        steps.stream()
            .filter(step -> step.getStepKind().equals("endpoint"))
            .filter(step -> step.getAction().filter(ConnectorAction.class::isInstance).isPresent())
            .filter(step -> step.getConnection().isPresent())
            .forEach(step -> {
                final String index = step.getMetadata(Step.METADATA_STEP_INDEX).orElseThrow(() -> new IllegalArgumentException("Missing index for step:" + step));
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
                                String key = String.format("%s-%s.%s", componentScheme, index, e.getKey());
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
                                String key = String.format("%s-%s.%s", componentScheme, index, e.getKey());
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
                                String key = String.format("%s.configurations.%s-%s.%s", componentScheme, componentScheme, index, e.getKey());
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
