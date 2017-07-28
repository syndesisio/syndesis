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
package io.syndesis.rest.v1.handler.connection;

import java.util.Date;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import io.swagger.annotations.Api;
import io.syndesis.credential.Credentials;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.Kind;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.rest.v1.handler.BaseHandler;
import io.syndesis.rest.v1.operations.Creator;
import io.syndesis.rest.v1.operations.Deleter;
import io.syndesis.rest.v1.operations.Getter;
import io.syndesis.rest.v1.operations.Lister;
import io.syndesis.rest.v1.operations.Updater;
import io.syndesis.rest.v1.state.ClientSideState;

import org.springframework.stereotype.Component;

@Path("/connections")
@Api(value = "connections")
@Component
public class ConnectionHandler extends BaseHandler
    implements Lister<Connection>, Getter<Connection>, Creator<Connection>, Deleter<Connection>, Updater<Connection> {

    private final Credentials credentials;

    private ClientSideState state;

    public ConnectionHandler(final DataManager dataMgr, final Credentials credentials, ClientSideState state) {
        super(dataMgr);
        this.credentials = credentials;
        this.state = state;
    }

    @Override
    public Kind resourceKind() {
        return Kind.Connection;
    }

    @Override
    public Connection get(final String id) {
        Connection connection = Getter.super.get(id);
        if (connection.getConnectorId().isPresent()) {
            final Connector connector = getDataManager().fetch(Connector.class, connection.getConnectorId().get());
            connection = new Connection.Builder().createFrom(connection).connector(connector).build();
        }
        return connection;
    }

    @Override
    public Connection create(final Connection connection) {
        final Date rightNow = new Date();
        final Connection updatedConnection = new Connection.Builder().createFrom(connection).createdDate(rightNow)
            .lastUpdated(rightNow).build();
        return Creator.super.create(updatedConnection);
    }

    @Override
    public void update(final String id, final Connection connection) {
        final Connection updatedConnection = new Connection.Builder().createFrom(connection).lastUpdated(new Date())
            .build();
        Updater.super.update(id, updatedConnection);
    }

    @Path("/{id}/credentials")
    public ConnectionCredentialHandler credentials(final @PathParam("id") String connectionId) {
        final String connectorId = get(connectionId).getConnector().flatMap(Connector::getId)
            .orElseThrow(() -> new EntityNotFoundException(connectionId));

        return new ConnectionCredentialHandler(credentials, state, connectionId, connectorId);
    }

}
