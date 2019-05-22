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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.integration.runtime.IntegrationTestSupport;
import io.syndesis.integration.runtime.capture.OutMessageCaptureProcessor;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.model.PipelineDefinition;
import org.apache.camel.model.ProcessDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.SetHeaderDefinition;
import org.apache.camel.model.ToDefinition;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
public class DataMapperStepHandlerTest extends IntegrationTestSupport {

    private CamelContext camelContext = new DefaultCamelContext();

    @Test
    public void testDataMapperStep() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routeBuilder = newIntegrationRouteBuilder(getTestSteps());

            // Set up the camel context
            context.addRoutes(routeBuilder);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            List<RouteDefinition> routes = context.getRouteDefinitions();
            assertThat(routes).hasSize(1);

            RouteDefinition route = context.getRouteDefinitions().get(0);
            assertThat(route).isNotNull();
            assertThat(route.getInputs()).hasSize(1);
            assertThat(route.getInputs().get(0)).hasFieldOrPropertyWithValue("uri", "direct:start");
            assertThat(route.getOutputs()).hasSize(5);
            assertThat(route.getOutputs().get(0)).isInstanceOf(PipelineDefinition.class);
            assertThat(route.getOutputs().get(0).getOutputs()).hasSize(2);
            assertThat(route.getOutputs().get(0).getOutputs().get(0)).isInstanceOf(SetHeaderDefinition.class);
            assertThat(route.getOutputs().get(0).getOutputs().get(1)).isInstanceOf(SetHeaderDefinition.class);
            assertThat(route.getOutputs().get(1)).isInstanceOf(SetHeaderDefinition.class);
            assertThat(route.getOutputs().get(2)).isInstanceOf(ProcessDefinition.class);

            // Atlas
            assertThat(route.getOutputs().get(3)).isInstanceOf(PipelineDefinition.class);
            assertThat(route.getOutputs().get(3).getOutputs()).hasSize(3);
            assertThat(route.getOutputs().get(3).getOutputs().get(0)).isInstanceOf(SetHeaderDefinition.class);
            assertThat(route.getOutputs().get(3).getOutputs().get(1)).isInstanceOf(ToDefinition.class);
            assertThat(route.getOutputs().get(3).getOutputs().get(1)).hasFieldOrPropertyWithValue(
                "uri",
                "atlas:mapping-flow-0-step-1.json?encoding=UTF-8&sourceMapName=" + OutMessageCaptureProcessor.CAPTURED_OUT_MESSAGES_MAP
            );

