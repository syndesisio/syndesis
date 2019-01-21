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
import io.syndesis.integration.runtime.IntegrationTestSupport;
import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.SpringCamelContext;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        ForeachStepHandlerTest.TestConfiguration.class
    },
    properties = {
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG"
    }
)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class ForeachStepHandlerTest extends IntegrationTestSupport {
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testForeach() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(
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
                    .stepKind(StepKind.foreach)
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "foreach")
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
            final MockEndpoint result = context.getEndpoint("mock:foreach", MockEndpoint.class);
            final List<String> body = Arrays.asList("a","b","c");

            result.expectedBodiesReceived("a", "b", "c");

            List<?> response = template.requestBody("direct:expression", body, List.class);

            result.assertIsSatisfied();
            Assert.assertEquals(body, response);
        } finally {
            context.stop();
        }
    }

    @Test
    public void testForeachWithTrailingSteps() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(
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
                        .stepKind(StepKind.foreach)
                        .build(),
                    new Step.Builder()
                        .stepKind(StepKind.endpoint)
                        .action(new ConnectorAction.Builder()
                            .descriptor(new ConnectorDescriptor.Builder()
                                .componentScheme("mock")
                                .putConfiguredProperty("name", "foreach")
                                .build())
                            .build())
                        .build(),
                    new Step.Builder()
                        .stepKind(StepKind.endForeach)
                        .build(),
                    new Step.Builder()
                        .stepKind(StepKind.endpoint)
                        .action(new ConnectorAction.Builder()
                            .descriptor(new ConnectorDescriptor.Builder()
                                .componentScheme("mock")
                                .putConfiguredProperty("name", "afterForeach")
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
            final MockEndpoint foreach = context.getEndpoint("mock:foreach", MockEndpoint.class);
            final MockEndpoint afterForeach = context.getEndpoint("mock:afterForeach", MockEndpoint.class);
            final List<String> body = Arrays.asList("a","b","c");

            foreach.expectedBodiesReceived("a", "b", "c");
            afterForeach.expectedMessageCount(1);

            List<?> response = template.requestBody("direct:expression", body, List.class);

            foreach.assertIsSatisfied();
            afterForeach.assertIsSatisfied();
            Assert.assertEquals(body, afterForeach.getExchanges().get(0).getIn().getBody());
            Assert.assertEquals(body, response);
        } finally {
            context.stop();
        }
    }

    @Test
    public void testForeachWithTransformation() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(
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
                        .stepKind(StepKind.foreach)
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
                                .putConfiguredProperty("name", "foreach")
                                .build())
                            .build())
                        .build(),
                    new Step.Builder()
                        .stepKind(StepKind.endForeach)
                        .build(),
                    new Step.Builder()
                        .stepKind(StepKind.endpoint)
                        .action(new ConnectorAction.Builder()
                            .descriptor(new ConnectorDescriptor.Builder()
                                .componentScheme("mock")
                                .putConfiguredProperty("name", "afterForeach")
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
            final MockEndpoint foreach = context.getEndpoint("mock:foreach", MockEndpoint.class);
            final MockEndpoint afterForeach = context.getEndpoint("mock:afterForeach", MockEndpoint.class);
            final List<String> body = Arrays.asList("a","b","c");

            foreach.expectedBodiesReceived("A", "B", "C");
            afterForeach.expectedMessageCount(1);

            List<?> response = template.requestBody("direct:expression", body, List.class);

            foreach.assertIsSatisfied();
            afterForeach.assertIsSatisfied();
            Assert.assertEquals(Arrays.asList("A", "B", "C"), afterForeach.getExchanges().get(0).getIn().getBody());

            Assertions.assertThat(response.size()).isEqualTo(3);
            Assert.assertEquals(3L, response.size());
        } finally {
            context.stop();
        }
    }

    @Test
    public void testForeachTokenize() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(
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
                    .stepKind(StepKind.foreach)
                    .putConfiguredProperty("language", "tokenize")
                    .putConfiguredProperty("expression", "|")
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "foreach")
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
            final MockEndpoint result = context.getEndpoint("mock:foreach", MockEndpoint.class);
            final String body = "a|b|c";

            result.expectedBodiesReceived((Object[])body.split("|"));

            List<?> response = template.requestBody("direct:expression", body, List.class);

            result.assertIsSatisfied();
            Assert.assertEquals(5, response.size());
            Assert.assertEquals(body, response.stream().map(Object::toString).collect(Collectors.joining()));
        } finally {
            context.stop();
        }
    }

    @Test
    public void testForeachWithOriginalAggregationStrategy() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(
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
                    .stepKind(StepKind.foreach)
                    .putConfiguredProperty("language", "tokenize")
                    .putConfiguredProperty("expression", "|")
                    .putConfiguredProperty("aggregationStrategy", "original")
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "foreach")
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
            final MockEndpoint result = context.getEndpoint("mock:foreach", MockEndpoint.class);
            final String body = "a|b|c";

            result.expectedBodiesReceived((Object[])body.split("|"));

            String response = template.requestBody("direct:expression", body, String.class);

            result.assertIsSatisfied();
            Assert.assertEquals(body, response);
        } finally {
            context.stop();
        }
    }

    @Test
    public void testForeachWithLatestAggregationStrategy() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(
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
                            .stepKind(StepKind.foreach)
                            .putConfiguredProperty("aggregationStrategy", "latest")
                            .build(),
                    new Step.Builder()
                            .stepKind(StepKind.endpoint)
                            .action(new ConnectorAction.Builder()
                                    .descriptor(new ConnectorDescriptor.Builder()
                                            .componentScheme("mock")
                                            .putConfiguredProperty("name", "foreach")
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
            final MockEndpoint result = context.getEndpoint("mock:foreach", MockEndpoint.class);
            final List<String> body = Arrays.asList("a","b","c");

            result.expectedBodiesReceived(body);

            String response = template.requestBody("direct:expression", body, String.class);

            result.assertIsSatisfied();
            Assert.assertEquals("c", response);
        } finally {
            context.stop();
        }
    }

    @Test
    public void testForeachScriptAggregationStrategy() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(
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
                            .stepKind(StepKind.foreach)
                            .putConfiguredProperty("language", "tokenize")
                            .putConfiguredProperty("expression", "|")
                            .putConfiguredProperty("aggregationStrategy", "script")
                            .putConfiguredProperty("aggregationScriptLanguage", "nashorn")
                            .putConfiguredProperty("aggregationScript", "newExchange.in.body += oldExchange ? oldExchange.in.body : '';\n" +
                                                                        "newExchange;")
                            .build(),
                    new Step.Builder()
                            .stepKind(StepKind.endpoint)
                            .action(new ConnectorAction.Builder()
                                    .descriptor(new ConnectorDescriptor.Builder()
                                            .componentScheme("mock")
                                            .putConfiguredProperty("name", "foreach")
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
            final MockEndpoint result = context.getEndpoint("mock:foreach", MockEndpoint.class);
            final String body = "a|b|c";

            result.expectedBodiesReceived((Object[])body.split("|"));

            String response = template.requestBody("direct:expression", body, String.class);

            result.assertIsSatisfied();
            Assert.assertEquals("c|b|a", response);
        } finally {
            context.stop();
        }
    }

    // ***************************
    //
    // ***************************

    @Configuration
    public static class TestConfiguration {
        @Bean
        public SimpleEndpointStepHandlerTest.MyBean myBean() {
            return new SimpleEndpointStepHandlerTest.MyBean();
        }
    }

    public static final class MyBean {
        @SuppressWarnings("PMD.UseLocaleWithCaseConversions")
        public String myProcessor(@Body String body) {
            return body.toUpperCase();
        }
    }
}
