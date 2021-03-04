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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.sql.common.DbEnum;
import io.syndesis.connector.sql.common.SqlTest.ConnectionInfo;
import io.syndesis.connector.sql.common.SqlTest.Setup;
import io.syndesis.connector.sql.common.SqlTest.Teardown;
import io.syndesis.connector.sql.common.SqlTest.Variant;
import io.syndesis.connector.sql.util.SqlConnectorTestSupport;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Setup(variants = {
    @Variant(
        type = DbEnum.POSTGRESQL,
        value = "CREATE TABLE ADDRESS (ID SERIAL PRIMARY KEY, street VARCHAR(255), nummer INTEGER)"),
    @Variant(
        type = DbEnum.MYSQL,
        value = "CREATE TABLE ADDRESS (ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, street VARCHAR(255), nummer INTEGER)"),
    @Variant(
        type = DbEnum.APACHE_DERBY,
        value = "CREATE TABLE ADDRESS (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), street VARCHAR(255), number INTEGER)"),
    @Variant(
        type = DbEnum.STANDARD,
        value = "CREATE TABLE ADDRESS (ID NUMBER GENERATED ALWAYS AS IDENTITY, street VARCHAR(255), nummer INTEGER)")
})
@Teardown("DROP TABLE ADDRESS")
public class SqlConnectorTest extends SqlConnectorTestSupport {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Class<List<String>> LIST_OF_STRINGS = (Class) List.class;

    private final List<Map<String, String[]>> expectedResults;

    private final Map<String, Object> parameters;

    private final String query;

    static class NoParameters extends SqlConnectorTest {

        public NoParameters(final ConnectionInfo info) {
            super(info, "INSERT INTO ADDRESS (street, number) VALUES ('East Davie Street', 100)",
                Collections.singletonList(Collections.singletonMap("ID", new String[] {"1"})), Collections.emptyMap());
        }

        @Test
        @Override
        public void sqlConnectorTest() throws Exception {
            super.sqlConnectorTest();
        }
    }

    static class WithParameters extends SqlConnectorTest {

        public WithParameters(final ConnectionInfo info) {
            super(info, "INSERT INTO ADDRESS (street, number) VALUES ('East Davie Street', 100)",
                Collections.singletonList(Collections.singletonMap("ID", new String[] {"1"})), params());
        }

        @Test
        @Override
        public void sqlConnectorTest() throws Exception {
            super.sqlConnectorTest();
        }

        private static Map<String, Object> params() {
            final Map<String, Object> parameters = new HashMap<>();
            parameters.put("number", 14);
            parameters.put("street", "LaborInVain");

            return parameters;
        }
    }

    public SqlConnectorTest(final ConnectionInfo info, final String query, final List<Map<String, String[]>> expectedResults,
        final Map<String, Object> parameters) {
        super(info);
        this.query = query;
        this.expectedResults = expectedResults;
        this.parameters = parameters;
    }

    public void sqlConnectorTest() throws Exception {
        final List<String> jsonBeans = template().requestBody("direct:start", parameters, LIST_OF_STRINGS);

        assertThat(jsonBeans.isEmpty()).isEqualTo(expectedResults.isEmpty());

        for (final Map<String, String[]> result : expectedResults) {
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
                "sql-connector",
                builder -> builder.putConfiguredProperty("query", query)),
            newSimpleEndpointStep(
                "log",
                builder -> builder.putConfiguredProperty("loggerName", "test")));
    }

}
