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
        boolean equivalent = equiv.equivalent(null, integration1, integration2);
        assertTrue(equivalent);
        assertTrue(equiv.message().isEmpty());
    }

    @Test
    public void shouldHaveNonEquivalentObjects() {
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
            .addConnection(conn2)
            .build();

        Equivalencer equiv = new Equivalencer();
        boolean equivalent = equiv.equivalent(null, integration1, integration2);
        assertFalse(equivalent);
        assertEquals(
                     integration1.getName() + COLON + "Integration" +
                     SPACE + CLOSE_ANGLE_BRACKET + SPACE +
                     conn1.getName() + COLON + "Connection" +
                     SPACE + CLOSE_ANGLE_BRACKET + SPACE +
                     connector1.getName() + COLON + "Connector" +
                     SPACE + CLOSE_ANGLE_BRACKET + SPACE +
                     action1.getName() + COLON + "ConnectorAction" +
                     DOLLAR_SIGN + "name" + NEW_LINE +
                     TAB + "=> 'Invoke SQL'" + NEW_LINE +
                     TAB + "=> 'Invoke PL-SQL'" + NEW_LINE,
                     equiv.message());
    }
}
