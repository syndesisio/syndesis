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
package io.syndesis.camel.component.proxy.runtime;

import java.util.Base64;

import io.syndesis.camel.component.proxy.ComponentProxyComponent;
import io.syndesis.camel.component.proxy.ComponentProxyComponentTest;
import io.syndesis.camel.component.proxy.ComponentProxyFactory;
import io.syndesis.integration.runtime.SyndesisRouteBuilder;
import org.apache.camel.Body;
import org.apache.camel.Processor;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ComponentProxyRuntimeTest extends CamelTestSupport {

    // ***************************
    // Set up camel context
    // ***************************

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();
        registry.bind("my-bean", new ComponentProxyComponentTest.MyBean());

        return registry;
    }

    // ***************************
    // Set up camel context
    // ***************************

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        final String path = getClass().getSimpleName() + ".yaml";
        final RoutesBuilder builder = new SyndesisRouteBuilder("classpath:" + path);

        return builder;
    }

    // ***************************
    // Test
    // ***************************

    @Test
    public void testRequest() {
        final String body = "hello";
        final String result = template().requestBody("direct:start", body, String.class);
        final String expected = Base64.getEncoder().encodeToString(body.toUpperCase().getBytes());

        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testSend() throws Exception {
        final MockEndpoint mock = getMockEndpoint("mock:result");
        final String body = "hello";
        final String expected = Base64.getEncoder().encodeToString(body.toUpperCase().getBytes());

        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived(expected);

        template().sendBody("direct:start", body);

        mock.assertIsSatisfied();
    }

    // ***************************
    // Support
    // ***************************

    public static class MyBean {
        public String process(@Body String body) {
            return body.toUpperCase();
        }
    }

    public static class MyFactory implements ComponentProxyFactory {
        @Override
        public ComponentProxyComponent newInstance(String componentId, String componentScheme) {
            return new ComponentProxyComponent(componentId, componentScheme) {
                @Override
                public Processor getAfterProducer() {
                    return e -> e.getIn().setBody(
                        Base64.getEncoder().encodeToString(e.getIn().getBody(byte[].class))
                    );
                }
            };
        }
    }
}
