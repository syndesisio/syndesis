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
package io.syndesis.server.endpoint.v1.handler.extension;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.WithConfigurationProperties;
import io.syndesis.common.model.WithConfiguredProperties;
import io.syndesis.common.model.WithName;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.extension.Extension;
import org.springframework.stereotype.Component;

@Component
public class ExtensionActivator {

    private final DataManager dataManager;

    public ExtensionActivator(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void activateExtension(Extension extension) {
        Date rightNow = new Date();

        // Uninstall other active extensions
        doDeleteInstalled(extension.getExtensionId());

        updateConnectors(extension);

        dataManager.update(new Extension.Builder().createFrom(extension)
            .status(Extension.Status.Installed)
            .lastUpdated(rightNow)
            .build());
    }

    public void deleteExtension(Extension extension) {
        if (extension.getStatus().isPresent() && extension.getStatus().get().equals(Extension.Status.Installed)) {
            doDeleteRelatedObjects(extension);
        }
        doDelete(extension);
    }

    private void doDeleteInstalled(String logicalExtensionId) {
        Set<String> ids = dataManager.fetchIdsByPropertyValue(Extension.class, "extensionId", logicalExtensionId);
        for (String id : ids) {
            Extension extension = dataManager.fetch(Extension.class, id);
            if (extension.getStatus().isPresent() && extension.getStatus().get() == Extension.Status.Installed) {
                doDelete(extension);
            }
        }
    }

    private void doDelete(Extension extension) {
        Date rightNow = new Date();
        dataManager.update(new Extension.Builder()
            .createFrom(extension)
            .status(Extension.Status.Deleted)
            .lastUpdated(rightNow)
            .build());
    }

    private void doDeleteRelatedObjects(Extension extension) {
        Optional<Connector> connector = findAssociatedConnector(extension);
        if (connector.isPresent()) {
            List<Connection> connections = findAssociatedConnections(connector.get());
            for (Connection connection : connections) {
                if (connection.getId().isPresent()) {
                    dataManager.delete(Connection.class, connection.getId().get());
                }
            }

            if (connector.get().getId().isPresent()) {
                dataManager.delete(Connector.class, connector.get().getId().get());
            }
        }
    }

    private void updateConnectors(Extension extension) {
        if (extension.getConnectorActions().size() == 0) {
            // No connector to create, but delete any previously created connector
            doDeleteRelatedObjects(extension);
            return;
        }

        Connector newConnector = toConnector(extension);

        Optional<Connector> existingConnector = findAssociatedConnector(extension);
        if (existingConnector.isPresent()) {
            // Put already configured properties into new connector
            newConnector = migrateOldConnectorData(existingConnector.get(), newConnector);
            dataManager.update(newConnector);

            List<Connection> existingConnections = findAssociatedConnections(existingConnector.get());
            for (Connection connection : existingConnections) {
                Connection newConnection = recreateConnection(connection, newConnector);
                dataManager.update(newConnection);
            }
        } else {
            dataManager.create(newConnector);
        }
    }

    private Connection recreateConnection(Connection existingConnection, Connector newConnector) {
        Optional<Connector> connectorToUse = existingConnection.getConnector().map(old -> newConnector);

        Map<String, String> confProperties = new TreeMap<>(existingConnection.getConfiguredProperties());
        confProperties.keySet().retainAll(newConnector.getProperties().keySet());

        return new Connection.Builder()
            .createFrom(existingConnection)
            .connector(connectorToUse)
            .icon(newConnector.getIcon())
            .configuredProperties(confProperties)
            .build();
    }

    private Connector migrateOldConnectorData(Connector existingConnector, Connector newConnector) {
        Map<String, String> propValues = new TreeMap<>(existingConnector.getConfiguredProperties());
        propValues.keySet().retainAll(newConnector.getProperties().keySet());

        if (!propValues.isEmpty()) {
            return new Connector.Builder()
                .createFrom(newConnector)
                .putAllConfiguredProperties(propValues)
                .build();
        }
        return newConnector;
    }

    private List<Connection> findAssociatedConnections(Connector connector) {
        return dataManager.fetchAll(Connection.class).getItems().stream()
            .filter(c -> connector.getId().equals(c.getConnectorId()))
            .collect(Collectors.toList());
    }

    private Optional<Connector> findAssociatedConnector(Extension extension) {
        return Optional.ofNullable(dataManager.fetch(Connector.class, getConnectorIdForExtension(extension)));
    }

    private Connector toConnector(Extension extension) {
        List<ConnectorAction> connectorActions = extension.getActions(ConnectorAction.class);

        return new Connector.Builder()
            .createFrom((WithName) extension)
            .createFrom((WithConfigurationProperties) extension)
            .createFrom((WithConfiguredProperties) extension)
            .kind(Kind.Connector)
            .actions(connectorActions)
            .description(extension.getDescription())
            .icon(extension.getIcon())
            .addDependency(new Dependency.Builder()
                .id(extension.getExtensionId())
                .type(Dependency.Type.EXTENSION)
                .build())
            .id(getConnectorIdForExtension(extension))
            .build();
    }

    private String getConnectorIdForExtension(Extension extension) {
        return "ext-" + extension.getExtensionId().replaceAll("[^a-zA-Z0-9]","-");
    }


}
