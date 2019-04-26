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

import com.acme.corp.AcmeComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.CollectionHelper;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomComponentTest {
    @Test(expected = IllegalArgumentException.class)
    public void testCustomComponent() {
        new ComponentProxyComponent("acme-1", "acme");
    }

    @Test
    public void testCustomComponentWithClass() {
        new ComponentProxyComponent("acme-1", "acme", AcmeComponent.class);
    }

    @Test
    public void testCustomComponentWithClassName() {
        new ComponentProxyComponent("acme-1", "acme", AcmeComponent.class.getName());
    }

    @Test
    public void testCustomComponentEndpoint() throws Exception {
        CamelContext context = new DefaultCamelContext();

        ComponentProxyComponent component = new ComponentProxyComponent("acme-1", "acme", AcmeComponent.class);
        component.setCamelContext(context);
        component.setOptions(CollectionHelper.mapOf("name", "test", "param", "p1"));
        component.start();

        Endpoint endpoint = component.createEndpoint("acme");

        assertThat(endpoint).isInstanceOf(ComponentProxyEndpoint.class);
        assertThat(endpoint).hasFieldOrPropertyWithValue("delegateEndpointUri", "acme://test?param=p1");
    }
}
