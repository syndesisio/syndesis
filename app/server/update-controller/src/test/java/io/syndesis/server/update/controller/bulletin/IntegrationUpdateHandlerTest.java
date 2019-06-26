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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.validation.Validator;
import org.junit.Test;
import io.syndesis.common.model.ChangeEvent;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.action.Action.Pattern;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.bulletin.IntegrationBulletinBoard;
import io.syndesis.common.model.bulletin.LeveledMessage;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.support.Equivalencer;
import io.syndesis.common.util.StringConstants;
import io.syndesis.server.dao.manager.DataManager;

public class IntegrationUpdateHandlerTest implements StringConstants {

    private static final String SQL_CONNECTOR_ACTION_ID = "sql-connector";

    private static final String CONNECTION_ID = "5";

    private static final String CONNECTOR_ID = "sql";

    private static final String SQL_CONNECTOR_NAME = "SQL Connector";

    private static final long INTEGRATION_CREATED_AT = 1533024000000L;  // 31/07/2018 09:00:00

    private static final long INTEGRATION_UPDATED_AT = 1533024600000L; // 31/07/2018 09:10:00

    private static final long DEPLOYMENT_CREATED_AT = 1533027600000L; // 31/07/2018 10:00:00

    private static final long DEPLOYMENT_UPDATED_AT = 1533028200000L; // 31/07/2018 10:10:00

    final DataManager dataManager = mock(DataManager.class);

    final Validator validator = mock(Validator.class);

    private Connector newSqlConnector() {
        ConnectorAction action1 = new ConnectorAction.Builder()
            .id(SQL_CONNECTOR_ACTION_ID)
            .actionType("connector")
            .description("Invoke SQL to obtain ...")
            .name("Invoke SQL")
            .addTag("dynamic")
            .pattern(Pattern.To)
            .build();

        return new Connector.Builder()
           .id(CONNECTOR_ID)
           .name(SQL_CONNECTOR_NAME)
           .addAction(action1)
           .build();
    }

    private Connection newSqlConnection(Connector connector) {
        assertNotNull(connector);

        Map<String, String> configuredProperties = new HashMap<>();
        configuredProperties.put("password", "password");
        configuredProperties.put("user", "developer");
        configuredProperties.put("schema", "sampledb");
        configuredProperties.put("url",  "jdbc:postgresql://syndesis-db:5432/sampledb");

        return new Connection.Builder()
            .id(CONNECTION_ID)
            .addTag("dynamic")
            .configuredProperties(configuredProperties)
            .connector(connector)
            .connectorId("sql")
            .description("Connection to Sampledb")
            .icon("fa-database")
            .name("PostgresDB")
            .build();
    }

    private Step newSqlStep(Connection connection) {
        ConnectorAction action = new ConnectorAction.Builder()
            .actionType("connector")
            .id(SQL_CONNECTOR_ACTION_ID)
            .name("Invoke SQL")
            .pattern(Pattern.To)
            .addTag("dynamic")
            .build();

        return new Step.Builder()
            .connection(connection)
            .id("SomeLongId")
            .action(action)
            .build();
    }

    private Integration newSqlIntegration(String id, Connection connection) {
        return new Integration.Builder()
            .id(id)
            .name(id)
            .addFlow(new Flow.Builder().id(id + ":flow").addStep(newSqlStep(connection)).build())
            .createdAt(INTEGRATION_CREATED_AT)
            .updatedAt(INTEGRATION_UPDATED_AT)
            .build();
    }

    private IntegrationDeployment newIntegrationDeployment(String id, int version, Integration integration, boolean published) {
        Map<String, String> stepsDone = new HashMap<>();
        stepsDone.put("buildv1", "ip:port/syndesis/buildName");
        stepsDone.put("deploy", "2");

        IntegrationDeployment.Builder builder = new IntegrationDeployment.Builder()
            .id(id + ":" + version)
            .createdAt(DEPLOYMENT_CREATED_AT)
            .spec(integration)
            .integrationId(Optional.of(id))
            .stepsDone(stepsDone)
            .targetState(IntegrationDeploymentState.Published)
            .userId("developer")
            .version(version)
            .updatedAt(DEPLOYMENT_UPDATED_AT);

        if (published) {
            builder = builder.currentState(IntegrationDeploymentState.Published);
        }

        return builder.build();
    }

