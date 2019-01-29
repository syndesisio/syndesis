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

import io.syndesis.common.model.Split;
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
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.SpringCamelContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        SplitStepHandlerTest.TestConfiguration.class
    },
    properties = {
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG"
    }
)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class SplitStepHandlerTest extends IntegrationTestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(SplitStepHandlerTest.class);

    @Autowired
    private ApplicationContext applicationContext;

    private ActivityTracker activityTracker = Mockito.mock(ActivityTracker.class);

    @Before
    public void setupMocks() {
        reset(activityTracker);
        doAnswer(invocation -> {
            LOGGER.debug(JsonSupport.toJsonObject(invocation.getArguments()));
            return null;
        }).when(activityTracker).track(any());
    }

    @Test
    public void testSplitBody() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "expression")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.split)
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "expression")
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
            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);
            final List<String> body = Arrays.asList("a","b","c");

            result.expectedMessageCount(3);
            result.expectedBodiesReceived(body);

            template.sendBody("direct:expression", body);

            result.assertIsSatisfied();

            verify(activityTracker).track(eq("exchange"), anyString(), eq("status"), eq("begin"));
            verify(activityTracker, times(4)).track(eq("exchange"), anyString(), eq("step"), anyString(), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
            verify(activityTracker).track(eq("exchange"), anyString(), eq("status"), eq("done"), eq("failed"), eq(false));
        } finally {
            context.stop();
        }
    }

    @Test
    public void testSplitBodyImplicit() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "expression")
                            .split(new Split.Builder().build())
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "expression")
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
            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);
            final List<String> body = Arrays.asList("a","b","c");

            result.expectedMessageCount(3);
            result.expectedBodiesReceived(body);

            template.sendBody("direct:expression", body);

            result.assertIsSatisfied();

            verify(activityTracker).track(eq("exchange"), anyString(), eq("status"), eq("begin"));
            verify(activityTracker, times(3)).track(eq("exchange"), anyString(), eq("step"), anyString(), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
            verify(activityTracker).track(eq("exchange"), anyString(), eq("status"), eq("done"), eq("failed"), eq(false));
        } finally {
            context.stop();
        }
    }

    @Test
    public void testTokenizeSplitStep() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "expression")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.split)
                    .putConfiguredProperty("language", "tokenize")
                    .putConfiguredProperty("expression", "|")
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "expression")
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
            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);
            final String body = "a|b|c";

            result.expectedMessageCount(3);
            result.expectedBodiesReceived((Object[])body.split("|"));

            template.sendBody("direct:expression", body);

            result.assertIsSatisfied();

            verify(activityTracker).track(eq("exchange"), anyString(), eq("status"), eq("begin"));
            verify(activityTracker, times(6)).track(eq("exchange"), anyString(), eq("step"), anyString(), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
            verify(activityTracker).track(eq("exchange"), anyString(), eq("status"), eq("done"), eq("failed"), eq(false));
        } finally {
            context.stop();
        }
    }

    @Test
    public void testTokenizeImplicitSplit() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "expression")
                            .split(new Split.Builder()
                                .language("tokenize")
                                .expression("|")
                                .build())
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "expression")
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
            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);
            final String body = "a|b|c";

            result.expectedMessageCount(3);
            result.expectedBodiesReceived((Object[])body.split("|"));

            template.sendBody("direct:expression", body);

            result.assertIsSatisfied();

            verify(activityTracker).track(eq("exchange"), anyString(), eq("status"), eq("begin"));
            verify(activityTracker, times(5)).track(eq("exchange"), anyString(), eq("step"), anyString(), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
            verify(activityTracker).track(eq("exchange"), anyString(), eq("status"), eq("done"), eq("failed"), eq(false));
        } finally {
            context.stop();
        }
    }

    // ***************************
    //
    // ***************************

    @Configuration
    public static class TestConfiguration {
    }
}
