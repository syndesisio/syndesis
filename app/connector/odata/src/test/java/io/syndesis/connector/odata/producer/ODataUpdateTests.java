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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.direct.DirectEndpoint;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.edm.constants.ODataServiceVersion;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.junit.After;
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
import io.syndesis.connector.odata.customizer.ODataPatchCustomizer;
import io.syndesis.connector.support.util.PropertyBuilder;

@DirtiesContext
@RunWith(SpringRunner.class)
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

        private String name;
        private String original;
        private String value;

        public Property(String name, String value, String original) {
            this.name = name;
            this.value = value;
            this.original = original;
        }
    }

    private static class KeyedProperty {

        private String keyPredicate;
        private Property property;

        public KeyedProperty(String keyPredicate, Property property) {
            this.keyPredicate = keyPredicate;
            this.property = property;
        }

        public ObjectNode buildNode() {
            ObjectNode node = OBJECT_MAPPER.createObjectNode();
            node.put(KEY_PREDICATE, keyPredicate);
            node.put(property.name, property.value);
            return node;
        }
    }

    @After
    public void deconstruct() throws Exception {
        //
        // Refresh server data back to original
        //
        defaultTestServer.reset();
    }

    public ODataUpdateTests() throws Exception {
        super();
    }

    @Override
    protected ConnectorAction createConnectorAction() throws Exception {
        ConnectorAction odataAction = new ConnectorAction.Builder()
            .description("Patch resource entity in resource on the server")
             .id("io.syndesis:odata-patch-connector")
             .name("Patch")
             .descriptor(new ConnectorDescriptor.Builder()
                        .componentScheme("olingo4")
                        .putConfiguredProperty(METHOD_NAME, Methods.PATCH.id())
                        .addConnectorCustomizer(ODataPatchCustomizer.class.getName())
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

    private String getPropertyFromEntity(String keyPredicate, String propertyName) {
        ClientEntity resultEntity = defaultTestServer.getData(keyPredicate);
        assertNotNull(resultEntity);
        ClientProperty property = resultEntity.getProperty(propertyName);
        assertNotNull(property.getValue());
        return property.getValue().toString();
    }

    @Test
    public void testPatchODataRoute() throws Exception {
        int initialResultCount = defaultTestServer.getResultCount();

        Step directStep = createDirectStep();

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, defaultTestServer.servicePlainUri()));


        String resourcePath = defaultTestServer.resourcePath();
        String keyPredicate = "1";
        String nameProperty = "Name";
        String originalName = getPropertyFromEntity(keyPredicate, nameProperty);

        Step odataStep = createODataStep(odataConnector, resourcePath);

        ObjectNode newProduct = OBJECT_MAPPER.createObjectNode();
        String newProductName = "NEC Screen";
        newProduct.put(nameProperty, newProductName);
        newProduct.put(KEY_PREDICATE, keyPredicate);

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

        String status = extractJsonFromExchgMsg(result, 0);
        String expected = createResponseJson(HttpStatusCode.NO_CONTENT);
        JSONAssert.assertEquals(expected, status, JSONCompareMode.LENIENT);

        assertEquals(initialResultCount, defaultTestServer.getResultCount());

        String newName = getPropertyFromEntity(keyPredicate, nameProperty);
        assertEquals(newProductName, newName);
        assertNotEquals(originalName, newName);
    }

    @Test
    public void testPatchODataRouteMultiple() throws Exception {
        int initialResultCount = defaultTestServer.getResultCount();

        Step directStep = createDirectStep();

        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, defaultTestServer.servicePlainUri()));

        String resourcePath = defaultTestServer.resourcePath();

        //
        // Holding objects for the new data to update the entities
        //
        List<KeyedProperty> keyedProperties = new ArrayList<>();
        for (int i = initialResultCount; i > 0; --i) {
            String keyPredicate = String.valueOf(i);
            String nameProperty = "Name";
            String originalName = getPropertyFromEntity(keyPredicate, nameProperty);
            String newName = "NEC Screen " + i;

            Property property = new Property(nameProperty, newName, originalName);
            keyedProperties.add(new KeyedProperty(keyPredicate, property));
        }

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
            String expected = createResponseJson(HttpStatusCode.NO_CONTENT);
            JSONAssert.assertEquals(expected, status, JSONCompareMode.LENIENT);

            assertEquals(initialResultCount, defaultTestServer.getResultCount());

            String keyPredicate = keyedProperty.keyPredicate;
            Property property = keyedProperty.property;

            String updatedName = getPropertyFromEntity(keyPredicate, property.name);

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
                                                            .property(SERVICE_URI, defaultTestServer.servicePlainUri()));

        String resourcePath = defaultTestServer.resourcePath();
        String nameProperty = "Name";
        Step odataStep = createODataStep(odataConnector, resourcePath);

        ObjectNode newProduct = OBJECT_MAPPER.createObjectNode();
        String newProductName = "NEC Screen";
        newProduct.put(nameProperty, newProductName);

        Step mockStep = createMockStep();
        Integration odataIntegration = createIntegration(directStep, odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        DirectEndpoint directEndpoint = context.getEndpoint("direct://start", DirectEndpoint.class);
        ProducerTemplate template = context.createProducerTemplate();

        context.start();

        String inputJson = OBJECT_MAPPER.writeValueAsString(newProduct);
        assertThatThrownBy(() -> {
            template.sendBody(directEndpoint, inputJson);
        })
            .isInstanceOf(CamelExecutionException.class)
            .hasMessageContaining("No Key Predicate available");
    }

    private String queryProperty(String serviceURI, String resourcePath, String keyPredicate, String property) {
        ClientEntity olEntity = null;
        ODataRetrieveResponse<ClientEntity> response = null;
        try {
            URI httpURI = URI.create(serviceURI + FORWARD_SLASH +
                                            resourcePath + OPEN_BRACKET +
                                            QUOTE_MARK + keyPredicate + QUOTE_MARK +
                                            CLOSE_BRACKET);
            ODataClient client = ODataClientFactory.getClient();
            response = client.getRetrieveRequestFactory().getEntityRequest(httpURI).execute();
            assertEquals(HttpStatus.SC_OK, response.getStatusCode());

            olEntity = response.getBody();
            assertNotNull(olEntity);
            return olEntity.getProperty(property).getPrimitiveValue().toString();
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private void updateProperty(String serviceURI, String resourcePath, String keyPredicate, String property, String value) throws Exception {
        URI httpURI = URI.create(serviceURI + FORWARD_SLASH +
                                 resourcePath + OPEN_BRACKET +
                                 QUOTE_MARK + keyPredicate + QUOTE_MARK +
                                 CLOSE_BRACKET);

        ObjectNode node = OBJECT_MAPPER.createObjectNode();
        node.put(property, value);

        String data = OBJECT_MAPPER.writeValueAsString(node);
        HttpPatch request = new HttpPatch(httpURI);

        StringEntity httpEntity = new StringEntity(data, org.apache.http.entity.ContentType.APPLICATION_JSON);
        httpEntity.setChunked(false);
        request.setEntity(httpEntity);

        Header contentTypeHeader = request.getEntity().getContentType();
        ContentType contentType = ContentType.parse(contentTypeHeader.getValue());

        request.addHeader(HttpHeaders.ACCEPT, "application/json;odata.metadata=full,application/xml,*/*");
        request.addHeader(HttpHeaders.ACCEPT_CHARSET, Constants.UTF8.toLowerCase());
        request.addHeader(HttpHeaders.CONTENT_TYPE, contentType.toString());
        request.addHeader(HttpHeader.ODATA_VERSION, ODataServiceVersion.V40.toString());
        request.addHeader(HttpHeader.ODATA_MAX_VERSION, ODataServiceVersion.V40.toString());

        CloseableHttpClient client = HttpClients.createMinimal();
        try {
            CloseableHttpResponse response = client.execute(request);
            StatusLine statusLine = response.getStatusLine();
            assertEquals(HttpStatus.SC_NO_CONTENT, statusLine.getStatusCode());
        } finally {
            client.close();
        }
    }

    /**
     * Tests alternative key predicate that is a string value
     * @see https://github.com/syndesisio/syndesis/issues/5241
     *
     * @throws Exception
     */
    @Test
    public void testPatchODataRouteOnRefServer() throws Exception {
        Step directStep = createDirectStep();

        //
        // Ensure we are using our own individual RW URI so not to impinge
        // on other tests seeing as we're modifying data
        //
        String refServiceURI = getRealRefServiceUrl(REF_SERVICE_URI);
        Connector odataConnector = createODataConnector(new PropertyBuilder<String>()
                                                            .property(SERVICE_URI, refServiceURI));

        String resourcePath = "People";
        String keyPredicate = "russellwhyte";
        String nameProperty = "MiddleName";
        String originalName = queryProperty(refServiceURI, resourcePath, keyPredicate, nameProperty);

        Step odataStep = createODataStep(odataConnector, resourcePath);

        ObjectNode newProduct = OBJECT_MAPPER.createObjectNode();
        // Append time to ensure old name is different from new name
        String newMiddleName = "Quentin" + System.currentTimeMillis();
        newProduct.put(nameProperty, newMiddleName);
        newProduct.put(KEY_PREDICATE, keyPredicate);

        Step mockStep = createMockStep();
        Integration odataIntegration = createIntegration(directStep, odataStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(odataIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setExpectedMessageCount(1);

        DirectEndpoint directEndpoint = context.getEndpoint("direct://start", DirectEndpoint.class);
        ProducerTemplate template = context.createProducerTemplate();

        try {
            context.start();

            String inputJson = OBJECT_MAPPER.writeValueAsString(newProduct);
            template.sendBody(directEndpoint, inputJson);

            result.assertIsSatisfied();

            String status = extractJsonFromExchgMsg(result, 0);
            String expected = createResponseJson(HttpStatusCode.NO_CONTENT);
            JSONAssert.assertEquals(expected, status, JSONCompareMode.LENIENT);

            String newName = queryProperty(refServiceURI, resourcePath, keyPredicate, nameProperty);
            assertEquals(newMiddleName, newName);
            assertNotEquals(originalName, newName);
        } finally {
            //
            // Reset property back to original value
            // Not strictly necessary as our own special service created but
            // just in case it ever pops up again.
            //
            updateProperty(refServiceURI, resourcePath, keyPredicate, nameProperty, originalName);
        }
    }
}