            assertThat(route.getOutputs().get(3).getOutputs().get(2)).isInstanceOf(ProcessDefinition.class);
            assertThat(route.getOutputs().get(4)).isInstanceOf(PipelineDefinition.class);
            assertThat(route.getOutputs().get(4).getOutputs()).hasSize(3);
            assertThat(route.getOutputs().get(4).getOutputs().get(0)).isInstanceOf(SetHeaderDefinition.class);
            assertThat(route.getOutputs().get(4).getOutputs().get(1)).isInstanceOf(ToDefinition.class);
            assertThat(route.getOutputs().get(4).getOutputs().get(1)).hasFieldOrPropertyWithValue("uri", "mock:result");
            assertThat(route.getOutputs().get(4).getOutputs().get(2)).isInstanceOf(ProcessDefinition.class);
        } finally {
            context.stop();
        }
    }

    @Test
    public void testJsonTypeProcessors() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routeBuilder = newIntegrationRouteBuilder(getTestSteps("{" +
                                    "\"AtlasMapping\":{" +
                                        "\"dataSource\":[" +
                                            "{\"jsonType\":\"" + DataMapperStepHandler.ATLASMAP_JSON_DATA_SOURCE + "\",\"id\":\"source\",\"uri\":\"atlas:json:source\",\"dataSourceType\":\"SOURCE\"}," +
                                            "{\"jsonType\":\"" + DataMapperStepHandler.ATLASMAP_JSON_DATA_SOURCE + "\",\"id\":\"target\",\"uri\":\"atlas:json:target\",\"dataSourceType\":\"TARGET\",\"template\":null}" +
                                        "]," +
                                        "\"mappings\":{}" +
                                        "}" +
                                    "}"));

            // Set up the camel context
            context.addRoutes(routeBuilder);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            List<RouteDefinition> routes = context.getRouteDefinitions();
            assertThat(routes).hasSize(1);

            RouteDefinition route = context.getRouteDefinitions().get(0);
            assertThat(route).isNotNull();
            assertThat(route.getInputs()).hasSize(1);
            assertThat(route.getInputs().get(0)).hasFieldOrPropertyWithValue("uri", "direct:start");
            assertThat(route.getOutputs()).hasSize(5);
            assertThat(route.getOutputs().get(0)).isInstanceOf(PipelineDefinition.class);
            assertThat(route.getOutputs().get(0).getOutputs()).hasSize(2);
            assertThat(route.getOutputs().get(0).getOutputs().get(0)).isInstanceOf(SetHeaderDefinition.class);
            assertThat(route.getOutputs().get(0).getOutputs().get(1)).isInstanceOf(SetHeaderDefinition.class);
            assertThat(route.getOutputs().get(1)).isInstanceOf(SetHeaderDefinition.class);
            assertThat(route.getOutputs().get(2)).isInstanceOf(ProcessDefinition.class);

            // Atlas
            assertThat(route.getOutputs().get(3)).isInstanceOf(PipelineDefinition.class);
            assertThat(route.getOutputs().get(3).getOutputs()).hasSize(5);
            assertThat(route.getOutputs().get(3).getOutputs().get(0)).isInstanceOf(SetHeaderDefinition.class);
            assertThat(route.getOutputs().get(3).getOutputs().get(1)).isInstanceOf(ProcessDefinition.class);
            assertThat(route.getOutputs().get(3).getOutputs().get(2)).isInstanceOf(ToDefinition.class);
            assertThat(route.getOutputs().get(3).getOutputs().get(2)).hasFieldOrPropertyWithValue(
                    "uri",
                    "atlas:mapping-flow-0-step-1.json?encoding=UTF-8&sourceMapName=" + OutMessageCaptureProcessor.CAPTURED_OUT_MESSAGES_MAP
            );
            assertThat(route.getOutputs().get(3).getOutputs().get(3)).isInstanceOf(ProcessDefinition.class);
            assertThat(route.getOutputs().get(3).getOutputs().get(4)).isInstanceOf(ProcessDefinition.class);
            assertThat(route.getOutputs().get(4)).isInstanceOf(PipelineDefinition.class);
            assertThat(route.getOutputs().get(4).getOutputs()).hasSize(3);
            assertThat(route.getOutputs().get(4).getOutputs().get(0)).isInstanceOf(SetHeaderDefinition.class);
            assertThat(route.getOutputs().get(4).getOutputs().get(1)).isInstanceOf(ToDefinition.class);
            assertThat(route.getOutputs().get(4).getOutputs().get(1)).hasFieldOrPropertyWithValue("uri", "mock:result");
            assertThat(route.getOutputs().get(4).getOutputs().get(2)).isInstanceOf(ProcessDefinition.class);
        } finally {
            context.stop();
        }
    }

    @Test
    public void testJsonTypeProcessorsSkip() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routeBuilder = newIntegrationRouteBuilder(getTestSteps("{" +
                                    "\"AtlasMapping\":{" +
                                        "\"dataSource\":[" +
                                            "{\"jsonType\":\"io.atlasmap.v2.DataSource\",\"id\":\"source\",\"uri\":\"atlas:java?className=twitter4j.Status\",\"dataSourceType\":\"SOURCE\"}," +
                                            "{\"jsonType\":\"io.atlasmap.v2.DataSource\",\"id\":\"target\",\"uri\":\"atlas:java?className=io.syndesis.connector.gmail.GmailMessageModel\",\"dataSourceType\":\"TARGET\",\"template\":null}" +
                                        "]," +
                                        "\"mappings\":{}" +
                                        "}" +
                                    "}"));

            // Set up the camel context
            context.addRoutes(routeBuilder);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            List<RouteDefinition> routes = context.getRouteDefinitions();
            assertThat(routes).hasSize(1);

            RouteDefinition route = context.getRouteDefinitions().get(0);
            assertThat(route).isNotNull();
            assertThat(route.getInputs()).hasSize(1);
            assertThat(route.getInputs().get(0)).hasFieldOrPropertyWithValue("uri", "direct:start");
            assertThat(route.getOutputs()).hasSize(5);
            assertThat(route.getOutputs().get(0)).isInstanceOf(PipelineDefinition.class);
            assertThat(route.getOutputs().get(0).getOutputs()).hasSize(2);
            assertThat(route.getOutputs().get(0).getOutputs().get(0)).isInstanceOf(SetHeaderDefinition.class);
            assertThat(route.getOutputs().get(0).getOutputs().get(1)).isInstanceOf(SetHeaderDefinition.class);
            assertThat(route.getOutputs().get(1)).isInstanceOf(SetHeaderDefinition.class);
            assertThat(route.getOutputs().get(2)).isInstanceOf(ProcessDefinition.class);

            // Atlas
            assertThat(route.getOutputs().get(3)).isInstanceOf(PipelineDefinition.class);
            assertThat(route.getOutputs().get(3).getOutputs()).hasSize(3);
            assertThat(route.getOutputs().get(3).getOutputs().get(0)).isInstanceOf(SetHeaderDefinition.class);
            assertThat(route.getOutputs().get(3).getOutputs().get(1)).isInstanceOf(ToDefinition.class);
            assertThat(route.getOutputs().get(3).getOutputs().get(1)).hasFieldOrPropertyWithValue(
                    "uri",
                    "atlas:mapping-flow-0-step-1.json?encoding=UTF-8&sourceMapName=" + OutMessageCaptureProcessor.CAPTURED_OUT_MESSAGES_MAP
            );
            assertThat(route.getOutputs().get(3).getOutputs().get(2)).isInstanceOf(ProcessDefinition.class);
            assertThat(route.getOutputs().get(4)).isInstanceOf(PipelineDefinition.class);
            assertThat(route.getOutputs().get(4).getOutputs()).hasSize(3);
            assertThat(route.getOutputs().get(4).getOutputs().get(0)).isInstanceOf(SetHeaderDefinition.class);
            assertThat(route.getOutputs().get(4).getOutputs().get(1)).isInstanceOf(ToDefinition.class);
            assertThat(route.getOutputs().get(4).getOutputs().get(1)).hasFieldOrPropertyWithValue("uri", "mock:result");
            assertThat(route.getOutputs().get(4).getOutputs().get(2)).isInstanceOf(ProcessDefinition.class);
        } finally {
            context.stop();
        }
    }

    private Step[] getTestSteps() {
        return getTestSteps("{}");
    }

    private Step[] getTestSteps(String atlasMapping) {
        return new Step[]{
                new Step.Builder()
                        .id("step-1")
                        .stepKind(StepKind.endpoint)
                        .action(new ConnectorAction.Builder()
                                .descriptor(new ConnectorDescriptor.Builder()
                                        .componentScheme("direct")
                                        .putConfiguredProperty("name", "start")
                                        .build())
                                .build())
                        .build(),
                new Step.Builder()
                        .id("step-2")
                        .stepKind(StepKind.mapper)
                        .putConfiguredProperty("atlasmapping", atlasMapping)
                        .build(),
                new Step.Builder()
                        .id("step-3")
                        .stepKind(StepKind.endpoint)
                        .action(new ConnectorAction.Builder()
                                .descriptor(new ConnectorDescriptor.Builder()
                                        .componentScheme("mock")
                                        .putConfiguredProperty("name", "result")
                                        .build())
                                .build())
                        .build()
        };
    }

    @Test
    public void testJsonTypeSourceProcessingOnExchange() throws Exception {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(Arrays.asList("{\"name\": \"Bert\", \"age\": 31}", "{\"name\": \"Stuart\", \"age\": 30}"));

        new DataMapperStepHandler.JsonTypeSourceProcessor(Collections.singletonList("m1"), 1).process(exchange);

        assertThat(exchange.getIn().getBody(String.class)).isEqualTo("[{\"name\": \"Bert\", \"age\": 31},{\"name\": \"Stuart\", \"age\": 30}]");
    }

    @Test
    public void testJsonTypeSourceProcessingOnExchangeMutipleSourceDocs() throws Exception {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(Arrays.asList("{\"name\": \"Howard\", \"age\": 29}", "{\"name\": \"Sheldon\", \"age\": 30}"));

        new DataMapperStepHandler.JsonTypeSourceProcessor(Collections.singletonList("m1"), 3).process(exchange);

        assertThat(exchange.getIn().getBody(String.class)).isEqualTo("[{\"name\": \"Howard\", \"age\": 29},{\"name\": \"Sheldon\", \"age\": 30}]");
    }

    @Test
    public void testJsonTypeSourceProcessingSkipOnNonListBody() throws Exception {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody("{\"name\": \"Leonard\", \"age\": 30}");

        new DataMapperStepHandler.JsonTypeSourceProcessor(Collections.singletonList("m1"), 1).process(exchange);

        assertThat(exchange.getIn().getBody(String.class)).isEqualTo("{\"name\": \"Leonard\", \"age\": 30}");
    }

    @Test
    public void testJsonTypeSourceProcessing() throws Exception {
        Exchange exchange = new DefaultExchange(camelContext);
        Map<String, Message> captured = new HashMap<>();

        Message m1 = new DefaultMessage(camelContext);
        m1.setBody(Arrays.asList("{\"name\": \"Howard\", \"age\": 29}", "{\"name\": \"Sheldon\", \"age\": 30}"));
        captured.put("m1", m1);

        Message m2 = new DefaultMessage(camelContext);
        m2.setBody("{\"something\": \"else\"}");
        captured.put("m2", m2);

        exchange.setProperty(OutMessageCaptureProcessor.CAPTURED_OUT_MESSAGES_MAP, captured);

        new DataMapperStepHandler.JsonTypeSourceProcessor(Arrays.asList("m1", "m2"), 2).process(exchange);

        assertThat(captured.get("m1").getBody(String.class)).isEqualTo("[{\"name\": \"Howard\", \"age\": 29},{\"name\": \"Sheldon\", \"age\": 30}]");
        assertThat(captured.get("m2").getBody(String.class)).isEqualTo("{\"something\": \"else\"}");
    }

    @Test
    public void testJsonTypeSourceProcessingMissingSources() throws Exception {
        Exchange exchange = new DefaultExchange(camelContext);
        Map<String, Message> captured = new HashMap<>();

        Message m1 = new DefaultMessage(camelContext);
        m1.setBody(Collections.singletonList("{\"name\": \"Amy\", \"age\": 29}"));
        captured.put("m1", m1);

        exchange.setProperty(OutMessageCaptureProcessor.CAPTURED_OUT_MESSAGES_MAP, captured);

        new DataMapperStepHandler.JsonTypeSourceProcessor(Arrays.asList("m1", "m2_missing", "m3_missing"), 3).process(exchange);

        assertThat(captured.get("m1").getBody(String.class)).isEqualTo("[{\"name\": \"Amy\", \"age\": 29}]");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testJsonTypeTargetProcessing() throws Exception {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody("[{\"name\": \"Bernadette\", \"age\": 27},{\"name\": \"Penny\", \"age\": 29}]");

        new DataMapperStepHandler.JsonTypeTargetProcessor().process(exchange);
        assertThat(exchange.getIn().getBody(String.class)).isEqualTo("[{\"name\": \"Bernadette\", \"age\": 27},{\"name\": \"Penny\", \"age\": 29}]");

        exchange.setProperty(DataMapperStepHandler.DATA_MAPPER_AUTO_CONVERSION, true);
        exchange.getIn().setBody("[{\"name\": \"Bernadette\", \"age\": 27},{\"name\": \"Penny\", \"age\": 29}]");

        new DataMapperStepHandler.JsonTypeTargetProcessor().process(exchange);

        List<String> jsonBeans = exchange.getIn().getBody(List.class);
        assertThat(jsonBeans).isEqualTo(Arrays.asList("{\"name\":\"Bernadette\",\"age\":27}", "{\"name\":\"Penny\",\"age\":29}"));
    }

    @Test
    public void testJsonTypeTargetProcessingEmptyList() throws Exception {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody("[]");

        new DataMapperStepHandler.JsonTypeTargetProcessor().process(exchange);
        assertThat(exchange.getIn().getBody(String.class)).isEqualTo("[]");

        exchange.setProperty(DataMapperStepHandler.DATA_MAPPER_AUTO_CONVERSION, true);
        exchange.getIn().setBody("[]");

        new DataMapperStepHandler.JsonTypeTargetProcessor().process(exchange);

        List<?> processedBody = exchange.getIn().getBody(List.class);
        assertThat(processedBody).hasSize(0);
    }

    @Test
    public void testJsonTypeTargetProcessingNoArrayType() throws Exception {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody("{\"name\": \"Leonard\", \"age\": 30}");

        new DataMapperStepHandler.JsonTypeTargetProcessor().process(exchange);

        assertThat(exchange.getIn().getBody(String.class)).isEqualTo("{\"name\": \"Leonard\", \"age\": 30}");

        exchange.setProperty(DataMapperStepHandler.DATA_MAPPER_AUTO_CONVERSION, true);
        new DataMapperStepHandler.JsonTypeTargetProcessor().process(exchange);

        assertThat(exchange.getIn().getBody(String.class)).isEqualTo("{\"name\": \"Leonard\", \"age\": 30}");
    }

    @Test
    public void testJsonTypeTargetProcessingNoJsonType() throws Exception {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody("something completely different");

        new DataMapperStepHandler.JsonTypeTargetProcessor().process(exchange);

        assertThat(exchange.getIn().getBody(String.class)).isEqualTo("something completely different");

        exchange.setProperty(DataMapperStepHandler.DATA_MAPPER_AUTO_CONVERSION, true);
        new DataMapperStepHandler.JsonTypeTargetProcessor().process(exchange);

        assertThat(exchange.getIn().getBody(String.class)).isEqualTo("something completely different");
    }

    @Test
    public void testJsonTypeTargetProcessingNoStringBody() throws Exception {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(100L);

        new DataMapperStepHandler.JsonTypeTargetProcessor().process(exchange);

        assertThat(exchange.getIn().getBody(Long.class)).isEqualTo(Long.valueOf(100L));

        exchange.setProperty(DataMapperStepHandler.DATA_MAPPER_AUTO_CONVERSION, true);
        new DataMapperStepHandler.JsonTypeTargetProcessor().process(exchange);

        assertThat(exchange.getIn().getBody(Long.class)).isEqualTo(Long.valueOf(100L));
    }
}
