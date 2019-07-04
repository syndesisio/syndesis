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
package io.syndesis.common.model.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import io.syndesis.common.model.action.Action.Pattern;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.util.StringConstants;

public class EquivalencerTest implements StringConstants {

    private static class NameTypePair {
        private final String name;
        private final String type;

        public NameTypePair(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String name() {
            return name;
        }

        public String type() {
            return type;
        }
    }

    private static NameTypePair ntPair(String name, String type) {
        return new NameTypePair(name, type);
    }

    private ConnectorAction connectorAction(String name) {
        return new ConnectorAction.Builder()
                     .id("sql-connector")
                     .actionType("connector")
                     .description("Invoke SQL to obtain ...")
                     .name(name)
                     .addTag("dynamic")
                     .pattern(Pattern.To)
                     .build();
    }

    private Connector connector(ConnectorAction connectorAction) {
        return new Connector.Builder()
                      .id("5")
                      .name("sql")
                      .addAction(connectorAction)
                      .build();
    }

    private Connector connectorWithDescription(ConnectorAction connectorAction, String description) {
        return new Connector.Builder()
                      .id("5")
                      .name("sql")
                      .description(description)
                      .addAction(connectorAction)
                      .build();
    }

    private Connection connection(Connector connector) {
        Map<String, String> configuredProperties = new HashMap<>();
        configuredProperties.put("password", "password");
        configuredProperties.put("user", "developer");
        configuredProperties.put("schema", "sampledb");
        configuredProperties.put("url",  "jdbc:postgresql://syndesis-db:5432/sampledb");

        return new Connection.Builder()
                       .id("5")
                       .addTag("dynamic")
                       .configuredProperties(configuredProperties)
                       .connectorId("sql")
                       .description("Connection to Sampledb")
                       .icon("fa-database")
                       .name("PostgresDB")
                       .connector(connector)
                       .build();
    }

    @Test
    public void shouldHaveEquivalentObjects() {
        ConnectorAction action1 = connectorAction("Invoke SQL");
        Connector connector1 = connector(action1);
        Connection conn1 = connection(connector1);
        Integration integration1 = new Integration.Builder()
            .name("myIntegration")
            .addConnection(conn1)
            .build();

        ConnectorAction action2 = connectorAction("Invoke SQL");
        Connector connector2 = connector(action2);
        Connection conn2 = connection(connector2);
        Integration integration2 = new Integration.Builder()
            .name("myIntegration")
            .addConnection(conn2)
            .build();

        Equivalencer equiv = new Equivalencer();
        boolean equivalent = equiv.equivalent(integration1, integration2);
        assertTrue(equivalent);
        assertTrue(equiv.failureMessage().isEmpty());
    }

    private String expectedMessage(String property, Object  value1, Object value2, NameTypePair... ctx) {
        StringBuilder msg = new StringBuilder();
        msg.append(
            "Reason: '" + property + "' is different" + NEW_LINE +
                TAB + "=> " + QUOTE_MARK + value1 + QUOTE_MARK + NEW_LINE +
                TAB + "=> " + QUOTE_MARK + value2 + QUOTE_MARK + NEW_LINE);

        msg.append("Context: ");
        for (int i = 0; i < ctx.length; ++i) {
            NameTypePair ntp = ctx[i];
            msg.append(ntp.type())
                .append(OPEN_BRACKET).append(QUOTE_MARK)
                .append(ntp.name())
                .append(QUOTE_MARK).append(CLOSE_BRACKET);

            if (i < ctx.length - 1) {
                msg.append(SPACE).append(FORWARD_SLASH).append(SPACE);
            }
        }

        return msg.append(NEW_LINE).toString();
    }

    @Test
    public void shouldHaveNonEquivalentObjects1() {
        Integration integration1 = new Integration.Builder()
            .name("myIntegration1")
            .build();

        Integration integration2 = new Integration.Builder()
            .name("myIntegration2")
            .build();

        Equivalencer equiv = new Equivalencer();
        boolean equivalent = equiv.equivalent(integration1, integration2);
        assertFalse(equivalent);
        String message = equiv.failureMessage();

        assertEquals(
                     expectedMessage("name", "myIntegration1", "myIntegration2",
                                     ntPair("myIntegration1", "Integration")),
                     message);
    }

    @Test
    public void shouldHaveNonEquivalentObjects2() {
        ConnectorAction action1 = connectorAction("Invoke SQL");
        Connector connector1 = connector(action1);
        Connection conn1 = connection(connector1);
        Integration integration1 = new Integration.Builder()
            .name("myIntegration")
            .addConnection(conn1)
            .build();

        ConnectorAction action2 = connectorAction("Invoke PL-SQL");
        Connector connector2 = connector(action2);
        Connection conn2 = connection(connector2);
        Integration integration2 = new Integration.Builder()
            .name("myIntegration")
            .addConnection(conn1)
            .addConnection(conn2)
            .build();

        Equivalencer equiv = new Equivalencer();
        boolean equivalent = equiv.equivalent(integration1, integration2);
        assertFalse(equivalent);
        String message = equiv.failureMessage();

        assertEquals(
                     expectedMessage("PostgresDB", "PostgresDB", Equivalencer.NULL,
                                     ntPair("myIntegration", "Integration"),
                                     ntPair("PostgresDB", "Connection")),
                     message);
    }

    @Test
    public void shouldHaveNonEquivalentObjects3() {
        ConnectorAction action1 = connectorAction("Invoke SQL");
        Connector connector1 = connector(action1);
        Connection conn1 = connection(connector1);
        Integration integration1 = new Integration.Builder()
            .name("myIntegration")
            .addConnection(conn1)
            .addConnection(conn1)
            .build();

        ConnectorAction action2 = connectorAction("Invoke PL-SQL");
        Connector connector2 = connector(action2);
        Connection conn2 = connection(connector2);
        Integration integration2 = new Integration.Builder()
            .name("myIntegration")
            .addConnection(conn1)
            .addConnection(conn2)
            .build();

        Equivalencer equiv = new Equivalencer();
        boolean equivalent = equiv.equivalent(integration1, integration2);
        assertFalse(equivalent);
        String message = equiv.failureMessage();

        assertEquals(
                     expectedMessage("name", "Invoke SQL", "Invoke PL-SQL",
                                     ntPair("myIntegration", "Integration"),
                                     ntPair("PostgresDB", "Connection"),
                                     ntPair("sql", "Connector"),
                                     ntPair("Invoke SQL", "ConnectorAction")),
                     message);
    }

    @Test
    public void shouldHaveNonEquivalentObjectsDescription() {
        ConnectorAction action1 = connectorAction("Invoke SQL");
        Connector connector1 = connectorWithDescription(action1, "My accurate description");
        Connection conn1 = connection(connector1);
        Integration integration1 = new Integration.Builder()
            .name("myIntegration")
            .addConnection(conn1)
            .build();

        Connector connector2 = connectorWithDescription(action1, "My new description");
        Connection conn2 = connection(connector2);
        Integration integration2 = new Integration.Builder()
            .name("myIntegration")
            .addConnection(conn2)
            .build();

        Equivalencer equiv = new Equivalencer();
        boolean equivalent = equiv.equivalent(integration1, integration2);
        assertFalse(equivalent);
        String message = equiv.failureMessage();

        assertEquals(
                     expectedMessage("description", "My accurate description", "My new description",
                                     ntPair("myIntegration", "Integration"),
                                     ntPair("PostgresDB", "Connection"),
                                     ntPair("sql", "Connector")),
                     message);
    }
}
