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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import javax.validation.Validator;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.ConnectionOverview;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.server.credential.Credentials;
import io.syndesis.server.dao.manager.DataAccessObject;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.endpoint.v1.state.ClientSideState;
import io.syndesis.server.verifier.MetadataConfigurationProperties;

public class ConnectionHandlerTest {

    private static final Credentials NO_CREDENTIALS = null;

    private static final EncryptionComponent NO_ENCRYPTION_COMPONENT = null;

    private static final ClientSideState NO_STATE = null;

    private static final Validator NO_VALIDATOR = null;

    private static final MetadataConfigurationProperties NO_CONFIG = null;

    private final DataManager dataManager = mock(DataManager.class);

    private final io.syndesis.server.endpoint.v1.handler.connection.ConnectionHandler handler =
                                        new io.syndesis.server.endpoint.v1.handler.connection.ConnectionHandler(
                                                                                                                dataManager,
                                                                                                                NO_VALIDATOR,
                                                                                                                NO_CREDENTIALS,
                                                                                                                NO_STATE,
                                                                                                                NO_CONFIG,
                                                                                                                NO_ENCRYPTION_COMPONENT);

    private String[] connectionIds = { "0", "1", "2" };

    private Connection[] connections;

    private Map<String, Integer> usesMap = new HashMap<>();

    @Before
    public void setup() {
        connections = new Connection[connectionIds.length];

        for (int i = 0; i < connectionIds.length; ++i) {
            connections[i] = newConnection(connectionIds[i]);
        }
    }

    @After
    public void tearDown() {
        connections = null;
        usesMap.clear();
    }

    private List<Step> noSteps() {
        return Collections.emptyList();
    }

    private List<Connection> noConnections() {
        return Collections.emptyList();
    }

    private Connection newConnection(String id) {
        return new Connection.Builder().id(id).build();
    }

    private Step newStep(Connection connection) {
        return new Step.Builder().connection(connection).build();
    }

    private Connection usedConnection(Connection connection, int usage) {
        return new Connection.Builder().createFrom(connection).uses(usage).build();
    }

    private void incrementUsesMap(Connection connection) {
        if (connection == null)
            return;

        String id = connection.getId().get();
        Integer frequency = usesMap.get(id);
        if (frequency == null) {
            frequency = 0;
        }

        usesMap.put(id, ++frequency);
    }

    private Integration newIntegration(List<Connection> connections, List<Step> steps) {
        for (Connection connection : connections) {
            incrementUsesMap(connection);
        }

        for (Step step : steps) {
            Connection connection = step.getConnection().get();
            incrementUsesMap(connection);
        }

        return new Integration.Builder()
            .id("test")
            .name("test")
            .connections(connections)
            .steps(steps)
            .build();
    }

    private Integration[] createTestIntegrations() {
        Step step1 = newStep(connections[0]);
        Step step2 = newStep(connections[1]);
        Step step3 = newStep(connections[2]);

        List<Integration> integrations = new ArrayList<>();
        integrations.add(newIntegration(
                                                  Collections.singletonList(connections[0]),
                                                  Collections.singletonList(step1)));
        integrations.add(newIntegration(
                                                  Arrays.asList(connections[0], connections[1]),
                                                  Arrays.asList(step1, step2)));
        integrations.add(newIntegration(
                                                  Arrays.asList(connections[0], connections[1], connections[2]),
                                                  Arrays.asList(step1, step2, step3)));

        return integrations.toArray(new Integration[0]);
    }

    /**
     * Connections added directly to the integration
     */
    @Test
    public void shouldAugmentWithConnectionUsageNoSteps() {

        Integration integration1 = newIntegration(Collections.singletonList(connections[0]), noSteps());
        Integration integration2 = newIntegration(Arrays.asList(connections[0], connections[1]), noSteps());
        Integration integration3 = newIntegration(Arrays.asList(connections[0], connections[1], connections[2]), noSteps());

        when(dataManager.fetchAll(Integration.class))
            .thenReturn(new ListResult.Builder<Integration>().addItem(integration1, integration2, integration3).build());

        final List<Connection> augmented = handler.augmentedWithUsage(Arrays.asList(connections[0], connections[1], connections[2]));

        assertThat(augmented).contains(
                                       usedConnection(connections[0], usesMap.get(connectionIds[0])),
                                       usedConnection(connections[1], usesMap.get(connectionIds[1])),
                                       usedConnection(connections[2], usesMap.get(connectionIds[2])));
    }

    /**
     * Connections added to the steps inside the integration
     */
    @Test
    public void shouldAugmentWithConnectionUsageNoDirect() {
        Step step1 = newStep(connections[0]);
        Step step2 = newStep(connections[1]);
        Step step3 = newStep(connections[2]);

        Integration integration1 = newIntegration(noConnections(), Collections.singletonList(step1));
        Integration integration2 = newIntegration(noConnections(), Arrays.asList(step1, step2));
        Integration integration3 = newIntegration(noConnections(), Arrays.asList(step1, step2, step3));

        when(dataManager.fetchAll(Integration.class))
            .thenReturn(new ListResult.Builder<Integration>().addItem(integration1, integration2, integration3).build());

        final List<Connection> augmented = handler.augmentedWithUsage(Arrays.asList(connections[0], connections[1], connections[2]));

        assertThat(augmented).contains(
                                       usedConnection(connections[0], usesMap.get(connectionIds[0])),
                                       usedConnection(connections[1], usesMap.get(connectionIds[1])),
                                       usedConnection(connections[2], usesMap.get(connectionIds[2])));
    }

