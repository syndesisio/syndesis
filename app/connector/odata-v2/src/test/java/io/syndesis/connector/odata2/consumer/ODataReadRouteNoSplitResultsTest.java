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

import java.util.List;
import java.util.Map;

import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.odata2.server.Certificates;
import io.syndesis.connector.odata2.server.ODataTestServer;
import io.syndesis.connector.support.util.PropertyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.olingo2.Olingo2Endpoint;
import org.apache.camel.spring.SpringCamelContext;
import org.junit.Ignore;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        ODataReadRouteNoSplitResultsTest.TestConfiguration.class
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
public class ODataReadRouteNoSplitResultsTest extends AbstractODataReadRouteTest {

    //
    // Set the split results to false
    //
    public ODataReadRouteNoSplitResultsTest() throws Exception {
        super(false);
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
        testListResult(result, 0, TEST_SERVER_DATA_1, TEST_SERVER_DATA_2);
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
        testListResult(result, 0, TEST_SERVER_DATA_1, TEST_SERVER_DATA_2);
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
        testListResult(result, 0, TEST_SERVER_DATA_1, TEST_SERVER_DATA_2);
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
        testListResult(result, 0, TEST_SERVER_DATA_1, TEST_SERVER_DATA_2);
    }

    @Test
    @Ignore("Run manually as not strictly required")
    public void testReferenceODataRoute() throws Exception {
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

        @SuppressWarnings( "unchecked" )
        List<String> json = extractJsonFromExchgMsg(result, 0, List.class);
        assertEquals(9, json.size());

        String expected = testData(REF_SERVER_PRODUCT_DATA_1);
        JSONAssert.assertEquals(expected, json.get(0), JSONCompareMode.LENIENT);
        expected = testData(REF_SERVER_PRODUCT_DATA_2);
        JSONAssert.assertEquals(expected, json.get(1), JSONCompareMode.LENIENT);
    }

    @Test
    public void testReferenceODataRouteWithCount() throws Exception {
        context = new SpringCamelContext(applicationContext);

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, REF_SERVICE_URI)
                                                            .property(QUERY_PARAMS, COUNT_PARAM)
                                                            .property(SERVER_CERTIFICATE, Certificates.REFERENCE_SERVICE.get()));

        Step odataStep = createODataStep(odataConnector, PRODUCTS);
        Integration odataIntegration = createIntegration(odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(1);

        context.start();

        result.assertIsSatisfied();

        @SuppressWarnings( "unchecked" )
        List<String> json = extractJsonFromExchgMsg(result, 0, List.class);
        assertEquals(9, json.size());

        String expected = testData(REF_SERVER_PRODUCT_DATA_1_WITH_COUNT);
        JSONAssert.assertEquals(expected, json.get(0), JSONCompareMode.LENIENT);
        expected = testData(REF_SERVER_PRODUCT_DATA_2_WITH_COUNT);
        JSONAssert.assertEquals(expected, json.get(1), JSONCompareMode.LENIENT);
    }

    @Test
    public void testReferenceODataRouteIssue4791_1() throws Exception {
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
        //
        // Return singleton json object rather than list due to key predicate
        //
        testResult(result, 0, REF_SERVER_SUPPLIER_DATA_1_ADDRESS);
    }

    @Test
    public void testReferenceODataRouteIssue4791_2() throws Exception {
        String keyPredicate = "1/Address/Street";

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
        //
        // Return singleton json object rather than list due to key predicate
        //
        testResult(result, 0, REF_SERVER_SUPPLIER_DATA_1_ADDRESS_STREET);
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
        testListResult(result, 0, TEST_SERVER_DATA_1);
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

        @SuppressWarnings( "unchecked" )
        List<String> json = extractJsonFromExchgMsg(result, 0, List.class);
        assertEquals(2, json.size());
        String expected = testData(TEST_SERVER_DATA_1_WITH_COUNT);
        JSONAssert.assertEquals(expected, json.get(0), JSONCompareMode.LENIENT);
        expected = testData(TEST_SERVER_DATA_2_WITH_COUNT);
        JSONAssert.assertEquals(expected, json.get(1), JSONCompareMode.LENIENT);
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

        //
        // Descending order in query means these are reversed
        //
        testListResult(result, 0, TEST_SERVER_DATA_2, TEST_SERVER_DATA_1);
    }

    /**
     * Despite split being set to false, the existence of a key predicate
     * forces the split since a predicate demands only 1 result which is
     * pointless putting into an array.
     */
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

        //
        // We expect the result object to be a single json object & not an array
        //
        testResult(result, 0, TEST_SERVER_DATA_1);
    }

    /**
     * Despite split being set to false, the existence of a key predicate
     * forces the split since a predicate demands only 1 result which is
     * pointless putting into an array.
     */
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
        //
        // We expect the result object to be a single json object & not an array
        //
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
        testListResult(result, 0, TEST_SERVER_DATA_1, TEST_SERVER_DATA_2);

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
        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(1);

        context.start();

        result.assertIsSatisfied();

        @SuppressWarnings( "unchecked" )
        List<String> json = extractJsonFromExchgMsg(result, 0, List.class);
        assertEquals(2, json.size());

        String expected;
        expected = testData(TEST_SERVER_DATA_1);
        JSONAssert.assertEquals(expected, json.get(0), JSONCompareMode.LENIENT);
        expected = testData(TEST_SERVER_DATA_2);
        JSONAssert.assertEquals(expected, json.get(1), JSONCompareMode.LENIENT);

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

    /**
     * Despite split being set to false, the existence of a key predicate
     * forces the split since a predicate demands only 1 result which is
     * pointless putting into an array.
     */
    @Test
    public void testReferenceODataRouteAlreadySeenWithKeyPredicate() throws Exception {
        String keyPredicate = "1/Address";
        String backoffIdleThreshold = "1";
        String backoffMultiplier = "1";

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, REF_SERVICE_URI)
                                                            .property(KEY_PREDICATE, keyPredicate)
                                                            .property(FILTER_ALREADY_SEEN, Boolean.TRUE.toString())
                                                            .property(BACKOFF_IDLE_THRESHOLD, backoffIdleThreshold)
                                                            .property(BACKOFF_MULTIPLIER, backoffMultiplier));

        Step odataStep = createODataStep(odataConnector, SUPPLIERS);
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
        String expected = testData(REF_SERVER_SUPPLIER_DATA_1_ADDRESS);
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
