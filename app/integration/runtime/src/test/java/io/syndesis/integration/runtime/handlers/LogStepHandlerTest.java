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
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationTestSupport;
import io.syndesis.integration.runtime.logging.ActivityTracker;
import io.syndesis.integration.runtime.logging.ActivityTrackingInterceptStrategy;
import io.syndesis.integration.runtime.logging.BodyLogger;
import io.syndesis.integration.runtime.logging.IntegrationLoggingListener;
import io.syndesis.integration.runtime.util.JsonSupport;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class LogStepHandlerTest extends IntegrationTestSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogStepHandlerTest.class);

    private static final String START_STEP = "start-step";
    private static final String LOG_STEP = "log-step";
    private static final String MOCK_STEP = "mock-step";

    final LogStepHandler handler = new LogStepHandler();

    final IntegrationRouteBuilder NOT_USED = null;

    final ProcessorDefinition<?> route = spy(new RouteDefinition());

    @Test
    public void shouldAddLogProcessorWithCustomMessage() {
        final Step step = new Step.Builder().putConfiguredProperty("customText", "Log me baby one more time").build();

        assertThat(handler.handle(step, route, NOT_USED, "1", "2")).contains(route);

        verify(route).log(LoggingLevel.INFO, "Log me baby one more time");
    }

    @Test
    public void shouldAddLogProcessorWithCustomMessageAndStepId() {
        final Step step = new Step.Builder().id("step-id")
            .putConfiguredProperty("customText", "Log me baby one more time").build();

        assertThat(handler.handle(step, route, NOT_USED, "1", "2")).contains(route);

        verify(route).log(LoggingLevel.INFO, (String) null, "step-id", "Log me baby one more time");
    }

    @Test
    public void shouldGenerateMessages() {
        final Step step = new Step.Builder().putConfiguredProperty("customText", "Log me baby one more time").build();

        assertThat(LogStepHandler.createMessage(step)).isEqualTo("Log me baby one more time");

        final Step withContext = new Step.Builder().createFrom(step)
            .putConfiguredProperty("contextLoggingEnabled", "true").build();
        assertThat(LogStepHandler.createMessage(withContext))
            .isEqualTo("Message Context: [${in.headers}] Log me baby one more time");

        final Step withBody = new Step.Builder().createFrom(step).putConfiguredProperty("bodyLoggingEnabled", "true")
            .build();
        assertThat(LogStepHandler.createMessage(withBody)).isEqualTo("Body: [${bean:bodyLogger}] Log me baby one more time");

        final Step withContextAndBody = new Step.Builder().createFrom(step)
            .putConfiguredProperty("contextLoggingEnabled", "true").putConfiguredProperty("bodyLoggingEnabled", "true")
            .build();
        assertThat(LogStepHandler.createMessage(withContextAndBody))
            .isEqualTo("Message Context: [${in.headers}] Body: [${bean:bodyLogger}] Log me baby one more time");
    }

    @Test
    public void shouldNotAddLogProcessorWhenNotingIsSpecifiedToLog() {
        final Step step = new Step.Builder().build();

        assertThat(handler.handle(step, route, NOT_USED, "1", "2")).isEmpty();

        verifyZeroInteractions(route);
    }

    @Test
    public void shouldAddLogStepActivityLogging() throws Exception {
        final ActivityTracker activityTracker = Mockito.mock(ActivityTracker.class);
        final DefaultCamelContext context = new DefaultCamelContext();

        doAnswer(invocation -> {
            ActivityTracker.initializeTracking(invocation.getArgument(0));
            return null;
        }).when(activityTracker).startTracking(any(Exchange.class));

        doAnswer(invocation -> {
            LOGGER.debug(JsonSupport.toJsonObject(invocation.getArguments()));
            return null;
        }).when(activityTracker).track(any());

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
                            .id(LOG_STEP)
                            .stepKind(StepKind.log)
                            .putConfiguredProperty("bodyLoggingEnabled", "true")
                            .putConfiguredProperty("customText", "Log me baby one more time")
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

            SimpleRegistry beanRegistry = new SimpleRegistry();
            beanRegistry.put("bodyLogger", new BodyLogger.Default());
            context.setRegistry(beanRegistry);

            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);

            List<String> messages = Arrays.asList("Hi", "Hello");

            result.expectedBodiesReceived(messages);

            for (Object body : messages) {
                template.sendBody("direct:start", body);
            }

            result.assertIsSatisfied();

            verify(activityTracker, times(messages.size())).startTracking(any(Exchange.class));
            verify(activityTracker, times(messages.size())).track(eq("exchange"), anyString(), eq("step"), eq(START_STEP), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
            verify(activityTracker).track(eq("exchange"), anyString(), eq("step"), eq(LOG_STEP), eq("id"), anyString(), eq("message"), eq("Body: [Hi] Log me baby one more time"));
            verify(activityTracker).track(eq("exchange"), anyString(), eq("step"), eq(LOG_STEP), eq("id"), anyString(), eq("message"), eq("Body: [Hello] Log me baby one more time"));
            verify(activityTracker, times(messages.size())).track(eq("exchange"), anyString(), eq("step"), eq(LOG_STEP), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
            verify(activityTracker, times(messages.size())).track(eq("exchange"), anyString(), eq("step"), eq(MOCK_STEP), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
            verify(activityTracker, times(6)).track(eq("exchange"), anyString(), eq("step"), anyString(), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
            verify(activityTracker, times(messages.size())).finishTracking(any(Exchange.class));
        } finally {
            context.stop();
        }
    }
}
