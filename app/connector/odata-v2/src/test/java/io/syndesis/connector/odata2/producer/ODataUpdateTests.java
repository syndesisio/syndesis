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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import io.syndesis.connector.odata2.ODataUtil;
import io.syndesis.connector.odata2.component.ODataComponentFactory;
import io.syndesis.connector.odata2.customizer.ODataUpdateCustomizer;
import io.syndesis.connector.support.util.PropertyBuilder;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.direct.DirectEndpoint;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DirtiesContext
@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = {
        ODataUpdateTests.TestConfiguration.class
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
public class ODataUpdateTests extends AbstractODataRouteTest {

    private static class Property {

        private final String name;
        private final String original;
        private final String value;

        public Property(String name, String value, String original) {
            this.name = name;
            this.value = value;
            this.original = original;
        }
    }

    private static class KeyedProperty {

        private final String keyProperty;
        private final int keyPredicate;
        private final Property property;

        public KeyedProperty(String keyProperty, int keyPredicate, Property property) {
            this.keyProperty = keyProperty;
            this.keyPredicate = keyPredicate;
            this.property = property;
        }

        public ObjectNode buildNode() {
            ObjectNode node = OBJECT_MAPPER.createObjectNode();
            node.put(keyProperty, keyPredicate);
            node.put(KEY_PREDICATE, keyPredicate);
            node.put(property.name, property.value);
            return node;
        }
    }

    public ODataUpdateTests() throws Exception {
        super();
    }

    @Override
    protected ConnectorAction createConnectorAction() {
        return new ConnectorAction.Builder()
            .description("Update an entity on a server resource")
             .id("io.syndesis:odata-merge-connector")
             .name("Merge")
             .descriptor(new ConnectorDescriptor.Builder()
                        .componentScheme("olingo2")
                        .putConfiguredProperty(METHOD_NAME, Methods.MERGE.id())
                        .addConnectorCustomizer(ODataUpdateCustomizer.class.getName())
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

    private static String getPropertyFromEntity(String serviceUrl, String resourcePath, Object keyPredicate, String propertyName) throws IOException, EntityProviderException, EdmException {
        Edm edm = ODataUtil.readEdm(serviceUrl, Collections.emptyMap());
        ODataEntry resultEntity = ODataUtil.readEntry(edm, resourcePath, serviceUrl + FORWARD_SLASH + resourcePath +
            OPEN_BRACKET + keyPredicate + CLOSE_BRACKET, Collections.emptyMap());
        assertNotNull(resultEntity);
        Object property = resultEntity.getProperties().get(propertyName);
        assertNotNull(property);
        return property.toString();
    }

    @Test
    public void testPatchODataRoute() throws Exception {
        Step directStep = createDirectStep();

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, odataTestServer.getServiceUri()));


        int keyPredicate = 1;
        String keyProperty = "Id";
        String nameProperty = "Lastname";
        String originalName = getPropertyFromEntity(odataTestServer.getServiceUri(), DRIVERS, keyPredicate, nameProperty);

        Step odataStep = createODataStep(odataConnector, DRIVERS);

        ObjectNode body = OBJECT_MAPPER.createObjectNode();
        String newNickName = "Driver" + System.currentTimeMillis();
        body.put(keyProperty, keyPredicate);
        body.put(nameProperty, newNickName);
        body.put(KEY_PREDICATE, keyPredicate);

        Step mockStep = createMockStep();
        Integration odataIntegration = createIntegration(directStep, odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setExpectedMessageCount(1);

        DirectEndpoint directEndpoint = context.getEndpoint("direct://start", DirectEndpoint.class);
        ProducerTemplate template = context.createProducerTemplate();

        context.start();

        String inputJson = OBJECT_MAPPER.writeValueAsString(body);
        template.sendBody(directEndpoint, inputJson);

        result.assertIsSatisfied();

        String status = extractJsonFromExchgMsg(result, 0);
        String expected = createResponseJson(HttpStatusCodes.NO_CONTENT);
        JSONAssert.assertEquals(expected, status, JSONCompareMode.LENIENT);

        String newName = getPropertyFromEntity(odataTestServer.getServiceUri(), DRIVERS, keyPredicate, nameProperty);
        assertEquals(newNickName, newName);
        assertNotEquals(originalName, newName);
    }

    @Test
    public void testPatchODataRouteMultiple() throws Exception {
        Step directStep = createDirectStep();

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, odataTestServer.getServiceUri()));

        //
        // Holding objects for the new data to update the entities
        //
        List<KeyedProperty> keyedProperties = new ArrayList<>();
        for (int i = 2; i > 0; --i) {
            String keyProperty = "Id";
            String nameProperty = "Lastname";
            String originalName = getPropertyFromEntity(odataTestServer.getServiceUri(), DRIVERS, i, nameProperty);
            String newNickName = "Driver" +  + System.currentTimeMillis();

            Property property = new Property(nameProperty, newNickName, originalName);
            keyedProperties.add(new KeyedProperty(keyProperty, i, property));
        }

        Step odataStep = createODataStep(odataConnector, DRIVERS);
        Step mockStep = createMockStep();
        Integration odataIntegration = createIntegration(directStep, odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setExpectedMessageCount(2);

        DirectEndpoint directEndpoint = context.getEndpoint("direct://start", DirectEndpoint.class);
        ProducerTemplate template = context.createProducerTemplate();

        context.start();

        //
        // Send the data through the route
        //
        for (KeyedProperty keyedProperty : keyedProperties) {
            String inputJson = OBJECT_MAPPER.writeValueAsString(keyedProperty.buildNode());
            template.sendBody(directEndpoint, inputJson);
        }

        result.assertIsSatisfied();

        //
        // Check each of the results of the route
        //
        for (int i = 0; i < keyedProperties.size(); ++i) {
            KeyedProperty keyedProperty = keyedProperties.get(i);

            String status = extractJsonFromExchgMsg(result, i);
            String expected = createResponseJson(HttpStatusCodes.NO_CONTENT);
            JSONAssert.assertEquals(expected, status, JSONCompareMode.LENIENT);

            int keyPredicate = keyedProperty.keyPredicate;
            Property property = keyedProperty.property;

            String updatedName = getPropertyFromEntity(odataTestServer.getServiceUri(), DRIVERS, keyPredicate, property.name);

            // property has been updated correctly
            assertEquals(property.value, updatedName);

            // property is no longer the original value before the update
            assertNotEquals(property.original, updatedName);
        }
    }

    @Test
    public void testPatchODataRouteWithNoKeyPredicate() throws Exception {
        Step directStep = createDirectStep();

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, odataTestServer.getServiceUri()));

        String nameProperty = "Lastname";
        Step odataStep = createODataStep(odataConnector, DRIVERS);

        ObjectNode body = OBJECT_MAPPER.createObjectNode();
        String newNickName = "Driver";
        body.put(nameProperty, newNickName);

        Step mockStep = createMockStep();
        Integration odataIntegration = createIntegration(directStep, odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        DirectEndpoint directEndpoint = context.getEndpoint("direct://start", DirectEndpoint.class);
        ProducerTemplate template = context.createProducerTemplate();

        context.start();

        String inputJson = OBJECT_MAPPER.writeValueAsString(body);
        assertThatThrownBy(() -> {
            template.sendBody(directEndpoint, inputJson);
        })
            .isInstanceOf(CamelExecutionException.class)
            .hasMessageContaining("No Key Predicate available");
    }
}
