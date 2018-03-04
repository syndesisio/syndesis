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
package io.syndesis.server.connector.generator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.syndesis.server.connector.generator.util.IconGenerator;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.ConnectorGroup;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.common.model.connection.ConnectorSummary;
import io.syndesis.common.model.connection.ConnectorTemplate;

public abstract class ConnectorGenerator {

    public abstract Connector generate(ConnectorTemplate connectorTemplate, ConnectorSettings connectorSettings);

    public abstract ConnectorSummary info(ConnectorTemplate connectorTemplate, ConnectorSettings connectorSettings);

    protected final Connector baseConnectorFrom(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final Set<String> properties = connectorTemplate.getProperties().keySet();

        final Map<String, String> configuredProperties = connectorSettings.getConfiguredProperties()//
            .entrySet().stream()//
            .filter(e -> properties.contains(e.getKey()))//
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

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

        return new Connector.Builder()//
            .id(KeyGenerator.createKey())//
            .name(name)//
            .description(description)//
            .icon(icon)//
            .configuredProperties(configuredProperties)//
            .connectorGroup(connectorGroup)//
            .connectorGroupId(connectorGroup.map(ConnectorGroup::getId).orElse(Optional.empty()))//
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
