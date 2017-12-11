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
package io.syndesis.rest.v1.handler.connection;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.syndesis.credential.CredentialFlowState;
import io.syndesis.credential.Credentials;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.Kind;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.validation.AllValidations;
import io.syndesis.rest.v1.handler.BaseHandler;
import io.syndesis.rest.v1.operations.Creator;
import io.syndesis.rest.v1.operations.Deleter;
import io.syndesis.rest.v1.operations.Getter;
import io.syndesis.rest.v1.operations.Lister;
import io.syndesis.rest.v1.operations.Updater;
import io.syndesis.rest.v1.operations.Validating;
import io.syndesis.rest.v1.state.ClientSideState;
import io.syndesis.dao.manager.EncryptionComponent;
import io.syndesis.verifier.VerificationConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@Path("/connections")
@Api(value = "connections")
@Component
public class ConnectionHandler extends BaseHandler implements Lister<Connection>, Getter<Connection>,
    Creator<Connection>, Deleter<Connection>, Updater<Connection>, Validating<Connection> {

    private final Credentials credentials;

    private final ClientSideState state;

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    private final Validator validator;

    private final VerificationConfigurationProperties config;
    private final EncryptionComponent encryptionComponent;

    public ConnectionHandler(final DataManager dataMgr, final Validator validator, final Credentials credentials,
                             final ClientSideState state, final VerificationConfigurationProperties config, final EncryptionComponent encryptionComponent) {
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
    public Connection get(final String id) {
        Connection connection = Getter.super.get(id);
        if (connection.getConnectorId().isPresent()) {
            final Connector connector = getDataManager().fetch(Connector.class, connection.getConnectorId().get());
            connection = new Connection.Builder().createFrom(connection).connector(connector).build();
        }
        return connection;
    }

    @Override
    public Connection
        create(@Context SecurityContext sec, @ConvertGroup(from = Default.class, to = AllValidations.class) final Connection connection) {
        final Date rightNow = new Date();

        // Lets make sure we store encrypt secrets.
        Map<String, String> configuredProperties =connection.getConfiguredProperties();
        if( connection.getConnectorId().isPresent() ) {
            Map<String, ConfigurationProperty> connectorProperties = getConnectorProperties(connection.getConnectorId().get());
            configuredProperties = encryptionComponent.encryptPropertyValues(configuredProperties, connectorProperties);
        }

        final Connection updatedConnection = new Connection.Builder()
            .createFrom(connection)
            .createdDate(rightNow)
            .lastUpdated(rightNow)
            .configuredProperties(configuredProperties)
            .userId(sec.getUserPrincipal().getName())
            .build();

        final Set<CredentialFlowState> flowStates = CredentialFlowState.Builder.restoreFrom(state::restoreFrom,
            request);

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
        return getDataManager().fetch(Connector.class, connectorId).getProperties();
    }

    @Override
    public void update(final String id,
        @ConvertGroup(from = Default.class, to = AllValidations.class) final Connection connection) {

        // Lets make sure we store encrypt secrets.
        Map<String, String> configuredProperties =connection.getConfiguredProperties();
        if( connection.getConnectorId().isPresent() ) {
            Map<String, ConfigurationProperty> connectorProperties = getConnectorProperties(connection.getConnectorId().get());
            configuredProperties = encryptionComponent.encryptPropertyValues(configuredProperties, connectorProperties);
        }

        final Connection updatedConnection = new Connection.Builder()
            .createFrom(connection)
            .configuredProperties(configuredProperties)
            .lastUpdated(new Date())
            .build();
        Updater.super.update(id, updatedConnection);
    }

    @Path("/{id}/actions")
    public ConnectionActionHandler credentials(
        @NotNull final @PathParam("id") @ApiParam(required = true, example = "my-connection") String connectionId) {
        final Connection connection = get(connectionId);

        return new ConnectionActionHandler(connection, config, encryptionComponent);
    }

    @Override
    public Validator getValidator() {
        return validator;
    }
}
