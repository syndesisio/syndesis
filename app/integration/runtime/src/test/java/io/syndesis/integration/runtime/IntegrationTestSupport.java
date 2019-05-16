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

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.util.Resources;
import io.syndesis.common.util.StringConstants;
import io.syndesis.integration.runtime.logging.ActivityTracker;
import io.syndesis.integration.runtime.logging.IntegrationActivityTrackingPolicyFactory;
import io.syndesis.integration.runtime.logging.FlowActivityTrackingPolicyFactory;
import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.model.ModelHelper;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrationTestSupport implements StringConstants {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTestSupport.class);

    protected ObjectMapper mapper = new ObjectMapper();

    protected static CamelContext getDefaultCamelContextWithMyBeanInRegistry(){
        SimpleRegistry sr = new SimpleRegistry();
        sr.put("myBean", new MyBean());
        DefaultCamelContext ctx = new DefaultCamelContext(sr);
        return ctx;
    }

    protected static class DataPair {
        private String key;
        private Object value;

        public DataPair(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    protected void dumpRoutes(CamelContext context, RoutesDefinition definition) {
        try {
            LOGGER.info("Routes: \n{}",ModelHelper.dumpModelAsXml(context, definition));
        } catch (JAXBException e) {
            LOGGER.warn("", e);
        }
    }

    protected void dumpRoutes(CamelContext context) {
        RoutesDefinition definition = new RoutesDefinition();
        definition.setRoutes(context.getRouteDefinitions());

        dumpRoutes(context, definition);
    }

    protected static IntegrationRouteBuilder newIntegrationRouteBuilder(ActivityTracker activityTracker, Step... steps) {
        return newIntegrationRouteBuilder(newIntegration(steps), activityTracker);
    }

    protected static IntegrationRouteBuilder newIntegrationRouteBuilder(Step... steps) {
        return newIntegrationRouteBuilder(null, steps);
    }

    protected static IntegrationRouteBuilder newIntegrationRouteBuilder(Integration integration) {
        return newIntegrationRouteBuilder(integration, null);
    }

    protected static IntegrationRouteBuilder newIntegrationRouteBuilder(Integration integration, ActivityTracker activityTracker) {
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

    protected static Integration newIntegration(Step... steps) {
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

    protected ProcessorDefinition<?> getOutput(RouteDefinition definition, int... indices) {
        ProcessorDefinition<?> output = definition;
        for (int index : indices) {
            output = output.getOutputs().get(index);
        }

        return output;
    }

    protected DataPair dataPair(String key, String value) {
        return new DataPair(key, value);
    }

    protected DataPair dataPair(String key, DataPair... values) {
        if (values.length == 1)
            return new DataPair(key, values[0]);

        return new DataPair(key, values);
    }

    private JsonNode jsonData(DataPair... snippets) {
        ObjectNode node = mapper.createObjectNode();

        for (DataPair snippet : snippets) {
            String key = snippet.key.toString();

            if (snippet.value instanceof String) {
                node.put(key, snippet.value.toString());
            } else if (snippet.value instanceof String[]) {
                ArrayNode array = mapper.createArrayNode();
                Arrays.stream((String[]) snippet.value).forEach(array::add);
                node.set(key, array);
            } else if (snippet.value instanceof DataPair[]) {
                ArrayNode array = mapper.createArrayNode();
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

    protected String data(DataPair... snippets) throws Exception {
        JsonNode jsonNode = jsonData(snippets);
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        return json;
    }

    public static final class MyBean {
        @SuppressWarnings("PMD.UseLocaleWithCaseConversions")
        public String myProcessor(@Body String body) {
            return body.toUpperCase();
        }
    }
}
