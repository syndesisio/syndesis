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

import io.syndesis.connector.rest.swagger.auth.apikey.ApiKey;
import io.syndesis.connector.rest.swagger.auth.basic.Basic;
import io.syndesis.connector.rest.swagger.auth.oauth.OAuth;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

import org.apache.camel.CamelContext;

public class AuthenticationCustomizer implements ComponentProxyCustomizer {

    @Override
    public void customize(final ComponentProxyComponent proxyComponent, final Map<String, Object> options) {
        consumeOption(options, "authenticationType", authenticationTypeObject -> {
            final String authenticationTypeAsString = String.valueOf(authenticationTypeObject);
            final AuthenticationType authenticationType = AuthenticationType.fromString(authenticationTypeAsString);
            if (authenticationType == AuthenticationType.none) {
                return;
            }

            final SwaggerProxyComponent component = (SwaggerProxyComponent) proxyComponent;

            final CamelContext context = component.getCamelContext();
            final Configuration configuration = new Configuration(this, context, options);

            switch (authenticationType) {
            case oauth2:
                OAuth.setup(component, configuration);
                break;
            case basic:
                Basic.setup(component, configuration);
                break;
            case apiKey:
                ApiKey.setup(component, configuration);
                break;
            default:
                throw new IllegalStateException("Unsupported authentication type: " + authenticationType);
            }
        });
    }

}
