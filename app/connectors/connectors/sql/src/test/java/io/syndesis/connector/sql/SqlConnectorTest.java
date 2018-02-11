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
import io.syndesis.model.integration.Step;
import org.assertj.core.api.Assertions;
import org.junit.Test;

@SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.JUnitTestsShouldIncludeAssert"})
public class SqlConnectorTest extends SqlConnectorTestSupport {

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
                "sql-connector",
                builder -> builder.putConfiguredProperty("query", "SELECT * FROM NAME ORDER BY id")),
            newSimpleEndpointStep(
                "log",
                builder -> builder.putConfiguredProperty("loggerName", "test"))
        );
    }

    // **************************
    // Tests
    // **************************

    @Test
    public void sqlConnectorTest() throws Exception {
        String result = template.requestBody("direct:start", null, String.class);
        Properties props = JSONBeanUtil.parsePropertiesFromJSONBean(result);

        Assertions.assertThat(props.getProperty("ID")).isEqualTo("1");
    }
}
