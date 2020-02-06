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
package io.syndesis.integration.runtime;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.camel.CamelContext;
import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.spi.ModelToXMLDumper;
import org.apache.camel.support.SimpleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.util.Resources;
import io.syndesis.integration.runtime.logging.ActivityTracker;
import io.syndesis.integration.runtime.logging.FlowActivityTrackingPolicyFactory;
import io.syndesis.integration.runtime.logging.IntegrationActivityTrackingPolicyFactory;

public final class IntegrationTestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTestSupport.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private IntegrationTestSupport() {
      // utility class
    }

    public static DefaultCamelContext getDefaultCamelContextWithMyBeanInRegistry(){
        SimpleRegistry sr = new SimpleRegistry();
        sr.bind("myBean", new MyBean());
        DefaultCamelContext ctx = new DefaultCamelContext(sr);
        return ctx;
    }

    public static class DataPair {
        private final String key;
        private final Object value;

        public DataPair(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    public static void dumpRoutes(CamelContext context, RoutesDefinition definition) {
        if (!LOGGER.isInfoEnabled()) {
            return;
        }

        try {
            ExtendedCamelContext extendedCamelContext = context.adapt(ExtendedCamelContext.class);
            ModelToXMLDumper dumper = extendedCamelContext.getModelToXMLDumper();
            LOGGER.info("Routes: \n{}", dumper.dumpModelAsXml(context, definition));
        } catch (Exception e) {
            LOGGER.warn("Unable to dump route definition as XML");
            LOGGER.debug("Error encountered while dumping route definition as XML", e);
        }
    }

    public static void dumpRoutes(ModelCamelContext context) {
        RoutesDefinition definition = new RoutesDefinition();
        definition.setRoutes(context.getRouteDefinitions());

        dumpRoutes(context, definition);
    }

    public static IntegrationRouteBuilder newIntegrationRouteBuilder(ActivityTracker activityTracker, Step... steps) {
        return newIntegrationRouteBuilder(newIntegration(steps), activityTracker);
    }

    public static IntegrationRouteBuilder newIntegrationRouteBuilder(Step... steps) {
        return newIntegrationRouteBuilder(null, steps);
    }

    public static IntegrationRouteBuilder newIntegrationRouteBuilder(Integration integration) {
        return newIntegrationRouteBuilder(integration, null);
    }

    public static IntegrationRouteBuilder newIntegrationRouteBuilder(Integration integration, ActivityTracker activityTracker) {
        List<ActivityTrackingPolicyFactory> activityTrackingPolicyFactories = Collections.emptyList();
        if(activityTracker!=null) {
            activityTrackingPolicyFactories = Arrays.asList(new IntegrationActivityTrackingPolicyFactory(activityTracker),
                                                            new FlowActivityTrackingPolicyFactory(activityTracker));
        }
        return new
            IntegrationRouteBuilder("", Resources.loadServices(IntegrationStepHandler.class), activityTrackingPolicyFactories) {
            @Override
            protected Integration loadIntegration() throws IOException {
                return integration;
            }
        };
    }

    public static Integration newIntegration(Step... steps) {
        for (int i = 0; i < steps.length; i++) {
            steps[i] = new Step.Builder().createFrom(steps[i]).build();
        }

        return new Integration.Builder()
            .id("test-integration")
            .name("Test Integration")
            .description("This is a test integration!")
            .addFlow(new Flow.Builder()
                    .steps(Arrays.asList(steps))
                .build())
            .build();
    }

    public static ProcessorDefinition<?> getOutput(RouteDefinition definition, int... indices) {
        ProcessorDefinition<?> output = definition;
        for (int index : indices) {
            output = output.getOutputs().get(index);
        }

        return output;
    }

    public static DataPair dataPair(String key, String value) {
        return new DataPair(key, value);
    }

    public static DataPair dataPair(String key, DataPair... values) {
        if (values.length == 1)
            return new DataPair(key, values[0]);

        return new DataPair(key, values);
    }

    private static JsonNode jsonData(DataPair... snippets) {
        ObjectNode node = MAPPER.createObjectNode();

        for (DataPair snippet : snippets) {
            String key = snippet.key.toString();

            if (snippet.value instanceof String) {
                node.put(key, snippet.value.toString());
            } else if (snippet.value instanceof String[]) {
                ArrayNode array = MAPPER.createArrayNode();
                Arrays.stream((String[]) snippet.value).forEach(array::add);
                node.set(key, array);
            } else if (snippet.value instanceof DataPair[]) {
                ArrayNode array = MAPPER.createArrayNode();
                Arrays.stream((DataPair[]) snippet.value).forEach(me -> {
                    JsonNode element = jsonData(me);
                    array.add(element);
                });
                node.set(key, array);
            } else if (snippet.value instanceof DataPair) {
                JsonNode valueNode = jsonData((DataPair) snippet.value);
                node.set(key, valueNode);
            }
        }

        return node;
    }

    public static String data(DataPair... snippets) throws Exception {
        JsonNode jsonNode = jsonData(snippets);

        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    }

}
