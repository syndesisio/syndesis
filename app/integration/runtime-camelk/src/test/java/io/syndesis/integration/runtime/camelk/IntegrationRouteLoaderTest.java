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

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.k.InMemoryRegistry;
import org.apache.camel.k.Source;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationRouteLoaderTest {

    @Test
    public void integrationRouteLoaderTest() throws Exception {
        IntegrationRouteLoader irl = new IntegrationRouteLoader();

        RouteBuilder rb = irl.load(new InMemoryRegistry(), Source.create("classpath:/syndesis/integration/integration.syndesis?language=syndesis"));

        assertThat(rb).isNotNull();
        // initialize routes
        rb.configure();
        assertThat(rb.getRouteCollection().getRoutes()).hasSize(1);
        assertThat(rb.getRouteCollection().getRoutes().get(0).getInputs()).hasSize(1);
        assertThat(rb.getRouteCollection().getRoutes().get(0).getInputs().get(0).getEndpointUri()).isEqualTo("direct:expression");
    }

}
