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
package io.syndesis.test.itest.apiconnector;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import io.syndesis.common.model.action.Action.Pattern;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.common.model.connection.ConnectorTemplate;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.Resources;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.ConnectorGenerator;
import io.syndesis.server.api.generator.openapi.OpenApiConnectorGenerator;
import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;

import org.junit.jupiter.api.Test;
import org.springframework.util.SocketUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.http.server.HttpServer;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

/**
 * End to end test, in the sense that API generator is used to create the API
 * client connector instead of having a predefined connector. Tests all
 * combinations of path/operation, defined/referenced parameters.
 */
@Testcontainers
public class REST_API_EndToEnd_IT extends SyndesisIntegrationTestSupport {

    @Container
    public static final SyndesisIntegrationRuntimeContainer INTEGRATION_CONTAINER;

    private static final HttpServer API_SERVER;

    static {
        API_SERVER = startup(CitrusEndpoints.http()
            .server()
            .port(SocketUtils.findAvailableTcpPort())
            .autoStart(true)
            .build());

        org.testcontainers.Testcontainers.exposeHostPorts(API_SERVER.getPort());

        INTEGRATION_CONTAINER = new SyndesisIntegrationRuntimeContainer.Builder()
            .name("end-to-end-api-client")
            .fromIntegration(createIntegration())
            .build()
            .withExposedPorts(SyndesisTestEnvironment.getServerPort(),
                SyndesisTestEnvironment.getManagementPort());
    }

    @Test
    @CitrusTest
    public void shouldPassHttpHeaders(@CitrusResource final TestRunner runner) {
        runner.http(builder -> builder.server(API_SERVER)
            .receive()
            .post("/path1-value/path2-value/path3-value/path4-value")
            .contentType("application/json;charset=UTF-8")
            .queryParam("query1", "query1-value")
            .queryParam("query2", "query2-value")
            .queryParam("query3", "query3-value")
            .queryParam("query4", "query4-value")
            .header("header1", "header1-value")
            .header("header2", "header2-value")
            .header("header3", "header3-value")
            .header("header4", "header4-value")
            .payload("{\"data\":\"data-value\"}"));
    }

    private static Connector createConnector() {
        try {
            final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()
                .putConfiguredProperty("specification", Resources.getResourceAsText("io/syndesis/test/itest/apiconnector/EndToEnd/openapi.json"))
                .build();
            return generator().generate(fetchConnectorTemplateFromDeployment(), connectorSettings);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Integration createIntegration() {
        final Connector connector = createConnector();

        final Connection connection = new Connection.Builder()
            .connector(connector)
            .putConfiguredProperty("host", String.format("http://%s:%s", GenericContainer.INTERNAL_HOST_HOSTNAME, API_SERVER.getPort()))
            .build();

        // openapi.json contains a single operation, i.e. single action
        final ConnectorAction action = connector.getActions().get(0);

        String mapping;
        try {
            mapping = Resources.getResourceAsText("io/syndesis/test/itest/apiconnector/EndToEnd/mapping.json");
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        return new Integration.Builder()
            .id("end-to-end")
            .name("end-to-end")
            .addFlows(new Flow.Builder()
                .id("end-to-end-flow")
                .addStep(new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("timer")
                            .putConfiguredProperty("period", "100")
                            .build())
                        .pattern(Pattern.From)
                        .build())
                    .build())
                .addStep(new Step.Builder()
                    .stepKind(StepKind.mapper)
                    .putConfiguredProperty("atlasmapping", mapping)
                    .build())
                .addStep(new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(action)
                    .connection(connection)
                    .build())
                .build())
            .build();
    }

    private static ConnectorTemplate fetchConnectorTemplateFromDeployment() throws IOException {
        final Configuration configuration = Configuration.builder()
            .jsonProvider(new JacksonJsonProvider(JsonUtils.copyObjectMapperConfiguration()))
            .mappingProvider(new JacksonMappingProvider(JsonUtils.copyObjectMapperConfiguration()))
            .build();

        final List<ConnectorTemplate> templates = JsonPath.using(configuration)
            .parse(Resources.getResourceAsText("io/syndesis/server/dao/deployment.json"))
            .read("$..[?(@['id'] == 'swagger-connector-template')]", new TypeRef<List<ConnectorTemplate>>() {
                // type token pattern
            });

        return templates.get(0);
    }

    private static ConnectorGenerator generator() throws IOException {
        final Connector connector = JsonUtils.reader().readValue(Resources.getResourceAsText("META-INF/syndesis/connector/rest-swagger.json"),
            Connector.class);

        return new OpenApiConnectorGenerator(connector);
    }
}
