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

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.connector.sql.common.JSONBeanUtil;
import io.syndesis.connector.sql.common.SqlConnectionRule;
import io.syndesis.connector.support.test.ConnectorTestSupport;
import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;

public abstract class SqlConnectorTestSupport extends ConnectorTestSupport {
    @ClassRule
    public static SqlConnectionRule db = new SqlConnectionRule();

    protected List<String> setupStatements() {
        return Collections.emptyList();
    }

    protected List<String> cleanupStatements() {
        return Collections.emptyList();
    }

    // **************************
    // Set up
    // **************************

    @Override
    protected void doPreSetup() throws Exception {
        try (Statement stmt = db.connection.createStatement()) {
            for (String sql : setupStatements()) {
                stmt.executeUpdate(sql);
            }
        }
    }

    @After
    public void after() throws SQLException {
        try (Statement stmt = db.connection.createStatement()) {
            for (String sql : cleanupStatements()) {
                stmt.executeUpdate(sql);
            }
        }
    }

    // **************************
    // Helpers
    // **************************

    protected Step newSqlEndpointStep(String actionId, Consumer<Step.Builder> consumer) {
        final Connector connector = getResourceManager().mandatoryLoadConnector("sql");
        final ConnectorAction action = getResourceManager().mandatoryLookupAction(connector, actionId);

        final Step.Builder builder = new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(action)
            .connection(new io.syndesis.common.model.connection.Connection.Builder()
                .connector(connector)
                .putConfiguredProperty("user", db.properties.getProperty("sql-connector.user"))
                .putConfiguredProperty("password", db.properties.getProperty("sql-connector.password"))
                .putConfiguredProperty("url", db.properties.getProperty("sql-connector.url"))
                .build());

        consumer.accept(builder);

        return builder.build();
    }

    protected Step newSqlEndpointStep(String actionId, Consumer<Step.Builder> stepConsumer, Consumer<ConnectorDescriptor.Builder> descriptorConsumer) {
        final Connector connector = getResourceManager().mandatoryLoadConnector("sql");
        final ConnectorAction action = getResourceManager().mandatoryLookupAction(connector, actionId);
        final ConnectorDescriptor.Builder descriptorBuilder = new ConnectorDescriptor.Builder().createFrom(action.getDescriptor());

        descriptorConsumer.accept(descriptorBuilder);

        final Step.Builder builder = new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(new ConnectorAction.Builder().createFrom(action).descriptor(descriptorBuilder.build()).build())
            .connection(new io.syndesis.common.model.connection.Connection.Builder()
                .connector(connector)
                .putConfiguredProperty("user", db.properties.getProperty("sql-connector.user"))
                .putConfiguredProperty("password", db.properties.getProperty("sql-connector.password"))
                .putConfiguredProperty("url", db.properties.getProperty("sql-connector.url"))
                .build());

        stepConsumer.accept(builder);

        return builder.build();
    }

    protected void validateProperty(List<Properties> propertyList, String propertyName, String ... expectedValues) {
        Assert.assertEquals(expectedValues.length, propertyList.size());

        for (int i = 0; i < expectedValues.length; i++) {
            Assert.assertEquals(expectedValues[i], propertyList.get(i).get(propertyName).toString());
        }
    }
    
    protected void validateJson(List<String> jsonBeans, String propertyName, String ... expectedValues) {
        Assert.assertEquals(expectedValues.length, jsonBeans.size());

        for (int i = 0; i < expectedValues.length; i++) {
            Properties properties = JSONBeanUtil.parsePropertiesFromJSONBean(jsonBeans.get(i));
            Assert.assertEquals(expectedValues[i], properties.get(propertyName).toString());
        }
    }
}
