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

import io.syndesis.connector.rest.swagger.auth.basic.Basic;
import io.syndesis.connector.rest.swagger.auth.oauth.OAuth;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

import org.apache.camel.CamelContext;

public class AuthenticationCustomizer implements ComponentProxyCustomizer {

    @Override
    public void customize(final ComponentProxyComponent component, final Map<String, Object> options) {
        consumeOption(options, "authenticationType", authenticationTypeObject -> {
            final AuthenticationType authenticationType = AuthenticationType.valueOf(String.valueOf(authenticationTypeObject));
            if (authenticationType == AuthenticationType.none) {
                return;
            }

            final CamelContext context = component.getCamelContext();
            final Configuration configuration = new Configuration(this, context, options);

            if (authenticationType == AuthenticationType.oauth2) {
                OAuth.setup(component, configuration);
            } else if (authenticationType == AuthenticationType.basic) {
                Basic.setup(component, configuration);
            } else {
                throw new IllegalStateException("Unsupported authentication type: " + authenticationType);
            }

        });
    }

}
