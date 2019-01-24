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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.olingo4.Olingo4Endpoint;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.spring.SpringCamelContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.connector.odata.component.ODataComponentFactory;
import io.syndesis.connector.odata.customizer.ODataStartCustomizer;
import io.syndesis.connector.odata.server.ODataTestServer;
import io.syndesis.connector.odata.server.ODataTestServer.Options;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        ODataReadRouteTest.TestConfiguration.class
    },
    properties = {
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG"
    }
)
@TestExecutionListeners(
    listeners = {
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class
    }
)
public class ODataReadRouteTest extends AbstractODataRouteTest {

    private static final int MOCK_TIMEOUT_MILLISECONDS = 3060000;

    private static final String TEST_SERVER_DATA = "test-server-data.json";
    private static final String TEST_SERVER_DATA_WITH_COUNT = "test-server-data-with-count.json";
    private static final String FILTERED_TEST_SERVER_DATA_ID_1 = "filtered-test-server-data-id-1.json";
    private static final String FILTERED_TEST_SERVER_DATA_ID_2 = "filtered-test-server-data-id-2.json";
    private static final String SINGLE_TEST_ENTITY_DATA = "single-test-entity.json";
    private static final String REF_SERVER_PEOPLE_DATA = "ref-server-people-data.json";
    private static final String TEST_SERVER_DATA_EMPTY = "test-server-data-empty.json";

    /**
     * Creates a camel context complete with a properties component that handles
     * lookups of secret values such as passwords. Fetches the values from external
     * properties file.
     *
     * @return CamelContext
     */
    private CamelContext createCamelContext() {
        CamelContext ctx = new SpringCamelContext(applicationContext);
        PropertiesComponent pc = new PropertiesComponent("classpath:odata-test-options.properties");
        ctx.addComponent("properties", pc);
        return ctx;
    }

    private ConnectorAction createReadAction() {
        ConnectorAction odataAction = new ConnectorAction.Builder()
            .description("Read a resource from the server")
             .id("io.syndesis:odata-read-connector")
             .name("Read")
             .descriptor(new ConnectorDescriptor.Builder()
                        .componentScheme("olingo4")
                        .putConfiguredProperty("apiName", "read")
                        .addConnectorCustomizer(ODataStartCustomizer.class.getName())
                        .connectorFactory(ODataComponentFactory.class.getName())
                        .build())
            .build();
        return odataAction;
    }

