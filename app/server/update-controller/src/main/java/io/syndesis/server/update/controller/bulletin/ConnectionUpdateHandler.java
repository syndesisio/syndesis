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
package io.syndesis.server.update.controller.bulletin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.validation.Validator;

import io.syndesis.common.model.ChangeEvent;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.bulletin.ConnectionBulletinBoard;
import io.syndesis.common.model.bulletin.LeveledMessage;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;

/**
 * This class handles updates on {@link Connection} and related resources and
 * generates related {@link ConnectionBulletinBoard}.
 */
public class ConnectionUpdateHandler extends AbstractResourceUpdateHandler<ConnectionBulletinBoard> {
    private final List<Kind> supportedKinds;

    public ConnectionUpdateHandler(DataManager dataManager, EncryptionComponent encryptionComponent, Validator validator) {
        super(dataManager, encryptionComponent, validator);

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
                .filter(Objects::nonNull)
                .forEach(boards::add);
        }

        return boards;
    }

    ConnectionBulletinBoard computeBoard(Connection connection, Connector oldConnector, Connector newConnector) {
        final DataManager dataManager = getDataManager();
        final String id = connection.getId().get();
        final ConnectionBulletinBoard board = dataManager.fetchByPropertyValue(ConnectionBulletinBoard.class, "targetResourceId", id).orElse(null);
        final ConnectionBulletinBoard.Builder builder;

        if (board != null) {
            builder = new ConnectionBulletinBoard.Builder()
                .createFrom(board);
        } else {
            builder = new ConnectionBulletinBoard.Builder()
                .id(KeyGenerator.createKey())
                .targetResourceId(id)
                .createdAt(System.currentTimeMillis());
        }


        List<LeveledMessage> messages = new ArrayList<>();
        messages.addAll(computeValidatorMessages(LeveledMessage.Builder::new, connection));
        messages.addAll(computePropertiesDiffMessages(LeveledMessage.Builder::new, oldConnector.getProperties(), newConnector.getProperties()));
        if (!connection.isDerived()) {
            messages.addAll(computeMissingMandatoryPropertiesMessages(
                LeveledMessage.Builder::new,
                newConnector.getProperties(),
                merge(newConnector.getConfiguredProperties(), connection.getConfiguredProperties()))
            );
        }
        messages.addAll(computeSecretsUpdateMessages(LeveledMessage.Builder::new, newConnector.getProperties(), connection.getConfiguredProperties()));

        builder.errors(countMessagesWithLevel(LeveledMessage.Level.ERROR, messages));
        builder.warnings(countMessagesWithLevel(LeveledMessage.Level.WARN, messages));
        builder.notices(countMessagesWithLevel(LeveledMessage.Level.INFO, messages));
        builder.putMetadata("connector-id", newConnector.getId().get());
        builder.putMetadata("connector-version-latest", Integer.toString(newConnector.getVersion()));
        builder.putMetadata("connector-version-connection", Integer.toString(oldConnector.getVersion()));
        builder.messages(messages);

        ConnectionBulletinBoard updated = builder.build();

        if (!updated.equals(board)) {
            return builder.updatedAt(System.currentTimeMillis()).build();
        }

        return null;
    }

    private static Map<String, String> merge(final Map<String, String> one, final Map<String, String> two) {
        if (one == null || one.isEmpty()) {
            return two;
        }

        if (two == null || two.isEmpty()) {
            return one;
        }

        final Map<String, String> merged = new HashMap<>(one.size() + two.size());
        merged.putAll(one);
        merged.putAll(two);

        return merged;
    }
}
