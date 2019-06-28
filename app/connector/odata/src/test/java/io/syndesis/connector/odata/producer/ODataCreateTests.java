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

import static org.junit.Assert.assertEquals;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.direct.DirectEndpoint;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.AfterClass;
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
import io.syndesis.connector.odata.customizer.ODataCreateCustomizer;
import io.syndesis.connector.support.util.PropertyBuilder;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        ODataCreateTests.TestConfiguration.class
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
public class ODataCreateTests extends AbstractODataRouteTest {

    @AfterClass
    public static void deconstruct() throws Exception {
        //
        // Refresh server data back to original
        //
        defaultTestServer.reset();
    }

    public ODataCreateTests() throws Exception {
        super();
    }

    @Override
    protected ConnectorAction createConnectorAction() throws Exception {
        ConnectorAction odataAction = new ConnectorAction.Builder()
            .description("Create resource entity in resource on the server")
             .id("io.syndesis:odata-create-connector")
             .name("Create")
             .descriptor(new ConnectorDescriptor.Builder()
                        .componentScheme("olingo4")
                        .putConfiguredProperty(METHOD_NAME, Methods.CREATE.id())
                        .addConnectorCustomizer(ODataCreateCustomizer.class.getName())
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

    private Step createDirectStep() {
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
    public void testCreateODataRoute() throws Exception {
        int initialResultCount = defaultTestServer.getResultCount();

        Step directStep = createDirectStep();

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, defaultTestServer.servicePlainUri()));


        String resourcePath = defaultTestServer.resourcePath();

        ObjectNode newProduct = OBJECT_MAPPER.createObjectNode();
        newProduct.put("Name", "NEC Screen");
        newProduct.put("Description", "New Monitor with Resolution 1920 x 1080 @ 60Hz");

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

        String inputJson = OBJECT_MAPPER.writeValueAsString(newProduct);
        template.sendBody(directEndpoint, inputJson);

        result.assertIsSatisfied();

        String entityJson = extractJsonFromExchgMsg(result, 0, String.class);
        JSONAssert.assertEquals(inputJson, entityJson, JSONCompareMode.LENIENT);

        assertEquals(initialResultCount + 1, defaultTestServer.getResultCount());
    }
}
