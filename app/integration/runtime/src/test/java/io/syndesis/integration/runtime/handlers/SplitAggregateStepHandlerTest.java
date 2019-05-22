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
import java.util.stream.Collectors;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.integration.runtime.IntegrationTestSupport;
import io.syndesis.integration.runtime.logging.ActivityTracker;
import io.syndesis.integration.runtime.logging.ActivityTrackingInterceptStrategy;
import io.syndesis.integration.runtime.logging.IntegrationLoggingListener;
import io.syndesis.integration.runtime.util.JsonSupport;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
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
public class SplitAggregateStepHandlerTest extends IntegrationTestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(SplitAggregateStepHandlerTest.class);

    private static final String START_STEP = "start-step";
    private static final String SPLIT_STEP = "split-step";
    private static final String AGGREGATE_STEP = "aggregate-step";
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
            LOGGER.debug(JsonSupport.toJsonObject(invocation.getArguments()));
            return null;
        }).when(activityTracker).track(any());
    }

    @Test
    public void testSplitAggregate() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                new Step.Builder()
                    .id(START_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "expression")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(SPLIT_STEP)
                    .stepKind(StepKind.split)
                    .build(),
                new Step.Builder()
                    .id(MOCK_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "split")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(AGGREGATE_STEP)
                    .stepKind(StepKind.aggregate)
                    .build()

            );

            // Set up the camel context
            context.setUuidGenerator(KeyGenerator::createKey);
            context.addLogListener(new IntegrationLoggingListener(activityTracker));
            context.addInterceptStrategy(new ActivityTrackingInterceptStrategy(activityTracker));
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:split", MockEndpoint.class);
            final List<String> body = Arrays.asList("a","b","c");

            result.expectedBodiesReceived("a", "b", "c");

            List<?> response = template.requestBody("direct:expression", body, List.class);

            result.assertIsSatisfied();
            Assert.assertEquals(body, response);

            verify(activityTracker).startTracking(any(Exchange.class));
            verifyActivityStepTracking(START_STEP, 1);
            verifyActivityStepTracking(SPLIT_STEP, 0);
            verifyActivityStepTracking(MOCK_STEP, 3);
            verifyActivityStepTracking(AGGREGATE_STEP, 0);
            verify(activityTracker, times(4)).track(eq("exchange"), anyString(), eq("step"), anyString(), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
            verify(activityTracker).finishTracking(any(Exchange.class));
        } finally {
            context.stop();
        }
    }

    @Test
    public void testSplitAggregateWithTrailingSteps() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                new Step.Builder()
                    .id(START_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "expression")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(SPLIT_STEP)
                    .stepKind(StepKind.split)
                    .build(),
                new Step.Builder()
                    .id("mock-before")
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "split")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(AGGREGATE_STEP)
                    .stepKind(StepKind.aggregate)
                    .build(),
                new Step.Builder()
                    .id(MOCK_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "afterSplit")
                            .build())
                        .build())
                    .build()
            );

            // Set up the camel context
            context.setUuidGenerator(KeyGenerator::createKey);
            context.addLogListener(new IntegrationLoggingListener(activityTracker));
            context.addInterceptStrategy(new ActivityTrackingInterceptStrategy(activityTracker));
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint split = context.getEndpoint("mock:split", MockEndpoint.class);
            final MockEndpoint afterSplit = context.getEndpoint("mock:afterSplit", MockEndpoint.class);
            final List<String> body = Arrays.asList("a","b","c");

            split.expectedBodiesReceived("a", "b", "c");
            afterSplit.expectedMessageCount(1);

            List<?> response = template.requestBody("direct:expression", body, List.class);

            split.assertIsSatisfied();
            afterSplit.assertIsSatisfied();
            Assert.assertEquals(body, afterSplit.getExchanges().get(0).getIn().getBody());
            Assert.assertEquals(body, response);

            verify(activityTracker).startTracking(any(Exchange.class));
            verifyActivityStepTracking(START_STEP, 1);
            verifyActivityStepTracking(SPLIT_STEP, 0);
            verifyActivityStepTracking(MOCK_STEP, 1);
            verifyActivityStepTracking(AGGREGATE_STEP, 0);
            verifyActivityStepTracking("mock-before", 3);
            verify(activityTracker, times(5)).track(eq("exchange"), anyString(), eq("step"), anyString(), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
            verify(activityTracker).finishTracking(any(Exchange.class));
        } finally {
            context.stop();
        }
    }

    @Test
    public void testSplitAggregateWithTransformation() throws Exception {
        final CamelContext context = getDefaultCamelContextWithMyBeanInRegistry();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                new Step.Builder()
                    .id(START_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "expression")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(SPLIT_STEP)
                    .stepKind(StepKind.split)
                    .build(),
                new Step.Builder()
                    .id("bean-step")
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("bean")
                            .putConfiguredProperty("beanName", "myBean")
                            .putConfiguredProperty("method", "myProcessor")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(MOCK_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "split")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(AGGREGATE_STEP)
                    .stepKind(StepKind.aggregate)
                    .build(),
                new Step.Builder()
                    .id("mock-after")
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "afterSplit")
                            .build())
                        .build())
                    .build()
            );

            // Set up the camel context
            context.setUuidGenerator(KeyGenerator::createKey);
            context.addLogListener(new IntegrationLoggingListener(activityTracker));
            context.addInterceptStrategy(new ActivityTrackingInterceptStrategy(activityTracker));
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint split = context.getEndpoint("mock:split", MockEndpoint.class);
            final MockEndpoint afterSplit = context.getEndpoint("mock:afterSplit", MockEndpoint.class);
            final List<String> body = Arrays.asList("a","b","c");

            split.expectedBodiesReceived("A", "B", "C");
            afterSplit.expectedMessageCount(1);

            List<?> response = template.requestBody("direct:expression", body, List.class);

            split.assertIsSatisfied();
            afterSplit.assertIsSatisfied();
            Assert.assertEquals(Arrays.asList("A", "B", "C"), afterSplit.getExchanges().get(0).getIn().getBody());

            Assertions.assertThat(response.size()).isEqualTo(3);
            Assert.assertEquals(3L, response.size());

            verify(activityTracker).startTracking(any(Exchange.class));
            verifyActivityStepTracking(START_STEP, 1);
            verifyActivityStepTracking(SPLIT_STEP, 0);
            verifyActivityStepTracking(MOCK_STEP, 3);
            verifyActivityStepTracking(AGGREGATE_STEP, 0);
            verifyActivityStepTracking("bean-step", 3);
            verifyActivityStepTracking("mock-after", 1);
            verify(activityTracker, times(8)).track(eq("exchange"), anyString(), eq("step"), anyString(), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
            verify(activityTracker).finishTracking(any(Exchange.class));
        } finally {
            context.stop();
        }
    }

    @Test
    public void testSplitAggregateTokenize() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                new Step.Builder()
                    .id(START_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "expression")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(SPLIT_STEP)
                    .stepKind(StepKind.split)
                    .putConfiguredProperty("language", "tokenize")
                    .putConfiguredProperty("expression", "|")
                    .build(),
                new Step.Builder()
                    .id(MOCK_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "split")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(AGGREGATE_STEP)
                    .stepKind(StepKind.aggregate)
                    .build()
            );

            // Set up the camel context
            context.setUuidGenerator(KeyGenerator::createKey);
            context.addLogListener(new IntegrationLoggingListener(activityTracker));
            context.addInterceptStrategy(new ActivityTrackingInterceptStrategy(activityTracker));
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:split", MockEndpoint.class);
            final String body = "a|b|c";

            result.expectedBodiesReceived((Object[])body.split("|"));

            List<?> response = template.requestBody("direct:expression", body, List.class);

            result.assertIsSatisfied();
            Assert.assertEquals(5, response.size());
            Assert.assertEquals(body, response.stream().map(Object::toString).collect(Collectors.joining()));

            verify(activityTracker).startTracking(any(Exchange.class));
            verifyActivityStepTracking(START_STEP, 1);
            verifyActivityStepTracking(SPLIT_STEP, 0);
            verifyActivityStepTracking(MOCK_STEP, 5);
            verifyActivityStepTracking(AGGREGATE_STEP, 0);
            verify(activityTracker, times(6)).track(eq("exchange"), anyString(), eq("step"), anyString(), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
            verify(activityTracker).finishTracking(any(Exchange.class));
        } finally {
            context.stop();
        }
    }

    @Test
    public void testSplitAggregateWithOriginalAggregationStrategy() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                new Step.Builder()
                    .id(START_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "expression")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(SPLIT_STEP)
                    .stepKind(StepKind.split)
                    .putConfiguredProperty("language", "tokenize")
                    .putConfiguredProperty("expression", "|")
                    .putConfiguredProperty("aggregationStrategy", "original")
                    .build(),
                new Step.Builder()
                    .id(MOCK_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "split")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(AGGREGATE_STEP)
                    .stepKind(StepKind.aggregate)
                    .build()
            );

            // Set up the camel context
            context.setUuidGenerator(KeyGenerator::createKey);
            context.addLogListener(new IntegrationLoggingListener(activityTracker));
            context.addInterceptStrategy(new ActivityTrackingInterceptStrategy(activityTracker));
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:split", MockEndpoint.class);
            final String body = "a|b|c";

            result.expectedBodiesReceived((Object[])body.split("|"));

            String response = template.requestBody("direct:expression", body, String.class);

            result.assertIsSatisfied();
            Assert.assertEquals(body, response);

            verify(activityTracker).startTracking(any(Exchange.class));
            verifyActivityStepTracking(START_STEP, 1);
            verifyActivityStepTracking(SPLIT_STEP, 0);
            verifyActivityStepTracking(MOCK_STEP, 5);
            verifyActivityStepTracking(AGGREGATE_STEP, 0);
            verify(activityTracker, times(6)).track(eq("exchange"), anyString(), eq("step"), anyString(), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
            verify(activityTracker).finishTracking(any(Exchange.class));
        } finally {
            context.stop();
        }
    }

    @Test
    public void testSplitAggregateWithLatestAggregationStrategy() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                new Step.Builder()
                    .id(START_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "expression")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(SPLIT_STEP)
                    .stepKind(StepKind.split)
                    .putConfiguredProperty("aggregationStrategy", "latest")
                    .build(),
                new Step.Builder()
                    .id(MOCK_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "split")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(AGGREGATE_STEP)
                    .stepKind(StepKind.aggregate)
                    .build()

            );

            // Set up the camel context
            context.setUuidGenerator(KeyGenerator::createKey);
            context.addLogListener(new IntegrationLoggingListener(activityTracker));
            context.addInterceptStrategy(new ActivityTrackingInterceptStrategy(activityTracker));
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:split", MockEndpoint.class);
            final List<String> body = Arrays.asList("a","b","c");

            result.expectedBodiesReceived(body);

            String response = template.requestBody("direct:expression", body, String.class);

            result.assertIsSatisfied();
            Assert.assertEquals("c", response);

            verify(activityTracker).startTracking(any(Exchange.class));
            verifyActivityStepTracking(START_STEP, 1);
            verifyActivityStepTracking(SPLIT_STEP, 0);
            verifyActivityStepTracking(MOCK_STEP, 3);
            verifyActivityStepTracking(AGGREGATE_STEP, 0);
            verify(activityTracker, times(4)).track(eq("exchange"), anyString(), eq("step"), anyString(), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
            verify(activityTracker).finishTracking(any(Exchange.class));
        } finally {
            context.stop();
        }
    }

    @Test
    public void testSplitAggregateScriptAggregationStrategy() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                new Step.Builder()
                    .id(START_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "expression")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(SPLIT_STEP)
                    .stepKind(StepKind.split)
                    .putConfiguredProperty("language", "tokenize")
                    .putConfiguredProperty("expression", "|")
                    .putConfiguredProperty("aggregationStrategy", "script")
                    .putConfiguredProperty("aggregationScriptLanguage", "nashorn")
                    .putConfiguredProperty("aggregationScript", "newExchange.in.body += oldExchange ? oldExchange.in.body : '';\n" +
                            "newExchange;")
                    .build(),
                new Step.Builder()
                    .id(MOCK_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "split")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(AGGREGATE_STEP)
                    .stepKind(StepKind.aggregate)
                    .build()
            );

            // Set up the camel context
            context.setUuidGenerator(KeyGenerator::createKey);
            context.addLogListener(new IntegrationLoggingListener(activityTracker));
            context.addInterceptStrategy(new ActivityTrackingInterceptStrategy(activityTracker));
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:split", MockEndpoint.class);
            final String body = "a|b|c";

            result.expectedBodiesReceived((Object[])body.split("|"));

            String response = template.requestBody("direct:expression", body, String.class);

            result.assertIsSatisfied();
            Assert.assertEquals("c|b|a", response);

            verify(activityTracker).startTracking(any(Exchange.class));
            verifyActivityStepTracking(START_STEP, 1);
            verifyActivityStepTracking(SPLIT_STEP, 0);
            verifyActivityStepTracking(MOCK_STEP, 5);
            verifyActivityStepTracking(AGGREGATE_STEP, 0);
            verify(activityTracker, times(6)).track(eq("exchange"), anyString(), eq("step"), anyString(), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
            verify(activityTracker).finishTracking(any(Exchange.class));
        } finally {
            context.stop();
        }
    }

    private void verifyActivityStepTracking(String stepId, int times) {
        verify(activityTracker, times(times)).track(eq("exchange"), anyString(), eq("step"), eq(stepId), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
    }
}
