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

package io.syndesis.connector.kudu;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

public class AbstractKuduCustomizerTestSupport {
    private final ComponentProxyComponent component = new ComponentProxyComponent("kudu-1", "kudu");

    public ComponentProxyComponent getComponent() {
        return component;
    }

    protected static CamelContext createCamelContext() {
        final DefaultCamelContext context = new DefaultCamelContext();
        context.disableJMX();
        context.setAutoStartup(false);

        return context;
    }
}
