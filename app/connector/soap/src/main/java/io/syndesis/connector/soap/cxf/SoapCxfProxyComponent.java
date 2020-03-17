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
package io.syndesis.connector.soap.cxf;

import java.util.Map;
import io.syndesis.integration.component.proxy.ComponentDefinition;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import org.apache.camel.Endpoint;
import org.apache.camel.component.cxf.CxfConfigurer;
import org.apache.camel.component.cxf.CxfEndpoint;

public final class SoapCxfProxyComponent extends ComponentProxyComponent {

    private CxfConfigurer endpointConfigurer;

    public SoapCxfProxyComponent(final String componentId, final String componentScheme) {
        super(componentId, componentScheme);
    }

    public void setCxfEndpointConfigurer(CxfConfigurer endpointConfigurer) {
        this.endpointConfigurer = endpointConfigurer;
    }

    @Override
    protected void configureDelegateEndpoint(ComponentDefinition definition, Endpoint endpoint, Map<String, Object> options) {
        super.configureDelegateEndpoint(definition, endpoint, options);
        ((CxfEndpoint)endpoint).setCxfConfigurer(endpointConfigurer);
    }
}
