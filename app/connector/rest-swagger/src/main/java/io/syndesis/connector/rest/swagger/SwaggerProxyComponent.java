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

import io.syndesis.connector.rest.swagger.auth.oauth.OAuthRefreshingEndpoint;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;

import org.apache.camel.Endpoint;

public final class SwaggerProxyComponent extends ComponentProxyComponent {
    private Configuration configuration;

    public SwaggerProxyComponent(final String componentId, final String componentScheme) {
        super(componentId, componentScheme);
    }

    @Override
    public Endpoint createEndpoint(final String uri) throws Exception {
        final Endpoint endpoint = super.createEndpoint(uri);

        if (configuration == null) {
            // AuthenticationCustomizer did not invoke setConfiguration
            // meaning we don't need to wrap the endpoint as no
            // authentication is to be performed
            return endpoint;
        }

        return new OAuthRefreshingEndpoint(this, configuration, endpoint);
    }

    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }
}
