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

import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.sql.common.JSONBeanUtil;
import io.syndesis.connector.sql.common.SqlTest;
import io.syndesis.connector.sql.common.SqlTest.ConnectionInfo;
import io.syndesis.connector.sql.common.SqlTest.Setup;
import io.syndesis.connector.sql.common.SqlTest.Teardown;
import io.syndesis.connector.sql.util.SqlConnectorTestSupport;

import org.apache.camel.CamelExecutionException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DuplicateKeyException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SqlTest.class)
@Setup({
    "CREATE TABLE ADDRESS ( id INTEGER NOT NULL, street VARCHAR(255), nummer INTEGER NOT NULL)",
    "ALTER TABLE ADDRESS ADD PRIMARY KEY(id)"
})
@Teardown("DROP TABLE ADDRESS")
public class SqlConnectorIdExistsTest extends SqlConnectorTestSupport {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Class<List<String>> LIST_OF_STRINGS = (Class) List.class;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String query;

    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    static class NoParameters extends SqlConnectorIdExistsTest {

        public NoParameters(final ConnectionInfo info) {
            super(info, "INSERT INTO ADDRESS (id, street, nummer) VALUES (1, 'East Davie Street', 100)");
        }

        @Test
        @Order(2)
        public void duplicateKey() {
            assertThatThrownBy(() -> {
                template().requestBody("direct:start", null, LIST_OF_STRINGS);
            }).isInstanceOf(CamelExecutionException.class)
                // Should throw an exception on the second insert
                .hasCauseInstanceOf(DuplicateKeyException.class);
        }

        @Test
        @Order(1)
        public void noParameters() {
            final List<String> jsonBeans = template().requestBody("direct:start", null, LIST_OF_STRINGS);
            assertThat(jsonBeans).isNull();
        }
    }

    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    static class WithParameters extends SqlConnectorIdExistsTest {

        final Map<String, Object> parameters;

        public WithParameters(final ConnectionInfo info) {
            super(info, "INSERT INTO ADDRESS (id, street, nummer) VALUES (1, :#street, :#number)");

            parameters = new HashMap<>();
            parameters.put("number", 14);
            parameters.put("street", "LaborInVain");
        }

        @Test
        @Order(2)
        public void duplicateKey() {
            assertThatThrownBy(() -> {
                template().requestBody("direct:start", JSONBeanUtil.toJSONBean(parameters), LIST_OF_STRINGS);
            }).isInstanceOf(CamelExecutionException.class)
                // Should throw an exception on the second insert
                .hasCauseInstanceOf(DuplicateKeyException.class);
        }

        @Test
        @Order(1)
        public void withParameters() throws JsonProcessingException {
            final List<String> jsonBeans = template().requestBody("direct:start", JSONBeanUtil.toJSONBean(parameters), LIST_OF_STRINGS);

            final Map<String, Map<String, Object>> result = new HashMap<>();
            result.put("number", valueMap(Types.INTEGER, "14"));
            result.put("street", valueMap(Types.VARCHAR, "LaborInVain"));

            assertThat(jsonBeans).hasSize(1);

            final String bean = jsonBeans.get(0);
            final Map<String, Object> received = MAPPER.readerForMapOf(Object.class).readValue(bean);

            assertThat(received).containsExactlyEntriesOf(result);
        }

    }

    public SqlConnectorIdExistsTest(final ConnectionInfo info, final String query) {
        super(info);
        this.query = query;
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

    static Map<String, Object> valueMap(final int type, final Object value) {
        final Map<String, Object> val = new LinkedHashMap<>();
        val.put("name", null);
        val.put("sqlType", type);
        val.put("typeName", null);
        val.put("scale", null);
        val.put("value", value);
        val.put("resultsParameter", false);
        val.put("inputValueProvided", true);

        return val;
    }
}
