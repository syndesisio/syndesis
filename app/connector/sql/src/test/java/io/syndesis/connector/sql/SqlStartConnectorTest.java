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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.sql.common.DbEnum;
import io.syndesis.connector.sql.common.JSONBeanUtil;
import io.syndesis.connector.sql.common.Params;
import io.syndesis.connector.sql.common.SqlTest;
import io.syndesis.connector.sql.common.SqlTest.ConnectionInfo;
import io.syndesis.connector.sql.common.SqlTest.Setup;
import io.syndesis.connector.sql.common.SqlTest.Teardown;
import io.syndesis.connector.sql.common.SqlTest.Variant;
import io.syndesis.connector.sql.common.TestParameters;
import io.syndesis.connector.sql.util.SqlConnectorTestSupport;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SqlTest.class)
@Setup(
    value = {
        "INSERT INTO NAME (firstname, lastname) VALUES ('Joe', 'Jackson')",
        "INSERT INTO NAME (firstname, lastname) VALUES ('Roger', 'Waters')"
    },
    variants = {
        @Variant(type = DbEnum.POSTGRESQL, value = "CREATE TABLE NAME (ID SERIAL PRIMARY KEY, firstName VARCHAR(255), lastName VARCHAR(255))"),
        @Variant(type = DbEnum.MYSQL, value = "CREATE TABLE NAME (ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, firstName VARCHAR(255), lastName VARCHAR(255))"),
        @Variant(type = DbEnum.APACHE_DERBY,
            value = "CREATE TABLE NAME (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), firstName VARCHAR(255), lastName VARCHAR(255))"),
        @Variant(type = DbEnum.STANDARD, value = "CREATE TABLE NAME (ID NUMBER GENERATED ALWAYS AS IDENTITY, firstName VARCHAR(255), lastName VARCHAR(255))"),
    })
@Teardown("DROP TABLE NAME")
public class SqlStartConnectorTest {

    private SqlStartConnectorTest() {
        // only a container for test cases
    }

    @ExtendWith(Cases.class)
    static class Case extends SqlConnectorTestSupport {

        private final Params params;

        Case(final ConnectionInfo info, final Params params) {
            super(info);
            this.params = params;
        }

        @TestTemplate
        void runCases() {
            String body;
            if (params.parameters.isEmpty()) {
                body = null;
            } else {
                body = JSONBeanUtil.toJSONBean(params.parameters);
            }

            @SuppressWarnings("unchecked")
            final List<String> jsonBeans = template().requestBody("direct:start", body, List.class);

            Assertions.assertEquals(params.expectedResults.isEmpty(), jsonBeans.isEmpty());

            for (final Map<String, String[]> result : params.expectedResults) {
                for (final Map.Entry<String, String[]> resultEntry : result.entrySet()) {
                    validateJson(jsonBeans, resultEntry.getKey(), resultEntry.getValue());
                }
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
                    builder -> builder.putConfiguredProperty("query", params.query)),
                newSimpleEndpointStep(
                    "log",
                    builder -> builder.putConfiguredProperty("loggerName", "test")),
                newSimpleEndpointStep(
                    "mock",
                    builder -> builder.putConfiguredProperty("name", "result")));
        }
    }

    static class Cases extends TestParameters {
        public Cases() {
            super(Stream.of(

                Params.query("SELECT * FROM NAME ORDER BY id")
                    .withResultColumnValues("ID", "1", "2")
                    .withResultColumnValues("FIRSTNAME", "Joe", "Roger")
                    .withResultColumnValues("LASTNAME", "Jackson", "Waters"),

                Params.query("SELECT * FROM NAME WHERE id = 2")
                    .withResultColumnValues("ID", "2")
                    .withResultColumnValues("FIRSTNAME", "Roger")
                    .withResultColumnValues("LASTNAME", "Waters"),

                Params.query("SELECT * FROM NAME WHERE id = 99"),

                Params.query("INSERT INTO NAME (firstname, lastname) VALUES ('Kurt', 'Cobain')")
                    .withResultColumnValues("ID", "3")

            ));
        }
    }
}
