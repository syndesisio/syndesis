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
package io.syndesis.connector.soap.cxf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.model.ModelCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.Resources;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class IntegrationTestBase {

    private static final String REQUEST_PAYLOAD =
            "<ns1:sayHi xmlns:ns1=\"http://camel.apache.org/cxf/wsrm\">" +
                "<arg0 xmlns=\"http://camel.apache.org/cxf/wsrm\">Hello</arg0>" +
            "</ns1:sayHi>";
    private static final String RESPONSE_PAYLOAD =
            "<ns1:sayHiResponse xmlns:ns1=\"http://camel.apache.org/cxf/wsrm\">" +
            "   <return xmlns=\"http://camel.apache.org/cxf/wsrm\">Hello Hello!</return>" +
            "</ns1:sayHiResponse>";
    protected static final String TEST_USER = "TestUser";
    protected static final String TEST_PASSWORD = "TestPassword";

    private static String wrapInEnvelope(String body) {
        return  "<?xml version='1.0' encoding='UTF-8'?>" +
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "<soap:Body>" + body + "</soap:Body>" +
                "</soap:Envelope>";
    }

    protected abstract String requestEnvelopePattern(String body);

    private static final DataShape XML_SCHEMA_SHAPE = new DataShape.Builder()
        .kind(DataShapeKinds.XML_SCHEMA)
        .build();

    protected static final Connector SOAP_CXF_CONNECTOR = loadConnector();

    @Rule
    public WireMockRule wiremock = new WireMockRule(WireMockConfiguration.options().dynamicPort());

    protected Connection connection;

    private CamelContext context;

    @Before
    public void setup() throws Exception {
        createConnection();

        final RouteBuilder builder = createRouteBuilder();

        context = builder.getContext();
        builder.addRoutesToCamelContext(context);

        context.start();
    }

    protected abstract void createConnection();

    @Test
    public void shouldInvokeRemoteApis() {
        wiremock.givenThat(post("/")
            .willReturn(ok(wrapInEnvelope(RESPONSE_PAYLOAD))
                .withHeader("Content-Type", "application/xml")));

        assertThat(context.createProducerTemplate().requestBody("direct:sayHi",
            wrapInEnvelope(REQUEST_PAYLOAD), String.class))
                .isEqualTo(wrapInEnvelope(RESPONSE_PAYLOAD));

        wiremock.verify(postRequestedFor(urlEqualTo("/"))
            .withRequestBody(WireMock.matching(requestEnvelopePattern(REQUEST_PAYLOAD))));

        verifyWireMock(wiremock);
    }

    protected void verifyWireMock(WireMockRule wiremock) {
        // override in derived class for extra verification
    }

    @After
    public void teardown() throws Exception {
        context.stop();
    }

    private Integration createIntegration() {
        return new Integration.Builder()
            .addFlow(testFlow("sayHi", XML_SCHEMA_SHAPE, XML_SCHEMA_SHAPE))
            .build();
    }

    private RouteBuilder createRouteBuilder() {
        return new IntegrationRouteBuilder("", Resources.loadServices(IntegrationStepHandler.class)) {

            @Override
            public void configure() throws Exception {
                errorHandler(defaultErrorHandler().maximumRedeliveries(1));

                super.configure();
            }

            @Override
            protected ModelCamelContext createContainer() {
                final Properties properties = new Properties();
                properties.put("flow-0.cxf-1.password", "TestPassword");

                final PropertiesComponent propertiesComponent = new PropertiesComponent();
                propertiesComponent.setInitialProperties(properties);

                final SimpleRegistry registry = new SimpleRegistry();
                registry.put("properties", propertiesComponent);

                final ModelCamelContext leanContext = new DefaultCamelContext(registry);
                leanContext.disableJMX();

                return leanContext;
            }

            @Override
            protected Integration loadIntegration() {
                return createIntegration();
            }
        };
    }

    private Step operation(final String operationId, final DataShape inputDataShape, final DataShape outputDataShape) {
        return new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .putConfiguredProperty("defaultOperationName", operationId)
                    .putConfiguredProperty("dataFormat", "PAYLOAD")
                    .componentScheme(SOAP_CXF_CONNECTOR.getComponentScheme().get())
                    .connectorId(SOAP_CXF_CONNECTOR.getId().get())
                    .inputDataShape(inputDataShape)
                    .outputDataShape(outputDataShape)
                    .build())
                .build())
            .connection(connection)
            .build();
    }

    private Flow testFlow(final String operationId, final DataShape inputDataShape, final DataShape outputDataShape) {
        return testFlow(operationId, operationId, inputDataShape, outputDataShape);
    }

    private Flow testFlow(final String operationId, final String triggerName, final DataShape inputDataShape,
        final DataShape outputDataShape) {
        return new Flow.Builder()
            .addStep(direct(triggerName))
            .addStep(operation(operationId, inputDataShape, outputDataShape))
            .build();
    }

    private static Step direct(final String name) {
        return new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .componentScheme("direct")
                    .putConfiguredProperty("name", name)
                    .build())
                .build())
            .build();
    }

    private static Connector loadConnector() {
        try (InputStream json = IntegrationTestBase.class
            .getResourceAsStream("/META-INF/syndesis/connector/soap.json")) {
            return JsonUtils.readFromStream(json, Connector.class);
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    protected static String readSpecification() {
        try {
            return Resources.getResourceAsText("HelloWorld.wsdl");
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

}
