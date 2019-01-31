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
package io.syndesis.integration.runtime.sb.capture;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.syndesis.common.model.Split;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Scheduler;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.capture.OutMessageCaptureProcessor;
import io.syndesis.integration.runtime.sb.IntegrationRuntimeAutoConfiguration;
import io.syndesis.integration.runtime.sb.IntegrationTestSupport;
import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.support.EventNotifierSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Handy class to test logging of log messages and errors
 */
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        IntegrationRuntimeAutoConfiguration.class,
    },
    properties = {
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG"
    }
)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class OutMessageCaptureProcessorTest extends IntegrationTestSupport {
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testCapture() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(
                new Step.Builder()
                    .id("s1")
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "expression")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id("s2")
                    .stepKind(StepKind.extension)
                    .action(new StepAction.Builder()
                        .descriptor(new StepDescriptor.Builder()
                            .kind(StepAction.Kind.BEAN)
                            .entrypoint(Bean1.class.getName())
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id("s3")
                    .stepKind(StepKind.extension)
                    .action(new StepAction.Builder()
                        .descriptor(new StepDescriptor.Builder()
                            .kind(StepAction.Kind.BEAN)
                            .entrypoint(Bean2.class.getName())
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id("s4")
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
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);

            result.expectedBodiesReceived("-862545276");
            template.sendBody("direct:expression", "World");
            result.assertIsSatisfied();

            Exchange exchange1 = result.getExchanges().get(0);
            Map<String, Message> messages = OutMessageCaptureProcessor.getCapturedMessageMap(exchange1);
            assertThat(messages).hasSize(4);
            assertThat(messages.get("s1").getBody()).isEqualTo("World");
            assertThat(messages.get("s2").getBody()).isEqualTo("Hello World");
            assertThat(messages.get("s3").getBody()).isEqualTo(-862545276);
            assertThat(messages.get("s4").getBody()).isEqualTo(-862545276);
        } finally {
            context.stop();
        }
    }

    @Test
    public void testCaptureWithSplit() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(
                new Step.Builder()
                    .id("s1")
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
                    .id("s2")
                    .stepKind(StepKind.extension)
                    .action(new StepAction.Builder()
                        .descriptor(new StepDescriptor.Builder()
                            .kind(StepAction.Kind.BEAN)
                            .entrypoint(Bean1.class.getName())
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id("s3")
                    .stepKind(StepKind.extension)
                    .action(new StepAction.Builder()
                        .descriptor(new StepDescriptor.Builder()
                            .kind(StepAction.Kind.BEAN)
                            .entrypoint(Bean2.class.getName())
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id("s4")
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
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);

            result.expectedBodiesReceived("-862545276");
            template.sendBody("direct:expression", "World");
            result.assertIsSatisfied();

            Exchange exchange1 = result.getExchanges().get(0);
            Map<String, Message> messages = OutMessageCaptureProcessor.getCapturedMessageMap(exchange1);
            assertThat(messages).hasSize(4);
            assertThat(messages.get("s1").getBody()).isEqualTo("World");
            assertThat(messages.get("s2").getBody()).isEqualTo("Hello World");
            assertThat(messages.get("s3").getBody()).isEqualTo(-862545276);
            assertThat(messages.get("s4").getBody()).isEqualTo(-862545276);
        } finally {
            context.stop();
        }
    }

    @Test
    public void testCaptureWithForeach() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(
                new Step.Builder()
                    .id("s1")
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "expression")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.foreach)
                    .build(),
                new Step.Builder()
                    .id("s2")
                    .stepKind(StepKind.extension)
                    .action(new StepAction.Builder()
                        .descriptor(new StepDescriptor.Builder()
                            .kind(StepAction.Kind.BEAN)
                            .entrypoint(Bean1.class.getName())
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id("s3")
                    .stepKind(StepKind.extension)
                    .action(new StepAction.Builder()
                        .descriptor(new StepDescriptor.Builder()
                            .kind(StepAction.Kind.BEAN)
                            .entrypoint(Bean2.class.getName())
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id("s4")
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "expression")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.endForeach)
                    .build()
            );

            // Set up the camel context
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);

            result.expectedBodiesReceived("-862545276");
            template.sendBody("direct:expression", "World");
            result.assertIsSatisfied();

            Exchange exchange1 = result.getExchanges().get(0);
            Map<String, Message> messages = OutMessageCaptureProcessor.getCapturedMessageMap(exchange1);
            assertThat(messages).hasSize(4);
            assertThat(messages.get("s1").getBody()).isEqualTo("World");
            assertThat(messages.get("s2").getBody()).isEqualTo("Hello World");
            assertThat(messages.get("s3").getBody()).isEqualTo(-862545276);
            assertThat(messages.get("s4").getBody()).isEqualTo(-862545276);
        } finally {
            context.stop();
        }
    }

    @Test
    public void testCaptureWithSplitAndSchedule() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        ExchangeCompletedNotifier exchangeCompletedNotifier = new ExchangeCompletedNotifier();
        context.getManagementStrategy().addEventNotifier(exchangeCompletedNotifier);

        try {
            Integration integration = newIntegration(
                new Step.Builder()
                    .id("s1")
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "getdata")
                            .split(new Split.Builder().build())
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id("s2")
                    .stepKind(StepKind.extension)
                    .action(new StepAction.Builder()
                        .descriptor(new StepDescriptor.Builder()
                            .kind(StepAction.Kind.BEAN)
                            .entrypoint(Bean1.class.getName())
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id("s3")
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "expression")
                            .build())
                        .build())
                    .build()
            );

            final Flow flow = integration.getFlows().get(0);
            final Flow flowWithScheduler = flow.builder()
                .scheduler(new Scheduler.Builder()
                    .expression("60s")
                    .build())
            .build();

            integration = new Integration.Builder()
                .createFrom(integration)
                .flows(singleton(flowWithScheduler))
                .build();

            IntegrationRouteBuilder routes = newIntegrationRouteBuilder(integration);
            routes.from("direct:getdata").bean(new Bean3());

            // Set up the camel context

            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);
            result.expectedBodiesReceived("Hello Hiram", "Hello World");
            result.assertIsSatisfied();

            Exchange exchange1 = result.getExchanges().get(0);
            exchangeCompletedNotifier.waitForCompletion(exchange1);
            Map<String, Message> messages = OutMessageCaptureProcessor.getCapturedMessageMap(exchange1);
            assertThat(messages).hasSize(3);
            assertThat(messages.get("s1").getBody()).isEqualTo("Hiram");
            assertThat(messages.get("s2").getBody()).isEqualTo("Hello Hiram");
            assertThat(messages.get("s3").getBody()).isEqualTo("Hello Hiram");

            Exchange exchange2 = result.getExchanges().get(1);
            exchangeCompletedNotifier.waitForCompletion(exchange2);
            Map<String, Message> messages2 = OutMessageCaptureProcessor.getCapturedMessageMap(exchange2);
            assertThat(messages2).hasSize(3);
            assertThat(messages2.get("s1").getBody()).isEqualTo("World");
            assertThat(messages2.get("s2").getBody()).isEqualTo("Hello World");
            assertThat(messages2.get("s3").getBody()).isEqualTo("Hello World");
        } finally {
            context.stop();
        }
    }

    @Test
    public void testCaptureWithForeachAndSchedule() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        ExchangeCompletedNotifier exchangeCompletedNotifier = new ExchangeCompletedNotifier();
        context.getManagementStrategy().addEventNotifier(exchangeCompletedNotifier);

        try {
            Integration integration = newIntegration(
                new Step.Builder()
                    .id("s1")
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "getdata")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.foreach)
                    .build(),
                new Step.Builder()
                    .id("s2")
                    .stepKind(StepKind.extension)
                    .action(new StepAction.Builder()
                        .descriptor(new StepDescriptor.Builder()
                            .kind(StepAction.Kind.BEAN)
                            .entrypoint(Bean1.class.getName())
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id("s3")
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "expression")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.endForeach)
                    .build()
            );

            final Flow flow = integration.getFlows().get(0);
            final Flow flowWithScheduler = flow.builder()
                .scheduler(new Scheduler.Builder()
                    .expression("60s")
                    .build())
            .build();

            integration = new Integration.Builder()
                .createFrom(integration)
                .flows(singleton(flowWithScheduler))
                .build();

            IntegrationRouteBuilder routes = newIntegrationRouteBuilder(integration);
            routes.from("direct:getdata").bean(new Bean3());

            // Set up the camel context

            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);
            result.expectedBodiesReceived("Hello Hiram", "Hello World");
            result.assertIsSatisfied();

            Exchange exchange1 = result.getExchanges().get(0);
            exchangeCompletedNotifier.waitForCompletion(exchange1);
            Map<String, Message> messages = OutMessageCaptureProcessor.getCapturedMessageMap(exchange1);
            assertThat(messages).hasSize(3);
            assertThat(messages.get("s1").getBody()).isEqualTo("Hiram");
            assertThat(messages.get("s2").getBody()).isEqualTo("Hello Hiram");
            assertThat(messages.get("s3").getBody()).isEqualTo("Hello Hiram");

            Exchange exchange2 = result.getExchanges().get(1);
            exchangeCompletedNotifier.waitForCompletion(exchange2);
            Map<String, Message> messages2 = OutMessageCaptureProcessor.getCapturedMessageMap(exchange2);
            assertThat(messages2).hasSize(3);
            assertThat(messages2.get("s1").getBody()).isEqualTo("World");
            assertThat(messages2.get("s2").getBody()).isEqualTo("Hello World");
            assertThat(messages2.get("s3").getBody()).isEqualTo("Hello World");
        } finally {
            context.stop();
        }
    }

    public static class Bean1 {
        @Handler
        public String apply(@Body String body) {
            return "Hello " + body;
        }
    }

    public static class Bean2 {
        @Handler
        public int apply(@Body String body) {
            return body.hashCode();
        }
    }
    public static class Bean3 {
        @Handler
        public String[] apply(@Body String body) {
            return new String[]{ "Hiram", "World" };
        }
    }

    private class ExchangeCompletedNotifier extends EventNotifierSupport {
        private List<Exchange> completedExchanges = new ArrayList<>();

        @Override
        public void notify(EventObject event) throws Exception {
            if (event instanceof ExchangeCompletedEvent) {
                completedExchanges.add(((ExchangeCompletedEvent) event).getExchange());
            }
        }

        @Override
        public boolean isEnabled(EventObject event) {
            return event instanceof ExchangeCompletedEvent;
        }

        public void waitForCompletion(Exchange exchange) throws InterruptedException, ExecutionException, TimeoutException {
            waitForCompletion(exchange, 5000L, TimeUnit.MILLISECONDS);
        }

        public void waitForCompletion(Exchange exchange, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            CompletableFuture<Exchange> completed = new CompletableFuture<>();
            ScheduledFuture<?> completedHandle = null;

            try {
                completedHandle = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                    if (completedExchanges.parallelStream().anyMatch((completedExchange) -> completedExchange.getExchangeId().equals(exchange.getExchangeId()))) {
                        completed.complete(exchange);
                    }
                }, 0, timeout / 10, unit);

                completed.get(timeout, unit);
            } finally {
                Optional.ofNullable(completedHandle).ifPresent((handle) -> handle.cancel(true));
            }
        }
    }
}
