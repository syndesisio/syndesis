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

@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.UseLocaleWithCaseConversions"})
public class SimpleEndpointStepHandlerTest extends IntegrationTestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleEndpointStepHandlerTest.class);

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
    public void testSimpleEndpointStep() throws Exception {
        final CamelContext context = getDefaultCamelContextWithMyBeanInRegistry();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "start")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
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

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            final String body = "testSimpleEndpointStep";

            result.expectedBodiesReceived(body.toUpperCase());
            template.sendBody("direct:start", body);

            result.assertIsSatisfied();

            verify(activityTracker).startTracking(any(Exchange.class));
            verify(activityTracker, times(3)).track(eq("exchange"), anyString(), eq("step"), anyString(), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
            verify(activityTracker).finishTracking(any(Exchange.class));
        } finally {
            context.stop();
        }
    }

    @Test
    public void testSimpleEndpointStepWithSplitAggregate() throws Exception {
        final CamelContext context = getDefaultCamelContextWithMyBeanInRegistry();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "start")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
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
                    .stepKind(StepKind.split)
                    .putConfiguredProperty("language", "tokenize")
                    .putConfiguredProperty("expression", "|")
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "result")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
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
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            final String body = "a|b|c";
            final String[] expected = body.toUpperCase().split("|");

            result.expectedMessageCount(3);
            result.expectedBodiesReceived((Object[])expected);
            template.sendBody("direct:start", body);

            result.assertIsSatisfied();

            verify(activityTracker).startTracking(any(Exchange.class));
            verify(activityTracker, times(7)).track(eq("exchange"), anyString(), eq("step"), anyString(), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
            verify(activityTracker).finishTracking(any(Exchange.class));
        } finally {
            context.stop();
        }
    }

}
