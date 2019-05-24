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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.sql.common.DbEnum;
import io.syndesis.connector.sql.common.JSONBeanUtil;
import io.syndesis.connector.sql.util.SqlConnectorTestSupport;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.JUnitTestsShouldIncludeAssert"})
public class SqlConnectorBatchUpdateTest extends SqlConnectorTestSupport {

    private final String sqlQuery = "INSERT INTO ADDRESS (street, number) VALUES (:#street, :#number)";

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
                builder -> builder
                        .putConfiguredProperty("batch", "true")
                        .putConfiguredProperty("query", sqlQuery)),
            newSimpleEndpointStep(
                "log",
                builder -> builder.putConfiguredProperty("loggerName", "test"))
        );
    }

    @Test
    public void sqlConnectorBatchUpdateTest() {
        final List<Map<String, Object>> parameters = new ArrayList<>();

        Map<String, Object> first = new HashMap<>();
        first.put("number", 14);
        first.put("street", "LaborInVain");

        Map<String, Object> second = new HashMap<>();
        second.put("number", 15);
        second.put("street", "Werner-von-Siemens-Ring");

        Map<String, Object> third = new HashMap<>();
        third.put("number", 75);
        third.put("street", "Am Treptower Park");

        parameters.add(first);
        parameters.add(second);
        parameters.add(third);

        List<String> body = new ArrayList<>();
        for (Map<String, Object> paramMap : parameters) {
            body.add(JSONBeanUtil.toJSONBean(paramMap));
        }

        @SuppressWarnings("unchecked")
        List<String> jsonBeans = template.requestBody("direct:start", body, List.class);

        Assert.assertFalse(jsonBeans.isEmpty());

        validateJson(jsonBeans, "ID", "3");
    }
}
