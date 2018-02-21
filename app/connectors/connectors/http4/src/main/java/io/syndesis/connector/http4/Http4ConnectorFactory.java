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
package io.syndesis.connector.http4;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.component.extension.ComponentExtension;

import io.syndesis.integration.component.proxy.ComponentDefinition;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyFactory;

public class Http4ConnectorFactory implements ComponentProxyFactory {

    @Override
    public ComponentProxyComponent newInstance(String componentId, String componentScheme) {
        return new Http4ProxyComponent(componentId, componentScheme);
    }

    private static class Http4ProxyComponent extends ComponentProxyComponent {

        private String httpUri;
        private String path;

        public Http4ProxyComponent(String componentId, String componentScheme) {
            super(componentId, componentScheme);
            registerExtension(this::getComponentVerifier);
        }

        private ComponentExtension getComponentVerifier() {
            return new Http4ComponentVerifierExtension(getComponentId(), getCamelContext());
        }

        @Override
        public void setOptions(Map<String, Object> options) {
            // connection parameters
            httpUri = (String) options.remove("httpUri");
            path = (String) options.remove("path");
            super.setOptions(options);
        }

        @Override
        @SuppressWarnings({"PMD.SignatureDeclareThrowsException","PMD.UseStringBufferForStringAppends"})
        protected Endpoint createDelegateEndpoint(ComponentDefinition definition, String scheme,
                Map<String, String> options) throws Exception {

            String uri;
            if (httpUri.startsWith("https://")) {
                uri = "https4://" + httpUri.substring(8);
            } else if (httpUri.startsWith("http://")) {
                uri = scheme + "://" + httpUri.substring(7);
            } else {
                uri = httpUri;
            }
            if (path != null) {
                if (uri.charAt(0) == '/' || path.charAt(0) == '/') {
                    uri += path;
                } else {
                    uri += "/" + path;
                }
            }
            return getCamelContext().getEndpoint(uri);
        }

    }
}
