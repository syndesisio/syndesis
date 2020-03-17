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
package io.syndesis.connector.soap.cxf.auth;

import java.util.HashMap;
import java.util.Map;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import org.apache.camel.CamelContext;
import org.apache.camel.component.cxf.ChainedCxfConfigurer;
import org.apache.cxf.common.util.PropertyUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.common.ConfigurationConstants;
import org.apache.wss4j.common.ext.WSPasswordCallback;

import io.syndesis.connector.soap.cxf.ComponentProperties;
import io.syndesis.connector.soap.cxf.SoapCxfProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

public final class AuthenticationCustomizer implements ComponentProxyCustomizer {

    @Override
    public void customize(final ComponentProxyComponent component, final Map<String, Object> options) {
        consumeOption(options, ComponentProperties.AUTHENTICATION_TYPE, authType -> {
            switch (AuthenticationType.fromValue((String) authType)) {
            case NONE:
                // remove unused properties
                options.remove(ComponentProperties.USERNAME);
                options.remove(ComponentProperties.PASSWORD);
                options.remove(ComponentProperties.ADD_TIMESTAMP);
                options.remove(ConfigurationConstants.ADD_USERNAMETOKEN_NONCE);
                options.remove(ConfigurationConstants.ADD_USERNAMETOKEN_CREATED);
                break;
            case BASIC:
                // assume username and password are set, and pass through
                options.remove(ComponentProperties.ADD_TIMESTAMP);
                options.remove(ConfigurationConstants.ADD_USERNAMETOKEN_NONCE);
                options.remove(ConfigurationConstants.ADD_USERNAMETOKEN_CREATED);
                break;
            case WSSE_UT:
                addUsernameTokenConfigurer(component, options);
                break;
            }
        });
    }

    private static void addUsernameTokenConfigurer(ComponentProxyComponent component, Map<String, Object> options) {

        final CamelContext camelContext = component.getCamelContext();
        final String username = getResolvedValue(camelContext, (String) options.remove(ComponentProperties.USERNAME));
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Invalid empty or null username for WS-Security Username Token");
        }
        final String password = getResolvedValue(camelContext, (String) options.remove(ComponentProperties.PASSWORD));
        final String passwordTypeString = (String) options.remove(ConfigurationConstants.PASSWORD_TYPE);
        final boolean addTimeStamp = PropertyUtils.isTrue(options.remove(ComponentProperties.ADD_TIMESTAMP));

        // set WSSE-UT options and create client out interceptor
        final SoapCxfProxyComponent proxyComponent = (SoapCxfProxyComponent) component;
        proxyComponent.setCxfEndpointConfigurer(new ChainedCxfConfigurer.NullCxfConfigurer() {

            @Override
            public void configureClient(Client client) {

                // TODO reconcile with any WSDL defined WS security policy
                // for now, we assume there are no security policies in WSDL
                final StringBuilder actions = new StringBuilder();
                final Map<String, Object> outProps = new HashMap<>();

                outProps.put(ConfigurationConstants.USER, username);
                if (password != null && !password.isEmpty()) {
                    outProps.put(ConfigurationConstants.PW_CALLBACK_REF, (CallbackHandler) callbacks -> {
                        for (Callback callback : callbacks) {
                            final WSPasswordCallback passwordCallback = (WSPasswordCallback) callback;
                            if (username.equals(passwordCallback.getIdentifier())) {
                                passwordCallback.setPassword(password);
                                return;
                            }
                        }
                    });
                }

                final PasswordType passwordType = PasswordType.fromValue(passwordTypeString);
                outProps.put(ConfigurationConstants.PASSWORD_TYPE, passwordType.getValue());

                // set action and other options based on type
                switch (passwordType) {
                case NONE:
                    actions.append(ConfigurationConstants.USERNAME_TOKEN_NO_PASSWORD);
                    outProps.remove(ConfigurationConstants.PW_CALLBACK_REF);
                    break;
                case TEXT:
                case DIGEST:
                    actions.append(ConfigurationConstants.USERNAME_TOKEN);
                    break;
                }

                if (addTimeStamp) {
                    actions.append(' ');
                    actions.append(ConfigurationConstants.TIMESTAMP);
                }

                final boolean addNonce = PropertyUtils.isTrue(options.remove(ConfigurationConstants.ADD_USERNAMETOKEN_NONCE));
                outProps.put(ConfigurationConstants.ADD_USERNAMETOKEN_NONCE, String.valueOf(addNonce));
                final boolean addCreated = PropertyUtils.isTrue(options.remove(ConfigurationConstants.ADD_USERNAMETOKEN_CREATED));
                outProps.put(ConfigurationConstants.ADD_USERNAMETOKEN_CREATED, String.valueOf(addCreated));

                outProps.put(ConfigurationConstants.ACTION, actions.toString());
                client.getOutInterceptors().add(new WSS4JOutInterceptor(outProps));
            }
        });
    }

    private static String getResolvedValue(CamelContext camelContext, String value) {
        try {
            return camelContext.resolvePropertyPlaceholders(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid parameter value: " + value, e);
        }
    }

}
