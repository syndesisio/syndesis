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
package io.syndesis.connector.generator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import io.syndesis.core.Names;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.connection.ConnectorTemplate;

public interface ConnectorGenerator {

    default Connector baseConnectorFrom(final ConnectorTemplate connectorTemplate, final Connector template) {
        final Set<String> properties = connectorTemplate.getProperties().keySet();

        final Map<String, String> configuredProperties = template.getConfiguredProperties()//
            .entrySet().stream()//
            .filter(e -> properties.contains(e.getKey()))//
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        final Connector.Builder connectorBuilder = new Connector.Builder()//
            .id(template.getId()
                .orElseGet(() -> Names.sanitize(connectorTemplate.getName()) + ":" + Names.sanitize(template.getName())))//
            .name(template.getName())//
            .description(template.getDescription())//
            .icon(template.getIcon())//
            .properties(connectorTemplate.getConnectorProperties())//
            .configuredProperties(configuredProperties)//
            .connectorGroup(connectorTemplate.getConnectorGroup());

        return connectorBuilder.build();
    }

    Connector generate(ConnectorTemplate connectorTemplate, Connector template);
}
