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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.sql.common.JSONBeanUtil;
import io.syndesis.connector.sql.util.SqlConnectorTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@SuppressWarnings({"PMD.SignatureDeclareThrowsException"})
@RunWith(Parameterized.class)
public class SqlStartConnectorTest extends SqlConnectorTestSupport {

    private final String sqlQuery;
    private final List<Map<String, String[]>> expectedResults;

    public SqlStartConnectorTest(String sqlQuery, List<Map<String, String[]>> expectedResults) {
        this.sqlQuery = sqlQuery;
        this.expectedResults = expectedResults;
    }

    @Override
    protected List<String> cleanupStatements() {
        return Collections.singletonList("DROP TABLE NAME");
    }

    @Override
    protected List<String> setupStatements() {
        return Arrays.asList("CREATE TABLE NAME (id INTEGER PRIMARY KEY, firstName VARCHAR(255), lastName VARCHAR(255))",
                "INSERT INTO NAME VALUES (1, 'Joe', 'Jackson')",
                "INSERT INTO NAME VALUES (2, 'Roger', 'Waters')");
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
                        Collections.singletonMap("LASTNAME", new String[] { "Jackson", "Waters" }))},
                { "SELECT * FROM NAME WHERE id = 2", Arrays.asList(Collections.singletonMap("ID", new String[] { "2" }),
                        Collections.singletonMap("FIRSTNAME", new String[] { "Roger" }),
                        Collections.singletonMap("LASTNAME", new String[] { "Waters" }))},
                { "SELECT * FROM NAME WHERE id = 99", Collections.emptyList()}
        });
    }

    // **************************
    // Tests
    // **************************

    @Test
    public void sqlStartConnectorTest() throws Exception {
        MockEndpoint mock = context.getEndpoint("mock:result", MockEndpoint.class);
        mock.expectedMessageCount(1);

        ProducerTemplate template = context.createProducerTemplate();
        template.sendBody("direct:start", null);

        mock.assertIsSatisfied();

        Exchange exchange = mock.getExchanges().get(0);
        List<?> body = exchange.getIn().getBody(List.class);
        List<Properties> jsonBeans = body.stream()
                .map(Object::toString)
                .map(JSONBeanUtil::parsePropertiesFromJSONBean)
                .collect(Collectors.toList());

        Assert.assertEquals(expectedResults.isEmpty(), jsonBeans.isEmpty());

        for (Map<String, String[]> result : expectedResults) {
            for (Map.Entry<String, String[]> resultEntry : result.entrySet()) {
                validateProperty(jsonBeans, resultEntry.getKey(), resultEntry.getValue());
            }
        }
    }
}
