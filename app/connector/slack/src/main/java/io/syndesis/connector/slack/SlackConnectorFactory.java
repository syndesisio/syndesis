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
package io.syndesis.connector.slack;

import java.util.Map;

import org.apache.camel.Endpoint;

import io.syndesis.integration.component.proxy.ComponentDefinition;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyFactory;

public class SlackConnectorFactory implements ComponentProxyFactory {

    @Override
    public ComponentProxyComponent newInstance(String componentId, String componentScheme) {
        return new SlackProxyComponent(componentId, componentScheme);
    }

    private static class SlackProxyComponent extends ComponentProxyComponent {

        private String username;
        private String channel;
        private String webhookUrl;
        private String sendingUsername;
        private String iconUrl;
        private String iconEmoji;

        public SlackProxyComponent(String componentId, String componentScheme) {
            super(componentId, componentScheme);
        }

        @Override
        public void setOptions(Map<String, Object> options) {
            // connection parameters
            username = (String) options.get("username");
            channel = (String) options.get("channel");
            webhookUrl = (String) options.get("webhookUrl");
            sendingUsername = (String) options.get("sendingUsername");
            iconUrl = (String) options.get("iconUrl");
            iconEmoji = (String) options.get("iconEmoji");
            super.setOptions(options);
        }

        @Override
        @SuppressWarnings({ "PMD.SignatureDeclareThrowsException", "PMD.CyclomaticComplexity" })
        protected Endpoint createDelegateEndpoint(ComponentDefinition definition, String scheme,
                Map<String, String> options) throws Exception {
            StringBuffer uri = new StringBuffer();
            uri.append(scheme).append(':');
            if (channel != null) {
                if (channel.charAt(0) == '#') {
                    uri.append(channel);
                } else {
                    uri.append('#').append(channel);
                }
            } else if (username != null) {
                if (username.charAt(0) == '@') {
                    uri.append(username);
                } else {
                    uri.append('@').append(username);
                }
            }
            uri.append("?webhookUrl=").append(webhookUrl);
            if (sendingUsername != null && !sendingUsername.equals("")) {
                uri.append("&username=").append(sendingUsername);
            }
            if (iconUrl != null && !iconUrl.equals("")) {
                uri.append("&iconUrl=").append(iconUrl);
            }
            if (iconEmoji != null && !iconEmoji.equals("")) {
                uri.append("&iconEmoji=").append(iconEmoji);
            }

            return getCamelContext().getEndpoint(uri.toString());
        }
    }

}
