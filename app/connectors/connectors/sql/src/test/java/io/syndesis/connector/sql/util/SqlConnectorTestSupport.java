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
package io.syndesis.connector.sql.util;

import java.util.function.Consumer;

import io.syndesis.connector.sql.common.SqlConnectionRule;
import io.syndesis.connector.test.ConnectorTestSupport;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Step;
import io.syndesis.model.integration.StepKind;
import org.junit.ClassRule;

public abstract class SqlConnectorTestSupport extends ConnectorTestSupport {
    @ClassRule
    public static SqlConnectionRule db = new SqlConnectionRule();

    // **************************
    // Helpers
    // **************************

    protected Step newSqlEndpointStep(String actionId, Consumer<Step.Builder> consumer) {
        final Connector connector = getResourceManager().mandatoryLoadConnector("sql");
        final ConnectorAction action = getResourceManager().mandatoryLookupAction(connector, actionId);

        final Step.Builder builder = new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(action)
            .connection(new io.syndesis.model.connection.Connection.Builder()
                .connector(connector)
                .putConfiguredProperty("user", db.properties.getProperty("sql-connector.user"))
                .putConfiguredProperty("password", db.properties.getProperty("sql-connector.password"))
                .putConfiguredProperty("url", db.properties.getProperty("sql-connector.url"))
                .build());

        consumer.accept(builder);

        return builder.build();
    }
}
