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
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.syndesis.common.model.integration.Step;
import io.syndesis.common.util.ErrorCategory;
import io.syndesis.common.util.SyndesisConnectorException;
import io.syndesis.connector.sql.common.JSONBeanUtil;
import io.syndesis.connector.sql.common.Params;
import io.syndesis.connector.sql.common.SqlTest;
import io.syndesis.connector.sql.common.SqlTest.ConnectionInfo;
import io.syndesis.connector.sql.common.SqlTest.Setup;
import io.syndesis.connector.sql.common.SqlTest.Teardown;
import io.syndesis.connector.sql.common.TestParameters;
import io.syndesis.connector.sql.util.SqlConnectorTestSupport;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SqlTest.class)
@Setup({"CREATE TABLE ADDRESS (street VARCHAR(255), number INTEGER)",
    "INSERT INTO ADDRESS VALUES ('East Davie Street', 100)",
    "INSERT INTO ADDRESS VALUES ('Am Treptower Park', 75)",
    "INSERT INTO ADDRESS VALUES ('Werner-von-Siemens-Ring', 14)"})
@Teardown("DROP TABLE ADDRESS")
public class SqlConnectorQueryTest {

    private SqlConnectorQueryTest() {
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

            final List<?> results;

            try {
                results = template().requestBody("direct:start", body, List.class);
            } catch (final Throwable e) {

                // check if this was an expected error
                final SyndesisConnectorException sce = SyndesisConnectorException.from(e);

                assertThat(sce.getMessage()).isEqualTo(params.error.getMessage());
                assertThat(sce.getCategory()).isEqualTo(params.error.getCategory());
                return;
            }

            if (params.expectedResults.isEmpty()) {
                assertThat(results).isNull();
            } else {
                final List<Properties> jsonBeans = results.stream()
                    .map(Object::toString)
                    .map(JSONBeanUtil::parsePropertiesFromJSONBean)
                    .collect(Collectors.toList());

                assertThat(jsonBeans.isEmpty()).isEqualTo(params.expectedResults.isEmpty());

                for (final Map<String, String[]> result : params.expectedResults) {
                    for (final Map.Entry<String, String[]> resultEntry : result.entrySet()) {
                        validateProperty(jsonBeans, resultEntry.getKey(), resultEntry.getValue());
                    }
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
                    builder -> builder.putConfiguredProperty("query", params.query)
                        .putConfiguredProperty("raiseErrorOnNotFound", "true")),
                newSimpleEndpointStep(
                    "log",
                    builder -> builder.putConfiguredProperty("loggerName", "test")));
        }
    }

    static class Cases extends TestParameters {
        public Cases() {
            super(Stream.of(

                Params.query("SELECT * FROM ADDRESS")
                    .withResultColumnValues("NUMBER", "100", "75", "14")
                    .withResultColumnValues("STREET", "East Davie Street", "Am Treptower Park", "Werner-von-Siemens-Ring"),

                Params.query("SELECT * FROM ADDRESS WHERE number = 14")
                    .withResultColumnValues("NUMBER", "14")
                    .withResultColumnValues("STREET", "Werner-von-Siemens-Ring"),

                Params.query("SELECT street FROM ADDRESS WHERE number = :#number")
                    .withParameter("number", "100")
                    .withResultColumnValues("STREET", "East Davie Street"),

                // Causes a SyndesisConnectorException since no such record
                // exists and isRaiseErrorOnNotFound is set to true
                Params.query("SELECT * FROM ADDRESS WHERE number = 0")
                    .withError(ErrorCategory.ENTITY_NOT_FOUND_ERROR, "SQL SELECT did not SELECT any records"),

                // Causes a runtime exception for bad SQL grammar not caught by
                // Camel, for now best we can do is to classify it as a
                // SERVER_ERROR, so let's check this happens.
                Params.query("INSERT INTO ADDRESS VALUES (4, 'angerloseweg', '11')")
                    .withError(ErrorCategory.SERVER_ERROR,
                        "PreparedStatementCallback; bad SQL grammar []; nested exception is java.sql.SQLSyntaxErrorException: The number of values assigned is not the same as the number of specified or implied columns."),

                Params.query("DELETE FROM ADDRESS WHERE number = 14")

            ));
        }
    }

}
