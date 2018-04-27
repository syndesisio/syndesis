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

import java.util.Collections;

import io.syndesis.common.model.Split;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Scheduler;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.PipelineDefinition;
import org.apache.camel.model.ProcessDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.model.SetHeaderDefinition;
import org.apache.camel.model.SplitDefinition;
import org.apache.camel.model.ToDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        IntegrationRouteTest.TestConfiguration.class
    },
    properties = {
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG"
    }
)
@SuppressWarnings("PMD")
public class IntegrationRouteTest extends IntegrationTestSupport {
    @Test
    public void integrationWithSchedulerTest() throws Exception {
        final RouteBuilder routeBuilder = new IntegrationRouteBuilder("", Collections.emptyList()) {
            @Override
            protected Integration loadIntegration() {
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

        // initialize routes
        routeBuilder.configure();

        dumpRoutes(new DefaultCamelContext(), routeBuilder.getRouteCollection());

        RoutesDefinition routes = routeBuilder.getRouteCollection();
        assertThat(routes.getRoutes()).hasSize(1);

        RouteDefinition route = routes.getRoutes().get(0);

        // Timer
        assertThat(route.getInputs()).hasSize(1);
        assertThat(route.getInputs().get(0)).hasFieldOrPropertyWithValue("uri", "timer:integration?period=1s");
        assertThat(route.getOutputs()).hasSize(3);
        assertThat(getOutput(route, 0)).isInstanceOf(ToDefinition.class);
        assertThat(getOutput(route, 0)).hasFieldOrPropertyWithValue("uri", "log:timer");
        assertThat(getOutput(route, 1)).isInstanceOf(ProcessDefinition.class);
        assertThat(getOutput(route, 2)).isInstanceOf(PipelineDefinition.class);
        assertThat(getOutput(route, 2).getOutputs()).hasSize(3);
        assertThat(getOutput(route, 2, 0)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(route, 2, 1)).isInstanceOf(ToDefinition.class);
        assertThat(getOutput(route, 2, 1)).hasFieldOrPropertyWithValue("uri", "mock:timer");
        assertThat(getOutput(route, 2, 2)).isInstanceOf(ProcessDefinition.class);
    }

    @Test
    public void integrationWithSplitTest() throws Exception {
        final RouteBuilder routeBuilder = new IntegrationRouteBuilder("", Collections.emptyList()) {
            @Override
            protected Integration loadIntegration() {
                Integration integration = newIntegration(
                    new Step.Builder()
                        .id("step-1")
                        .stepKind(StepKind.endpoint)
                        .action(new ConnectorAction.Builder()
                            .descriptor(new ConnectorDescriptor.Builder()
                                .componentScheme("direct")
                                .putConfiguredProperty("name", "start")
                                .split(new Split.Builder().build())
                                .build())
                            .build())
                        .build(),
                    new Step.Builder()
                        .id("step-2")
                        .stepKind(StepKind.endpoint)
                        .action(new ConnectorAction.Builder()
                            .descriptor(new ConnectorDescriptor.Builder()
                                .componentScheme("bean")
                                .putConfiguredProperty("beanName", "io.syndesis.integration.runtime.IntegrationRouteTest.TestConfiguration")
                                .build())
                            .build())
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
                        .build());

                return new Integration.Builder()
                    .createFrom(integration)
                    .build();
            }
        };

        // initialize routes
        routeBuilder.configure();

        dumpRoutes(new DefaultCamelContext(), routeBuilder.getRouteCollection());

        RoutesDefinition routes = routeBuilder.getRouteCollection();
        assertThat(routes.getRoutes()).hasSize(1);

        RouteDefinition route = routes.getRoutes().get(0);

        assertThat(route.getInputs()).hasSize(1);
        assertThat(route.getInputs().get(0)).hasFieldOrPropertyWithValue("uri", "direct:start");
        assertThat(route.getOutputs()).hasSize(2);
        assertThat(getOutput(route, 0)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(route, 1)).isInstanceOf(SplitDefinition.class);
        assertThat(getOutput(route, 1).getOutputs()).hasSize(3);
        assertThat(getOutput(route, 1, 0)).isInstanceOf(ProcessDefinition.class);
        assertThat(getOutput(route, 1, 1)).isInstanceOf(PipelineDefinition.class);
        assertThat(getOutput(route, 1, 2)).isInstanceOf(PipelineDefinition.class);
    }

    @Test
    public void integrationWithSchedulerAndSplitTest() throws Exception {
        final RouteBuilder routeBuilder = new IntegrationRouteBuilder("", Collections.emptyList()) {
            @Override
            protected Integration loadIntegration() {
                Integration integration = newIntegration(
                    new Step.Builder()
                        .id("step-1")
                        .stepKind(StepKind.endpoint)
                        .action(new ConnectorAction.Builder()
                            .descriptor(new ConnectorDescriptor.Builder()
                                .componentScheme("log")
                                .putConfiguredProperty("loggerName", "timer")
                                .split(new Split.Builder().build())
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

        // initialize routes
        routeBuilder.configure();

        dumpRoutes(new DefaultCamelContext(), routeBuilder.getRouteCollection());

        RoutesDefinition routes = routeBuilder.getRouteCollection();
        assertThat(routes.getRoutes()).hasSize(1);

        RouteDefinition route = routes.getRoutes().get(0);

        assertThat(route.getInputs()).hasSize(1);
        assertThat(route.getInputs().get(0)).hasFieldOrPropertyWithValue("uri", "timer:integration?period=1s");
        assertThat(route.getOutputs()).hasSize(2);
        assertThat(getOutput(route, 0)).isInstanceOf(ToDefinition.class);
        assertThat(getOutput(route, 1)).isInstanceOf(SplitDefinition.class);
        assertThat(getOutput(route, 1).getOutputs()).hasSize(3);
        assertThat(getOutput(route, 1, 0)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(route, 1, 1)).isInstanceOf(ProcessDefinition.class);
        assertThat(getOutput(route, 1, 2)).isInstanceOf(PipelineDefinition.class);
        assertThat(getOutput(route, 1, 2).getOutputs()).hasSize(3);
        assertThat(getOutput(route, 1, 2, 0)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(route, 1, 2, 1)).isInstanceOf(ToDefinition.class);
        assertThat(getOutput(route, 1, 2, 1)).hasFieldOrPropertyWithValue("uri", "mock:timer");
        assertThat(getOutput(route, 1, 2, 2)).isInstanceOf(ProcessDefinition.class);
    }

    // ***************************
    //
    // ***************************

    @Configuration
    public static class TestConfiguration {
    }
}