    /**
     * * Connection is created
     * * Integration is created from Connection
     * * Deployment is created from Integration
     * * The connection in all the above remains equivalent
     */
    @Test
    public void shouldComputeSameIntegrationAndDeployment() {
        final IntegrationUpdateHandler updateHandler = new IntegrationUpdateHandler(dataManager, null, validator);

        // integration
        String id = "MyTestIntegration-x123456";

        Connector sqlConnector = newSqlConnector();

        Connection sqlConnection = newSqlConnection(sqlConnector);
        Integration sqlIntegration = newSqlIntegration(id, sqlConnection);
        IntegrationDeployment integrationDeployment = newIntegrationDeployment(id, 1, sqlIntegration, true);

        // Returns the unchanged sql connection
        when(dataManager.fetchAll(Connection.class)).thenReturn(
                                                                new ListResult.Builder<Connection>()
                                                                .addItem(sqlConnection).build());
        when(dataManager.fetch(Connection.class, CONNECTION_ID)).thenReturn(sqlConnection);
        when(dataManager.fetch(Connector.class, CONNECTOR_ID)).thenReturn(sqlConnector);

        // Returns the integration deployment
        when(dataManager.fetchAll(IntegrationDeployment.class)).thenReturn(
                                                                new ListResult.Builder<IntegrationDeployment>()
                                                                .addItem(integrationDeployment).build());
        when(dataManager.fetch(IntegrationDeployment.class, id)).thenReturn(integrationDeployment);

        Set<String> ids = Collections.singleton(id);
        when(dataManager.fetchIdsByPropertyValue(IntegrationDeployment.class, "integrationId", id)).thenReturn(ids);

        // Returns the unchanged integration
        when(dataManager.fetchAll(Integration.class))
            .thenReturn(new ListResult.Builder<Integration>().addItem(sqlIntegration).build());

        ChangeEvent event = new ChangeEvent.Builder()
            .action("updated")
            .id(CONNECTION_ID)
            .kind("connection")
            .build();

        List<IntegrationBulletinBoard> boards = updateHandler.compute(event);
        assertFalse(boards.isEmpty());
        assertEquals(1, boards.size());

        IntegrationBulletinBoard board = boards.get(0);
        assertTrue(board.getMessages().isEmpty());
    }

    /**
     * Connection is modified after an integration is drafted and deployed
     * * DataManager returns the modified connection
     * * DataManager returns the original integration
     * * DataManager returns the original deployment
     *
     * Both the integration and deployment and computed to be stale
     */
    @Test
    public void shouldComputeStaleIntegrationAndDeployment() {
        final IntegrationUpdateHandler updateHandler = new IntegrationUpdateHandler(dataManager, null, validator);

        // integration
        String id = "MyTestIntegration-x123456";
        Connector sqlConnector = newSqlConnector();
        Connection sqlConnection = newSqlConnection(sqlConnector);
        Integration sqlIntegration = newSqlIntegration(id, sqlConnection);
        IntegrationDeployment integrationDeployment = newIntegrationDeployment(id, 1, sqlIntegration, true);

        String connectorNewName = SQL_CONNECTOR_NAME + "_new";

        Map<String, String> properties = new HashMap<>();
        properties.put("blah", "blah");

        Connector modSqlConnector = new Connector.Builder()
            .createFrom(sqlConnector)
            .configuredProperties(properties)
            .name(connectorNewName)
            .build();

        Equivalencer equiv = new Equivalencer();
        assertFalse(equiv.equivalent(sqlConnector, modSqlConnector));

        Connection modSqlConnection = new Connection.Builder()
            .createFrom(sqlConnection)
            .connector(modSqlConnector)
            .build();

        assertNotEquals(sqlConnection, modSqlConnection);

        // Returns the changed sql connection
        when(dataManager.fetchAll(Connection.class)).thenReturn(
                                                                           new ListResult.Builder<Connection>()
                                                                           .addItem(modSqlConnection).build());
        when(dataManager.fetch(Connection.class, CONNECTION_ID)).thenReturn(modSqlConnection);
        when(dataManager.fetch(Connector.class, CONNECTOR_ID)).thenReturn(modSqlConnector);

        // Returns the integration deployment
        when(dataManager.fetchAll(IntegrationDeployment.class)).thenReturn(
                                                                           new ListResult.Builder<IntegrationDeployment>()
                                                                           .addItem(integrationDeployment).build());
        when(dataManager.fetch(IntegrationDeployment.class, id)).thenReturn(integrationDeployment);

        Set<String> ids = Collections.singleton(id);
        when(dataManager.fetchIdsByPropertyValue(IntegrationDeployment.class, "integrationId", id)).thenReturn(ids);

        // Returns the ORIGINAL integration
        when(dataManager.fetchAll(Integration.class)).thenReturn(new ListResult.Builder<Integration>().addItem(sqlIntegration).build());

        ChangeEvent event = new ChangeEvent.Builder()
            .action("updated")
            .id(CONNECTION_ID)
            .kind("connection")
            .build();

        List<IntegrationBulletinBoard> boards = updateHandler.compute(event);
        assertFalse(boards.isEmpty());
        assertEquals(1, boards.size());

        IntegrationBulletinBoard board = boards.get(0);
        List<LeveledMessage> messages = board.getMessages();
        assertFalse(messages.isEmpty());
        assertEquals(1, messages.size());

        messages.forEach(message -> {
            switch (message.getCode()) {
                case SYNDESIS011:
                    Optional<String> detail = message.getDetail();
                    assertTrue(detail.isPresent());
                    String msg = detail.get();
                    assertEquals(
                                 "Reason: 'name' is different" + NEW_LINE +
                                 TAB + "=> 'SQL Connector'" + NEW_LINE +
                                 TAB + "=> 'SQL Connector_new'" + NEW_LINE +
                                 "Context: Connection('PostgresDB') / Connector('SQL Connector')" + NEW_LINE,
                                 msg);
                    break;
                case SYNDESIS012:
                    break;
                default:
                    fail("Expecting syndesis codes 11 & 12 only");
            }
        });
    }

