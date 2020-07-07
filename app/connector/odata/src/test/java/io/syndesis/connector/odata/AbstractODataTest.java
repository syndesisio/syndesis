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
package io.syndesis.connector.odata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.connector.odata.server.ODataTestServer;
import io.syndesis.connector.odata.server.ODataTestServer.Options;
import io.syndesis.connector.support.util.PropertyBuilder;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.properties.DefaultPropertiesParser;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.component.properties.PropertiesLookup;
import org.apache.camel.component.properties.PropertiesParser;
import org.apache.camel.spring.SpringCamelContext;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertyResolver;

public abstract class AbstractODataTest implements ODataConstants {

    protected final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected static final int MOCK_TIMEOUT_MILLISECONDS = 60000;

    protected static final String OLINGO4_READ_FROM_ENDPOINT = "olingo4-olingo4-0-0://read";

    protected static final String OLINGO4_READ_TO_ENDPOINT = "olingo4-olingo4-0-1://read";

    @Autowired
    protected ApplicationContext applicationContext;

    protected static ODataTestServer defaultTestServer;

    protected static ODataTestServer authTestServer;

    protected static ODataTestServer sslTestServer;

    protected static ODataTestServer sslAuthTestServer;

    protected static final String REF_SERVICE_URI = "https://services.odata.org/TripPinRESTierService/(S(4gus0w41xmxedlzbywj2srqo))";

    protected CamelContext context;

    @Configuration
    public static class TestConfiguration {
        @Bean
        public PropertiesParser propertiesParser(PropertyResolver propertyResolver) {
            return new DefaultPropertiesParser() {
                @Override
                public String parseProperty(String key, String value, PropertiesLookup properties) {
                    return propertyResolver.getProperty(key);
                }
            };
        }

        @Bean(destroyMethod = "")
        public PropertiesComponent properties(PropertiesParser parser) {
            PropertiesComponent pc = new PropertiesComponent();
            pc.setPropertiesParser(parser);
            return pc;
        }
    }

    @BeforeClass
    public static void startTestServers() throws Exception {
        if (defaultTestServer == null) {
            defaultTestServer = new ODataTestServer();
            defaultTestServer.start();
        }

        if (authTestServer == null) {
            authTestServer = new ODataTestServer(Options.AUTH_USER);
            authTestServer.start();
        }

        if (sslTestServer == null) {
            sslTestServer = new ODataTestServer(Options.SSL);
            sslTestServer.start();
        }

        if (sslAuthTestServer == null) {
            sslAuthTestServer = new ODataTestServer(Options.SSL, Options.AUTH_USER);
            sslAuthTestServer.start();
        }
    }

    /**
     * @return a string representation of the content of the given stream
     */
    public static String streamToString(InputStream inStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(NEW_LINE);
        }

        return builder.toString().trim();
    }

    protected Connector createODataConnector(PropertyBuilder<String> configurePropBuilder,
                                                                                       PropertyBuilder<ConfigurationProperty> propBuilder) {
        Connector.Builder builder = new Connector.Builder()
            .id("odata")
            .name("OData")
            .componentScheme("olingo4")
            .description("Communicate with an OData service")
            .addDependency(Dependency.maven("org.apache.camel:camel-olingo4:latest"));

        if (configurePropBuilder != null) {
            builder.configuredProperties(configurePropBuilder.build());
        }

        if (propBuilder != null) {
            builder.properties(propBuilder.build());
        }

        return builder.build();
    }

    /**
     * Creates a camel context complete with a properties component that handles
     * lookups of secret values such as passwords. Fetches the values from external
     * properties file.
     */
    protected CamelContext createCamelContext() {
        CamelContext ctx = new SpringCamelContext(applicationContext);
        PropertiesComponent pc = new PropertiesComponent("classpath:odata-test-options.properties");
        ctx.setPropertiesComponent(pc);
        return ctx;
    }

    protected Connector createODataConnector(PropertyBuilder<String> configurePropBuilder) {
        return createODataConnector(configurePropBuilder, null);
    }

    protected Integration createIntegration(Step... steps) {

        Flow.Builder flowBuilder = new Flow.Builder();
        for (Step step : steps) {
            flowBuilder.addStep(step);
        }

        Integration odataIntegration = new Integration.Builder()
            .id("i-LTS2tYXwF8odCm87k6gz")
            .name("MyODataInt")
            .addTags("log", "odata")
            .addFlow(flowBuilder.build())
            .build();
        return odataIntegration;
    }

    protected static IntegrationRouteBuilder newIntegrationRouteBuilder(Integration integration) {
        return new IntegrationRouteBuilder("") {
            @Override
            protected Integration loadIntegration() {
                return integration;
            }
        };
    }

    protected String testData(String fileName, Class<?> tgtClass) throws IOException {
        try (InputStream in = tgtClass.getResourceAsStream(fileName)) {
            String expected = streamToString(in);
            return expected;
        }
    }

    protected String testData(String fileName) throws IOException {
        return testData(fileName, getClass());
    }

    protected Step createMockStep() {
        Step mockStep = new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(new ConnectorAction.Builder()
                    .descriptor(new ConnectorDescriptor.Builder()
                                .componentScheme("mock")
                                .putConfiguredProperty("name", "result")
                                .build())
                    .build())
            .build();
        return mockStep;
    }

    @SuppressWarnings( "unchecked" )
    protected <T> T extractJsonFromExchgMsg(MockEndpoint result, int index, Class<T> bodyClass) {
        Object body = result.getExchanges().get(index).getIn().getBody();
        assertTrue(bodyClass.isInstance(body));
        T json = (T) body;
        return json;
    }

    protected String extractJsonFromExchgMsg(MockEndpoint result, int index) {
        return extractJsonFromExchgMsg(result, index, String.class);
    }

    protected MockEndpoint initMockEndpoint() {
        MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
        result.setResultWaitTime(MOCK_TIMEOUT_MILLISECONDS);
        return result;
    }

    protected void testResult(MockEndpoint result, int exchangeIdx, String testDataFile) throws Exception {
        String json = extractJsonFromExchgMsg(result, exchangeIdx);
        String expected = testData(testDataFile);
        assertThatJson(json).isEqualTo(expected);
    }

    @SuppressWarnings( "unchecked" )
    protected void testListResult(MockEndpoint result, int exchangeIdx, String... testDataFiles) throws Exception {
        List<String> json = extractJsonFromExchgMsg(result, exchangeIdx, List.class);
        assertEquals(testDataFiles.length, json.size());
        for (int i = 0; i < testDataFiles.length; ++i) {
            String expected = testData(testDataFiles[i]);
            assertThatJson(json.get(i)).isEqualTo(expected);
        }
    }

}
