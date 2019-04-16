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
package io.syndesis.server.api.generator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.syndesis.common.model.api.APISummary;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.ConnectorGroup;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.common.model.connection.ConnectorTemplate;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.server.api.generator.util.IconGenerator;

public abstract class ConnectorGenerator {

    private final Connector baseConnector;

    public ConnectorGenerator(final Connector baseConnector) {
        // we want to inherit all the configuration metadata from the connector
        // the template only points to the connector and holds properties
        // metadata needs to be sourced from the connector implementation
        // so we keep it in sync

        // properties should be taken based on the given settings and the
        // specification
        final Map<String, ConfigurationProperty> properties = Collections.emptyMap();

        // some metadata needs to be removed
        final Map<String, String> metadata = new HashMap<>(baseConnector.getMetadata());
        metadata.remove("hide-from-connection-pages"); // we want the generated
                                                       // connector to show up
                                                       // on connection pages

        this.baseConnector = new Connector.Builder()
            .createFrom(baseConnector)
            .metadata(metadata)
            .properties(properties)
            .build();
    }

    public abstract Connector generate(ConnectorTemplate connectorTemplate, ConnectorSettings connectorSettings);

    public abstract APISummary info(ConnectorTemplate connectorTemplate, ConnectorSettings connectorSettings);

    protected final Connector baseConnectorFrom(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final Set<String> properties = connectorTemplate.getProperties().keySet();

        final Map<String, String> configuredProperties = connectorSettings.getConfiguredProperties()
            .entrySet().stream()
            .filter(e -> properties.contains(e.getKey()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        configuredProperties.putAll(baseConnector.getConfiguredProperties());

        final String name = Optional.ofNullable(connectorSettings.getName())
            .orElseGet(() -> determineConnectorName(connectorTemplate, connectorSettings));

        final String description = Optional.ofNullable(connectorSettings.getDescription())
            .orElseGet(() -> determineConnectorDescription(connectorTemplate, connectorSettings));

        final Optional<ConnectorGroup> connectorGroup = connectorTemplate.getConnectorGroup();

        final String icon;
        if (connectorSettings.getIcon() != null) {
            icon = connectorSettings.getIcon();
        } else {
            icon = IconGenerator.generate(connectorTemplate.getId().get(), name);
        }

        return new Connector.Builder()
            .createFrom(baseConnector)
            .id(KeyGenerator.createKey())
            .name(name)
            .description(description)
            .icon(icon)
            .configuredProperties(configuredProperties)
            .connectorGroup(connectorGroup)
            .connectorGroupId(connectorGroup.map(ConnectorGroup::getId).orElse(Optional.empty()))
            .build();
    }

    /**
     * Determines the newly created connector description.
     *
     * @param connectorTemplate connector template
     * @param connectorSettings custom connector definition
     */
    protected String determineConnectorDescription(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        return "unspecified";
    }

    /**
     * Determines the newly created connector name.
     *
     * @param connectorTemplate connector template
     * @param connectorSettings custom connector definition
     */
    protected String determineConnectorName(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        return "unspecified";
    }
}
