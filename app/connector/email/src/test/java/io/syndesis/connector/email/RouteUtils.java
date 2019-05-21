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
package io.syndesis.connector.email;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.connector.email.EMailConstants.Protocol;
import io.syndesis.connector.email.model.EMailMessageModel;
import io.syndesis.connector.email.server.EMailTestServer;
import io.syndesis.connector.support.util.PropertyBuilder;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;

public class RouteUtils {

    public static final int MOCK_TIMEOUT_MILLISECONDS = 60000;

    @FunctionalInterface
    public interface ConnectorActionFactory {
        public ConnectorAction createConnectorAction(Map<String, String> configuredProperties);
    }

    private static Step mockStep;

    public static String componentScheme(EMailTestServer server) {
        String protocolId = server.getProtocol();
        assertNotNull(protocolId);
        Protocol protocol = Protocol.getValueOf(protocolId);
        assertNotNull(protocol);
        return protocol.componentSchema();
    }

    public static Step createMockStep() {
        if (mockStep == null) {
            mockStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(new ConnectorAction.Builder()
                    .descriptor(new ConnectorDescriptor.Builder()
                                .componentScheme("mock")
                                .putConfiguredProperty("name", "result")
                                .build())
                    .build())
                .build();
        }

        return mockStep;
    }

    public static Integration createIntegration(Collection<Step> steps) {

        Flow.Builder flowBuilder = new Flow.Builder();
        for (Step step : steps) {
            flowBuilder.addStep(step);
        }

        Integration odataIntegration = new Integration.Builder()
            .id("i-LTS2tYXwF8odCm87k6gz")
            .name("MyEMailInt")
            .addTags("log", "email")
            .addFlow(flowBuilder.build())
            .build();
        return odataIntegration;
    }

    public static Integration createIntegrationWithMock(Step... steps) {
        List<Step> stepList = new ArrayList<>();
        stepList.addAll(Arrays.asList(steps));
        stepList.add(createMockStep());
        return createIntegration(stepList);
    }

    public static IntegrationRouteBuilder newIntegrationRouteBuilder(Integration integration) {
        return new IntegrationRouteBuilder("") {
            @Override
            public Integration loadIntegration() throws IOException {
                return integration;
            }
        };
    }

    public static MockEndpoint initMockEndpoint(CamelContext context) {
        MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
        result.setResultWaitTime(MOCK_TIMEOUT_MILLISECONDS);
        return result;
    }

    public static Connector createEMailConnector(String componentScheme,
                                             PropertyBuilder<String> configurePropBuilder,
                                             PropertyBuilder<ConfigurationProperty> propBuilder) {
        Connector.Builder builder = new Connector.Builder()
            .id("email")
            .name("EMail")
            .componentScheme(componentScheme)
            .description("Communicate with an EMail service")
            .addDependency(Dependency.maven("org.apache.camel:camel-mail:latest"));

        if (configurePropBuilder != null) {
            builder.configuredProperties(configurePropBuilder.build());
        }

        if (propBuilder != null) {
            builder.properties(propBuilder.build());
        }

        return builder.build();
    }

    public static Connector createEMailConnector(String componentScheme, PropertyBuilder<String> configurePropBuilder) {
        return createEMailConnector(componentScheme, configurePropBuilder, null);
    }

    public static Connector createEMailConnector(EMailTestServer server, PropertyBuilder<String> configurePropBuilder) {
        return createEMailConnector(componentScheme(server), configurePropBuilder, null);
    }

    public static Step.Builder emailStepBuilder(Connector mailConnector, Function<Map<String, String>, ConnectorAction> connectorFunction, Map<String, String> configuredProperties) throws Exception {
        if (configuredProperties == null) {
            configuredProperties = new HashMap<>();
        }

        Step.Builder mailStepBuilder = new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(connectorFunction.apply(configuredProperties))
            .connection(
                            new Connection.Builder()
                                .connector(mailConnector)
                                .build());
        return mailStepBuilder;
    }

    public static Step createEMailStep(Connector emailConnector, Function<Map<String, String>, ConnectorAction> connectorFunction) throws Exception {
        return emailStepBuilder(emailConnector, connectorFunction, new HashMap<>())
                                        .build();
    }

    public static Step createEMailStep(Connector emailConnector, Function<Map<String, String>, ConnectorAction> connectorFunction, Map<String, String> configuredProperties) throws Exception {
        return emailStepBuilder(emailConnector, connectorFunction, configuredProperties)
                                        .build();
    }

    @SuppressWarnings( "unchecked" )
    public static <T> T extractModelFromExchgMsg(MockEndpoint result, int index, Class<T> bodyClass) {
        Object body = result.getExchanges().get(index).getIn().getBody();
        assertTrue(bodyClass.isInstance(body));
        T json = (T) body;
        return json;
    }

    public static EMailMessageModel extractModelFromExchgMsg(MockEndpoint result, int index) {
        return extractModelFromExchgMsg(result, index, EMailMessageModel.class);
    }

    public static void assertSatisfied(MockEndpoint result) throws InterruptedException {
        try {
            result.assertIsSatisfied();
        } catch (Exception ex) {
            List<Throwable> failures = result.getFailures();
            for (Throwable t : failures) {
                t.printStackTrace();
            }
            throw ex;
        }
    }

    public static void testResult(MockEndpoint result, int exchangeIdx, EMailMessageModel expectedModel) throws Exception {
        EMailMessageModel actualModel = extractModelFromExchgMsg(result, exchangeIdx);
        assertEquals(expectedModel, actualModel);
    }
}
