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
        IntegrationRouteBuilderTest.TestConfiguration.class,
    },
    properties = {
        "debug = true",
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG"
    }
)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class IntegrationRouteBuilderTest extends IntegrationTestSupport {
    @Test
    public void testBuilder() throws Exception {
        IntegrationRuntimeConfiguration configuration = new IntegrationRuntimeConfiguration();
        IntegrationRouteBuilder routeBuilder = new IntegrationRouteBuilder(configuration.getConfigurationLocation(), Collections.emptyList());

        // initialize routes
        routeBuilder.configure();

        // Dump routes as XML for troubleshooting
        dumpRoutes(new DefaultCamelContext(), routeBuilder.getRouteCollection());

        RoutesDefinition routes = routeBuilder.getRouteCollection();

        assertThat(routes.getRoutes()).hasSize(1);

        RouteDefinition route = routes.getRoutes().get(0);

        assertThat(route.getInputs()).hasSize(1);
        assertThat(route.getInputs().get(0)).hasFieldOrPropertyWithValue("uri", "direct:expression");
        assertThat(route.getOutputs()).hasSize(3);
        assertThat(getOutput(route, 0)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(route, 1)).isInstanceOf(ProcessDefinition.class);
        assertThat(getOutput(route, 2)).isInstanceOf(PipelineDefinition.class);
        assertThat(getOutput(route, 2).getOutputs()).hasSize(2);
        assertThat(getOutput(route, 2, 0)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(route, 2, 1)).isInstanceOf(SplitDefinition.class);
        assertThat(getOutput(route, 2, 1).getOutputs()).hasSize(2);
        assertThat(getOutput(route, 2, 1, 0)).isInstanceOf(ProcessDefinition.class);
        assertThat(getOutput(route, 2, 1, 1)).isInstanceOf(PipelineDefinition.class);
        assertThat(getOutput(route, 2, 1, 1).getOutputs()).hasSize(3);
        assertThat(getOutput(route, 2, 1, 1, 0)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(route, 2, 1, 1, 1)).isInstanceOf(ToDefinition.class);
        assertThat(getOutput(route, 2, 1, 1, 1)).hasFieldOrPropertyWithValue("uri", "mock:expression");
        assertThat(getOutput(route, 2, 1, 1, 2)).isInstanceOf(ProcessDefinition.class);
    }

    // ***************************
    //
    // ***************************

    @Configuration
    public static class TestConfiguration {
    }
}
