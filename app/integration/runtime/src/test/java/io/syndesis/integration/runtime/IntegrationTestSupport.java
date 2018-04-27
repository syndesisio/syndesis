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
import javax.xml.bind.JAXBException;

import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import org.apache.camel.CamelContext;
import org.apache.camel.model.ModelHelper;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrationTestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTestSupport.class);

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

    protected static IntegrationRouteBuilder newIntegrationRouteBuilder(Step... steps) {
        return newIntegrationRouteBuilder(newIntegration(steps));
    }

    protected static IntegrationRouteBuilder newIntegrationRouteBuilder(Integration integration) {
        return new IntegrationRouteBuilder("", Collections.emptyList()) {
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
            .steps(Arrays.asList(steps))
            .build();
    }

    protected ProcessorDefinition<?> getOutput(RouteDefinition definition, int... indices) {
        ProcessorDefinition<?> output = definition;
        for (int index : indices) {
            output = output.getOutputs().get(index);
        }

        return output;
    }
}