    private Step createMockStep() {
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

    /**
     * Generates a {@link ConfigurationProperty} for the basic password
     * mimicking the secret operations conducted for real openshift passwords.
     * The actual password is fetched from the camel context's properties component.
     * The defaultValue is just a placeholder as it is checked for non-nullability.
     */
    private ConfigurationProperty basicPasswordProperty() {
        return new ConfigurationProperty.Builder()
              .secret(Boolean.TRUE)
              .defaultValue(BASIC_PASSWORD)
              .build();
    }

    @Test
    public void testSimpleODataRoute() throws Exception {
        final CamelContext context = createCamelContext();

        ODataTestServer server = null;
        try {
            server = new ODataTestServer();
            server.start();

            ConnectorAction odataAction = createReadAction();
            Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, server.serviceUrl()));

            Step odataStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(odataAction)
                .connection(
                            new Connection.Builder()
                                .connector(odataConnector)
                                .build())
                .putConfiguredProperty(METHOD_NAME, server.methodName())
                .build();

            Step mockStep = createMockStep();
            Integration odataIntegration = createIntegration(odataStep, mockStep);

            RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
            context.addRoutes(routes);
            context.start();

            dumpRoutes(context);

            MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            result.setExpectedMessageCount(1);
            result.setResultWaitTime(MOCK_TIMEOUT_MILLISECONDS);
            result.assertIsSatisfied();

            Object body = result.getExchanges().get(0).getIn().getBody();
            assertTrue(body instanceof String);
            String json = (String) body;

            String expected = testData(TEST_SERVER_DATA);
            JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);

        } finally {
            context.stop();
            if (server != null) {
                server.stop();
            }
        }
    }

    @Test
    public void testAuthenticatedODataRoute() throws Exception {
        final CamelContext context = createCamelContext();
        ODataTestServer server = null;
        try {
            server = new ODataTestServer(Options.AUTH_USER);
            server.start();

            ConnectorAction odataAction = createReadAction();
            Connector odataConnector = createODataConnector(
                                                        new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, server.serviceUrl())
                                                            .property(BASIC_USER_NAME, ODataTestServer.USER),
                                                        new PropertyBuilder<ConfigurationProperty>()
                                                            .property(BASIC_PASSWORD, basicPasswordProperty())
                                                        );

            Step odataStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(odataAction)
                .connection(
                            new Connection.Builder()
                                .connector(odataConnector)
                                .build())
                .putConfiguredProperty(METHOD_NAME, server.methodName())
                .build();

            Step mockStep = createMockStep();
            Integration odataIntegration = createIntegration(odataStep, mockStep);

            RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
            context.addRoutes(routes);
            context.start();

            MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            result.setExpectedMessageCount(1);
            result.setResultWaitTime(MOCK_TIMEOUT_MILLISECONDS);
            result.assertIsSatisfied();

            Object body = result.getExchanges().get(0).getIn().getBody();
            assertTrue(body instanceof String);
            String json = (String) body;

            String expected = testData(TEST_SERVER_DATA);
            JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);

        } finally {
            context.stop();
            if (server != null) {
                server.stop();
            }
        }
    }

    @Test
    public void testSSLODataRoute() throws Exception {
        final CamelContext context = createCamelContext();

        ODataTestServer server = null;
        try {
            server = new ODataTestServer(Options.SSL);
            server.start();

            ConnectorAction odataAction = createReadAction();
            Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, server.serviceUrl())
                                                            .property(CLIENT_CERTIFICATE, ODataTestServer.serverCertificate()));

            Step odataStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(odataAction)
                .connection(
                            new Connection.Builder()
                                .connector(odataConnector)
                                .build())
                .putConfiguredProperty(METHOD_NAME, server.methodName())
                .build();

            Step mockStep = createMockStep();
            Integration odataIntegration = createIntegration(odataStep, mockStep);

            RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
            context.addRoutes(routes);
            context.start();

            MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            result.setExpectedMessageCount(1);
            result.setResultWaitTime(MOCK_TIMEOUT_MILLISECONDS);
            result.assertIsSatisfied();

            Object body = result.getExchanges().get(0).getIn().getBody();
            assertTrue(body instanceof String);
            String json = (String) body;
            String expected = testData(TEST_SERVER_DATA);
            JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
        } finally {
            context.stop();
            if (server != null) {
                server.stop();
            }
        }
    }

    @Test
    public void testSSLAuthenticatedODataRoute() throws Exception {
        final CamelContext context = createCamelContext();

        ODataTestServer server = null;
        try {
            server = new ODataTestServer(Options.SSL, Options.AUTH_USER);
            server.start();

            ConnectorAction odataAction = createReadAction();

            Connector odataConnector = createODataConnector(
                                                            new PropertyBuilder<String>()
                                                                .property(SERVICE_URI, server.serviceUrl())
                                                                .property(CLIENT_CERTIFICATE, ODataTestServer.serverCertificate())
                                                                .property(BASIC_USER_NAME, ODataTestServer.USER),
                                                            new PropertyBuilder<ConfigurationProperty>()
                                                                .property(BASIC_PASSWORD, basicPasswordProperty())
                                                            );

            Step odataStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(odataAction)
                .connection(
                            new Connection.Builder()
                                .connector(odataConnector)
                                .build())
                .putConfiguredProperty(METHOD_NAME, server.methodName())
                .build();

            Step mockStep = createMockStep();
            Integration odataIntegration = createIntegration(odataStep, mockStep);

            RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
            context.addRoutes(routes);
            context.start();

            MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            result.setExpectedMessageCount(1);
            result.setResultWaitTime(MOCK_TIMEOUT_MILLISECONDS);
            result.assertIsSatisfied();

            Object body = result.getExchanges().get(0).getIn().getBody();
            assertTrue(body instanceof String);
            String json = (String) body;

            String expected = testData(TEST_SERVER_DATA);
            JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);

        } finally {
            context.stop();
            if (server != null) {
                server.stop();
            }
        }
    }

    @Test
    public void testReferenceODataRoute() throws Exception {
        String serviceUri = "https://services.odata.org/TripPinRESTierService";
        String methodName = "People";
        String queryParam = "$count=true";

        final CamelContext context = new SpringCamelContext(applicationContext);

        try {
            ConnectorAction odataAction = createReadAction();
            Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, serviceUri)
                                                            .property(QUERY_PARAMS, queryParam)
                                                            .property(CLIENT_CERTIFICATE, ODataTestServer.referenceServiceCertificate()));

            Step odataStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(odataAction)
                .connection(
                            new Connection.Builder()
                                .connector(odataConnector)
                                .build())
                .putConfiguredProperty(METHOD_NAME, methodName)
                .build();

            Step mockStep = createMockStep();
            Integration odataIntegration = createIntegration(odataStep, mockStep);

            RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
            context.addRoutes(routes);
            context.start();

            dumpRoutes(context);

            MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            result.setExpectedMessageCount(1);
            result.setResultWaitTime(MOCK_TIMEOUT_MILLISECONDS);
            result.assertIsSatisfied();

            Object body = result.getExchanges().get(0).getIn().getBody();
            assertTrue(body instanceof String);
            String json = (String) body;

            String expected = testData(REF_SERVER_PEOPLE_DATA);
            JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);

        } finally {
            context.stop();
        }
    }

    @Test
    public void testODataRouteWithSimpleQuery() throws Exception {
        final CamelContext context = createCamelContext();
        String queryParams = "$filter=ID eq 1";

        ODataTestServer server = null;
        try {
            server = new ODataTestServer();
            server.start();

            ConnectorAction odataAction = createReadAction();
            Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, server.serviceUrl())
                                                            .property(QUERY_PARAMS, queryParams));

            Step odataStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(odataAction)
                .connection(
                            new Connection.Builder()
                                .connector(odataConnector)
                                .build())
                .putConfiguredProperty(METHOD_NAME, server.methodName())
                .build();

            Step mockStep = createMockStep();
            Integration odataIntegration = createIntegration(odataStep, mockStep);

            RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
            context.addRoutes(routes);
            context.start();

            dumpRoutes(context);

            MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            result.setExpectedMessageCount(1);
            result.setResultWaitTime(MOCK_TIMEOUT_MILLISECONDS);
            result.assertIsSatisfied();

            Object body = result.getExchanges().get(0).getIn().getBody();
            assertTrue(body instanceof String);
            String json = (String) body;

            String expected = testData(FILTERED_TEST_SERVER_DATA_ID_1);
            JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);

        } finally {
            context.stop();
            if (server != null) {
                server.stop();
            }
        }
    }

    @Test
    public void testODataRouteWithCountQuery() throws Exception {
        final CamelContext context = createCamelContext();
        String queryParams = "$count=true";

        ODataTestServer server = null;
        try {
            server = new ODataTestServer();
            server.start();

            ConnectorAction odataAction = createReadAction();
            Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, server.serviceUrl())
                                                            .property(QUERY_PARAMS, queryParams));

            Step odataStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(odataAction)
                .connection(
                            new Connection.Builder()
                                .connector(odataConnector)
                                .build())
                .putConfiguredProperty(METHOD_NAME, server.methodName())
                .build();

            Step mockStep = createMockStep();
            Integration odataIntegration = createIntegration(odataStep, mockStep);

            RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
            context.addRoutes(routes);
            context.start();

            dumpRoutes(context);

            MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            result.setExpectedMessageCount(1);
            result.setResultWaitTime(MOCK_TIMEOUT_MILLISECONDS);
            result.assertIsSatisfied();

            Object body = result.getExchanges().get(0).getIn().getBody();
            assertTrue(body instanceof String);
            String json = (String) body;
            String expected = testData(TEST_SERVER_DATA_WITH_COUNT);
            JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);

        } finally {
            context.stop();
            if (server != null) {
                server.stop();
            }
        }
    }

    @Test
    public void testODataRouteWithMoreComplexQuery() throws Exception {
        final CamelContext context = createCamelContext();
        String queryParams = "$filter=ID le 2&$orderby=ID desc";

        ODataTestServer server = null;
        try {
            server = new ODataTestServer();
            server.start();

            ConnectorAction odataAction = createReadAction();
            Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, server.serviceUrl())
                                                            .property(QUERY_PARAMS, queryParams));

            Step odataStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(odataAction)
                .connection(
                            new Connection.Builder()
                                .connector(odataConnector)
                                .build())
                .putConfiguredProperty(METHOD_NAME, server.methodName())
                .build();

            Step mockStep = createMockStep();
            Integration odataIntegration = createIntegration(odataStep, mockStep);

            RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
            context.addRoutes(routes);
            context.start();

            dumpRoutes(context);

            MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            result.setExpectedMessageCount(1);
            result.setResultWaitTime(MOCK_TIMEOUT_MILLISECONDS);
            result.assertIsSatisfied();

            Object body = result.getExchanges().get(0).getIn().getBody();
            assertTrue(body instanceof String);
            String json = (String) body;

            String expected = testData(FILTERED_TEST_SERVER_DATA_ID_2);
            JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);

        } finally {
            context.stop();
            if (server != null) {
                server.stop();
            }
        }
    }

    @Test
    public void testODataRouteWithKeyPredicate() throws Exception {
        final CamelContext context = createCamelContext();
        String keyPredicate = "1";

        ODataTestServer server = null;
        try {
            server = new ODataTestServer();
            server.start();

            ConnectorAction odataAction = createReadAction();
            Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, server.serviceUrl())
                                                            .property(KEY_PREDICATE, keyPredicate));

            Step odataStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(odataAction)
                .connection(
                            new Connection.Builder()
                                .connector(odataConnector)
                                .build())
                .putConfiguredProperty(METHOD_NAME, server.methodName())
                .build();

            Step mockStep = createMockStep();
            Integration odataIntegration = createIntegration(odataStep, mockStep);

            RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
            context.addRoutes(routes);
            context.start();

            dumpRoutes(context);

            MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            result.setExpectedMessageCount(1);
            result.setResultWaitTime(MOCK_TIMEOUT_MILLISECONDS);
            result.assertIsSatisfied();

            Object body = result.getExchanges().get(0).getIn().getBody();
            assertTrue(body instanceof String);
            String json = (String) body;
            String expected = testData(SINGLE_TEST_ENTITY_DATA);
            JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);

        } finally {
            context.stop();
            if (server != null) {
                server.stop();
            }
        }
    }

    @Test
    public void testODataRouteWithKeyPredicateWithBrackets() throws Exception {
        final CamelContext context = createCamelContext();
        String keyPredicate = "(1)";

        ODataTestServer server = null;
        try {
            server = new ODataTestServer();
            server.start();

            ConnectorAction odataAction = createReadAction();
            Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, server.serviceUrl())
                                                            .property(KEY_PREDICATE, keyPredicate));

            Step odataStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(odataAction)
                .connection(
                            new Connection.Builder()
                                .connector(odataConnector)
                                .build())
                .putConfiguredProperty(METHOD_NAME, server.methodName())
                .build();

            Step mockStep = createMockStep();
            Integration odataIntegration = createIntegration(odataStep, mockStep);

            RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
            context.addRoutes(routes);
            context.start();

            dumpRoutes(context);

            MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            result.setExpectedMessageCount(1);
            result.setResultWaitTime(MOCK_TIMEOUT_MILLISECONDS);
            result.assertIsSatisfied();

            Object body = result.getExchanges().get(0).getIn().getBody();
            assertTrue(body instanceof String);
            String json = (String) body;
            String expected = testData(SINGLE_TEST_ENTITY_DATA);
            JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);

        } finally {
            context.stop();
            if (server != null) {
                server.stop();
            }
        }
    }

    @Test
    public void testODataRouteWithConsumerDelayProperties() throws Exception {
        String initialDelayValue = "5000";
        String delayValue = "30000";

        final CamelContext context = createCamelContext();

        ODataTestServer server = null;
        try {
            server = new ODataTestServer();
            server.start();

            ConnectorAction odataAction = createReadAction();
            Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, server.serviceUrl())
                                                            .property(INITIAL_DELAY, initialDelayValue)
                                                            .property(DELAY, delayValue));

            Step odataStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(odataAction)
                .connection(
                            new Connection.Builder()
                                .connector(odataConnector)
                                .build())
                .putConfiguredProperty(METHOD_NAME, server.methodName())
                .build();

            Step mockStep = createMockStep();
            Integration odataIntegration = createIntegration(odataStep, mockStep);

            RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
            context.addRoutes(routes);
            context.start();

            dumpRoutes(context);

            MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            result.setExpectedMessageCount(1);
            result.setResultWaitTime(MOCK_TIMEOUT_MILLISECONDS);
            result.assertIsSatisfied();

            Object body = result.getExchanges().get(0).getIn().getBody();
            assertTrue(body instanceof String);
            String json = (String) body;

            String expected = testData(TEST_SERVER_DATA);
            JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);

            Olingo4Endpoint olingo4Endpoint = context.getEndpoint("olingo4-olingo4-0-0://read/Products", Olingo4Endpoint.class);
            assertNotNull(olingo4Endpoint);
            Map<String, Object> consumerProperties = olingo4Endpoint.getConsumerProperties();
            assertNotNull(consumerProperties);
            assertTrue(consumerProperties.size() > 0);
            assertEquals(delayValue, consumerProperties.get(DELAY));
            assertEquals(initialDelayValue, consumerProperties.get(INITIAL_DELAY));

        } finally {
            context.stop();
            if (server != null) {
                server.stop();
            }
        }
    }

    @Test
    public void testODataRouteAlreadySeen() throws Exception {
        String backoffIdleThreshold = "1";
        String backoffMultiplier = "2";

        final CamelContext context = createCamelContext();

        ODataTestServer server = null;
        try {
            server = new ODataTestServer();
            server.start();

            ConnectorAction odataAction = createReadAction();
            Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, server.serviceUrl())
                                                            .property(FILTER_ALREADY_SEEN, Boolean.TRUE.toString())
                                                            .property(BACKOFF_IDLE_THRESHOLD, backoffIdleThreshold)
                                                            .property(BACKOFF_MULTIPLIER, backoffMultiplier));

            Step odataStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(odataAction)
                .connection(
                            new Connection.Builder()
                                .connector(odataConnector)
                                .build())
                .putConfiguredProperty(METHOD_NAME, server.methodName())
                .build();

            Step mockStep = createMockStep();
            Integration odataIntegration = createIntegration(odataStep, mockStep);

            RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
            context.addRoutes(routes);
            context.start();

            dumpRoutes(context);

            int expectedMsgCount = 3;
            MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            result.setExpectedMessageCount(expectedMsgCount);
            result.setResultWaitTime(MOCK_TIMEOUT_MILLISECONDS);
            result.assertIsSatisfied();

            for (int i = 0; i < expectedMsgCount; ++i) {
                Object body = result.getExchanges().get(i).getIn().getBody();
                assertTrue(body instanceof String);
                String json = (String) body;

                if (i == 0) {
                    //
                    // Expect all results to be returned in the first polling message
                    //
                    String expected = testData(TEST_SERVER_DATA);
                    JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
                }
                else {
                    //
                    // Subsequent polling messages should be empty
                    //
                    String expected = testData(TEST_SERVER_DATA_EMPTY);
                    JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
                }
            }

            //
            // Check backup consumer options carried through to olingo4 component
            //
            Olingo4Endpoint olingo4Endpoint = context.getEndpoint("olingo4-olingo4-0-0://read/Products", Olingo4Endpoint.class);
            assertNotNull(olingo4Endpoint);
            Map<String, Object> consumerProperties = olingo4Endpoint.getConsumerProperties();
            assertNotNull(consumerProperties);
            assertTrue(consumerProperties.size() > 0);
            assertEquals(backoffIdleThreshold, consumerProperties.get(BACKOFF_IDLE_THRESHOLD));
            assertEquals(backoffMultiplier, consumerProperties.get(BACKOFF_MULTIPLIER));

        } finally {
            context.stop();
            if (server != null) {
                server.stop();
            }
        }
    }
}
