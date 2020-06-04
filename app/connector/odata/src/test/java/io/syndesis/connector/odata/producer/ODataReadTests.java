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
package io.syndesis.connector.odata.producer;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.direct.DirectEndpoint;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.connector.odata.AbstractODataRouteTest;
import io.syndesis.connector.odata.component.ODataComponentFactory;
import io.syndesis.connector.odata.consumer.AbstractODataReadRouteTest;
import io.syndesis.connector.odata.customizer.ODataReadToCustomizer;
import io.syndesis.connector.support.util.PropertyBuilder;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        ODataReadTests.TestConfiguration.class
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
public class ODataReadTests extends AbstractODataRouteTest {

    public ODataReadTests() throws Exception {
        super();
    }

    @After
    public void deconstruct() throws Exception {
        //
        // Refresh server data back to original
        //
        defaultTestServer.reset();
    }

    @Override
    protected ConnectorAction createConnectorAction() {
        ConnectorAction odataAction = new ConnectorAction.Builder()
            .description("Read resource entities from the server subject to keyPredicates")
             .id("io.syndesis:" + Methods.READ.actionIdentifierRoot() + HYPHEN + TO)
             .name("Read")
             .descriptor(new ConnectorDescriptor.Builder()
                        .componentScheme("olingo4")
                        .putConfiguredProperty(METHOD_NAME, Methods.READ.id())
                        .putConfiguredProperty(CONNECTOR_DIRECTION, TO)
                        .addConnectorCustomizer(ODataReadToCustomizer.class.getName())
                        .connectorFactory(ODataComponentFactory.class.getName())
                        .inputDataShape(new DataShape.Builder()
                                        .kind(DataShapeKinds.JSON_SCHEMA)
                                        .build())
                        .outputDataShape(new DataShape.Builder()
                                         .kind(DataShapeKinds.JSON_INSTANCE)
                                         .build())
                        .build())
            .build();
        return odataAction;
    }

    private static Step createDirectStep() {
        Step directStep = new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(new ConnectorAction.Builder()
                    .descriptor(new ConnectorDescriptor.Builder()
                                .componentScheme("direct")
                                .putConfiguredProperty("name", "start")
                                .build())
                    .build())
            .build();
        return directStep;
    }

