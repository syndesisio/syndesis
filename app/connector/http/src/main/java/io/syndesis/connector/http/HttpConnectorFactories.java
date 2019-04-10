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
package io.syndesis.connector.http;

import java.util.Map;
import java.util.Optional;

import org.apache.camel.Component;
import org.apache.camel.Endpoint;
import org.apache.camel.catalog.DefaultCamelCatalog;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.StringHelper;
import org.apache.commons.lang3.StringUtils;

import io.syndesis.integration.component.proxy.ComponentDefinition;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyFactory;

public final class HttpConnectorFactories {
    private HttpConnectorFactories() {
    }

    // *******************************
    // Http4
    // *******************************

    public static final class Http4 implements ComponentProxyFactory {
        @Override
        public ComponentProxyComponent newInstance(String componentId, String componentScheme) {
            return new ComponentProxyComponent(componentId, componentScheme) {
                @Override
                @SuppressWarnings("PMD.SignatureDeclareThrowsException")
                protected Optional<Component> createDelegateComponent(ComponentDefinition definition, Map<String, Object> options) throws Exception {
                    // As we don't have any specific component setting, we do
                    // not need
                    // to create a delegated component
                    return Optional.empty();
                }

                @Override
                @SuppressWarnings("PMD.SignatureDeclareThrowsException")
                protected Map<String, String> buildEndpointOptions(String remaining, Map<String, Object> options) throws Exception {
                    options.put("httpUri", computeHttpUri("http", options));

                    return super.buildEndpointOptions(remaining, options);
                }

                @Override
                @SuppressWarnings("PMD.SignatureDeclareThrowsException")
                protected Endpoint createDelegateEndpoint(ComponentDefinition definition, String scheme, Map<String, String> options) throws Exception {
                    // Build the delegate uri using the catalog
                    DefaultCamelCatalog catalog = new DefaultCamelCatalog(false);
                    String uri = catalog.asEndpointUri(scheme, options, false);
                    final String uriFinal = uri + "&httpClient.redirectsEnabled=true";
                    return getCamelContext().getEndpoint(uriFinal);
                }
            };
        }
    }

    // *******************************
    // Https4
    // *******************************

    public static final class Https4 implements ComponentProxyFactory {
        @Override
        public ComponentProxyComponent newInstance(String componentId, String componentScheme) {
            return new ComponentProxyComponent(componentId, componentScheme) {
                @Override
                @SuppressWarnings("PMD.SignatureDeclareThrowsException")
                protected Optional<Component> createDelegateComponent(ComponentDefinition definition, Map<String, Object> options) throws Exception {
                    // As we don't have any specific component setting, we do
                    // not need
                    // to create a delegated component (till we add support for
                    // ssl)
                    return Optional.empty();
                }

                @Override
                @SuppressWarnings("PMD.SignatureDeclareThrowsException")
                protected Map<String, String> buildEndpointOptions(String remaining, Map<String, Object> options) throws Exception {
                    options.put("httpUri", computeHttpUri("https", options));

                    return super.buildEndpointOptions(remaining, options);
                }

                @Override
                @SuppressWarnings("PMD.SignatureDeclareThrowsException")
                protected Endpoint createDelegateEndpoint(ComponentDefinition definition, String scheme, Map<String, String> options) throws Exception {
                    // Build the delegate uri using the catalog
                    DefaultCamelCatalog catalog = new DefaultCamelCatalog(false);
                    String uri = catalog.asEndpointUri(scheme, options, false);
                    final String uriFinal = uri + "&httpClient.redirectsEnabled=true";
                    return getCamelContext().getEndpoint(uriFinal);
                }
            };
        }
    }

    // *******************************
    // Helpers
    // *******************************

    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
    public static String computeHttpUri(String scheme, Map<String, Object> options) {
        String baseUrl = (String)options.remove("baseUrl");

        if (ObjectHelper.isEmpty(baseUrl)) {
            throw new IllegalArgumentException("baseUrl si mandatory");
        }

        String uriScheme = StringHelper.before(baseUrl, "://");
        if (ObjectHelper.isNotEmpty(uriScheme) && !ObjectHelper.equal(scheme, uriScheme)) {
            throw new IllegalArgumentException("Unsupported scheme: " + uriScheme);
        }

        if (ObjectHelper.isNotEmpty(uriScheme)) {
            baseUrl = StringHelper.after(baseUrl, "://");
        }

        String path = (String)options.remove("path");
        if (StringUtils.isNotEmpty(path)) {
            if (path.charAt(0) != '/') {
                path = "/" + path;
            }

            return StringUtils.removeEnd(baseUrl, "/") + path;
        } else {
            return baseUrl;
        }
    }
}
