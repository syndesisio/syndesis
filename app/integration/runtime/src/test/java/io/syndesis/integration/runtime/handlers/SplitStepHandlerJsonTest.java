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
import java.util.Collections;
import java.util.List;

import io.syndesis.common.util.Resources;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import io.syndesis.integration.runtime.IntegrationTestSupport;
import io.syndesis.integration.runtime.logging.BodyLogger;
import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class SplitStepHandlerJsonTest extends IntegrationTestSupport {

    /**
     * Test split to the very end of the integration - no aggregate
     * direct -> split -> mock
     */
    @Test
    public void testSplitToEnd() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = new IntegrationRouteBuilder(
                    "classpath:/syndesis/integration/split-to-end.json",
                    Resources.loadServices(IntegrationStepHandler.class)
            );

            // Set up the camel context
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);
            final String[] expected = { "a","b","c" };
            final List<String> body = Arrays.asList(expected);

            result.expectedMessageCount(3);
            result.expectedBodiesReceived((Object[])expected);

            template.sendBody("direct:expression", body);

            result.assertIsSatisfied();
        } finally {
            context.stop();
        }
    }

    /**
     * Test simple split/aggregate use case.
     * direct -> split -> log -> aggregate -> mock
     */
    @Test
    public void testSplitAggregate() throws Exception {
        final DefaultCamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = new IntegrationRouteBuilder(
                    "classpath:/syndesis/integration/split-aggregate.json",
                    Resources.loadServices(IntegrationStepHandler.class)
            );

            // Set up the camel context
            context.addRoutes(routes);
            addBodyLogger(context);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);
            final List<String> body = Arrays.asList("a", "b", "c");

            result.expectedMessageCount(1);

            template.sendBody("direct:expression", body);

            result.assertIsSatisfied();
            List<?> bodyReceived = result.getExchanges().get(0).getIn().getBody(List.class);
            assertThat(bodyReceived).hasSize(3);
            assertThat(bodyReceived.get(0)).isEqualTo("a");
            assertThat(bodyReceived.get(1)).isEqualTo("b");
            assertThat(bodyReceived.get(2)).isEqualTo("c");
        } finally {
            context.stop();
        }
    }

    /**
     * Test inconsistent split aggregate combination where some additional aggregate steps are added to the integration.
     * direct -> split -> log -> aggregate -> aggregate -> split -> log -> aggregate -> aggregate -> mock
     */
    @Test
    public void testInconsistentSplitAggregate() throws Exception {
        final DefaultCamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = new IntegrationRouteBuilder(
                    "classpath:/syndesis/integration/inconsistent-split.json",
                    Resources.loadServices(IntegrationStepHandler.class)
            );

            // Set up the camel context
            context.addRoutes(routes);
            addBodyLogger(context);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);
            final List<String> body = Arrays.asList("a,b,c", "de", "f,g");

            result.expectedBodiesReceived(body);

            template.sendBody("direct:expression", body);

            result.assertIsSatisfied();
        } finally {
            context.stop();
        }
    }

    /**
     * Test chains of split/aggregate where a 2nd split directly follows on the 1st aggregate.
     * direct -> split -> log -> aggregate -> split -> log -> aggregate -> mock
     */
    @Test
    public void testSplitAggregateChain() throws Exception {
        final DefaultCamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = new IntegrationRouteBuilder(
                    "classpath:/syndesis/integration/split-chain.json",
                    Resources.loadServices(IntegrationStepHandler.class)
            );

            // Set up the camel context
            context.addRoutes(routes);
            addBodyLogger(context);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);
            final List<String> body = Arrays.asList("a,b,c", "de", "f,g");

            result.expectedBodiesReceived(body);

            template.sendBody("direct:expression", body);

            result.assertIsSatisfied();
        } finally {
            context.stop();
        }
    }

    /**
     * Test subsequent split/aggregate where a 1st split operates on a initial collection of elements and a
     * 2nd split operates on a completely new collection provided by some mock endpoint.
     * direct -> split -> log -> aggregate -> bean:myMock -> split -> log -> aggregate -> mock
     */
    @Test
    public void testSubsequentSplitAggregate() throws Exception {
        final DefaultCamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = new IntegrationRouteBuilder(
                    "classpath:/syndesis/integration/subsequent-split.json",
                    Resources.loadServices(IntegrationStepHandler.class)
            );

            // Set up the camel context
            context.addRoutes(routes);

            SimpleRegistry beanRegistry = new SimpleRegistry();
            beanRegistry.put("bodyLogger", new BodyLogger.Default());
            beanRegistry.put("myMock", (Processor) exchange -> exchange.getIn().setBody(Arrays.asList("d", "e", "f")));
            context.setRegistry(beanRegistry);

            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);
            final List<String> body = Arrays.asList("a", "b", "c");

            result.expectedMessageCount(1);

            template.sendBody("direct:expression", body);

            result.assertIsSatisfied();
            List<?> bodyReceived = result.getExchanges().get(0).getIn().getBody(List.class);
            assertThat(bodyReceived).hasSize(3);
            assertThat(bodyReceived.get(0)).isEqualTo("d");
            assertThat(bodyReceived.get(1)).isEqualTo("e");
            assertThat(bodyReceived.get(2)).isEqualTo("f");
        } finally {
            context.stop();
        }
    }

    /**
     * Test multiple split steps in an integration, both split operating to the very end of an integration - no aggregate
     * direct -> split -> log -> split -> mock
     */
    @Test
    public void testMultipleSplit() throws Exception {
        final DefaultCamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = new IntegrationRouteBuilder(
                    "classpath:/syndesis/integration/multiple-split.json",
                    Resources.loadServices(IntegrationStepHandler.class)
            );

            // Set up the camel context
            context.addRoutes(routes);
            addBodyLogger(context);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);
            final List<String> body = Arrays.asList("a,b,c", "de", "f,g");

            result.expectedBodiesReceived("a", "b", "c", "de", "f", "g");

            template.sendBody("direct:expression", body);

            result.assertIsSatisfied();
        } finally {
            context.stop();
        }
    }

    /**
     * Test nested split/aggregate where a 2nd split lives inside the 1st split/aggregate
     * direct -> split -> log -> split -> log -> mock -> aggregate -> log -> aggregate -> mock
     */
    @Test
    public void testNestedSplitAggregate() throws Exception {
        final DefaultCamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = new IntegrationRouteBuilder(
                    "classpath:/syndesis/integration/nested-split.json",
                    Resources.loadServices(IntegrationStepHandler.class)
            );

            // Set up the camel context
            context.addRoutes(routes);
            addBodyLogger(context);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);
            final MockEndpoint nestedResult = context.getEndpoint("mock:nested-mock", MockEndpoint.class);
            final List<String> body = Arrays.asList("a,b,c", "de", "f,g");

            result.expectedMessageCount(1);
            nestedResult.expectedBodiesReceived("a", "b", "c", "de", "f", "g");

            template.sendBody("direct:expression", body);

            result.assertIsSatisfied();
            nestedResult.assertIsSatisfied();

            List<?> bodyReceived = result.getExchanges().get(0).getIn().getBody(List.class);
            assertThat(bodyReceived).hasSize(3);
            assertThat(bodyReceived.get(0)).isEqualTo(Arrays.asList("a","b","c"));
            assertThat(bodyReceived.get(1)).isEqualTo(Collections.singletonList("de"));
            assertThat(bodyReceived.get(2)).isEqualTo(Arrays.asList("f","g"));
        } finally {
            context.stop();
        }
    }

    /**
     * Test filter step that lives after a split - no aggregate
     * direct -> split -> filter -> log -> mock
     */
    @Test
    public void testFilterAfterSplit() throws Exception {
        final DefaultCamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = new IntegrationRouteBuilder(
                    "classpath:/syndesis/integration/filter-after-split.json",
                    Resources.loadServices(IntegrationStepHandler.class)
            );

            // Set up the camel context
            context.addRoutes(routes);
            addBodyLogger(context);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);
            final List<String> body = Arrays.asList("{\"task\": \"Play with the dog\"}",
                                                    "{\"task\": \"Wash the dog\"}",
                                                    "{\"task\": \"Feed the dog\"}",
                                                    "{\"task\": \"Walk the dog\"}");

            result.expectedMessageCount(1);

            template.sendBody("direct:expression", body);

            result.assertIsSatisfied();
            assertThat(result.getExchanges().get(0).getIn().getBody(String.class)).isEqualTo("{\"task\": \"Feed the dog\"}");
        } finally {
            context.stop();
        }
    }

    /**
     * Test filter step that lives inside of a split/aggregate. Only the filtered matches should be aggregated.
     * direct -> split -> filter -> log -> aggregate -> mock
     */
    @Test
    public void testFilterInSplitAggregate() throws Exception {
        final DefaultCamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = new IntegrationRouteBuilder(
                "classpath:/syndesis/integration/filter-in-split.json",
                Resources.loadServices(IntegrationStepHandler.class)
            );

            // Set up the camel context
            context.addRoutes(routes);
            addBodyLogger(context);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);
            final List<String> body = Arrays.asList("{\"task\": \"Play with the dog\"}",
                                                    "{\"task\": \"Wash the dog\"}",
                                                    "{\"task\": \"Feed the dog\"}",
                                                    "{\"task\": \"Walk the dog\"}");


            result.expectedMessageCount(1);

            template.sendBody("direct:expression", body);

            result.assertIsSatisfied();
            List<?> bodyReceived = result.getExchanges().get(0).getIn().getBody(List.class);
            assertThat(bodyReceived).hasSize(1);
            assertThat(bodyReceived.get(0)).isEqualTo("{\"task\": \"Wash the dog\"}");
        } finally {
            context.stop();
        }
    }

    private void addBodyLogger(DefaultCamelContext context) {
        SimpleRegistry beanRegistry = new SimpleRegistry();
        beanRegistry.put("bodyLogger", new BodyLogger.Default());
        context.setRegistry(beanRegistry);
    }
}
