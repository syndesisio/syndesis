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
package io.syndesis.test.itest.csv;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.action.ConnectorDescriptor.StandardizedError;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.container.IteratingConditionExpression;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.http.client.HttpClient;

public class CSV_WebHook_to_DB_IT extends SyndesisIntegrationTestSupport {

    @Container
    public static final SyndesisIntegrationRuntimeContainer INTEGRATION_CONTAINER = new SyndesisIntegrationRuntimeContainer.Builder()
        .name("csv-webhook-to-db")
        .fromIntegration(new Integration.Builder()
            .name("csv-webhook-to-db")
            .addFlow(new Flow.Builder()
                .addStep(new Step.Builder()
                    .id("webhook")
                    .stepKind(StepKind.endpoint)
                    .putConfiguredProperty("contextPath", "import")
                    .putConfiguredProperty("httpResponseCode", "200")
                    .putConfiguredProperty("returnBody", "false")
                    .putConfiguredProperty("errorResponseCodes", "{\"SERVER_ERROR\":\"500\"}")
                    .action(new ConnectorAction.Builder()
                        .id("io.syndesis:webhook-incoming")
                        .pattern(Action.Pattern.From)
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("servlet")
                            .addConnectorCustomizer("io.syndesis.connector.webhook.WebhookConnectorCustomizer")
                            .exceptionHandler("io.syndesis.connector.webhook.WebhookOnExceptionHandler")
                            .addStandardizedErrors(new StandardizedError.Builder()
                                .displayName("ServerError")
                                .name("SERVER_ERROR")
                                .build())
                            .outputDataShape(new DataShape.Builder()
                                .kind(DataShapeKinds.CSV_INSTANCE)
                                .specification("task,completed")
                                .build())
                            .putConfiguredProperty("headerFilterStrategy", "syndesisHeaderStrategy")
                            .putConfiguredProperty("httpMethodRestrict", "POST")
                            .build())
                        .build())
                    .connection(new Connection.Builder()
                        .id("webhook")
                        .connectorId("webhook")
                        .connector(new Connector.Builder()
                            .id("webhook")
                            .addDependency(new Dependency.Builder()
                                .type(Dependency.Type.MAVEN)
                                .id("io.syndesis.connector:connector-webhook:1.12-SNAPSHOT")
                                .build())
                            .build())
                        .build())
                    .build())
                .addStep(new Step.Builder()
                    .id("atlasmap")
                    .stepKind(StepKind.mapper)
                    .putConfiguredProperty("atlasmapping", "{\n"
                        + "  \"AtlasMapping\": {\n"
                        + "    \"jsonType\": \"io.atlasmap.v2.AtlasMapping\",\n"
                        + "    \"dataSource\": [\n"
                        + "      {\n"
                        + "        \"jsonType\": \"io.atlasmap.v2.DataSource\",\n"
                        + "        \"id\": \"webhook\",\n"
                        + "        \"name\": \"1 - CSV data\",\n"
                        + "        \"description\": \"webhook\",\n"
                        + "        \"uri\": \"atlas:csv:webhook\",\n"
                        + "        \"dataSourceType\": \"SOURCE\"\n"
                        + "      },\n"
                        + "      {\n"
                        + "        \"jsonType\": \"io.atlasmap.json.v2.JsonDataSource\",\n"
                        + "        \"id\": \"database\",\n"
                        + "        \"name\": \"2 - SQL Parameter\",\n"
                        + "        \"description\": \"Parameters of SQL [INSERT INTO todo (task, completed) VALUES (:#task,:#completed)]\",\n"
                        + "        \"uri\": \"atlas:json:database\",\n"
                        + "        \"dataSourceType\": \"TARGET\",\n"
                        + "        \"template\": null\n"
                        + "      }\n"
                        + "    ],\n"
                        + "    \"mappings\": {\n"
                        + "      \"mapping\": [\n"
                        + "        {\n"
                        + "          \"jsonType\": \"io.atlasmap.v2.Mapping\",\n"
                        + "          \"id\": \"mapping.537603\",\n"
                        + "          \"inputField\": [\n"
                        + "            {\n"
                        + "              \"jsonType\": \"io.atlasmap.csv.v2.CsvField\",\n"
                        + "              \"name\": \"0\",\n"
                        + "              \"path\": \"/<>/0\",\n"
                        + "              \"fieldType\": \"STRING\",\n"
                        + "              \"docId\": \"webhook\",\n"
                        + "              \"column\": 0\n"
                        + "            }\n"
                        + "          ],\n"
                        + "          \"outputField\": [\n"
                        + "            {\n"
                        + "              \"jsonType\": \"io.atlasmap.json.v2.JsonField\",\n"
                        + "              \"name\": \"task\",\n"
                        + "              \"path\": \"/<>/task\",\n"
                        + "              \"fieldType\": \"STRING\",\n"
                        + "              \"docId\": \"database\",\n"
                        + "              \"userCreated\": false\n"
                        + "            }\n"
                        + "          ]\n"
                        + "        },\n"
                        + "        {\n"
                        + "          \"jsonType\": \"io.atlasmap.v2.Mapping\",\n"
                        + "          \"id\": \"mapping.908273\",\n"
                        + "          \"inputField\": [\n"
                        + "            {\n"
                        + "              \"jsonType\": \"io.atlasmap.csv.v2.CsvField\",\n"
                        + "              \"name\": \"1\",\n"
                        + "              \"path\": \"/<>/1\",\n"
                        + "              \"fieldType\": \"STRING\",\n"
                        + "              \"docId\": \"webhook\",\n"
                        + "              \"column\": 1\n"
                        + "            }\n"
                        + "          ],\n"
                        + "          \"outputField\": [\n"
                        + "            {\n"
                        + "              \"jsonType\": \"io.atlasmap.json.v2.JsonField\",\n"
                        + "              \"name\": \"completed\",\n"
                        + "              \"path\": \"/<>/completed\",\n"
                        + "              \"fieldType\": \"STRING\",\n"
                        + "              \"docId\": \"database\",\n"
                        + "              \"userCreated\": false\n"
                        + "            }\n"
                        + "          ]\n"
                        + "        }\n"
                        + "      ]\n"
                        + "    },\n"
                        + "    \"name\": \"UI.0\",\n"
                        + "    \"lookupTables\": {\n"
                        + "      \"lookupTable\": []\n"
                        + "    },\n"
                        + "    \"constants\": {\n"
                        + "      \"constant\": []\n"
                        + "    },\n"
                        + "    \"properties\": {\n"
                        + "      \"property\": []\n"
                        + "    }\n"
                        + "  }\n"
                        + "}")
                    .build())
                .addStep(new Step.Builder()
                    .id("db")
                    .stepKind(StepKind.endpoint)
                    .putConfiguredProperty("query", "INSERT INTO todo (task, completed) VALUES (:#task,:#completed)")
                    .putConfiguredProperty("batch", "true")
                    .putConfiguredProperty("raiseErrorOnNotFound", "false")
                    .action(new ConnectorAction.Builder()
                        .id("sql-connector")
                        .pattern(Action.Pattern.To)
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("sql")
                            .addConnectorCustomizer("io.syndesis.connector.sql.customizer.SqlConnectorCustomizer")
                            .addStandardizedErrors(new StandardizedError.Builder()
                                .displayName("DataAccessError")
                                .name("DATA_ACCESS_ERROR")
                                .build(),
                                new StandardizedError.Builder()
                                    .displayName("EntityNotFoundError")
                                    .name("ENTITY_NOT_FOUND_ERROR")
                                    .build(),
                                new StandardizedError.Builder()
                                    .displayName("DuplicateKeyError")
                                    .name("DUPLICATE_KEY_ERROR")
                                    .build(),
                                new StandardizedError.Builder()
                                    .displayName("ConnectorError")
                                    .name("CONNECTOR_ERROR")
                                    .build())
                            .build())
                        .build())
                    .connection(new Connection.Builder()
                        .id("database")
                        .connectorId("sql")
                        .putConfiguredProperty("user", "sampledb")
                        .putConfiguredProperty("password", "secret")
                        .putConfiguredProperty("url", "jdbc:postgresql://syndesis-db:5432/sampledb")
                        .putConfiguredProperty("schema", "sampledb")
                        .connector(new Connector.Builder()
                            .id("sql")
                            .addConnectorCustomizer("io.syndesis.connector.sql.customizer.DataSourceCustomizer")
                            .addDependencies(new Dependency.Builder()
                                .type(Dependency.Type.MAVEN)
                                .id("io.syndesis.connector:connector-sql:1.12-SNAPSHOT")
                                .build())
                            .build())
                        .build())
                    .build())
                .build())
            .build())
        .build()
        .withNetwork(getSyndesisDb().getNetwork())
        .withExposedPorts(SyndesisTestEnvironment.getServerPort());

    private final HttpClient webHookClient = CitrusEndpoints.http().client()
        .requestUrl(String.format("http://localhost:%s/webhook/import", INTEGRATION_CONTAINER.getServerPort()))
        .build();

    @Test
    @CitrusTest
    public void testFtpSplitToDB(@CitrusResource TestRunner runner) {
        runner.http(builder -> builder.client(webHookClient)
            .send()
            .post()
            .payload("Buy eggs,0\n"
                + "Buy milk,0\n"
                + "Feed cat,1"));

        runner.repeatOnError()
            .startsWith(1)
            .autoSleep(100L)
            .until(new IteratingConditionExpression() {
                @Override
                public boolean evaluate(int index, TestContext context) {
                    return index > 10;
                }
            })
            .actions(runner.query(builder -> builder.dataSource(sampleDb())
                .statement("SELECT count(*) AS found_records FROM todo")
                .validate("found_records", String.valueOf(3))));
    }

    public static SyndesisIntegrationRuntimeContainer getIntegrationContainer() {
        return INTEGRATION_CONTAINER;
    }
}
