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
package io.syndesis.connector.odata2.consumer;

import java.util.Map;

import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.odata2.server.Certificates;
import io.syndesis.connector.odata2.server.ODataTestServer;
import io.syndesis.connector.support.util.PropertyBuilder;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.olingo2.Olingo2Endpoint;
import org.apache.camel.spring.SpringCamelContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext
@ExtendWith(SpringExtension.class)
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
                                                                .property(SERVICE_URI, odataTestServer.getServiceUri()));

        Step odataStep = createODataStep(odataConnector, MANUFACTURERS);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(2);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, TEST_SERVER_DATA_1);
        testResult(result, 1, TEST_SERVER_DATA_2);
    }

    @Test
    public void testAuthenticatedODataRoute() throws Exception {
        Connector odataConnector = createODataConnector(
                                                    new PropertyBuilder<String>()
                                                        .property(SERVICE_URI, odataTestServer.getServiceUri())
                                                        .property(BASIC_USER_NAME, ODataTestServer.USER),
                                                    new PropertyBuilder<ConfigurationProperty>()
                                                        .property(BASIC_PASSWORD, basicPasswordProperty())
                                                    );

        Step odataStep = createODataStep(odataConnector, MANUFACTURERS);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(2);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, TEST_SERVER_DATA_1);
        testResult(result, 1, TEST_SERVER_DATA_2);
    }

    /**
     * Needs to supply server certificate since the server is unknown to the default
     * certificate authorities that is loaded into the keystore by default
     */
    @Test
    public void testSSLODataRoute() throws Exception {
        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, odataTestServer.getServiceUri())
                                                            .property(SERVER_CERTIFICATE, Certificates.TEST_SERVICE.get()));

        Step odataStep = createODataStep(odataConnector, MANUFACTURERS);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(2);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, TEST_SERVER_DATA_1);
        testResult(result, 1, TEST_SERVER_DATA_2);
    }

    @Test
    public void testEmptyServiceUri() throws Exception {
        Connector odataConnector = createODataConnector(new PropertyBuilder<>());

        Step odataStep = createODataStep(odataConnector, MANUFACTURERS);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        assertThatExceptionOfType(RuntimeCamelException.class)
            .isThrownBy(() -> {
                context.start();
            })
            .withMessageContaining("due to: serviceUri");
    }

    @Test
    public void testEndSlashOnServiceUri() throws Exception {
        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                                .property(SERVICE_URI, odataTestServer.getServiceUri() + FORWARD_SLASH));

        Step odataStep = createODataStep(odataConnector, MANUFACTURERS);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(2);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, TEST_SERVER_DATA_1);
        testResult(result, 1, TEST_SERVER_DATA_2);

        Olingo2Endpoint olingo2Endpoint = context.getEndpoint(OLINGO2_READ_FROM_ENDPOINT, Olingo2Endpoint.class);
        assertNotNull(olingo2Endpoint);
        String endpointServiceURI = olingo2Endpoint.getConfiguration().getServiceUri();
        assertEquals(odataTestServer.getServiceUri(), endpointServiceURI);
    }

    /**
     * Needs to supply server certificate since the server is unknown to the default
     * certificate authorities that is loaded into the keystore by default
     */
    @Test
    public void testSSLAuthenticatedODataRoute() throws Exception {
        Connector odataConnector = createODataConnector(
                                                        new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, odataTestServer.getServiceUri())
                                                            .property(SERVER_CERTIFICATE, Certificates.TEST_SERVICE.get())
                                                            .property(BASIC_USER_NAME, ODataTestServer.USER),
                                                        new PropertyBuilder<ConfigurationProperty>()
                                                            .property(BASIC_PASSWORD, basicPasswordProperty())
                                                        );

        Step odataStep = createODataStep(odataConnector, MANUFACTURERS);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(2);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, TEST_SERVER_DATA_1);
        testResult(result, 1, TEST_SERVER_DATA_2);
    }

    @Test
    public void testReferenceODataRouteWithCertificate() throws Exception {
        context = new SpringCamelContext(applicationContext);

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, REF_SERVICE_URI)
                                                            .property(SERVER_CERTIFICATE, Certificates.REFERENCE_SERVICE.get()));

        Step odataStep = createODataStep(odataConnector, PRODUCTS);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(1);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, REF_SERVER_PRODUCT_DATA_1);
    }

    /**
     * The reference server has a certificate trusted by the root certificates already
     * in the java cacerts keystore. Therefore, it should not be necessary to supply
     * a server certifcate.
     */
    @Test
    public void testReferenceODataRouteWithoutCertificate() throws Exception {
        context = new SpringCamelContext(applicationContext);

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, REF_SERVICE_URI));

        Step odataStep = createODataStep(odataConnector, PRODUCTS);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(1);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, REF_SERVER_PRODUCT_DATA_1);
    }

    @Test
    public void testReferenceODataRouteKeyPredicate() throws Exception {
        String keyPredicate = "1/Address";

        context = new SpringCamelContext(applicationContext);

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, REF_SERVICE_URI)
                                                            .property(KEY_PREDICATE, keyPredicate));

        Step odataStep = createODataStep(odataConnector, SUPPLIERS);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(1);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, REF_SERVER_SUPPLIER_DATA_1_ADDRESS);
    }

    @Test
    public void testReferenceODataRouteKeyPredicateAndSubPredicate() throws Exception {
        String keyPredicate = "(1)/Address/Street";

        context = new SpringCamelContext(applicationContext);

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, REF_SERVICE_URI)
                                                            .property(KEY_PREDICATE, keyPredicate));

        Step odataStep = createODataStep(odataConnector, SUPPLIERS);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(1);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, REF_SERVER_SUPPLIER_DATA_1_ADDRESS_STREET);
    }

    /*
     * The Address property in Airports dataset is complex. Tests
     * support for complex properties is working against ref server
     */
    @Test
    public void testReferenceODataRouteComplexValue() throws Exception {
        context = new SpringCamelContext(applicationContext);

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, REF_SERVICE_URI));

        Step odataStep = createODataStep(odataConnector, SUPPLIERS);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(1);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, REF_SERVER_SUPPLIER_DATA_1);
    }

    /*
     * Tests a query with $expand and $filter.
     */
    @Test
    public void testReferenceODataRouteQueryWithFilterAndExpand() throws Exception {
        context = new SpringCamelContext(applicationContext);

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, REF_SERVICE_URI)
                                                            .property(QUERY_PARAMS, "$filter=Name eq 'Exotic Liquids'&$expand=Products"));

        Step odataStep = createODataStep(odataConnector, SUPPLIERS);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(1);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, REF_SERVER_SUPPLIER_DATA_1_EXPANDED);
    }

    @Test
    public void testODataRouteWithSimpleQuery() throws Exception {
        String queryParams = "$filter=Id eq '1'";

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, odataTestServer.getServiceUri())
                                                            .property(QUERY_PARAMS, queryParams));

        Step odataStep = createODataStep(odataConnector, MANUFACTURERS);
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
    public void testODataRouteWithCountQuery() throws Exception {
        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, odataTestServer.getServiceUri())
                                                            .property(QUERY_PARAMS, COUNT_PARAM));

        Step odataStep = createODataStep(odataConnector, MANUFACTURERS);
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

    @Test
    public void testODataRouteWithMoreComplexQuery() throws Exception {
        String queryParams = "$filter=Id le '2'&$orderby=Id desc";

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, odataTestServer.getServiceUri())
                                                            .property(QUERY_PARAMS, queryParams));

        Step odataStep = createODataStep(odataConnector, MANUFACTURERS);
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
        String keyPredicate = "'1'";
        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, odataTestServer.getServiceUri())
                                                            .property(KEY_PREDICATE, keyPredicate));

        Step odataStep = createODataStep(odataConnector, MANUFACTURERS);
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
        String keyPredicate = "('1')";
        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, odataTestServer.getServiceUri())
                                                            .property(KEY_PREDICATE, keyPredicate));

        Step odataStep = createODataStep(odataConnector, MANUFACTURERS);
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
                                                            .property(SERVICE_URI, odataTestServer.getServiceUri())
                                                            .property(INITIAL_DELAY, initialDelayValue)
                                                            .property(DELAY, delayValue));

        Step odataStep = createODataStep(odataConnector, MANUFACTURERS);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(2);

        context.start();

        result.assertIsSatisfied();
        testResult(result, 0, TEST_SERVER_DATA_1);
        testResult(result, 1, TEST_SERVER_DATA_2);

        Olingo2Endpoint olingo2Endpoint = context.getEndpoint(OLINGO2_READ_FROM_ENDPOINT, Olingo2Endpoint.class);
        assertNotNull(olingo2Endpoint);
        Map<String, Object> schedulerProperties = olingo2Endpoint.getConsumerProperties();
        assertNotNull(schedulerProperties);
        assertTrue(schedulerProperties.size() > 0);
        assertEquals(delayValue, schedulerProperties.get(DELAY));
        assertEquals(initialDelayValue, schedulerProperties.get(INITIAL_DELAY));
    }

    @Test
    public void testODataRouteAlreadySeen() throws Exception {
        String backoffIdleThreshold = "1";
        String backoffMultiplier = "1";

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, odataTestServer.getServiceUri())
                                                            .property(FILTER_ALREADY_SEEN, Boolean.TRUE.toString())
                                                            .property(BACKOFF_IDLE_THRESHOLD, backoffIdleThreshold)
                                                            .property(BACKOFF_MULTIPLIER, backoffMultiplier));

        Step odataStep = createODataStep(odataConnector, MANUFACTURERS);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        int expectedMsgCount = 2;
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
                default:
                    //
                    // Subsequent polling messages should be empty
                    //
                    expected = testData(TEST_SERVER_DATA_EMPTY);
                    JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
            }
        }

        //
        // Check backup consumer options carried through to olingo2 component
        //
        Olingo2Endpoint olingo2Endpoint = context.getEndpoint(OLINGO2_READ_FROM_ENDPOINT, Olingo2Endpoint.class);
        assertNotNull(olingo2Endpoint);
        Map<String, Object> consumerProperties = olingo2Endpoint.getConsumerProperties();
        assertNotNull(consumerProperties);
        assertTrue(consumerProperties.size() > 0);
        assertEquals(backoffIdleThreshold, consumerProperties.get(BACKOFF_IDLE_THRESHOLD));
        assertEquals(backoffMultiplier, consumerProperties.get(BACKOFF_MULTIPLIER));
    }

    @Test
    public void testReferenceODataRouteAlreadySeenWithKeyPredicate() throws Exception {
        String backoffIdleThreshold = "1";
        String backoffMultiplier = "1";

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, REF_SERVICE_URI)
                                                            .property(FILTER_ALREADY_SEEN, Boolean.TRUE.toString())
                                                            .property(BACKOFF_IDLE_THRESHOLD, backoffIdleThreshold)
                                                            .property(BACKOFF_MULTIPLIER, backoffMultiplier));

        Step odataStep = createODataStep(odataConnector, SUPPLIERS);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        int expectedMsgCount = 2;
        MockEndpoint result = initMockEndpoint();
        result.setExpectedMessageCount(expectedMsgCount);

        context.start();

        result.assertIsSatisfied();

        String json = extractJsonFromExchgMsg(result, 0, String.class);
        assertNotNull(json);
        String expected = testData(REF_SERVER_SUPPLIER_DATA_1);
        JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);

        json = extractJsonFromExchgMsg(result, 1, String.class);
        assertNotNull(json);
        expected = testData(REF_SERVER_SUPPLIER_DATA_2);
        JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);

        //
        // Check backup consumer options carried through to olingo2 component
        //
        Olingo2Endpoint olingo2Endpoint = context.getEndpoint(OLINGO2_READ_FROM_ENDPOINT, Olingo2Endpoint.class);
        assertNotNull(olingo2Endpoint);
        Map<String, Object> consumerProperties = olingo2Endpoint.getConsumerProperties();
        assertNotNull(consumerProperties);
        assertTrue(consumerProperties.size() > 0);
        assertEquals(backoffIdleThreshold, consumerProperties.get(BACKOFF_IDLE_THRESHOLD));
        assertEquals(backoffMultiplier, consumerProperties.get(BACKOFF_MULTIPLIER));
    }
}
