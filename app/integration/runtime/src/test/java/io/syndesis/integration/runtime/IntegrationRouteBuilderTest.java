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

import io.syndesis.integration.runtime.handlers.support.StepHandlerTestSupport;
import org.apache.camel.model.FromDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.model.SplitDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        IntegrationRuntimeAutoConfiguration.class,
        IntegrationRouteBuilderTest.TestConfiguration.class
    },
    properties = {
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG"
    }
)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class IntegrationRouteBuilderTest extends StepHandlerTestSupport {
    @Autowired
    private IntegrationRouteBuilder routeBuilder;

    @Test
    public void testBuilder() throws Exception {
        // initialize routes
        routeBuilder.configure();

        RoutesDefinition routes = routeBuilder.getRouteCollection();

        assertThat(routes.getRoutes()).hasSize(1);

        RouteDefinition definition = routes.getRoutes().get(0);
        
        assertThat(definition.getInputs()).hasSize(1);
        assertThat(definition.getInputs().get(0)).isInstanceOf(FromDefinition.class);
        assertThat(definition.getOutputs()).hasSize(1);
        assertThat(definition.getOutputs().get(0)).isInstanceOf(SplitDefinition.class);
    }

    // ***************************
    //
    // ***************************

    @Configuration
    public static class TestConfiguration {
    }
}
