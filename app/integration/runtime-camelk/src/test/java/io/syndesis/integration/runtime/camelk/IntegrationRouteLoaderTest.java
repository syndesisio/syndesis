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
package io.syndesis.integration.runtime.camelk;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.k.Runtime;
import org.apache.camel.k.Sources;
import org.apache.camel.model.FromDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationRouteLoaderTest {

    @Test
    public void integrationRouteLoaderTest() throws Exception {
        IntegrationRouteLoader irl = new IntegrationRouteLoader();
        TestRuntime runtime = new TestRuntime();

        irl.load(runtime,
            Sources.fromURI("classpath:/syndesis/integration/integration.syndesis?language=syndesis"));

        assertThat(runtime.builders).hasSize(1);
        final RoutesBuilder routeBuilder = runtime.builders.get(0);
        assertThat(routeBuilder).isInstanceOf(RouteBuilder.class);

        RouteBuilder rb = (RouteBuilder) routeBuilder;
        // initialize routes
        rb.configure();
        final RoutesDefinition routeCollection = rb.getRouteCollection();
        final List<RouteDefinition> routes = routeCollection.getRoutes();
        assertThat(routes).hasSize(1);
        final RouteDefinition route = routes.get(0);
        final FromDefinition input = route.getInput();
        assertThat(input).isNotNull();
        assertThat(input.getEndpointUri()).isEqualTo("direct:expression");
    }

    static class TestRuntime implements Runtime {
        private final DefaultCamelContext camelContext;
        private final List<RoutesBuilder> builders;

        public TestRuntime() {
            this.camelContext = new DefaultCamelContext();
            this.builders = new ArrayList<>();
        }

        @Override
        public CamelContext getCamelContext() {
            return this.camelContext;
        }

        @Override
        public void addRoutes(RoutesBuilder builder) {
            this.builders.add(builder);
        }
    }
}
