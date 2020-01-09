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

import org.apache.camel.CamelExecutionException;
import org.assertj.core.api.Assertions;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.dao.DuplicateKeyException;

import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.sql.common.JSONBeanUtil;
import io.syndesis.connector.sql.util.SqlConnectorTestSupport;

@RunWith(Parameterized.class)
public class SqlConnectorIdExistsTest extends SqlConnectorTestSupport {

    private final String sqlQuery;
    private final Map<String, Object> parameters;

    public SqlConnectorIdExistsTest(String sqlQuery, List<Map<String, String[]>> expectedResults, Map<String, Object> parameters) {
        this.sqlQuery = sqlQuery;
        this.parameters = parameters;
    }

    @BeforeClass
    public static void beforeClass() throws SQLException {
        try (Statement stmt = db.connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE ADDRESS ( id INTEGER NOT NULL," + 
                    "  street VARCHAR(255), nummer INTEGER NOT NULL)");
            stmt.executeUpdate("ALTER TABLE ADDRESS ADD PRIMARY KEY(id)");
        }
    }

    @AfterClass
    public static void afterClass() throws SQLException {
        try (Statement stmt = db.connection.createStatement()) {
                stmt.executeUpdate("DROP TABLE ADDRESS");
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
                builder -> builder.putConfiguredProperty("query", sqlQuery)),
            newSimpleEndpointStep(
                "log",
                builder -> builder.putConfiguredProperty("loggerName", "test"))
        );
    }

    // **************************
    // Parameters
    // **************************

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("number", 14);
        parameters.put("street", "LaborInVain");

        return Arrays.asList(new Object[][] {
                { "INSERT INTO ADDRESS (id, street, nummer) VALUES (1, 'East Davie Street', 100)",
                        Collections.singletonList(Collections.singletonMap("ID", new String[]{"1"})),
                        Collections.emptyMap()},
                { "INSERT INTO ADDRESS (id, street, nummer) VALUES (1, :#street, :#number)",
                        Collections.singletonList(Collections.singletonMap("ID", new String[]{"1"})),
                        parameters}
        });
    }

    // **************************
    // Tests
    // **************************

    @Test
    public void sqlConnectorTest() {
        String body;
        if (parameters.isEmpty()) {
            body = null;
            @SuppressWarnings("unchecked")
            List<String> jsonBeans = template.requestBody("direct:start", body, List.class);
            Assertions.assertThat(jsonBeans).isNull();
        } else {
            body = JSONBeanUtil.toJSONBean(parameters);
            Assertions.assertThatThrownBy(() -> {
               @SuppressWarnings({ "unchecked", "unused" })
               List<String> jsonBeans = template.requestBody("direct:start", body, List.class);
            }).isInstanceOf(CamelExecutionException.class)
                //Should throw an exception on the second insert
                .hasCauseInstanceOf(DuplicateKeyException.class);
        }
    }
}
