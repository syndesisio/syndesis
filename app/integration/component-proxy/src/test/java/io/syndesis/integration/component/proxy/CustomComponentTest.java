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

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.CollectionHelper;
import org.junit.jupiter.api.Test;

import com.acme.corp.AcmeComponent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class CustomComponentTest {

    @Test
    public void testCustomComponent() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new ComponentProxyComponent("acme-1", "acme"))
            .withMessage(
                "Failed to find component definition for scheme 'acme'. Missing component definition in classpath 'org/apache/camel/catalog/components/acme.json'");
    }

    @Test
    public void testCustomComponentEndpoint() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        final ComponentProxyComponent component = new ComponentProxyComponent("acme-1", "acme", AcmeComponent.class);
        component.setCamelContext(context);
        component.setOptions(CollectionHelper.mapOf("name", "test", "param", "p1"));
        component.start();

        final Endpoint endpoint = component.createEndpoint("acme");

        assertThat(endpoint).isInstanceOf(ComponentProxyEndpoint.class);
        assertThat(endpoint).hasFieldOrPropertyWithValue("delegateEndpointUri", "acme://test?param=p1");
    }

    @Test
    public void testCustomComponentWithClass() {
        final ComponentProxyComponent proxyComponent = new ComponentProxyComponent("acme-1", "acme", AcmeComponent.class);
        assertThat(proxyComponent.getComponentId()).isEqualTo("acme-1");
        assertThat(proxyComponent.getComponentScheme()).isEqualTo("acme");
    }

    @Test
    public void testCustomComponentWithClassName() {
        final ComponentProxyComponent proxyComponent = new ComponentProxyComponent("acme-1", "acme", AcmeComponent.class.getName());
        assertThat(proxyComponent.getComponentId()).isEqualTo("acme-1");
        assertThat(proxyComponent.getComponentScheme()).isEqualTo("acme");
    }

}