    @Test
    public void testReadODataRoute() throws Exception {
        int initialResultCount = defaultTestServer.getResultCount();

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, defaultTestServer.servicePlainUri()));

        String resourcePath = defaultTestServer.resourcePath();

        ObjectNode keyPredicateJson = OBJECT_MAPPER.createObjectNode();
        keyPredicateJson.put(KEY_PREDICATE, "1");

        Step directStep = createDirectStep();
        Step odataStep = createODataStep(odataConnector, resourcePath);
        Step mockStep = createMockStep();
        Integration odataIntegration = createIntegration(directStep, odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setExpectedMessageCount(1);

        DirectEndpoint directEndpoint = context.getEndpoint("direct://start", DirectEndpoint.class);
        ProducerTemplate template = context.createProducerTemplate();

        context.start();
        String inputJson = OBJECT_MAPPER.writeValueAsString(keyPredicateJson);
        template.sendBody(directEndpoint, inputJson);

        result.assertIsSatisfied();

        String entityJson = extractJsonFromExchgMsg(result, 0, String.class);
        assertThatJson(entityJson).isEqualTo(testData(TEST_SERVER_DATA_1, AbstractODataReadRouteTest.class));
        assertEquals(initialResultCount, defaultTestServer.getResultCount());
    }

    @Test
    public void testReadODataRouteKeyPredicateFilter() throws Exception {
        int initialResultCount = defaultTestServer.getResultCount();

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, defaultTestServer.servicePlainUri()));


        String resourcePath = defaultTestServer.resourcePath();

        ObjectNode keyPredicateJson = OBJECT_MAPPER.createObjectNode();
        keyPredicateJson.put(KEY_PREDICATE, "ID=2");

        Step directStep = createDirectStep();
        Step odataStep = createODataStep(odataConnector, resourcePath);
        Step mockStep = createMockStep();
        Integration odataIntegration = createIntegration(directStep, odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setExpectedMessageCount(1);

        DirectEndpoint directEndpoint = context.getEndpoint("direct://start", DirectEndpoint.class);
        ProducerTemplate template = context.createProducerTemplate();

        context.start();
        String inputJson = OBJECT_MAPPER.writeValueAsString(keyPredicateJson);
        template.sendBody(directEndpoint, inputJson);

        result.assertIsSatisfied();

        String entityJson = extractJsonFromExchgMsg(result, 0, String.class);
        assertThatJson(entityJson).isEqualTo(testData(TEST_SERVER_DATA_2, AbstractODataReadRouteTest.class));
        assertEquals(initialResultCount, defaultTestServer.getResultCount());
    }

    @Test
    public void testReadODataRouteAllData() throws Exception {
        int initialResultCount = defaultTestServer.getResultCount();

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, defaultTestServer.servicePlainUri()));

        String resourcePath = defaultTestServer.resourcePath();

        Step directStep = createDirectStep();
        Step odataStep = createODataStep(odataConnector, resourcePath);
        Step mockStep = createMockStep();
        Integration odataIntegration = createIntegration(directStep, odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setExpectedMessageCount(initialResultCount);

        DirectEndpoint directEndpoint = context.getEndpoint("direct://start", DirectEndpoint.class);
        ProducerTemplate template = context.createProducerTemplate();

        context.start();
        for (int i = 1; i <= initialResultCount; ++i) {
            ObjectNode keyPredicateJson = OBJECT_MAPPER.createObjectNode();
            keyPredicateJson.put(KEY_PREDICATE, String.valueOf(i));
            String inputJson = OBJECT_MAPPER.writeValueAsString(keyPredicateJson);
            template.sendBody(directEndpoint, inputJson);
        }

        result.assertIsSatisfied();

        for (int i = 0; i < initialResultCount; ++i) {
            String entityJson = extractJsonFromExchgMsg(result, i, String.class);
            String expectedData = null;
            switch (i) {
                case 0:
                    expectedData = TEST_SERVER_DATA_1;
                    break;
                case 1:
                    expectedData = TEST_SERVER_DATA_2;
                    break;
                case 2:
                    expectedData = TEST_SERVER_DATA_3;
                    break;
            }

            assertNotNull(expectedData);
            assertThatJson(entityJson).isEqualTo(testData(expectedData, AbstractODataReadRouteTest.class));
            assertEquals(initialResultCount, defaultTestServer.getResultCount());
        }
    }

    @Test
    public void testReadODataRouteKeyPredicateWithSubPredicate() throws Exception {
        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, REF_SERVICE_URI));

        String resourcePath = "Airports";

        ObjectNode keyPredicateJson = OBJECT_MAPPER.createObjectNode();
        keyPredicateJson.put(KEY_PREDICATE, "('KLAX')/Location");

        Step directStep = createDirectStep();
        Step odataStep = createODataStep(odataConnector, resourcePath);
        Step mockStep = createMockStep();
        Integration odataIntegration = createIntegration(directStep, odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setExpectedMessageCount(1);

        DirectEndpoint directEndpoint = context.getEndpoint("direct://start", DirectEndpoint.class);
        ProducerTemplate template = context.createProducerTemplate();

        context.start();
        String inputJson = OBJECT_MAPPER.writeValueAsString(keyPredicateJson);
        template.sendBody(directEndpoint, inputJson);

        result.assertIsSatisfied();

        String entityJson = extractJsonFromExchgMsg(result, 0, String.class);
        assertThatJson(entityJson).isEqualTo(testData(REF_SERVER_PEOPLE_DATA_KLAX_LOC, AbstractODataReadRouteTest.class));
    }
}
