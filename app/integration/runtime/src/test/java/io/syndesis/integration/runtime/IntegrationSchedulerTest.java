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
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Scheduler;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spring.SpringCamelContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        IntegrationSchedulerTest.TestConfiguration.class
    },
    properties = {
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG"
    }
)
@SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.JUnitTestsShouldIncludeAssert"})
public class IntegrationSchedulerTest extends IntegrationTestSupport {
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void integrationSchedulerTest() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        try {
            final RouteBuilder routes = new IntegrationRouteBuilder("", Collections.emptyList()) {
                @Override
                protected Integration loadIntegration() throws IOException {
                    Integration integration = newIntegration(
                        new Step.Builder()
                            .id("step-1")
                            .stepKind(StepKind.endpoint)
                            .action(new ConnectorAction.Builder()
                                .descriptor(new ConnectorDescriptor.Builder()
                                    .componentScheme("log")
                                    .putConfiguredProperty("loggerName", "timer")
                                    .build())
                                .build())
                            .build(),
                        new Step.Builder()
                            .id("step-2")
                            .stepKind(StepKind.endpoint)
                            .action(new ConnectorAction.Builder()
                                .descriptor(new ConnectorDescriptor.Builder()
                                    .componentScheme("mock")
                                    .putConfiguredProperty("name", "timer")
                                    .build())
                                .build())
                            .build());

                    return new Integration.Builder()
                        .createFrom(integration)
                        .scheduler(new Scheduler.Builder()
                            .type(Scheduler.Type.timer)
                            .expression("1s")
                            .build())
                        .build();
                }
            };

            // Set up the camel context
            context.addRoutes(routes);
            context.start();

            dumpRoutes(context);

            RouteDefinition routeDefinition = context.getRouteDefinition("test-integration");

            assertThat(routeDefinition).isNotNull();
            assertThat(routeDefinition).hasFieldOrPropertyWithValue("id", "test-integration");
            assertThat(routeDefinition.getInputs()).hasSize(1);
            assertThat(routeDefinition.getInputs().get(0).getEndpointUri()).isEqualTo("timer:integration?period=1s");
            assertThat(routeDefinition.getOutputs()).hasSize(4);
            assertThat(routeDefinition.getOutputs().get(0)).hasFieldOrPropertyWithValue("endpointUri", "log:timer");
            assertThat(routeDefinition.getOutputs().get(1)).isInstanceOf(ProcessDefinition.class);
            assertThat(routeDefinition.getOutputs().get(2)).hasFieldOrPropertyWithValue("endpointUri", "mock:timer");
            assertThat(routeDefinition.getOutputs().get(3)).isInstanceOf(ProcessDefinition.class);

            assertThat(routeDefinition.getInputs().get(0)).hasFieldOrPropertyWithValue("id", "integration-scheduler");
            assertThat(routeDefinition.getOutputs().get(0)).hasFieldOrPropertyWithValue("id", "step-1");
            assertThat(routeDefinition.getOutputs().get(1)).hasFieldOrPropertyWithValue("id", "step-1-capture");
            assertThat(routeDefinition.getOutputs().get(2)).hasFieldOrPropertyWithValue("id", "step-2");
            assertThat(routeDefinition.getOutputs().get(3)).hasFieldOrPropertyWithValue("id", "step-2-capture");
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
        public Processor myProcessor() {
            final AtomicInteger counter = new AtomicInteger();

            return e -> {
                e.getIn().setBody(counter.getAndIncrement());
            };
        }
    }
}
