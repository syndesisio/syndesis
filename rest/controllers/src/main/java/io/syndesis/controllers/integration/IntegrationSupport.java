/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.controllers.integration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.syndesis.dao.manager.EncryptionComponent;
import io.syndesis.integration.model.steps.Endpoint;
import io.syndesis.model.WithConfigurationProperties;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.action.ConnectorDescriptor;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.Step;

public final class IntegrationSupport {
    private IntegrationSupport() {
    }

    public static <K, V> Map<K, V> aggregate(Map<K, V> ... maps) {
        return Stream.of(maps)
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue));
    }

    public static <T> Predicate<T> or(Predicate<T>... predicates) {
        Predicate<T> predicate = predicates[0];

        for (int i = 1; i < predicates.length; i++) {
            predicate = predicate.or(predicates[i]);
        }

        return predicate;
    }

    public static boolean hasComponentProperties(Map<String, String> properties, WithConfigurationProperties... withConfigurationProperties) {
        for (WithConfigurationProperties wcp : withConfigurationProperties) {
            if (properties.entrySet().stream().anyMatch(wcp::isComponentProperty)) {
                return true;
            }
        }

        return false;
    }

    public static Map<Step, String> buildConnectorSuffixMap(Integration integration) {
        final Map<Step, String> connectorIdMap = new HashMap<>();

        integration.getSteps().stream()
            .filter(s -> s.getStepKind().equals(Endpoint.KIND))
            .filter(s -> s.getAction().filter(ConnectorAction.class::isInstance).isPresent())
            .filter(s -> s.getConnection().isPresent())
            .collect(Collectors.groupingBy(s -> s.getAction().map(ConnectorAction.class::cast).get().getDescriptor().getCamelConnectorPrefix()))
            .forEach(
                (prefix, stepList) -> {
                    if (stepList.size() > 1) {
                        for (int i = 0; i < stepList.size(); i++) {
                            connectorIdMap.put(stepList.get(i), Integer.toString(i + 1));
                        }
                    }
                }
            );

        return Collections.unmodifiableMap(connectorIdMap);
    }

    public static Properties buildApplicationProperties(Integration integration, Map<String, Connector> connectorMap, EncryptionComponent encryptionSupport) {
        final Properties applicationProperties = new Properties();
        final Map<Step, String> connectorIdMap = buildConnectorSuffixMap(integration);

        integration.getSteps().stream()
            .filter(step -> step.getStepKind().equals(Endpoint.KIND))
            .filter(s -> s.getAction().filter(ConnectorAction.class::isInstance).isPresent())
            .filter(step -> step.getConnection().isPresent())
            .forEach(step -> {
                final ConnectorAction action = step.getAction().map(ConnectorAction.class::cast).get();
                final ConnectorDescriptor descriptor = action.getDescriptor();
                final Connection connection = step.getConnection().get();
                final String connectorId = connection.getConnectorId().orElseGet(descriptor::getConnectorId);

                if (!connectorMap.containsKey(connectorId)) {
                    throw new IllegalStateException("Connector:[" + connectorId + "] not found.");
                }

                final String connectorPrefix = descriptor.getCamelConnectorPrefix();
                final Connector connector = connectorMap.get(connectorId);
                final Map<String, String> properties = aggregate(connector.getConfiguredProperties(), connection.getConfiguredProperties(), step.getConfiguredProperties());

                final Function<Map.Entry<String, String>, String> componentKeyConverter;
                final Function<Map.Entry<String, String>, String> secretKeyConverter;

                // Enable configuration aliases only if the connector
                // has component options otherwise it does not get
                // configured by camel.
                //
                // The real connector id is calculated from the
                // number of instances a connector should be
                // instantiated.
                //
                // Example:
                //
                // if the twitter-search-connector is used twice,
                // secrets will be in the form
                //
                //     twitter-search-connector.configurations.twitter-search-connector-1.propertyName
                //
                // otherwise it fallback to
                //
                //     twitter-search-connector.propertyName
                if (hasComponentProperties(properties, connector, action) && connectorIdMap.containsKey(step)) {
                    componentKeyConverter = e -> String.join(".", connectorPrefix, "configurations", connectorPrefix + "-" + connectorIdMap.get(step), e.getKey()).toString();
                } else {
                    componentKeyConverter = e -> String.join(".", connectorPrefix, e.getKey()).toString();
                }

                // Secrets does not follow the component convention so
                // the property is always flattered at connector level
                //
                // Example:
                //
                //     twitter-search-connector-1.propertyName
                //
                // otherwise it fallback to
                //
                //     witter-search-connector.propertyName
                //
                if (connectorIdMap.containsKey(step)) {
                    secretKeyConverter = e -> String.join(".", connectorPrefix + "-" + connectorIdMap.get(step), e.getKey()).toString();
                } else {
                    secretKeyConverter = e -> String.join(".", connectorPrefix, e.getKey()).toString();
                }

                // Merge properties set on connection and step and
                // create secrets for component options or for sensitive
                // information.
                //
                // NOTE: if an option is both a component option and
                //       a sensitive information it is then only added
                //       to the component configuration to avoid dups
                //       and possible error at runtime.
                properties.entrySet().stream()
                    .filter(or(connector::isSecretOrComponentProperty, action::isSecretOrComponentProperty))
                    .distinct()
                    .forEach(
                        e -> {
                            if (connector.isComponentProperty(e) || action.isComponentProperty(e)) {
                                applicationProperties.put(componentKeyConverter.apply(e), encryptionSupport.decrypt(e.getValue()));
                            } else if (connector.isSecret(e) || action.isSecret(e)) {
                                applicationProperties.put(secretKeyConverter.apply(e), encryptionSupport.decrypt(e.getValue()));
                            }
                        }
                    );
            });


        return applicationProperties;
    }
}
