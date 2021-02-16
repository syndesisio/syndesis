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

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.sql.common.DbEnum;
import io.syndesis.connector.sql.common.JSONBeanUtil;
import io.syndesis.connector.sql.util.SqlConnectorTestSupport;

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
        String dbProductName = null;
        try {
            dbProductName = db.connection.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            e.printStackTrace();
            Assertions.fail(e.getMessage());
        }
        if (DbEnum.POSTGRESQL.equals(DbEnum.fromName(dbProductName))) {
            return Collections.singletonList("CREATE TABLE ADDRESS ("
                    + "ID SERIAL PRIMARY KEY, "
                    + "street VARCHAR(255), nummer INTEGER)");
        } else if (DbEnum.MYSQL.equals(DbEnum.fromName(dbProductName))) {
            return Collections.singletonList("CREATE TABLE ADDRESS ("
                    + "ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                    + "street VARCHAR(255), nummer INTEGER)");
        } else if (DbEnum.APACHE_DERBY.equals(DbEnum.fromName(dbProductName))) {
            return Collections.singletonList("CREATE TABLE ADDRESS (ID INTEGER NOT NULL "
                    + "GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                    + "street VARCHAR(255), number INTEGER)");
        } else {
            return Collections.singletonList("CREATE TABLE ADDRESS ("
                    + "ID NUMBER GENERATED ALWAYS AS IDENTITY, "
                    + "street VARCHAR(255), nummer INTEGER)");
        }
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
                { "INSERT INTO ADDRESS (street, number) VALUES ('East Davie Street', 100)",
                        Collections.singletonList(Collections.singletonMap("ID", new String[]{"1"})),
                        Collections.emptyMap()},
                { "INSERT INTO ADDRESS (street, number) VALUES (:#street, :#number)",
                        Collections.singletonList(Collections.singletonMap("ID", new String[]{"1"})),
                        parameters}
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

        @SuppressWarnings("unchecked")
        List<String> jsonBeans = template.requestBody("direct:start", body, List.class);

        Assertions.assertEquals(expectedResults.isEmpty(), jsonBeans.isEmpty());

        for (Map<String, String[]> result : expectedResults) {
            for (Map.Entry<String, String[]> resultEntry : result.entrySet()) {
                validateJson(jsonBeans, resultEntry.getKey(), resultEntry.getValue());
            }
        }
    }
}
