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

import java.util.Map;
import java.util.function.Function;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import org.apache.camel.Endpoint;

public final class SwaggerProxyComponent extends ComponentProxyComponent {

    private Function<Endpoint, Endpoint> endpointOverride = Function.identity();

    public SwaggerProxyComponent(final String componentId, final String componentScheme) {
        super(componentId, componentScheme);
    }

    @Override
    public Endpoint createEndpoint(final String uri) throws Exception {
        final Endpoint endpoint = super.createEndpoint(uri);

        return endpointOverride.apply(endpoint);
    }

    public void overrideEndpoint(final Function<Endpoint, Endpoint> endpointOverride) {
        this.endpointOverride = endpointOverride;
    }

    @Override
    protected void enrichOptions(Map<String, Object> options) {
        //
        // We need to force to use the SyndesisRestSwaggerComponent as:
        //
        // - in case of multiple http component present in the camel registry, the behavior of the delegating
        //   component could change and lead to unexpected results
        // - in case an integration contains multiple rest compatible component, then each of the rest connector
        //   could use a different http component depending of the initialization order
        //
        options.put("componentName", SyndesisRestSwaggerComponent.COMPONENT_NAME);
    }
}
