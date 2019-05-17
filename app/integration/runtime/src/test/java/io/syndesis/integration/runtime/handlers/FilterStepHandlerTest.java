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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.filter.FilterPredicate;
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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
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
public class FilterStepHandlerTest extends IntegrationTestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterStepHandlerTest.class);

    private static final String START_STEP = "start-step";
    private static final String FILTER_STEP = "filter-step";
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
    public void testPlaintextFilterStep() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                new Step.Builder()
                    .id(START_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "start")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(FILTER_STEP)
                    .stepKind(StepKind.expressionFilter)
                    .putConfiguredProperty("filter", "${body} contains 'number'")
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
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final List<String> matchingMessages = Collections.singletonList("Body: [number:9436] Log zo syndesisu");
            final List<String> notMatchingMessages = Collections.singletonList("something else");
            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);

            List<String> allMessages = new ArrayList<>();
            allMessages.addAll(matchingMessages);
            allMessages.addAll(notMatchingMessages);

            result.expectedBodiesReceived(matchingMessages);

            for (Object body : allMessages) {
                template.sendBody("direct:start", body);
            }

            result.assertIsSatisfied();

            verify(activityTracker, times(allMessages.size())).startTracking(any(Exchange.class));
            verifyActivityStepTracking(MOCK_STEP, matchingMessages.size());
            verifyActivityStepTracking(FILTER_STEP, allMessages.size());
            verify(activityTracker, times(allMessages.size())).finishTracking(any(Exchange.class));
        } finally {
            context.stop();
        }
    }

    @Test
    public void testExpressionFilterStep() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                    new Step.Builder()
                            .id(START_STEP)
                            .stepKind(StepKind.endpoint)
                            .action(new ConnectorAction.Builder()
                                    .descriptor(new ConnectorDescriptor.Builder()
                                            .componentScheme("direct")
                                            .putConfiguredProperty("name", "start")
                                            .build())
                                    .build())
                            .build(),
                    new Step.Builder()
                            .id(FILTER_STEP)
                            .stepKind(StepKind.expressionFilter)
                            .putConfiguredProperty("filter", "${body.name} == 'James'")
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
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final List<String> matchingMessages = Collections.singletonList(buildPersonJson("James"));
            final List<String> notMatchingMessages = Collections.singletonList(buildPersonJson("Jimmi"));
            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);

            List<String> allMessages = new ArrayList<>();
            allMessages.addAll(matchingMessages);
            allMessages.addAll(notMatchingMessages);

            result.expectedBodiesReceived(matchingMessages);

            for (Object body : allMessages) {
                template.sendBody("direct:start", body);
            }

            result.assertIsSatisfied();

            verify(activityTracker, times(allMessages.size())).startTracking(any(Exchange.class));
            verifyActivityStepTracking(MOCK_STEP, matchingMessages.size());
            verifyActivityStepTracking(FILTER_STEP, allMessages.size());
            verify(activityTracker, times(allMessages.size())).finishTracking(any(Exchange.class));
        } finally {
            context.stop();
        }
    }

    @Test
    public void testExpressionFilterStepOnArray() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                    new Step.Builder()
                            .id(START_STEP)
                            .stepKind(StepKind.endpoint)
                            .action(new ConnectorAction.Builder()
                                    .descriptor(new ConnectorDescriptor.Builder()
                                            .componentScheme("direct")
                                            .putConfiguredProperty("name", "start")
                                            .build())
                                    .build())
                            .build(),
                    new Step.Builder()
                            .id(FILTER_STEP)
                            .stepKind(StepKind.expressionFilter)
                            .putConfiguredProperty("filter", "${body.size()} > 0 && ${body[0].name} == 'James'")
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
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final List<String> matchingMessages = Collections.singletonList(buildPersonJsonArray("James"));
            final List<String> notMatchingMessages = Arrays.asList(buildPersonJsonArray(),
                                                                   buildPersonJsonArray("Jimmi"));
            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);

            List<String> allMessages = new ArrayList<>();
            allMessages.addAll(matchingMessages);
            allMessages.addAll(notMatchingMessages);

            result.expectedBodiesReceived(matchingMessages);

            for (Object body : allMessages) {
                template.sendBody("direct:start", body);
            }

            result.assertIsSatisfied();

            verify(activityTracker, times(allMessages.size())).startTracking(any(Exchange.class));
            verifyActivityStepTracking(MOCK_STEP, matchingMessages.size());
            verifyActivityStepTracking(FILTER_STEP, allMessages.size());
            verify(activityTracker, times(allMessages.size())).finishTracking(any(Exchange.class));
        } finally {
            context.stop();
        }
    }

    @Test
    public void testRuleFilterExpression() {
        String expression = new RuleFilterStepHandler().getFilterExpression(
            new Step.Builder()
                .stepKind(StepKind.ruleFilter)
                .putConfiguredProperty("predicate", FilterPredicate.AND.toString())
                .putConfiguredProperty("rules","[ { \"path\": \"person.name\", \"op\": \"==\", \"value\": \"Ioannis\"}, " +
                    "  { \"path\": \"person.favoriteDrinks\", \"op\": \"contains\", \"value\": \"Gin\" } ]")
                .build()
        );

        // Reading notes: Unit tests are like personal diaries. Feel honoured when you have the chance to be part of them ;-)
        assertThat("${body.person.name} == 'Ioannis' && ${body.person.favoriteDrinks} contains 'Gin'").isEqualTo(expression);
    }

    @Test
    public void testRuleFilterStepWithJsonSimplePath() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                new Step.Builder()
                    .id(START_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "start")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(FILTER_STEP)
                    .stepKind(StepKind.ruleFilter)
                    .putConfiguredProperty("type", "rule")
                    .putConfiguredProperty("predicate", "OR")
                    .putConfiguredProperty("rules", "[{\"path\":\"name\",\"op\":\"==\",\"value\":\"James\"}, {\"path\":\"name\",\"op\":\"==\",\"value\":\"Roland\"}]")
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
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final List<String> matchingMessages = Arrays.asList(buildPersonJson("James"), buildPersonJson("Roland"));
            final List<String> notMatchingMessages = Collections.singletonList(buildPersonJson("Jimmi"));
            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);

            List<String> allMessages = new ArrayList<>();
            allMessages.addAll(matchingMessages);
            allMessages.addAll(notMatchingMessages);

            result.expectedBodiesReceived(matchingMessages);

            for (Object body : allMessages) {
                template.sendBody("direct:start", body);
            }

            result.assertIsSatisfied();

            verify(activityTracker, times(allMessages.size())).startTracking(any(Exchange.class));
            verifyActivityStepTracking(MOCK_STEP, matchingMessages.size());
            verifyActivityStepTracking(FILTER_STEP, allMessages.size());
            verify(activityTracker, times(allMessages.size())).finishTracking(any(Exchange.class));
        } finally {
            context.stop();
        }
    }

    @Test
    public void testRuleFilterStepWithJsonComplexPath() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                new Step.Builder()
                    .id(START_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "start")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(FILTER_STEP)
                    .stepKind(StepKind.ruleFilter)
                    .putConfiguredProperty("type", "rule")
                    .putConfiguredProperty("predicate", "OR")
                    .putConfiguredProperty("rules", "[{\"path\":\"user.name\",\"op\":\"==\",\"value\":\"James\"}, {\"path\":\"user.name\",\"op\":\"==\",\"value\":\"Roland\"}]")
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
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final List<String> matchingMessages = Arrays.asList(buildUserJson("James"), buildUserJson("Roland"));
            final List<String> notMatchingMessages = Collections.singletonList(buildUserJson("Jimmi"));
            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);

            List<String> allMessages = new ArrayList<>();
            allMessages.addAll(matchingMessages);
            allMessages.addAll(notMatchingMessages);

            result.expectedBodiesReceived(matchingMessages);

            for (Object body : allMessages) {
                template.sendBody("direct:start", body);
            }

            result.assertIsSatisfied();

            verify(activityTracker, times(allMessages.size())).startTracking(any(Exchange.class));
            verifyActivityStepTracking(MOCK_STEP, matchingMessages.size());
            verifyActivityStepTracking(FILTER_STEP, allMessages.size());
            verify(activityTracker, times(allMessages.size())).finishTracking(any(Exchange.class));
        } finally {
            context.stop();
        }
    }

    @Test
    public void testRuleFilterStepWithJsonArrayPath() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                    new Step.Builder()
                            .id(START_STEP)
                            .stepKind(StepKind.endpoint)
                            .action(new ConnectorAction.Builder()
                                    .descriptor(new ConnectorDescriptor.Builder()
                                            .componentScheme("direct")
                                            .putConfiguredProperty("name", "start")
                                            .build())
                                    .build())
                            .build(),
                    new Step.Builder()
                            .id(FILTER_STEP)
                            .stepKind(StepKind.ruleFilter)
                            .putConfiguredProperty("type", "rule")
                            .putConfiguredProperty("predicate", "AND")
                            .putConfiguredProperty("rules", "[{\"path\":\"size()\",\"op\":\"==\",\"value\":\"2\"}, {\"path\":\"[0].user.name\",\"op\":\"==\",\"value\":\"James\"}, {\"path\":\"[1].user.name\",\"op\":\"==\",\"value\":\"Roland\"}]")
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
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final List<String> matchingMessages = Collections.singletonList(buildUserJsonArray("James", "Roland"));
            final List<String> notMatchingMessages = Arrays.asList(buildUserJsonArray(),
                                                                    buildUserJsonArray("Jimmi"),
                                                                    buildUserJsonArray("Jimmi", "Roland"),
                                                                    buildUserJsonArray("James", "Roland", "Jimmi"));
            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);

            List<String> allMessages = new ArrayList<>();
            allMessages.addAll(matchingMessages);
            allMessages.addAll(notMatchingMessages);

            result.expectedBodiesReceived(matchingMessages);

            for (Object body : allMessages) {
                template.sendBody("direct:start", body);
            }

            result.assertIsSatisfied();

            verify(activityTracker, times(allMessages.size())).startTracking(any(Exchange.class));
            verifyActivityStepTracking(MOCK_STEP, matchingMessages.size());
            verifyActivityStepTracking(FILTER_STEP, allMessages.size());
            verify(activityTracker, times(allMessages.size())).finishTracking(any(Exchange.class));
        } finally {
            context.stop();
        }
    }

    @Test
    public void testRuleFilterStepWithPOJO() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                new Step.Builder()
                    .id(START_STEP)
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "start")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id(FILTER_STEP)
                    .stepKind(StepKind.ruleFilter)
                    .putConfiguredProperty("type", "rule")
                    .putConfiguredProperty("predicate", "OR")
                    .putConfiguredProperty("rules", "[{\"path\":\"name\",\"op\":\"==\",\"value\":\"James\"}, {\"path\":\"name\",\"op\":\"==\",\"value\":\"Roland\"}]")
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
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final List<User> matchingMessages = Arrays.asList(new User("James"), new User("Roland"));
            final List<User> notMatchingMessages = Collections.singletonList(new User("Jimmy"));
            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);

            List<User> allMessages = new ArrayList<>();
            allMessages.addAll(matchingMessages);
            allMessages.addAll(notMatchingMessages);

            result.expectedBodiesReceived(matchingMessages);

            for (Object body : allMessages) {
                template.sendBody("direct:start", body);
            }

            result.assertIsSatisfied();

            verify(activityTracker, times(allMessages.size())).startTracking(any(Exchange.class));
            verifyActivityStepTracking(MOCK_STEP, matchingMessages.size());
            verifyActivityStepTracking(FILTER_STEP, allMessages.size());
            verify(activityTracker, times(allMessages.size())).finishTracking(any(Exchange.class));
        } finally {
            context.stop();
        }
    }

    private String buildPersonJson(String name) {
        return "{ \"name\": \"" + name + "\" }";
    }

    private String buildPersonJsonArray(String ... names) {
        return "[" + Stream.of(names)
                        .map(this::buildPersonJson)
                        .collect(Collectors.joining(",")) + "]";
    }

    private String buildUserJson(String ... names) {
        return Stream.of(names)
                .map(name -> "{ \"user\": " + buildPersonJson(name) + " }")
                .collect(Collectors.joining(","));
    }

    private String buildUserJsonArray(String ... names) {
        return "[" + buildUserJson(names) + "]";
    }

    private void verifyActivityStepTracking(String stepId, int times) {
        verify(activityTracker, times(times)).track(eq("exchange"), anyString(), eq("step"), eq(stepId), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
    }

    // ***************************
    //
    // ***************************

    public static final class User {
        private String name;

        public User(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            User user = (User) o;
            return Objects.equals(name, user.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
