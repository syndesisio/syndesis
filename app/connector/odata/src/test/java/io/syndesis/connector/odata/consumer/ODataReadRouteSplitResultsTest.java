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
package io.syndesis.connector.odata.consumer;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Map;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.olingo4.Olingo4Endpoint;
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
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.odata.server.ODataTestServer;
import io.syndesis.connector.support.util.PropertyBuilder;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        ODataReadRouteSplitResultsTest.TestConfiguration.class
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
public class ODataReadRouteSplitResultsTest extends AbstractODataReadRouteTest {

    //
    // Set the split results to true
    //
    public ODataReadRouteSplitResultsTest() throws Exception {
        super(true);
    }

    @Test
    public void testSimpleODataRoute() throws Exception {
        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                                .property(SERVICE_URI, defaultTestServer.servicePlainUri()));

        Step odataStep = createODataStep(odataConnector, defaultTestServer.resourcePath());
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(defaultTestServer.getResultCount());

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, TEST_SERVER_DATA_1);
        testResult(result, 1, TEST_SERVER_DATA_2);
        testResult(result, 2, TEST_SERVER_DATA_3);
    }

    @Test
    public void testAuthenticatedODataRoute() throws Exception {
        Connector odataConnector = createODataConnector(
                                                    new PropertyBuilder<String>()
                                                        .property(SERVICE_URI, authTestServer.servicePlainUri())
                                                        .property(BASIC_USER_NAME, ODataTestServer.USER),
                                                    new PropertyBuilder<ConfigurationProperty>()
                                                        .property(BASIC_PASSWORD, basicPasswordProperty())
                                                    );

        Step odataStep = createODataStep(odataConnector, authTestServer.resourcePath());
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(authTestServer.getResultCount());

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, TEST_SERVER_DATA_1);
        testResult(result, 1, TEST_SERVER_DATA_2);
        testResult(result, 2, TEST_SERVER_DATA_3);
    }

    /**
     * Needs to supply server certificate since the server is unknown to the default
     * certificate authorities that is loaded into the keystore by default
     */
    @Test
    public void testSSLODataRoute() throws Exception {
        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, sslTestServer.serviceSSLUri())
                                                            .property(SERVER_CERTIFICATE, ODataTestServer.serverCertificate()));

        Step odataStep = createODataStep(odataConnector, sslTestServer.resourcePath());
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(sslTestServer.getResultCount());

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, TEST_SERVER_DATA_1);
        testResult(result, 1, TEST_SERVER_DATA_2);
        testResult(result, 2, TEST_SERVER_DATA_3);
    }

    @Test
    public void testEmptyServiceUri() throws Exception {
        Connector odataConnector = createODataConnector(new PropertyBuilder<String>());

        Step odataStep = createODataStep(odataConnector, defaultTestServer.resourcePath());
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        assertThatExceptionOfType(RuntimeCamelException.class)
            .isThrownBy(() -> {
                context.start();
            })
            .withMessageContaining("serviceUri is not set");
    }

    @Test
    public void testEndSlashOnServiceUri() throws Exception {
        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                                .property(SERVICE_URI, defaultTestServer.servicePlainUri() + FORWARD_SLASH));

        Step odataStep = createODataStep(odataConnector, defaultTestServer.resourcePath());
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(defaultTestServer.getResultCount());

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, TEST_SERVER_DATA_1);
        testResult(result, 1, TEST_SERVER_DATA_2);
        testResult(result, 2, TEST_SERVER_DATA_3);

        Olingo4Endpoint olingo4Endpoint = context.getEndpoint(OLINGO4_READ_FROM_ENDPOINT, Olingo4Endpoint.class);
        assertNotNull(olingo4Endpoint);
        String endpointServiceURI = olingo4Endpoint.getConfiguration().getServiceUri();
        assertEquals(defaultTestServer.servicePlainUri(), endpointServiceURI);
    }

    /**
     * Needs to supply server certificate since the server is unknown to the default
     * certificate authorities that is loaded into the keystore by default
     */
    @Test
    public void testSSLAuthenticatedODataRoute() throws Exception {
        Connector odataConnector = createODataConnector(
                                                        new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, sslAuthTestServer.serviceSSLUri())
                                                            .property(SERVER_CERTIFICATE, ODataTestServer.serverCertificate())
                                                            .property(BASIC_USER_NAME, ODataTestServer.USER),
                                                        new PropertyBuilder<ConfigurationProperty>()
                                                            .property(BASIC_PASSWORD, basicPasswordProperty())
                                                        );

        Step odataStep = createODataStep(odataConnector, sslAuthTestServer.resourcePath());
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(sslAuthTestServer.getResultCount());

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, TEST_SERVER_DATA_1);
        testResult(result, 1, TEST_SERVER_DATA_2);
        testResult(result, 2, TEST_SERVER_DATA_3);
    }

    @Test
    public void testReferenceODataRouteWithCertificate() throws Exception {
        String resourcePath = "People";
        String queryParam = "$count=true";

        context = new SpringCamelContext(applicationContext);

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, REF_SERVICE_URI)
                                                            .property(QUERY_PARAMS, queryParam)
                                                            .property(SERVER_CERTIFICATE, ODataTestServer.referenceServiceCertificate()));

        Step odataStep = createODataStep(odataConnector, resourcePath);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(1);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, REF_SERVER_PEOPLE_DATA_1);
    }

    /**
     * The reference server has a certificate trusted by the root certificates already
     * in the java cacerts keystore. Therefore, it should not be necessary to supply
     * a server certifcate.
     */
    @Test
    public void testReferenceODataRouteWithoutCertificate() throws Exception {
        String resourcePath = "People";
        String queryParam = "$count=true";

        context = new SpringCamelContext(applicationContext);

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, REF_SERVICE_URI)
                                                            .property(QUERY_PARAMS, queryParam));

        Step odataStep = createODataStep(odataConnector, resourcePath);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(1);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, REF_SERVER_PEOPLE_DATA_1);
    }

    @Test
    public void testReferenceODataRouteKeyPredicate() throws Exception {
        String resourcePath = "Airports";
        String keyPredicate = "KLAX";

        context = new SpringCamelContext(applicationContext);

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, REF_SERVICE_URI)
                                                            .property(KEY_PREDICATE, keyPredicate));

        Step odataStep = createODataStep(odataConnector, resourcePath);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(1);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, REF_SERVER_AIRPORT_DATA_KLAX);
    }

    @Test
    public void testReferenceODataRouteKeyPredicateAndSubPredicate() throws Exception {
        String resourcePath = "Airports";
        String keyPredicate = "('KLAX')/Location";

        context = new SpringCamelContext(applicationContext);

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, REF_SERVICE_URI)
                                                            .property(KEY_PREDICATE, keyPredicate));

        Step odataStep = createODataStep(odataConnector, resourcePath);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(1);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, REF_SERVER_PEOPLE_DATA_KLAX_LOC);
    }

    /*
     * The Address property in Airports dataset is complex. Tests
     * support for complex properties is working against ref server
     */
    @Test
    public void testReferenceODataRouteComplexValue() throws Exception {
        String resourcePath = "Airports";

        context = new SpringCamelContext(applicationContext);

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, REF_SERVICE_URI));

        Step odataStep = createODataStep(odataConnector, resourcePath);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(1);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, REF_SERVER_AIRPORT_DATA_1);
    }

    /*
     * Tests a query with $expand and $filter.
     */
    @Test
    public void testReferenceODataRouteQueryWithFilterAndExpand() throws Exception {
        String resourcePath = "People";

        context = new SpringCamelContext(applicationContext);

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, REF_SERVICE_URI)
                                                            .property(QUERY_PARAMS, "$filter=LastName eq 'Whyte'&$expand=Trips"));

        Step odataStep = createODataStep(odataConnector, resourcePath);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(1);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, REF_SERVER_PEOPLE_DATA_1_EXPANDED_TRIPS);
    }

    @Test
    public void testODataRouteWithSimpleQuery() throws Exception {
        String queryParams = "$filter=ID eq 1";

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, defaultTestServer.servicePlainUri())
                                                            .property(QUERY_PARAMS, queryParams));

        Step odataStep = createODataStep(odataConnector, defaultTestServer.resourcePath());
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(1);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, TEST_SERVER_DATA_1);
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void testODataRouteWithCountQuery() throws Exception {
        String queryParams = "$count=true";
        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, defaultTestServer.servicePlainUri())
                                                            .property(QUERY_PARAMS, queryParams));

        Step odataStep = createODataStep(odataConnector, defaultTestServer.resourcePath());
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(1);

        context.start();

        result.assertIsSatisfied();
        String json = extractJsonFromExchgMsg(result, 0, String.class);
        assertNotNull(json);
        String expected = testData(TEST_SERVER_DATA_1_WITH_COUNT);
        JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void testODataRouteWithMoreComplexQuery() throws Exception {
        String queryParams = "$filter=ID le 2&$orderby=ID desc";

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, defaultTestServer.servicePlainUri())
                                                            .property(QUERY_PARAMS, queryParams));

        Step odataStep = createODataStep(odataConnector, defaultTestServer.resourcePath());
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(2);

        context.start();

        result.assertIsSatisfied();
        String json = extractJsonFromExchgMsg(result, 0, String.class);
        assertNotNull(json);
        String expected = testData(TEST_SERVER_DATA_2);
        JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);

        json = extractJsonFromExchgMsg(result, 1, String.class);
        assertNotNull(json);
        expected = testData(TEST_SERVER_DATA_1);
        JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
    }

    @Test
    public void testODataRouteWithKeyPredicate() throws Exception {
        String keyPredicate = "1";
        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, defaultTestServer.servicePlainUri())
                                                            .property(KEY_PREDICATE, keyPredicate));

        Step odataStep = createODataStep(odataConnector, defaultTestServer.resourcePath());
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(1);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, TEST_SERVER_DATA_1);
    }

    @Test
    public void testODataRouteWithKeyPredicateWithBrackets() throws Exception {
        String keyPredicate = "(1)";
        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, defaultTestServer.servicePlainUri())
                                                            .property(KEY_PREDICATE, keyPredicate));

        Step odataStep = createODataStep(odataConnector, defaultTestServer.resourcePath());
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(1);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, TEST_SERVER_DATA_1);
    }

    @Test
    public void testODataRouteWithConsumerDelayProperties() throws Exception {
        String initialDelayValue = "500";
        String delayValue = "1000";

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, defaultTestServer.servicePlainUri())
                                                            .property(INITIAL_DELAY, initialDelayValue)
                                                            .property(DELAY, delayValue));

        Step odataStep = createODataStep(odataConnector, defaultTestServer.resourcePath());
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(defaultTestServer.getResultCount());

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, TEST_SERVER_DATA_1);
        testResult(result, 1, TEST_SERVER_DATA_2);
        testResult(result, 2, TEST_SERVER_DATA_3);

        Olingo4Endpoint olingo4Endpoint = context.getEndpoint(OLINGO4_READ_FROM_ENDPOINT, Olingo4Endpoint.class);
        assertNotNull(olingo4Endpoint);
        Map<String, Object> consumerProperties = olingo4Endpoint.getConsumerProperties();
        assertNotNull(consumerProperties);
        assertTrue(consumerProperties.size() > 0);
        assertEquals(delayValue, consumerProperties.get(DELAY));
        assertEquals(initialDelayValue, consumerProperties.get(INITIAL_DELAY));
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void testODataRouteAlreadySeen() throws Exception {
        String backoffIdleThreshold = "1";
        String backoffMultiplier = "1";

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, defaultTestServer.servicePlainUri())
                                                            .property(FILTER_ALREADY_SEEN, Boolean.TRUE.toString())
                                                            .property(BACKOFF_IDLE_THRESHOLD, backoffIdleThreshold)
                                                            .property(BACKOFF_MULTIPLIER, backoffMultiplier));

        Step odataStep = createODataStep(odataConnector, defaultTestServer.resourcePath());
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        int expectedMsgCount = defaultTestServer.getResultCount();
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(expectedMsgCount);

        context.start();

        result.assertIsSatisfied();

        for (int i = 0; i < expectedMsgCount; ++i) {
            String json = extractJsonFromExchgMsg(result, i, String.class);
            assertNotNull(json);

            String expected;
            switch (i) {
                case 0:
                    expected = testData(TEST_SERVER_DATA_1);
                    JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
                    break;
                case 1:
                    expected = testData(TEST_SERVER_DATA_2);
                    JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
                    break;
                case 2:
                    expected = testData(TEST_SERVER_DATA_3);
                    JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
                    break;
                default:
                    //
                    // Subsequent polling messages should be empty
                    //
                    expected = testData(TEST_SERVER_DATA_EMPTY);
                    JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
            }
        }

        //
        // Check backup consumer options carried through to olingo4 component
        //
        Olingo4Endpoint olingo4Endpoint = context.getEndpoint(OLINGO4_READ_FROM_ENDPOINT, Olingo4Endpoint.class);
        assertNotNull(olingo4Endpoint);
        Map<String, Object> consumerProperties = olingo4Endpoint.getConsumerProperties();
        assertNotNull(consumerProperties);
        assertTrue(consumerProperties.size() > 0);
        assertEquals(backoffIdleThreshold, consumerProperties.get(BACKOFF_IDLE_THRESHOLD));
        assertEquals(backoffMultiplier, consumerProperties.get(BACKOFF_MULTIPLIER));
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void testReferenceODataRouteAlreadySeenWithKeyPredicate() throws Exception {
        String resourcePath = "Airports";
        String keyPredicate = "KSFO";
        String backoffIdleThreshold = "1";
        String backoffMultiplier = "1";

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, REF_SERVICE_URI)
                                                            .property(KEY_PREDICATE, keyPredicate)
                                                            .property(FILTER_ALREADY_SEEN, Boolean.TRUE.toString())
                                                            .property(BACKOFF_IDLE_THRESHOLD, backoffIdleThreshold)
                                                            .property(BACKOFF_MULTIPLIER, backoffMultiplier));

        Step odataStep = createODataStep(odataConnector, resourcePath);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        int expectedMsgCount = 1;
        MockEndpoint result = initMockEndpoint();
        result.setExpectedMessageCount(expectedMsgCount);

        context.start();

        result.assertIsSatisfied();

        String json = extractJsonFromExchgMsg(result, 0, String.class);
        assertNotNull(json);
        String expected = testData(REF_SERVER_AIRPORT_DATA_1);
        JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);

        //
        // Check backup consumer options carried through to olingo4 component
        //
        Olingo4Endpoint olingo4Endpoint = context.getEndpoint(OLINGO4_READ_FROM_ENDPOINT, Olingo4Endpoint.class);
        assertNotNull(olingo4Endpoint);
        Map<String, Object> consumerProperties = olingo4Endpoint.getConsumerProperties();
        assertNotNull(consumerProperties);
        assertTrue(consumerProperties.size() > 0);
        assertEquals(backoffIdleThreshold, consumerProperties.get(BACKOFF_IDLE_THRESHOLD));
        assertEquals(backoffMultiplier, consumerProperties.get(BACKOFF_MULTIPLIER));
    }
}
