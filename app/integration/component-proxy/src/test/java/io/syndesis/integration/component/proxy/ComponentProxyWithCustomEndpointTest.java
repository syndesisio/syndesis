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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.remote.FtpEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.JUnitTestsShouldIncludeAssert"})
public class ComponentProxyWithCustomEndpointTest {

    // *************************
    // Tests
    // *************************

    @Test
    public void testCustomizeDelegatedEndpoint() throws Exception{
        final boolean binary = true;
        final String username = "my-user";

        Map<String, Object> properties = new HashMap<>();
        properties.put("transfer-mode-binary", Boolean.toString(binary));
        properties.put("username", username);
        properties.put("host", "localhost");

        ComponentProxyComponent component = new ComponentProxyComponent("my-ftp-proxy", "ftp") {
            @Override
            protected void configureDelegateEndpoint(ComponentDefinition definition, Endpoint endpoint, Map<String, Object> options) throws Exception {
                assertThat(endpoint).isNotNull();
                assertThat(endpoint).isInstanceOf(FtpEndpoint.class);

                FtpEndpoint<?> ftpEndpoint = FtpEndpoint.class.cast(endpoint);
                boolean binary = Objects.equals("true", options.get("transfer-mode-binary"));

                ftpEndpoint.getConfiguration().setBinary(binary);
                assertThat(ftpEndpoint.getConfiguration().isBinary()).isEqualTo(binary);
            }
        };

        component.setOptions(properties);

        SimpleRegistry registry = new SimpleRegistry();
        registry.put(component.getComponentId() + "-component", component);

        final CamelContext context = new DefaultCamelContext(registry);

        try {
            context.setAutoStartup(false);
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("direct:start")
                        .to("my-ftp-proxy")
                        .to("mock:result");
                }
            });

            context.start();

            Collection<String> names = context.getComponentNames();
            assertThat(names).contains("my-ftp-proxy");
            assertThat(names).contains("ftp-my-ftp-proxy");
            assertThat(context.getEndpointMap().keySet()).contains("ftp-my-ftp-proxy://localhost?username=my-user");

            FtpEndpoint<?> ftpEndpoint = context.getEndpoint("ftp-my-ftp-proxy://localhost?username=my-user", FtpEndpoint.class);
            assertThat(ftpEndpoint).isNotNull();
            assertThat(ftpEndpoint.getConfiguration()).hasFieldOrPropertyWithValue("binary", binary);
            assertThat(ftpEndpoint.getConfiguration()).hasFieldOrPropertyWithValue("username", username);

        } finally {
            context.stop();
        }
    }
}
