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
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
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
import io.syndesis.server.api.generator.soap.SoapApiConnectorGenerator;
import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.SocketUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.http.security.BasicAuthConstraint;
import com.consol.citrus.ws.security.SecurityHandlerFactory;
import com.consol.citrus.ws.security.User;
import com.consol.citrus.ws.server.WebServiceServer;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

/**
 * End to end test, in the sense that SOAP API generator is used to create the
 * API client connector instead of having a predefined connector. Tests all
 * combinations of path/operation, defined/referenced parameters.
 */
@Testcontainers
public class SOAP_API_EndToEnd_IT extends SyndesisIntegrationTestSupport {

    @Container
    public static final SyndesisIntegrationRuntimeContainer INTEGRATION_CONTAINER;

    private static final WebServiceServer SOAP_SERVER;

    static {
        final SecurityHandlerFactory securityHandlerFactory = new SecurityHandlerFactory();
        final User testUser = new User();
        testUser.setName("test");
        testUser.setPassword("secret");
        final String[] authenticated = new String[] {"authenticated"};
        testUser.setRoles(authenticated);
        securityHandlerFactory.setUsers(Collections.singletonList(testUser));
        securityHandlerFactory.setConstraints(Collections.singletonMap("/endpoint/*", new BasicAuthConstraint(authenticated)));
        try {
            securityHandlerFactory.afterPropertiesSet();
            SOAP_SERVER = startup(CitrusEndpoints.soap()
                .server()
                .port(SocketUtils.findAvailableTcpPort())
                .autoStart(true)
                .securityHandler(securityHandlerFactory.getObject())
                .build());
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }

        org.testcontainers.Testcontainers.exposeHostPorts(SOAP_SERVER.getPort());

        INTEGRATION_CONTAINER = new SyndesisIntegrationRuntimeContainer.Builder()
            .name("end-to-end-soap-api-client")
            .fromIntegration(createIntegration())
            .build()
            .withExposedPorts(SyndesisTestEnvironment.getServerPort(),
                SyndesisTestEnvironment.getManagementPort());
    }

    @Test
    @CitrusTest
    public void shouldInvokeEndpoint(@CitrusResource final TestRunner runner) {
        runner.soap(builder -> builder.server(SOAP_SERVER)
            .receive()
            .endpoint("/alfresco/cmisws/ObjectService")
            .payload(new ClassPathResource("/io/syndesis/test/itest/apiconnector/SOAP_EndToEnd/expected-request.xml")));

        runner.soap(builder -> builder.server(SOAP_SERVER)
            .send()
            .payload(new ClassPathResource("/io/syndesis/test/itest/apiconnector/SOAP_EndToEnd/expected-response.xml")));
    }

    private static Connector createConnector() {
        try (InputStream wsdl = SOAP_API_EndToEnd_IT.class.getResourceAsStream("/io/syndesis/test/itest/apiconnector/SOAP_EndToEnd/cmis.wsdl")) {
            final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()
                .connectorTemplateId("soap-connector-template")
                .putConfiguredProperty("serviceName", "{http://docs.oasis-open.org/ns/cmis/ws/200908/}ObjectService")
                .putConfiguredProperty("portName", "ObjectServicePort")
                .putConfiguredProperty("address", "http://bogus/alfresco/cmisws/ObjectService")
                .putConfiguredProperty("description", "Web Services Connector for service {http://docs.oasis-open.org/ns/cmis/ws/200908/}CMISWebServices")
                .putConfiguredProperty("name", "CMISWebServices")
                .specification(wsdl)
                .build();
            final Connector connector = generator().generate(fetchConnectorTemplateFromDeployment(), connectorSettings);

            final String id = connector.getId().get();
            return new Connector.Builder().createFrom(connector)
                .putConfiguredProperty("specification", "db:" + id)
                .build();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Integration createIntegration() {
        final Connector connector = createConnector();

        final Connection connection = new Connection.Builder()
            .connector(connector)
            .putConfiguredProperty("address", String.format("http://%s:%s/endpoint", GenericContainer.INTERNAL_HOST_HOSTNAME, SOAP_SERVER.getPort()))
            .putConfiguredProperty("authenticationType", "basic")
            .putConfiguredProperty("username", "test")
            // see
            // io.syndesis.test.integration.project.AbstractMavenProjectBuilder.StaticIntegrationResourceManager.decrypt(String)
            .putConfiguredProperty("password", "»ENC:HARDCODED_SECRET")
            .build();

        // openapi.json contains a single operation, i.e. single action
        final ConnectorAction action = connector.getActions().get(0);

        String mapping;
        try {
            mapping = Resources.getResourceAsText("io/syndesis/test/itest/apiconnector/SOAP_EndToEnd/mapping.json");
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
                            .putConfiguredProperty("period", "100000")
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
            .read("$..[?(@['id'] == 'soap-connector-template')]", new TypeRef<List<ConnectorTemplate>>() {
                // type token pattern
            });

        return templates.get(0);
    }

    private static ConnectorGenerator generator() throws IOException {
        final Connector connector = JsonUtils.reader().readValue(Resources.getResourceAsText("META-INF/syndesis/connector/soap.json"),
            Connector.class);

        return new SoapApiConnectorGenerator(connector);
    }

}
