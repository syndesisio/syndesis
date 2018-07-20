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

import java.util.List;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.integration.runtime.IntegrationTestSupport;
import io.syndesis.integration.runtime.capture.OutMessageCaptureProcessor;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.PipelineDefinition;
import org.apache.camel.model.ProcessDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.SetHeaderDefinition;
import org.apache.camel.model.ToDefinition;
import org.apache.camel.spring.SpringCamelContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        DataMapperStepHandlerTest.TestConfiguration.class
    },
    properties = {
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG"
    }
)
@TestExecutionListeners(
    listeners = {
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class
    }
)
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
public class DataMapperStepHandlerTest extends IntegrationTestSupport {
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testDataMapperStep() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        try {
            final RouteBuilder routeBuilder = newIntegrationRouteBuilder(
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
                    .putConfiguredProperty("atlasmapping", "{}")
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
            );

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
            assertThat(route.getOutputs()).hasSize(4);
            assertThat(route.getOutputs().get(0)).isInstanceOf(SetHeaderDefinition.class);
            assertThat(route.getOutputs().get(1)).isInstanceOf(ProcessDefinition.class);

            // Atlas
            assertThat(route.getOutputs().get(2)).isInstanceOf(PipelineDefinition.class);
            assertThat(route.getOutputs().get(2).getOutputs()).hasSize(3);
            assertThat(route.getOutputs().get(2).getOutputs().get(0)).isInstanceOf(SetHeaderDefinition.class);
            assertThat(route.getOutputs().get(2).getOutputs().get(1)).isInstanceOf(ToDefinition.class);
            assertThat(route.getOutputs().get(2).getOutputs().get(1)).hasFieldOrPropertyWithValue(
                "uri",
                "atlas:mapping-step-2.json?encoding=UTF-8&sourceMapName=" + OutMessageCaptureProcessor.CAPTURED_OUT_MESSAGES_MAP
            );

            assertThat(route.getOutputs().get(2).getOutputs().get(2)).isInstanceOf(ProcessDefinition.class);
            assertThat(route.getOutputs().get(3)).isInstanceOf(PipelineDefinition.class);
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
