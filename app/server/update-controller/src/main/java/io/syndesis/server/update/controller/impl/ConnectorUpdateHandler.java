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
package io.syndesis.server.update.controller.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.syndesis.common.model.ChangeEvent;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.bulletin.ConnectionBulletinBoard;
import io.syndesis.common.model.bulletin.LeveledMessage;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.update.controller.AbstractResourceUpdateHandler;
import io.syndesis.server.update.controller.ResourceUpdateHelper;


public class ConnectorUpdateHandler extends AbstractResourceUpdateHandler<ConnectionBulletinBoard> {
    private final List<Kind> supportedKinds;

    public ConnectorUpdateHandler(DataManager dataManager) {
        super(dataManager);

        this.supportedKinds = Arrays.asList(Kind.Connector, Kind.Connection);
    }

    @Override
    public boolean canHandle(ChangeEvent event) {
        return event.getKind().map(Kind::from).filter(supportedKinds::contains).isPresent();
    }

    @Override
    protected List<ConnectionBulletinBoard> compute(ChangeEvent event) {
        final List<ConnectionBulletinBoard> boards = new ArrayList<>();
        final DataManager dataManager = getDataManager();
        final List<Connector> connectors = dataManager.fetchAll(Connector.class).getItems();

        for (int i = 0; i < connectors.size(); i++) {
            final Connector connector = connectors.get(i);
            final String id = connector.getId().get();

            dataManager.fetchAllByPropertyValue(Connection.class, "connectorId", id)
                .filter(connection -> connection.getConnector().isPresent())
                .map(connection -> computeBoard(connection, connection.getConnector().get(), connector))
                .forEach(boards::add);
        }

        return boards;
    }

    private ConnectionBulletinBoard computeBoard(Connection connection, Connector oldConnector, Connector newConnector) {
        final DataManager dataManager = getDataManager();
        final List<LeveledMessage> messages = ResourceUpdateHelper.computeBulletinMessages(oldConnector.getProperties(), newConnector.getProperties());
        final String id = connection.getId().get();
        final ConnectionBulletinBoard board = dataManager.fetchByPropertyValue(ConnectionBulletinBoard.class, "targetResourceId", id).orElse(null);
        final ConnectionBulletinBoard.Builder builder;

        if (board != null) {
            builder = new ConnectionBulletinBoard.Builder()
                .createFrom(board)
                .updatedAt(System.currentTimeMillis());
        } else {
            builder = new ConnectionBulletinBoard.Builder()
                .id(KeyGenerator.createKey())
                .targetResourceId(id)
                .createdAt(System.currentTimeMillis());
        }

        builder.errors(countMessagesWithLevel(LeveledMessage.Level.ERROR, messages));
        builder.warnings(countMessagesWithLevel(LeveledMessage.Level.WARN, messages));
        builder.notices(countMessagesWithLevel(LeveledMessage.Level.INFO, messages));
        builder.putMetadata("connector-id", newConnector.getId().get());
        builder.putMetadata("connector-version-latest", Long.toString(newConnector.getVersion()));
        builder.putMetadata("connector-version-connection", Long.toString(oldConnector.getVersion()));
        builder.messages(messages);

        return builder.build();
    }

    private int countMessagesWithLevel(LeveledMessage.Level level, List<LeveledMessage> messages) {
        int count = 0;

        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getLevel() == level) {
                count++;
            }
        }

        return count;
    }
}
