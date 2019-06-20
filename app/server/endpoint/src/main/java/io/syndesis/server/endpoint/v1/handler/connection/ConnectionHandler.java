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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.springframework.stereotype.Component;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.bulletin.ConnectionBulletinBoard;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.ConnectionOverview;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.credential.CredentialFlowState;
import io.syndesis.server.credential.Credentials;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.endpoint.v1.handler.BaseHandler;
import io.syndesis.server.endpoint.v1.operations.Creator;
import io.syndesis.server.endpoint.v1.operations.Deleter;
import io.syndesis.server.endpoint.v1.operations.Getter;
import io.syndesis.server.endpoint.v1.operations.Lister;
import io.syndesis.server.endpoint.v1.operations.Updater;
import io.syndesis.server.endpoint.v1.operations.Validating;
import io.syndesis.server.endpoint.v1.state.ClientSideState;
import io.syndesis.server.endpoint.v1.util.DataManagerSupport;
import io.syndesis.server.verifier.MetadataConfigurationProperties;

@Path("/connections")
@Api(value = "connections")
@Component
public class ConnectionHandler
        extends BaseHandler
        implements Lister<ConnectionOverview>, Getter<ConnectionOverview>, Creator<Connection>, Deleter<Connection>, Updater<Connection>, Validating<Connection> {

    private final Credentials credentials;
    private final ClientSideState state;

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    private final Validator validator;

    private final MetadataConfigurationProperties config;
    private final EncryptionComponent encryptionComponent;

    public ConnectionHandler(final DataManager dataMgr, final Validator validator, final Credentials credentials,
                             final ClientSideState state, final MetadataConfigurationProperties config, final EncryptionComponent encryptionComponent) {
        super(dataMgr);
        this.validator = validator;
        this.credentials = credentials;
        this.state = state;
        this.config = config;
        this.encryptionComponent = encryptionComponent;
    }

    @Override
    public Kind resourceKind() {
        return Kind.Connection;
    }

    @Override
    public ListResult<ConnectionOverview> list(@Context UriInfo uriInfo) {
        final DataManager dataManager = getDataManager();
        final ListResult<Connection> connectionResults = fetchAll(Connection.class, uriInfo);
        final List<Connection> connections = connectionResults.getItems();
        final List<ConnectionOverview> overviews = new ArrayList<>(connectionResults.getTotalCount());

        for (Connection connection : connections) {
            final String id = connection.getId().get();
            final ConnectionOverview.Builder builder = new ConnectionOverview.Builder().createFrom(connection);

            // set the connector
            DataManagerSupport.fetch(dataManager, Connector.class, connection.getConnectorId()).ifPresent(builder::connector);

            // set the board
            DataManagerSupport.fetchBoard(dataManager, ConnectionBulletinBoard.class, id).ifPresent(builder::board);

            overviews.add(builder.build());
        }

        return ListResult.of(overviews);
    }

    @Override
    public ConnectionOverview get(final String id) {
        final DataManager dataManager = getDataManager();
        final Connection connection = dataManager.fetch(Connection.class, id);

        if( connection == null ) {
            throw new EntityNotFoundException();
        }

        final ConnectionOverview.Builder builder = new ConnectionOverview.Builder().createFrom(connection);

        // set the connector
        DataManagerSupport.fetch(dataManager, Connector.class, connection.getConnectorId()).ifPresent(builder::connector);

        // set the board
        DataManagerSupport.fetchBoard(dataManager, ConnectionBulletinBoard.class, id).ifPresent(builder::board);

        return builder.build();
    }

    @Override
    public Connection create(@Context SecurityContext sec, final Connection connection) {
        final Date rightNow = new Date();

        // Lets make sure we store encrypt secrets.
        Map<String, String> configuredProperties = connection.getConfiguredProperties();
        Map<String, ConfigurationProperty> connectorProperties = getConnectorProperties(connection.getConnectorId());

        configuredProperties = encryptionComponent.encryptPropertyValues(configuredProperties, connectorProperties);

        final Connection updatedConnection = new Connection.Builder()
            .createFrom(connection)
            .createdDate(rightNow)
            .lastUpdated(rightNow)
            .configuredProperties(configuredProperties)
            .userId(sec.getUserPrincipal().getName())
            .build();

        final Connection connectionToCreate = applyCredentialFlowStateTo(updatedConnection);

        return Creator.super.create(sec, connectionToCreate);
    }

    @Override
    public void delete(String id) {
        final DataManager dataManager = getDataManager();

        dataManager.fetchIdsByPropertyValue(ConnectionBulletinBoard.class, "targetResourceId", id)
            .forEach(cbbId -> dataManager.delete(ConnectionBulletinBoard.class, cbbId));

        Deleter.super.delete(id);
    }

    Connection applyCredentialFlowStateTo(final Connection connection) {
        final Set<CredentialFlowState> flowStates = CredentialFlowState.Builder.restoreFrom(state::restoreFrom, request);

        return flowStates.stream().map(s -> {
            final Cookie removal = new Cookie(s.persistenceKey(), "");
            removal.setPath("/");
            removal.setMaxAge(0);

            response.addCookie(removal);

            return credentials.apply(connection, s);
        }).findFirst().orElse(connection);
    }

    private Map<String, ConfigurationProperty> getConnectorProperties(String connectorId) {
        Connector connector = getDataManager().fetch(Connector.class, connectorId);
        if (connector != null) {
            return connector.getProperties();
        }

        return Collections.emptyMap();
    }

    @Override
    public void update(final String id, final Connection connection) {
        // Lets make sure we store encrypt secrets.
        Map<String, String> configuredProperties = connection.getConfiguredProperties();
        Map<String, ConfigurationProperty> connectorProperties = getConnectorProperties(connection.getConnectorId());

        configuredProperties = encryptionComponent.encryptPropertyValues(configuredProperties, connectorProperties);

        final Connection updatedConnection = new Connection.Builder()
            .createFrom(connection)
            .configuredProperties(configuredProperties)
            .lastUpdated(new Date())
            .build();

        final Connection connectionToUpdate = applyCredentialFlowStateTo(updatedConnection);

        Updater.super.update(id, connectionToUpdate);
    }

    @Path("/{id}/actions")
    public ConnectionActionHandler metadata(@NotNull @PathParam("id") @ApiParam(required = true, example = "my-connection") final String connectionId) {
        return new ConnectionActionHandler(get(connectionId), config, encryptionComponent);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{id}/bulletins")
    public ConnectionBulletinBoard getBulletins(@NotNull @PathParam("id") @ApiParam(required = true) String id) {
        ConnectionBulletinBoard result = getDataManager().fetch(ConnectionBulletinBoard.class, id);
        if( result == null ) {
            result = new ConnectionBulletinBoard.Builder().build();
        }
        return result;
    }

    @Override
    public Validator getValidator() {
        return validator;
    }

}
