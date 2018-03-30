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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.bulletin.ConnectionBulletinBoard;
import io.syndesis.common.model.bulletin.LeveledMessage;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.ConnectionOverview;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.validation.AllValidations;
import io.syndesis.server.credential.CredentialFlowState;
import io.syndesis.server.credential.Credentials;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.endpoint.v1.handler.BaseHandler;
import io.syndesis.server.endpoint.v1.handler.integration.IntegrationHandler;
import io.syndesis.server.endpoint.v1.operations.Creator;
import io.syndesis.server.endpoint.v1.operations.Deleter;
import io.syndesis.server.endpoint.v1.operations.Getter;
import io.syndesis.server.endpoint.v1.operations.Lister;
import io.syndesis.server.endpoint.v1.operations.Updater;
import io.syndesis.server.endpoint.v1.operations.Validating;
import io.syndesis.server.endpoint.v1.state.ClientSideState;
import io.syndesis.server.verifier.MetadataConfigurationProperties;
import org.springframework.stereotype.Component;

import static io.syndesis.common.model.bulletin.LeveledMessage.Level.ERROR;
import static io.syndesis.common.model.bulletin.LeveledMessage.Level.WARN;

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
    private final IntegrationHandler integrationHandler;

    public ConnectionHandler(final DataManager dataMgr, final Validator validator, final Credentials credentials,
                             final ClientSideState state, final MetadataConfigurationProperties config, final EncryptionComponent encryptionComponent,
                             IntegrationHandler integrationHandler) {
        super(dataMgr);
        this.validator = validator;
        this.credentials = credentials;
        this.state = state;
        this.config = config;
        this.encryptionComponent = encryptionComponent;
        this.integrationHandler = integrationHandler;
    }

    @Override
    public Kind resourceKind() {
        return Kind.Connection;
    }

    @Override
    public ListResult<ConnectionOverview> list(@Context UriInfo uriInfo) {
        final DataManager dataManager = getDataManager();
        final ListResult<Connection> connections = fetchAll(Connection.class, uriInfo);
        final List<ConnectionOverview> overviews = new ArrayList<>(connections.getTotalCount());

        for (Connection connection: connections.getItems()) {
            final String id = connection.getId().get();
            final Connector connector = dataManager.fetch(Connector.class, connection.getConnectorId());
            final ConnectionBulletinBoard board = dataManager.fetchByPropertyValue(ConnectionBulletinBoard.class, "targetResourceId", id).orElse(ConnectionBulletinBoard.emptyBoard());

            overviews.add(
                new ConnectionOverview.Builder()
                    .createFrom(connection)
                    .connector(connector)
                    .board(board)
                    .build()
            );
        }

        return ListResult.of(overviews);
    }

    @Override
    public ConnectionOverview get(final String id) {
        final DataManager dataManager = getDataManager();
        final Connection connection = dataManager.fetch(Connection.class, id);
        final ConnectionBulletinBoard board = dataManager.fetchByPropertyValue(ConnectionBulletinBoard.class, "targetResourceId", id).orElse(ConnectionBulletinBoard.emptyBoard());
        final Connector connector = dataManager.fetch(Connector.class, connection.getConnectorId());

        return new ConnectionOverview.Builder()
            .createFrom(connection)
            .connector(connector)
            .board(board)
            .build();
    }

    @Override
    public Connection create(@Context SecurityContext sec, @ConvertGroup(from = Default.class, to = AllValidations.class) final Connection connection) {
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

        final Set<CredentialFlowState> flowStates = CredentialFlowState.Builder.restoreFrom(state::restoreFrom, request);

        final Connection connectionToCreate = flowStates.stream().map(s -> {
            final Cookie removal = new Cookie(s.persistenceKey(), "");
            removal.setPath("/");
            removal.setMaxAge(0);

            response.addCookie(removal);

            return credentials.apply(updatedConnection, s);
        }).findFirst().orElse(updatedConnection);

        return Creator.super.create(sec, connectionToCreate);
    }

    private Map<String, ConfigurationProperty> getConnectorProperties(String connectorId) {
        Connector connector = getDataManager().fetch(Connector.class, connectorId);
        if (connector != null) {
            return connector.getProperties();
        }

        return Collections.emptyMap();
    }

    @Override
    public void update(final String id, @ConvertGroup(from = Default.class, to = AllValidations.class) final Connection connection) {
        // Lets make sure we store encrypt secrets.
        Map<String, String> configuredProperties = connection.getConfiguredProperties();
        Map<String, ConfigurationProperty> connectorProperties = getConnectorProperties(connection.getConnectorId());

        configuredProperties = encryptionComponent.encryptPropertyValues(configuredProperties, connectorProperties);

        final Connection updatedConnection = new Connection.Builder()
            .createFrom(connection)
            .configuredProperties(configuredProperties)
            .lastUpdated(new Date())
            .build();
        Updater.super.update(id, updatedConnection);


        // TODO: do this async perhaps..
        // We may need to trigger creating bulletins for some integrations.
        for (String integrationId : getDataManager().fetchIds(Integration.class)) {
            integrationHandler.updateBulletinBoard(integrationId);
        }
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

    /**
     * Update the list of notices for a given connection
     *
     * @param id
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/update-bulletins")
    public ConnectionBulletinBoard updateBulletinBoard(@NotNull @PathParam("id") @ApiParam(required = true) String id) {
        final List<LeveledMessage> messages = new ArrayList<>();
        final Connection connection = getDataManager().fetch(Connection.class, id);

        final Set<ConstraintViolation<Connection>> constraintViolations = getValidator().validate(connection, AllValidations.class);
        for (ConstraintViolation<Connection> violation : constraintViolations) {
            messages.add(LeveledMessage.of(ERROR, violation.getMessage()));
        }

        connection.getConnector().ifPresent(connector -> {
            Connector current = getDataManager().fetch(Connector.class, connector.getId().get());
            if (current == null) {
                messages.add(LeveledMessage.of(WARN, String.format("Connector '%s' has been deleted.", connector.getName())));
            }
        });

        // We have a null value if it was an encrypted property that was imported into
        // a different system.
        Map<String, String> configuredProperties = encryptionComponent.decrypt(connection.getConfiguredProperties());
        if (configuredProperties.values().contains(null)) {
            messages.add(LeveledMessage.of(ERROR, "Configuration missing"));
        }

        ConnectionBulletinBoard bulletinBoard = ConnectionBulletinBoard.of(id, messages);
        getDataManager().set(bulletinBoard);
        return bulletinBoard;
    }
}
