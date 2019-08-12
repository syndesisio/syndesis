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
import java.util.List;
import java.util.Map;

import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.sql.common.DbEnum;
import io.syndesis.connector.sql.common.JSONBeanUtil;
import io.syndesis.connector.sql.util.SqlConnectorTestSupport;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@SuppressWarnings({"PMD.SignatureDeclareThrowsException"})
@RunWith(Parameterized.class)
public class SqlStartConnectorTest extends SqlConnectorTestSupport {

    private final String sqlQuery;
    private final List<Map<String, String[]>> expectedResults;
    private final Map<String, Object> parameters;

    public SqlStartConnectorTest(String sqlQuery, List<Map<String, String[]>> expectedResults, Map<String, Object> parameters) {
        this.sqlQuery = sqlQuery;
        this.expectedResults = expectedResults;
        this.parameters = parameters;
    }

    @Override
    protected List<String> cleanupStatements() {
        return Collections.singletonList("DROP TABLE NAME");
    }

    @Override
    protected List<String> setupStatements() {
        String dbProductName = null;
        try {
            dbProductName = db.connection.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        if (DbEnum.POSTGRESQL.equals(DbEnum.fromName(dbProductName))) {
            return Arrays.asList("CREATE TABLE NAME ("
                    + "ID SERIAL PRIMARY KEY, "
                    + "firstName VARCHAR(255), lastName VARCHAR(255))",
                    "INSERT INTO NAME (firstname, lastname) VALUES ('Joe', 'Jackson')",
                    "INSERT INTO NAME (firstname, lastname) VALUES ('Roger', 'Waters')");
        } else if (DbEnum.MYSQL.equals(DbEnum.fromName(dbProductName))) {
            return Arrays.asList("CREATE TABLE NAME ("
                    + "ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                    + "firstName VARCHAR(255), lastName VARCHAR(255))",
                    "INSERT INTO NAME (firstname, lastname) VALUES ('Joe', 'Jackson')",
                    "INSERT INTO NAME (firstname, lastname) VALUES ('Roger', 'Waters')");
        } else if (DbEnum.APACHE_DERBY.equals(DbEnum.fromName(dbProductName))) {
            return Arrays.asList("CREATE TABLE NAME (ID INTEGER NOT NULL "
                    + "GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                    + "firstName VARCHAR(255), lastName VARCHAR(255))",
                    "INSERT INTO NAME (firstname, lastname) VALUES ('Joe', 'Jackson')",
                    "INSERT INTO NAME (firstname, lastname) VALUES ('Roger', 'Waters')");
        } else {
            return Arrays.asList("CREATE TABLE NAME ("
                    + "ID NUMBER GENERATED ALWAYS AS IDENTITY, "
                    + "firstName VARCHAR(255), lastName VARCHAR(255))",
                    "INSERT INTO NAME (firstname, lastname) VALUES ('Joe', 'Jackson')",
                    "INSERT INTO NAME (firstname, lastname) VALUES ('Roger', 'Waters')");
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
                builder -> builder.putConfiguredProperty("query", sqlQuery)),
            newSimpleEndpointStep(
                "log",
                builder -> builder.putConfiguredProperty("loggerName", "test")),
            newSimpleEndpointStep(
                "mock",
                builder -> builder.putConfiguredProperty("name", "result"))
        );
    }

    // **************************
    // Parameters
    // **************************

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "SELECT * FROM NAME ORDER BY id", Arrays.asList(Collections.singletonMap("ID", new String[] { "1", "2" }),
                        Collections.singletonMap("FIRSTNAME", new String[] { "Joe", "Roger" }),
                        Collections.singletonMap("LASTNAME", new String[] { "Jackson", "Waters" })),
                        Collections.emptyMap()},
                { "SELECT * FROM NAME WHERE id = 2", Arrays.asList(Collections.singletonMap("ID", new String[] { "2" }),
                        Collections.singletonMap("FIRSTNAME", new String[] { "Roger" }),
                        Collections.singletonMap("LASTNAME", new String[] { "Waters" })),
                        Collections.emptyMap()},
                { "SELECT * FROM NAME WHERE id = 99", Collections.emptyList(), Collections.emptyMap()},
                { "INSERT INTO NAME (firstname, lastname) VALUES ('Kurt', 'Cobain')",
                        Collections.singletonList(Collections.singletonMap("ID", new String[]{"3"})),
                        Collections.emptyMap()}
        });
    }

    // **************************
    // Tests
    // **************************

    @Test
    public void sqlStartConnectorTest() throws Exception {

        String body;
        if (parameters.isEmpty()) {
            body = null;
        } else {
            body = JSONBeanUtil.toJSONBean(parameters);
        }

        @SuppressWarnings("unchecked")
        List<String> jsonBeans = template.requestBody("direct:start", body, List.class);
        
        Assert.assertEquals(expectedResults.isEmpty(), jsonBeans.isEmpty());

        for (Map<String, String[]> result : expectedResults) {
            for (Map.Entry<String, String[]> resultEntry : result.entrySet()) {
                validateJson(jsonBeans, resultEntry.getKey(), resultEntry.getValue());
            }
        }
    }
}
