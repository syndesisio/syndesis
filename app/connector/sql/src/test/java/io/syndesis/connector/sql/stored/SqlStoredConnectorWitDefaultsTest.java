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

import org.apache.camel.ProducerTemplate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Setup(SampleStoredProcedures.DERBY_DEMO_ADD_SQL)
@Teardown("DROP PROCEDURE DEMO_ADD")
public class SqlStoredConnectorWitDefaultsTest extends SqlConnectorTestSupport {

    public SqlStoredConnectorWitDefaultsTest(ConnectionInfo info) {
        super(info);
    }

    @Override
    protected List<Step> createSteps() {
        return Arrays.asList(
            newSimpleEndpointStep(
                "direct",
                builder -> builder.putConfiguredProperty("name", "start")),
            newSqlEndpointStep(
                info,
                "sql-stored-connector",
                nop(Step.Builder.class),
                builder -> builder.replaceConfigurationProperty("template",
                    b -> b.defaultValue("DEMO_ADD(INTEGER ${body[a]}, INTEGER ${body[b]}, OUT INTEGER c)"))),
            newSimpleEndpointStep(
                "mock",
                builder -> builder.putConfiguredProperty("name", "result")));
    }

    @Test
    public void sqlStoredStartConnectorTest() throws Exception {
        String jsonBody = "{\"a\":20,\"b\":30}";
        ProducerTemplate template = context().createProducerTemplate();
        String result = template.requestBody("direct:start", jsonBody, String.class);

        Assertions.assertThat(result).isEqualTo("{\"c\":50}");
    }
}
