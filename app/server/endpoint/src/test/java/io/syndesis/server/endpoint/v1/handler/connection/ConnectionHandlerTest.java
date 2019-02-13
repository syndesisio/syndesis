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

import javax.validation.Validator;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.ConnectionOverview;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.server.credential.Credentials;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.endpoint.v1.state.ClientSideState;
import io.syndesis.server.verifier.MetadataConfigurationProperties;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectionHandlerTest {

    private static final Credentials NO_CREDENTIALS = null;

    private static final EncryptionComponent NO_ENCRYPTION_COMPONENT = null;

    private static final ClientSideState NO_STATE = null;

    private static final Validator NO_VALIDATOR = null;

    private static final MetadataConfigurationProperties NO_CONFIG = null;

    private final DataManager dataManager = mock(DataManager.class);

    private final ConnectionHandler handler = new ConnectionHandler(dataManager, NO_VALIDATOR, NO_CREDENTIALS, NO_STATE, NO_CONFIG, NO_ENCRYPTION_COMPONENT);

    private Connection c1 = newConnectionWithId("c1");

    private Connection c2 = newConnectionWithId("c2");

    private Connection c3 = newConnectionWithId("c3");

    @Test
    public void withNoIntegrationsConnectionUsageShouldBeZero() {
        when(dataManager.fetchAll(Integration.class)).thenReturn(ListResult.of(emptyList()));

        assertThat(handler.augmentedWithUsage(c1)).isEqualTo(new Connection.Builder().createFrom(c1).uses(0).build());
    }

    @Test
    public void unusedConnectionsShouldHaveUseOfZero() {
        final Integration emptyIntegration = new Integration.Builder().build();
        when(dataManager.fetchAll(Integration.class)).thenReturn(ListResult.of(emptyIntegration, emptyIntegration));

        assertThat(handler.augmentedWithUsage(c1)).isEqualTo(new Connection.Builder().createFrom(c1).uses(0).build());
    }

    @Test
    public void connectionsReferencedFromTheIntegrationShouldHaveTheirUseCounted() {
        final Integration usesC1 = testIntegration().withFlowConnections(c1).build();
        final Integration usesC1andC2 = testIntegration().withFlowConnections(c1, c2).build();
        final Integration usesC2andC3 = testIntegration().withFlowConnections(c2, c3).build();
        final Integration usesC1andC2andC3 = testIntegration().withFlowConnections(c1, c2, c3).build();
        when(dataManager.fetchAll(Integration.class)).thenReturn(ListResult.of(usesC1, usesC1andC2, usesC2andC3, usesC1andC2andC3));

        assertThat(handler.augmentedWithUsage(c1)).isEqualTo(connectionUsed(c1, 3));
        assertThat(handler.augmentedWithUsage(c2)).isEqualTo(connectionUsed(c2, 3));
        assertThat(handler.augmentedWithUsage(c3)).isEqualTo(connectionUsed(c3, 2));

        assertThat(handler.augmentedWithUsage(asList(c1, c2, c3))).containsOnly(connectionUsed(c1, 3), connectionUsed(c2, 3), connectionUsed(c3, 2));
    }

    @Test
    public void connectionsReferencedFromTheStepsShouldHaveTheirUseCounted() {
        final Integration usesC1 = testIntegration().withFlowStepsUsingConnections(c1).build();
        final Integration usesC1andC2 = testIntegration().withFlowStepsUsingConnections(c1, c2).build();
        final Integration usesC2andC3 = testIntegration().withFlowStepsUsingConnections(c2, c3).build();
        final Integration usesC1andC2andC3 = testIntegration().withFlowStepsUsingConnections(c1, c2, c3).build();
        when(dataManager.fetchAll(Integration.class)).thenReturn(ListResult.of(usesC1, usesC1andC2, usesC2andC3, usesC1andC2andC3));

        assertThat(handler.augmentedWithUsage(c1)).isEqualTo(connectionUsed(c1, 3));
        assertThat(handler.augmentedWithUsage(c2)).isEqualTo(connectionUsed(c2, 3));
        assertThat(handler.augmentedWithUsage(c3)).isEqualTo(connectionUsed(c3, 2));

        assertThat(handler.augmentedWithUsage(asList(c1, c2, c3))).containsOnly(connectionUsed(c1, 3), connectionUsed(c2, 3), connectionUsed(c3, 2));
    }

    @Test
    public void mixedUseOfConnectionsFromIntegrationsAndStepsShouldBeCounted() {
        final Integration usesC1 = testIntegration().withFlowConnections(c1).build();
        final Integration usesC1andC2 = testIntegration().withFlowConnections(c1).withFlowStepsUsingConnections(c2).build();
        final Integration usesC2andC3 = testIntegration().withFlowStepsUsingConnections(c2, c3).build();
        final Integration usesC1andC2andC3 = testIntegration().withFlowConnections(c1, c2).withFlowStepsUsingConnections(c3).build();
        when(dataManager.fetchAll(Integration.class)).thenReturn(ListResult.of(usesC1, usesC1andC2, usesC2andC3, usesC1andC2andC3));

        assertThat(handler.augmentedWithUsage(c1)).isEqualTo(connectionUsed(c1, 3));
        assertThat(handler.augmentedWithUsage(c2)).isEqualTo(connectionUsed(c2, 3));
        assertThat(handler.augmentedWithUsage(c3)).isEqualTo(connectionUsed(c3, 2));

        assertThat(handler.augmentedWithUsage(asList(c1, c2, c3))).containsOnly(connectionUsed(c1, 3), connectionUsed(c2, 3), connectionUsed(c3, 2));
    }

    @Test
    public void someStepsDoNotUseConnectionsAndShouldNotBeConsidered() {
        final Step stepWithoutConnection = new Step.Builder().build();
        final Integration integration = testIntegration().withFlowConnections(c1, c2).withFlowStepsUsingConnections(c1, c3).addFlow(new Flow.Builder().addStep(stepWithoutConnection).build()).build();
        when(dataManager.fetchAll(Integration.class)).thenReturn(ListResult.of(integration));

        assertThat(handler.augmentedWithUsage(c1)).isEqualTo(connectionUsed(c1, 2));
        assertThat(handler.augmentedWithUsage(c2)).isEqualTo(connectionUsed(c2, 1));
        assertThat(handler.augmentedWithUsage(c3)).isEqualTo(connectionUsed(c3, 1));

        assertThat(handler.augmentedWithUsage(asList(c1, c2, c3))).containsOnly(connectionUsed(c1, 2), connectionUsed(c2, 1), connectionUsed(c3, 1));
    }

    @Test
    public void overviewGetShouldAugmentWithConnectionUsage() {
        final Step stepWithoutConnection = new Step.Builder().build();
        final Integration usesC1 = testIntegration().withFlowConnections(c1).build();
        final Integration usesC1andC2 = testIntegration().withFlowConnections(c1, c2).build();
        final Integration usesC1andC2andC3 = testIntegration().withFlowConnections(c1, c2).withFlowStepsUsingConnections(c1, c3).addFlow(new Flow.Builder().addStep(stepWithoutConnection).build()).build();

        when(dataManager.fetchAll(Integration.class)).thenReturn(ListResult.of(usesC1, usesC1andC2, usesC1andC2andC3));
        when(dataManager.fetch(Connection.class, "c1")).thenReturn(c1);

        final ConnectionOverview overview = handler.get("c1");
        assertThat(overview).isNotNull();
        assertThat(overview.getUses()).isPresent();
        assertThat(overview.getUses()).hasValue(4);
    }

    @Test
    public void connectionsUsedInDeletedIntegrationsShouldNotBeCountedAsUsed() {
        final Integration integration1 = testIntegration().withFlowConnections(c1).isDeleted(true).build();
        final Integration integration2 = testIntegration().withFlowStepsUsingConnections(c1).isDeleted(true).build();
        final Integration integration3 = testIntegration().addConnection(c1).isDeleted(true).build();

        when(dataManager.fetchAll(Integration.class)).thenReturn(ListResult.of(integration1, integration2, integration3));
        when(dataManager.fetch(Connection.class, "c1")).thenReturn(c1);

        final ConnectionOverview overview = handler.get("c1");
        assertThat(overview).isNotNull();
        assertThat(overview.getUses()).isPresent();
        assertThat(overview.getUses()).hasValue(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void overviewListShouldAugmentWithConnectionUsageNoIntegrations() {
        ListResult<Connection> connectionResult = new ListResult.Builder<Connection>().addItems(c1, c2, c3).build();

        when(dataManager.fetchAll(eq(Connection.class), any())).thenReturn(connectionResult);
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

    private static Connection newConnectionWithId(final String id) {
        return new Connection.Builder().id(id).build();
    }

    private static Connection connectionUsed(final Connection connection, final int times) {
        return new Connection.Builder().createFrom(connection).uses(times).build();
    }

    private static TestIntegrationBuilder testIntegration() {
        return new TestIntegrationBuilder();
    }

    static class TestIntegrationBuilder extends Integration.Builder {
        TestIntegrationBuilder withFlowConnections(final Connection... connections) {
            return (TestIntegrationBuilder) addFlow(new Flow.Builder().addConnections(connections).build());
        }

        TestIntegrationBuilder withFlowStepsUsingConnections(final Connection... connections) {
            return (TestIntegrationBuilder) addFlow(
                new Flow.Builder().addAllSteps(Arrays.stream(connections).map(c -> new Step.Builder().connection(c).build()).collect(Collectors.toList())).build());
        }

    }

}
