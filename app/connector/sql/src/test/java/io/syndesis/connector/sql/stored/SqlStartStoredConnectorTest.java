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
package io.syndesis.connector.sql.stored;

import java.util.Arrays;
import java.util.List;

import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.sql.common.SqlTest.ConnectionInfo;
import io.syndesis.connector.sql.common.SqlTest.Setup;
import io.syndesis.connector.sql.common.SqlTest.Teardown;
import io.syndesis.connector.sql.util.SqlConnectorTestSupport;

import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;

@Setup(SampleStoredProcedures.DERBY_DEMO_OUT_SQL)
@Teardown("DROP PROCEDURE DEMO_OUT")
public class SqlStartStoredConnectorTest extends SqlConnectorTestSupport {

    public SqlStartStoredConnectorTest(ConnectionInfo info) {
        super(info);
    }

    @Override
    protected List<Step> createSteps() {
        return Arrays.asList(
            newSimpleEndpointStep(
                "direct",
                builder -> builder.putConfiguredProperty("name", "start")),
            newSqlEndpointStep(
                "sql-stored-start-connector",
                builder -> builder.putConfiguredProperty("template", "DEMO_OUT( OUT INTEGER c)")),
            newSimpleEndpointStep(
                "mock",
                builder -> builder.putConfiguredProperty("name", "result")));
    }

    // **************************
    // Tests
    // **************************

    @Test
    public void sqlStoredStartConnectorTest() throws Exception {
        MockEndpoint mock = context().getEndpoint("mock:result", MockEndpoint.class);
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("{\"c\":60}");

        template().sendBody("direct:start", null);

        mock.assertIsSatisfied();
    }
}
