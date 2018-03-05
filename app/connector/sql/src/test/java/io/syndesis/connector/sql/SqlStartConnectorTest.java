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
package io.syndesis.connector.sql;

import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import io.syndesis.connector.sql.common.JSONBeanUtil;
import io.syndesis.connector.sql.util.SqlConnectorTestSupport;
import io.syndesis.common.model.integration.Step;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.util.ObjectHelper;
import org.junit.Test;

@SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.JUnitTestsShouldIncludeAssert"})
public class SqlStartConnectorTest extends SqlConnectorTestSupport {

    // **************************
    // Set up
    // **************************

    @Override
    protected void doPreSetup() throws Exception {
        try (Statement stmt = db.connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE NAME (id INTEGER PRIMARY KEY, firstName VARCHAR(255), lastName VARCHAR(255))");
            stmt.executeUpdate("INSERT INTO NAME VALUES (1, 'Joe', 'Jackson')");
            stmt.executeUpdate("INSERT INTO NAME VALUES (2, 'Roger', 'Waters')");
        }
    }

    @Override
    protected List<Step> createSteps() {
        return Arrays.asList(
            newSimpleEndpointStep(
                "direct",
                builder -> builder.putConfiguredProperty("name", "start")),
            newSqlEndpointStep(
                "sql-start-connector",
                builder -> builder.putConfiguredProperty("query", "SELECT * FROM NAME ORDER BY id")),
            newSimpleEndpointStep(
                "log",
                builder -> builder.putConfiguredProperty("loggerName", "test")),
            newSimpleEndpointStep(
                "mock",
                builder -> builder.putConfiguredProperty("name", "result"))
        );
    }

    // **************************
    // Tests
    // **************************

    @Test
    public void sqlStartConnectorTest() throws Exception {
        MockEndpoint mock = context.getEndpoint("mock:result", MockEndpoint.class);
        mock.expectedMessageCount(2);
        mock.expectedMessagesMatches(
            exchange -> validateProperty(exchange, "ID", "1"),
            exchange -> validateProperty(exchange, "ID", "2")
        );

        ProducerTemplate template = context.createProducerTemplate();
        template.sendBody("direct:start", null);

        mock.assertIsSatisfied();
    }

    // **************************
    // Helpers
    // **************************

    private boolean validateProperty(Exchange exchange, String propertyName, String expectedValue) {
        String body = exchange.getIn().getBody(String.class);
        Properties props = JSONBeanUtil.parsePropertiesFromJSONBean(body);

        return ObjectHelper.equal(props.getProperty(propertyName), expectedValue);
    }
}
