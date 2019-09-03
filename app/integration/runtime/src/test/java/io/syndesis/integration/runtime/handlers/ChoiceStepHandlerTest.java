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
package io.syndesis.integration.runtime.handlers;

import java.util.Arrays;
import java.util.List;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.integration.runtime.IntegrationTestSupport;
import io.syndesis.integration.runtime.logging.ActivityTracker;
import io.syndesis.integration.runtime.logging.ActivityTrackingInterceptStrategy;
import io.syndesis.integration.runtime.logging.BodyLogger;
import io.syndesis.integration.runtime.logging.IntegrationLoggingListener;
import io.syndesis.integration.runtime.util.JsonSupport;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class ChoiceStepHandlerTest extends IntegrationTestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChoiceStepHandlerTest.class);

    private static final String START_STEP = "start-step";
    private static final String CHOICE_STEP = "choice-step";
    private static final String MOCK_STEP = "mock-step";

    private ActivityTracker activityTracker = Mockito.mock(ActivityTracker.class);

    @Before
    public void setupMocks() {
        reset(activityTracker);

        doAnswer(invocation -> {
            ActivityTracker.initializeTracking(invocation.getArgument(0));
            return null;
        }).when(activityTracker).startTracking(any(Exchange.class));

        doAnswer(invocation -> {
            LOGGER.info(JsonSupport.toJsonObject(invocation.getArguments()));
            return null;
        }).when(activityTracker).track(any());
    }

    @Test
    public void testChoiceStep() throws Exception {
        final DefaultCamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder integrationRoute = newIntegrationRouteBuilder(activityTracker,
                new Step.Builder()
                    .id(START_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "flow")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(CHOICE_STEP)
                    .stepKind(StepKind.choice)
                    .putConfiguredProperty("flows", "[" +
                                        "{\"condition\": \"${body} contains 'Hello'\", \"flow\": \"hello-flow\"}," +
                                        "{\"condition\": \"${body} contains 'Bye'\", \"flow\": \"bye-flow\"}" +
                                    "]")
                    .build(),
                new Step.Builder()
                    .id(MOCK_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "result")
                            .build())
                        .build())
                    .build()
            );

            final RouteBuilder helloRoute = new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("direct:hello-flow")
                            .to("mock:hello");
                }
            };

            final RouteBuilder byeRoute = new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("direct:bye-flow")
                            .to("mock:bye");
                }
            };

            // Set up the camel context
            context.setUuidGenerator(KeyGenerator::createKey);
            context.addLogListener(new IntegrationLoggingListener(activityTracker));
            context.addInterceptStrategy(new ActivityTrackingInterceptStrategy(activityTracker));
            context.addRoutes(helloRoute);
            context.addRoutes(byeRoute);
            context.addRoutes(integrationRoute);

            SimpleRegistry beanRegistry = new SimpleRegistry();
            beanRegistry.put("bodyLogger", new BodyLogger.Default());
            context.setRegistry(beanRegistry);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            final MockEndpoint helloResult = context.getEndpoint("mock:hello", MockEndpoint.class);
            final MockEndpoint byeResult = context.getEndpoint("mock:bye", MockEndpoint.class);

            final List<String> messages = Arrays.asList("Hello Camel!", "Bye Camel!", "And Now for Something Completely Different");

            result.expectedBodiesReceived(messages);
            helloResult.expectedBodiesReceived("Hello Camel!");
            byeResult.expectedBodiesReceived("Bye Camel!");

            for (String message : messages) {
                template.sendBody("direct:flow", message);
            }

            result.assertIsSatisfied();
            helloResult.assertIsSatisfied();
            byeResult.assertIsSatisfied();

            verify(activityTracker, times(3)).startTracking(any(Exchange.class));
            verifyActivityStepTracking(CHOICE_STEP, 3);
            verifyActivityStepTracking(MOCK_STEP, 3);
            verify(activityTracker, times(3)).finishTracking(any(Exchange.class));
        } finally {
            context.stop();
        }
    }

    @Test
    public void testChoiceStepWithDefaultFlow() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder integrationRoute = newIntegrationRouteBuilder(activityTracker,
                    new Step.Builder()
                            .id(START_STEP)
                            .stepKind(StepKind.endpoint)
                            .action(new ConnectorAction.Builder()
                                    .descriptor(new ConnectorDescriptor.Builder()
                                            .componentScheme("direct")
                                            .putConfiguredProperty("name", "flow")
                                            .build())
                                    .build())
                            .build(),
                    new Step.Builder()
                            .id(CHOICE_STEP)
                            .stepKind(StepKind.choice)
                            .putConfiguredProperty("routingScheme", "mock")
                            .putConfiguredProperty("default", "default-flow")
                            .putConfiguredProperty("flows", "[" +
                                    "{\"condition\": \"${body} contains 'Hello'\", \"flow\": \"hello-flow\"}," +
                                    "{\"condition\": \"${body} contains 'Bye'\", \"flow\": \"bye-flow\"}" +
                                    "]")
                            .build(),
                    new Step.Builder()
                            .id(MOCK_STEP)
                            .stepKind(StepKind.endpoint)
                            .action(new ConnectorAction.Builder()
                                    .descriptor(new ConnectorDescriptor.Builder()
                                            .componentScheme("mock")
                                            .putConfiguredProperty("name", "result")
                                            .build())
                                    .build())
                            .build()
            );

            // Set up the camel context
            context.setUuidGenerator(KeyGenerator::createKey);
            context.addLogListener(new IntegrationLoggingListener(activityTracker));
            context.addInterceptStrategy(new ActivityTrackingInterceptStrategy(activityTracker));
            context.addRoutes(integrationRoute);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            final MockEndpoint defaultResult = context.getEndpoint("mock:default-flow", MockEndpoint.class);
            final MockEndpoint helloResult = context.getEndpoint("mock:hello-flow", MockEndpoint.class);
            final MockEndpoint byeResult = context.getEndpoint("mock:bye-flow", MockEndpoint.class);

            final List<String> messages = Arrays.asList("Hello Camel!", "Bye Camel!", "And Now for Something Completely Different");

            result.expectedBodiesReceived(messages);
            defaultResult.expectedBodiesReceived("And Now for Something Completely Different");
            helloResult.expectedBodiesReceived("Hello Camel!");
            byeResult.expectedBodiesReceived("Bye Camel!");

            for (String message : messages) {
                template.sendBody("direct:flow", message);
            }

            result.assertIsSatisfied();
            helloResult.assertIsSatisfied();
            byeResult.assertIsSatisfied();

            verify(activityTracker, times(3)).startTracking(any(Exchange.class));
            verifyActivityStepTracking(CHOICE_STEP, 3);
            verifyActivityStepTracking(MOCK_STEP, 3);
            verify(activityTracker, times(3)).finishTracking(any(Exchange.class));

        } finally {
            context.stop();
        }
    }

    @Test
    public void testChoiceStepWithRuleBasedConditions() throws Exception {
        final DefaultCamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder integrationRoute = newIntegrationRouteBuilder(activityTracker,
                    new Step.Builder()
                            .id(START_STEP)
                            .stepKind(StepKind.endpoint)
                            .action(new ConnectorAction.Builder()
                                    .descriptor(new ConnectorDescriptor.Builder()
                                            .componentScheme("direct")
                                            .putConfiguredProperty("name", "flow")
                                            .build())
                                    .build())
                            .build(),
                    new Step.Builder()
                            .id(CHOICE_STEP)
                            .stepKind(StepKind.choice)
                            .putConfiguredProperty("routingScheme", "mock")
                            .putConfiguredProperty("flows", "[" +
                                        "{\"condition\": \"\", \"path\": \"text\", \"op\": \"contains\", \"value\": \"Hello\", \"flow\": \"hello-flow\"}," +
                                        "{\"condition\": \"\", \"path\": \"text\", \"op\": \"contains\", \"value\": \"Bye\", \"flow\": \"bye-flow\"}" +
                                    "]")
                            .build(),
                    new Step.Builder()
                            .id(MOCK_STEP)
                            .stepKind(StepKind.endpoint)
                            .action(new ConnectorAction.Builder()
                                    .descriptor(new ConnectorDescriptor.Builder()
                                            .componentScheme("mock")
                                            .putConfiguredProperty("name", "result")
                                            .build())
                                    .build())
                            .build()
            );

            // Set up the camel context
            context.setUuidGenerator(KeyGenerator::createKey);
            context.addLogListener(new IntegrationLoggingListener(activityTracker));
            context.addInterceptStrategy(new ActivityTrackingInterceptStrategy(activityTracker));
            context.addRoutes(integrationRoute);

            SimpleRegistry beanRegistry = new SimpleRegistry();
            beanRegistry.put("bodyLogger", new BodyLogger.Default());
            context.setRegistry(beanRegistry);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            final MockEndpoint helloResult = context.getEndpoint("mock:hello-flow", MockEndpoint.class);
            final MockEndpoint byeResult = context.getEndpoint("mock:bye-flow", MockEndpoint.class);

            final List<String> messages = Arrays.asList("{\"text\": \"Hello Camel!\"}", "{\"text\": \"Bye Camel!\"}", "{\"text\": \"And Now for Something Completely Different\"}");

            result.expectedBodiesReceived(messages);
            helloResult.expectedBodiesReceived("{\"text\": \"Hello Camel!\"}");
            byeResult.expectedBodiesReceived("{\"text\": \"Bye Camel!\"}");

            for (String message : messages) {
                template.sendBody("direct:flow", message);
            }

            result.assertIsSatisfied();
            helloResult.assertIsSatisfied();
            byeResult.assertIsSatisfied();

            verify(activityTracker, times(3)).startTracking(any(Exchange.class));
            verifyActivityStepTracking(CHOICE_STEP, 3);
            verifyActivityStepTracking(MOCK_STEP, 3);
            verify(activityTracker, times(3)).finishTracking(any(Exchange.class));

        } finally {
            context.stop();
        }
    }

    @Test
    public void testChoiceStepNoConfiguredFlows() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder integrationRoute = newIntegrationRouteBuilder(activityTracker,
                    new Step.Builder()
                            .id(START_STEP)
                            .stepKind(StepKind.endpoint)
                            .action(new ConnectorAction.Builder()
                                    .descriptor(new ConnectorDescriptor.Builder()
                                            .componentScheme("direct")
                                            .putConfiguredProperty("name", "flow")
                                            .build())
                                    .build())
                            .build(),
                    new Step.Builder()
                            .id(CHOICE_STEP)
                            .stepKind(StepKind.choice)
                            .build(),
                    new Step.Builder()
                            .id(MOCK_STEP)
                            .stepKind(StepKind.endpoint)
                            .action(new ConnectorAction.Builder()
                                    .descriptor(new ConnectorDescriptor.Builder()
                                            .componentScheme("mock")
                                            .putConfiguredProperty("name", "result")
                                            .build())
                                    .build())
                            .build()
            );

            // Set up the camel context
            context.setUuidGenerator(KeyGenerator::createKey);
            context.addLogListener(new IntegrationLoggingListener(activityTracker));
            context.addInterceptStrategy(new ActivityTrackingInterceptStrategy(activityTracker));
            context.addRoutes(integrationRoute);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);

            final List<String> messages = Arrays.asList("Hello Camel!", "Bye Camel!", "And Now for Something Completely Different");

            result.expectedBodiesReceived(messages);

            for (String message : messages) {
                template.sendBody("direct:flow", message);
            }

            result.assertIsSatisfied();

            verify(activityTracker, times(3)).startTracking(any(Exchange.class));
            verifyActivityStepTracking(CHOICE_STEP, 3);
            verifyActivityStepTracking(MOCK_STEP, 3);
            verify(activityTracker, times(3)).finishTracking(any(Exchange.class));

        } finally {
            context.stop();
        }
    }

    private void verifyActivityStepTracking(String stepId, int times) {
        verify(activityTracker, times(times)).track(eq("exchange"), anyString(), eq("step"), eq(stepId), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
    }
}
