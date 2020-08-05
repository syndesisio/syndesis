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
package io.syndesis.connector.odata2.producer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.connector.odata2.AbstractODataRouteTest;
import io.syndesis.connector.odata2.component.ODataComponentFactory;
import io.syndesis.connector.odata2.customizer.ODataDeleteCustomizer;
import io.syndesis.connector.support.util.PropertyBuilder;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.direct.DirectEndpoint;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
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


@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        ODataDeleteTests.TestConfiguration.class
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
public class ODataDeleteTests extends AbstractODataRouteTest {

    public ODataDeleteTests() throws Exception {
        super();
    }

    @Override
    protected ConnectorAction createConnectorAction() {
        return new ConnectorAction.Builder()
            .description("Delete resource entities from the server")
             .id("io.syndesis:odata-delete-connector")
             .name("Delete")
             .descriptor(new ConnectorDescriptor.Builder()
                        .componentScheme("olingo2")
                        .putConfiguredProperty(METHOD_NAME, Methods.DELETE.id())
                        .addConnectorCustomizer(ODataDeleteCustomizer.class.getName())
                        .connectorFactory(ODataComponentFactory.class.getName())
                        .inputDataShape(new DataShape.Builder()
                                        .kind(DataShapeKinds.JSON_SCHEMA)
                                        .build())
                        .outputDataShape(new DataShape.Builder()
                                         .kind(DataShapeKinds.JSON_INSTANCE)
                                         .build())
                        .build())
            .build();
    }

    private static Step createDirectStep() {
        return new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(new ConnectorAction.Builder()
                    .descriptor(new ConnectorDescriptor.Builder()
                                .componentScheme("direct")
                                .putConfiguredProperty("name", "start")
                                .build())
                    .build())
            .build();
    }

    @Test
    public void testDeleteODataRoute() throws Exception {
        Step directStep = createDirectStep();

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, odataTestServer.getServiceUri()));

        ObjectNode keyPredicateJson = OBJECT_MAPPER.createObjectNode();
        keyPredicateJson.put(KEY_PREDICATE, "'1'");

        Step odataStep = createODataStep(odataConnector, CARS);
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

        String status = extractJsonFromExchgMsg(result, 0);
        String expected = createResponseJson(HttpStatusCodes.NO_CONTENT);
        JSONAssert.assertEquals(expected, status, JSONCompareMode.LENIENT);
    }

    @Test
    public void testDeleteODataRouteKeyPredicateFilter() throws Exception {
        Step directStep = createDirectStep();

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, odataTestServer.getServiceUri()));


        ObjectNode keyPredicateJson = OBJECT_MAPPER.createObjectNode();
        keyPredicateJson.put(KEY_PREDICATE, "Id='2'");

        Step odataStep = createODataStep(odataConnector, CARS);
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

        String status = extractJsonFromExchgMsg(result, 0);
        String expected = createResponseJson(HttpStatusCodes.NO_CONTENT);
        JSONAssert.assertEquals(expected, status, JSONCompareMode.LENIENT);
    }

    @Test
    public void testDeleteODataRouteAllData() throws Exception {
        Step directStep = createDirectStep();

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, odataTestServer.getServiceUri()));

        Step odataStep = createODataStep(odataConnector, CARS);
        Step mockStep = createMockStep();
        Integration odataIntegration = createIntegration(directStep, odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setExpectedMessageCount(2);

        DirectEndpoint directEndpoint = context.getEndpoint("direct://start", DirectEndpoint.class);
        ProducerTemplate template = context.createProducerTemplate();

        context.start();
        for (int i = 3; i <= 4; ++i) {
            ObjectNode keyPredicateJson = OBJECT_MAPPER.createObjectNode();
            keyPredicateJson.put(KEY_PREDICATE, String.format("'%s'", i));
            String inputJson = OBJECT_MAPPER.writeValueAsString(keyPredicateJson);
            template.sendBody(directEndpoint, inputJson);
        }

        result.assertIsSatisfied();

        String status = extractJsonFromExchgMsg(result, 0);
        String expected = createResponseJson(HttpStatusCodes.NO_CONTENT);
        JSONAssert.assertEquals(expected, status, JSONCompareMode.LENIENT);
    }
}
