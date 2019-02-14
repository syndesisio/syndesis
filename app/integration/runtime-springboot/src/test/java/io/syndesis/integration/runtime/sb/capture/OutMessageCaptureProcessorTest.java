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

import java.util.Collections;
import java.util.Map;

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
import org.apache.camel.spring.SpringCamelContext;
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
    public void testCaptureWithSplitAggregate() throws Exception {
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
                    .id("s-split")
                    .stepKind(StepKind.split)
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
                    .id("s-aggregate")
                    .stepKind(StepKind.aggregate)
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
            assertThat(messages).hasSize(5);
            assertThat(messages.get("s-split").getBody()).isEqualTo("World");
            assertThat(messages.get("s2").getBody()).isEqualTo("Hello World");
            assertThat(messages.get("s3").getBody()).isEqualTo(-862545276);
            assertThat(messages.get("s4").getBody()).isEqualTo(-862545276);
            assertThat(messages.get("s-aggregate").getBody()).isEqualTo(Collections.singletonList(-862545276));
        } finally {
            context.stop();
        }
    }

    @Test
    public void testCaptureWithSplitAggregateAndSchedule() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

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
                    .id("s-split")
                    .stepKind(StepKind.split)
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
                    .id("s-aggregate")
                    .stepKind(StepKind.aggregate)
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
            Map<String, Message> messages = OutMessageCaptureProcessor.getCapturedMessageMap(exchange1);
            assertThat(messages.get("s-split").getBody()).isEqualTo("Hiram");
            assertThat(messages.get("s2").getBody()).isEqualTo("Hello Hiram");

            Exchange exchange2 = result.getExchanges().get(1);
            Map<String, Message> messages2 = OutMessageCaptureProcessor.getCapturedMessageMap(exchange2);
            assertThat(messages2.get("s-split").getBody()).isEqualTo("World");
            assertThat(messages2.get("s2").getBody()).isEqualTo("Hello World");
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
}