    /**
     * Connection is modified after an integration is drafted and deployed
     * * DataManager returns the modified connection
     * * DataManager returns the modified integration
     * * DataManager returns the original deployment
     *
     * Only the deployment is stale while the modified integration is fine.
     */
    @Test
    public void shouldComputeStaleDeploymentOnly() {
        final IntegrationUpdateHandler updateHandler = new IntegrationUpdateHandler(dataManager, null, validator);

        // integration
        String id = "MyTestIntegration-x123456";
        Connector sqlConnector = newSqlConnector();
        Connection sqlConnection = newSqlConnection(sqlConnector);
        Integration sqlIntegration = newSqlIntegration(id, sqlConnection);
        IntegrationDeployment integrationDeployment = newIntegrationDeployment(id, 1, sqlIntegration, true);

        String connectorNewName = SQL_CONNECTOR_NAME + "_new";

        Map<String, String> properties = new HashMap<>();
        properties.put("blah", "blah");

        Connector modSqlConnector = new Connector.Builder()
            .createFrom(sqlConnector)
            .configuredProperties(properties)
            .name(connectorNewName)
            .build();

        Equivalencer equiv = new Equivalencer();
        assertFalse(equiv.equivalent(sqlConnector, modSqlConnector));

        Connection modSqlConnection = new Connection.Builder()
            .createFrom(sqlConnection)
            .connector(modSqlConnector)
            .build();

        assertNotEquals(sqlConnection, modSqlConnection);

        Step modStep = new Step.Builder()
          .createFrom(sqlIntegration.getFlows().get(0).getSteps().get(0))
          .connection(modSqlConnection)
          .build();

        Integration modSqlIntegration = new Integration.Builder()
            .createFrom(sqlIntegration)
            .flows(Collections.singleton(new Flow.Builder().id(id + ":flow").addStep(modStep).build()))
            .build();

        equiv = new Equivalencer();
        assertFalse(equiv.equivalent(sqlIntegration, modSqlIntegration));

        // Returns the changed sql connection
        when(dataManager.fetchAll(Connection.class)).thenReturn(
                                                                new ListResult.Builder<Connection>()
                                                                .addItem(modSqlConnection).build());
        when(dataManager.fetch(Connection.class, CONNECTION_ID)).thenReturn(modSqlConnection);
        when(dataManager.fetch(Connector.class, CONNECTOR_ID)).thenReturn(modSqlConnector);

        // Returns the integration deployment
        when(dataManager.fetchAll(IntegrationDeployment.class)).thenReturn(
                                                                new ListResult.Builder<IntegrationDeployment>()
                                                                .addItem(integrationDeployment).build());
        when(dataManager.fetch(IntegrationDeployment.class, id)).thenReturn(integrationDeployment);

        Set<String> ids = Collections.singleton(id);
        when(dataManager.fetchIdsByPropertyValue(IntegrationDeployment.class, "integrationId", id)).thenReturn(ids);

        // Returns the MODIFIED integration
        when(dataManager.fetchAll(Integration.class)).thenReturn(new ListResult.Builder<Integration>().addItem(modSqlIntegration).build());

        ChangeEvent event = new ChangeEvent.Builder()
            .action("updated")
            .id(CONNECTION_ID)
            .kind("connection")
            .build();

        List<IntegrationBulletinBoard> boards = updateHandler.compute(event);
        assertFalse(boards.isEmpty());
        assertEquals(1, boards.size());

        IntegrationBulletinBoard board = boards.get(0);
        List<LeveledMessage> messages = board.getMessages();
        assertFalse(messages.isEmpty());
        assertEquals(1, messages.size());

        messages.forEach(message -> {
            switch (message.getCode()) {
                case SYNDESIS012:
                    break;
                default:
                    fail("Expecting syndesis code 12 only");
            }
        });
    }

