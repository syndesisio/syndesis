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
package io.syndesis.server.endpoint.v1.handler.connection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashSet;
import java.util.Set;
import javax.validation.Validator;
import org.junit.Before;
import org.junit.Test;
import io.syndesis.common.model.bulletin.ConnectionBulletinBoard;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.server.credential.Credentials;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.endpoint.v1.state.ClientSideState;
import io.syndesis.server.verifier.MetadataConfigurationProperties;

public class ConnectionHandlerTest {

    private ConnectionHandler handler;
    private DataManager dataManager;

    @Before
    public void setUp() {
        dataManager = mock(DataManager.class);
        Validator validator = mock(Validator.class);
        Credentials credentials = mock(Credentials.class);
        ClientSideState state = mock(ClientSideState.class);
        MetadataConfigurationProperties config = mock(MetadataConfigurationProperties.class);
        EncryptionComponent encryptionSupport = mock(EncryptionComponent.class);
        handler = new ConnectionHandler(dataManager, validator, credentials, state, config, encryptionSupport);
    }

    @Test
    public void shouldDeleteConnectionsAndDeletingAssociatedResources() {
        String id = "to-delete";
        Connection connection = new Connection.Builder().id(id).build();

        when(dataManager.fetch(Connection.class, id)).thenReturn(connection);

        // Connection bulletin boards identifiers
        Set<String> cbbIds = new HashSet<>();
        for (int i = 1; i <= 2; ++i) {
            cbbIds.add(id + "-cbb" + i);
        }

        when(dataManager.fetchIdsByPropertyValue(ConnectionBulletinBoard.class, "targetResourceId", id))
            .thenReturn(cbbIds);
        for (String cbbId : cbbIds) {
            when(dataManager.delete(ConnectionBulletinBoard.class, cbbId)).thenReturn(true);
        }
        when(dataManager.delete(Connection.class, id)).thenReturn(true);

        handler.delete(id);

        verify(dataManager).delete(Connection.class, id);
        for (String cbbId : cbbIds) {
            verify(dataManager).delete(ConnectionBulletinBoard.class, cbbId);
        }
    }
}
