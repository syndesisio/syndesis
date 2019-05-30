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
package io.syndesis.connector.rest.swagger;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.DelegateEndpoint;
import org.apache.camel.Endpoint;
import org.apache.camel.component.rest.swagger.RestSwaggerEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SwaggerProxyComponentTest {
    @Test
    public void testSwaggerProxyComponent() throws Exception {
        CamelContext context = new DefaultCamelContext();
        ComponentProxyFactory factory = new ConnectorFactory();
        ComponentProxyComponent proxy = factory.newInstance("swagger-1", "rest-swagger");

        try {
            proxy.setCamelContext(context);
            proxy.start();

            Endpoint endpoint = proxy.createEndpoint("swagger-1:http://foo.bar");

            assertThat(endpoint).isInstanceOfSatisfying(DelegateEndpoint.class, e -> {
                assertThat(e.getEndpoint()).isInstanceOf(RestSwaggerEndpoint.class);
                assertThat(e.getEndpoint()).hasFieldOrPropertyWithValue("componentName", SyndesisRestSwaggerComponent.COMPONENT_NAME);
            });
        } finally {
            proxy.stop();
        }
    }
}