    /**
     * connection is updated in the integration after a deployment is created but not published
     */
    @Test
    public void shouldComputeComparisonForModifiedIntegrationAndUnpubishedDeployment() {
        final IntegrationUpdateHandler updateHandler = new IntegrationUpdateHandler(dataManager, null, validator);

        // integration
        String id = "MyTestIntegration-x123456";
        Connector sqlConnector = newSqlConnector();
        Connection sqlConnection = newSqlConnection(sqlConnector);
        Integration sqlIntegration = newSqlIntegration(id, sqlConnection);
        IntegrationDeployment integrationDeployment = newIntegrationDeployment(id, 1, sqlIntegration, false);

        String connectorNewName = SQL_CONNECTOR_NAME + "_new";

        Map<String, String> properties = new HashMap<>();
        properties.put("blah", "blah");

        Connector modSqlConnector = new Connector.Builder()
            .createFrom(sqlConnector)
            .configuredProperties(properties)
            .name(connectorNewName)
            .build();

        Equivalencer equiv = new Equivalencer();
        assertFalse(equiv.equivalent(sqlConnector, modSqlConnector));

        Connection modSqlConnection = new Connection.Builder()
            .createFrom(sqlConnection)
            .connector(modSqlConnector)
            .build();

        equiv = new Equivalencer();
        assertFalse(equiv.equivalent(sqlConnection, modSqlConnection));

        Step modStep = new Step.Builder()
            .createFrom(sqlIntegration.getFlows().get(0).getSteps().get(0))
            .connection(modSqlConnection)
            .build();

        Integration modSqlIntegration = new Integration.Builder()
            .createFrom(sqlIntegration)
            .flows(Collections.singleton(new Flow.Builder().steps(Collections.singletonList(modStep)).build()))
            .build();

        equiv = new Equivalencer();
        assertFalse(equiv.equivalent(sqlIntegration, modSqlIntegration));

        // Returns the changed sql connection
        when(dataManager.fetchAll(Connection.class)).thenReturn(
                                                                new ListResult.Builder<Connection>()
                                                                .addItem(modSqlConnection).build());
        when(dataManager.fetch(Connection.class, CONNECTION_ID)).thenReturn(modSqlConnection);
        when(dataManager.fetch(Connector.class, CONNECTOR_ID)).thenReturn(modSqlConnector);

        // Returns the integration deployment
        when(dataManager.fetchAll(IntegrationDeployment.class)).thenReturn(
                                                                new ListResult.Builder<IntegrationDeployment>()
                                                                .addItem(integrationDeployment).build());
        when(dataManager.fetch(IntegrationDeployment.class, id)).thenReturn(integrationDeployment);

        Set<String> ids = Collections.singleton(id);
        when(dataManager.fetchIdsByPropertyValue(IntegrationDeployment.class, "integrationId", id)).thenReturn(ids);

        // Returns the changed integration
        when(dataManager.fetchAll(Integration.class)).thenReturn(new ListResult.Builder<Integration>().addItem(modSqlIntegration).build());

        ChangeEvent event = new ChangeEvent.Builder()
            .action("updated")
            .id(CONNECTION_ID)
            .kind("connection")
            .build();

        List<IntegrationBulletinBoard> boards = updateHandler.compute(event);
        assertFalse(boards.isEmpty());
        assertEquals(1, boards.size());

        //
        // Will not compute differences for the deployment since it is unpublished
        //
        IntegrationBulletinBoard board = boards.get(0);
        List<LeveledMessage> messages = board.getMessages();
        assertTrue(messages.isEmpty());
    }
}
