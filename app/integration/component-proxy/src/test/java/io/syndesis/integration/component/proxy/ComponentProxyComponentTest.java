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
package io.syndesis.integration.component.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

import org.apache.camel.Body;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.Registry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ComponentProxyComponentTest extends CamelTestSupport {

    private void injectBeanToRegistry() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("beanName", "my-bean");

        ComponentProxyComponent component = new ComponentProxyComponent("my-bean-proxy", "bean");
        component.setOptions(properties);

        Registry registry = super.context.getRegistry();
        registry.bind("my-bean", new MyBean());
        registry.bind(component.getComponentId() + "-component", component);
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        this.injectBeanToRegistry();
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start")
                    .to("my-bean-proxy:my-bean")
                    .to("mock:result");
            }
        };
    }

    // ***************************
    // Test
    // ***************************

    @Test
    public void testRequest() {
        final String body = "hello";
        final String result = template().requestBody("direct:start", body, String.class);

        Assertions.assertThat(result).isEqualTo(body.toUpperCase(Locale.US));
    }

    @Test
    public void testSend() throws Exception {
        final MockEndpoint mock = getMockEndpoint("mock:result");
        final String body = "hello";

        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived(body.toUpperCase(Locale.US));

        template().sendBody("direct:start", body);

        mock.assertIsSatisfied();
    }

    // ***************************
    // Support
    // ***************************

    public static class MyBean {
        public String process(@Body String body) {
            return body.toUpperCase(Locale.US);
        }
    }
}
