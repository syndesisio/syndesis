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

import io.syndesis.integration.runtime.handlers.support.StepHandlerTestSupport;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.action.ConnectorDescriptor;
import io.syndesis.model.integration.Step;
import io.syndesis.model.integration.StepKind;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spring.SpringCamelContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        StepWithIdTest.TestConfiguration.class
    },
    properties = {
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG"
    }
)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class StepWithIdTest extends StepHandlerTestSupport {
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testWithIdStep() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(
                new Step.Builder()
                    .id("endpoint-1")
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "expression")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .id("split-1")
                    .stepKind(StepKind.split)
                    .build(),
                new Step.Builder()
                    .id("endpoint-2")
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

            RouteDefinition routeDefinition = context.getRouteDefinition("test-integration");

            assertThat(routeDefinition).isNotNull();
            assertThat(routeDefinition).hasFieldOrPropertyWithValue("id", "test-integration");

            assertThat(routeDefinition.getInputs()).hasSize(1);
            assertThat(routeDefinition.getInputs().get(0)).hasFieldOrPropertyWithValue("id", "endpoint-1");
            assertThat(routeDefinition.getOutputs()).hasSize(1);
            assertThat(routeDefinition.getOutputs().get(0)).hasFieldOrPropertyWithValue("id", "split-1");
            assertThat(routeDefinition.getOutputs().get(0).getOutputs()).hasSize(1);
            assertThat(routeDefinition.getOutputs().get(0).getOutputs().get(0)).hasFieldOrPropertyWithValue("id", "endpoint-2");
        } finally {
            context.stop();
        }
    }

    // ***************************
    //
    // ***************************

    @Configuration
    public static class TestConfiguration {
    }
}
