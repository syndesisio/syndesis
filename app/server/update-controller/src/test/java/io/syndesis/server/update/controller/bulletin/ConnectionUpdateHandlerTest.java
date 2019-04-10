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

import java.util.Optional;

import javax.validation.Validator;

import io.syndesis.common.model.bulletin.ConnectionBulletinBoard;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.dao.manager.DataManager;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectionUpdateHandlerTest {

    final DataManager dataManager = mock(DataManager.class);

    final Validator validator = mock(Validator.class);

    @Test
    public void shouldNotComputeConnectorConfiguredPropertiesAsMissing() {
        final ConnectionUpdateHandler updateHandler = new ConnectionUpdateHandler(dataManager, null, validator);

        final Connection connection = new Connection.Builder()//
            .id("connection")//
            .putConfiguredProperty("req2", "value2")//
            .build();

        final ConfigurationProperty required = new ConfigurationProperty.Builder().required(true).build();
        final Connector sameConnector = new Connector.Builder()//
            .id("new-connector")//
            .putProperty("req1", required)//
            .putProperty("req2", required)//
            .putConfiguredProperty("req1", "value1")//
            .build();

        when(dataManager.fetchByPropertyValue(ConnectionBulletinBoard.class, "targetResourceId", "connection"))
            .thenReturn(Optional.empty());

        final ConnectionBulletinBoard board = updateHandler.computeBoard(connection, sameConnector, sameConnector);

        assertThat(board.getMessages()).isEmpty();
    }
}
