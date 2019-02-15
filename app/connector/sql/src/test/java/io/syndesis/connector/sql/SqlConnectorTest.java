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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.sql.common.JSONBeanUtil;
import io.syndesis.connector.sql.util.SqlConnectorTestSupport;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.JUnitTestsShouldIncludeAssert"})
@RunWith(Parameterized.class)
public class SqlConnectorTest extends SqlConnectorTestSupport {

    private final String sqlQuery;
    private final List<Map<String, String[]>> expectedResults;
    private final Map<String, Object> parameters;

    public SqlConnectorTest(String sqlQuery, List<Map<String, String[]>> expectedResults, Map<String, Object> parameters) {
        this.sqlQuery = sqlQuery;
        this.expectedResults = expectedResults;
        this.parameters = parameters;
    }

    @Override
    protected List<String> setupStatements() {
        return Collections.singletonList("CREATE TABLE ADDRESS (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 2, INCREMENT BY 1), street VARCHAR(255), number INTEGER)");
    }

    @Override
    protected List<String> cleanupStatements() {
        return Collections.singletonList("DROP TABLE ADDRESS");
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
                { "INSERT INTO ADDRESS (street, number) VALUES ('East Davie Street', 100)", Arrays.asList(Collections.singletonMap("NUMBER", new String[] { "100" }),
                        Collections.singletonMap("STREET", new String[] { "East Davie Street" })), Collections.emptyMap()},
                { "INSERT INTO ADDRESS (street, number) VALUES (:#street, :#number)", Arrays.asList(Collections.singletonMap("NUMBER", new String[] { "14" }),
                        Collections.singletonMap("STREET", new String[] { "LaborInVain" })), parameters}
        });
    }

    // **************************
    // Tests
    // **************************

    @Test
    public void sqlConnectorTest() throws Exception {
        String body;
        if (parameters.isEmpty()) {
            body = null;
        } else {
            body = JSONBeanUtil.toJSONBean(parameters);
        }

        template.requestBody("direct:start", body, List.class);

        try (Statement stmt = db.connection.createStatement()) {
            stmt.execute("SELECT * FROM ADDRESS");

            List<Properties> jsonBeans = resultSetToList(stmt.getResultSet())
                                                    .stream()
                                                    .map(raw -> {
                                                        Properties properties = new Properties();
                                                        properties.putAll(raw);
                                                        return properties;
                                                    })
                                                    .collect(Collectors.toList());

            Assert.assertEquals(expectedResults.isEmpty(), jsonBeans.isEmpty());

            for (Map<String, String[]> result : expectedResults) {
                for (Map.Entry<String, String[]> resultEntry : result.entrySet()) {
                    validateProperty(jsonBeans, resultEntry.getKey(), resultEntry.getValue());
                }
            }
        }
    }

    // **************************
    // Helpers
    // **************************

    private List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<Map<String, Object>> list = new ArrayList<>();
        while (rs.next()){
            Map<String, Object> row = new HashMap<>(columns);
            for(int i = 1; i <= columns; ++i){
                row.put(md.getColumnName(i).toUpperCase(), rs.getObject(i));
            }
            list.add(row);
        }

        return list;
    }
}