    /**
     * Connections added to both directly and to the steps inside the integration
     */
    @Test
    public void shouldAugmentWithConnectionUsageDirectAndSteps() {
        Integration[] integrations = createTestIntegrations();

        when(dataManager.fetchAll(Integration.class))
            .thenReturn(new ListResult.Builder<Integration>().addItem(integrations[0], integrations[1], integrations[2]).build());

        final List<Connection> augmented = handler.augmentedWithUsage(Arrays.asList(connections[0], connections[1], connections[2]));

        assertThat(augmented).contains(
                                       usedConnection(connections[0], usesMap.get(connectionIds[0])),
                                       usedConnection(connections[1], usesMap.get(connectionIds[1])),
                                       usedConnection(connections[2], usesMap.get(connectionIds[2])));
    }

    @Test
    public void overviewGetShouldAugmentWithConnectionUsage() {
        Integration[] integrations = createTestIntegrations();

        when(dataManager.fetchAll(Integration.class))
                        .thenReturn(new ListResult.Builder<Integration>().addItem(integrations[0], integrations[1], integrations[2]).build());
        for (int i = 0; i < connectionIds.length; ++i) {
            when(dataManager.fetch(Connection.class, connectionIds[i])).thenReturn(connections[i]);
        }

        for (int i = 0; i < connectionIds.length; ++i) {
            String id = connectionIds[i];
            ConnectionOverview overview = handler.get(id);
            assertNotNull(overview);
            OptionalInt uses = overview.getUses();
            assertThat(uses.isPresent()).isTrue();
            assertThat(uses.getAsInt()).isEqualTo(usesMap.get(id));
        }
    }

    @Test
    public void overviewGetShouldAugmentWithConnectionUsageNoIntegrations() {
        when(dataManager.fetchAll(Integration.class))
                        .thenReturn(new ListResult.Builder<Integration>().build());
        for (int i = 0; i < connectionIds.length; ++i) {
            when(dataManager.fetch(Connection.class, connectionIds[i])).thenReturn(connections[i]);
        }

        for (int i = 0; i < connectionIds.length; ++i) {
            String id = connectionIds[i];
            ConnectionOverview overview = handler.get(id);
            assertNotNull(overview);
            OptionalInt uses = overview.getUses();
            assertThat(uses.isPresent()).isTrue();
            assertThat(uses.getAsInt()).isZero();
        }
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void overviewListShouldAugmentWithConnectionUsage() {
        Integration[] integrations = createTestIntegrations();

        ListResult<Connection> connectionResult = new ListResult.Builder<Connection>()
                                                                                            .addItem(connections[0], connections[1], connections[2])
                                                                                            .build();

        DataAccessObject<Connection> dao = mock(DataAccessObject.class);
        when(dao.fetchAll(Mockito.any())).thenReturn(connectionResult);
        when(dataManager.getDataAccessObject(Connection.class)).thenReturn(dao);

        // Matches the appearances of each connection in the above integrations
        ListResult<Integration> integrationResult = new ListResult.Builder<Integration>()
                                                                                            .addItem(integrations[0], integrations[1], integrations[2])
                                                                                            .build();

        when(dataManager.fetchAll(Integration.class)).thenReturn(integrationResult);

        UriInfo uriInfo = mock(UriInfo.class);
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(uriInfo.getQueryParameters()).thenReturn(params);

        ListResult<ConnectionOverview> overviewResult = handler.list(uriInfo);
        assertNotNull(overviewResult);
        assertThat(overviewResult.getTotalCount()).isPositive();

        List<ConnectionOverview> items = overviewResult.getItems();
        for (int i = 0; i < items.size(); ++i) {
            ConnectionOverview overview = items.get(i);
            OptionalInt uses = overview.getUses();
            assertThat(uses.isPresent()).isTrue();
            assertThat(uses.getAsInt()).isEqualTo(usesMap.get(overview.getId().get()));
        }
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void overviewListShouldAugmentWithConnectionUsageNoIntegrations() {
        ListResult<Connection> connectionResult = new ListResult.Builder<Connection>()
                                                                                            .addItem(connections[0], connections[1], connections[2])
                                                                                            .build();

        DataAccessObject<Connection> dao = mock(DataAccessObject.class);
        when(dao.fetchAll(Mockito.any())).thenReturn(connectionResult);
        when(dataManager.getDataAccessObject(Connection.class)).thenReturn(dao);
        when(dataManager.fetchAll(Integration.class)).thenReturn(new ListResult.Builder<Integration>().build());

        UriInfo uriInfo = mock(UriInfo.class);
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(uriInfo.getQueryParameters()).thenReturn(params);

        ListResult<ConnectionOverview> overviewResult = handler.list(uriInfo);
        assertNotNull(overviewResult);
        assertThat(overviewResult.getTotalCount()).isPositive();

        List<ConnectionOverview> items = overviewResult.getItems();
        for (int i = 0; i < items.size(); ++i) {
            ConnectionOverview overview = items.get(i);
            OptionalInt uses = overview.getUses();
            assertThat(uses.isPresent()).isTrue();
            assertThat(uses.getAsInt()).isZero();
        }
    }
}
