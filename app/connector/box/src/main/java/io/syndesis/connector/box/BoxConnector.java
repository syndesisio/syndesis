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
package io.syndesis.connector.box;

import java.util.Map;
import java.util.stream.Stream;
import org.apache.camel.Component;
import org.apache.camel.component.box.BoxComponent;
import org.apache.camel.component.box.BoxConfiguration;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentDefinition;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;

public class BoxConnector extends ComponentProxyComponent {

    private String authenticationType;
    private String userName;
    private String userPassword;
    private String clientId;
    private String clientSecret;

    public BoxConnector(String componentId, String componentScheme) {
        super(componentId, componentScheme);
    }

    @Override
    protected void configureDelegateComponent(ComponentDefinition definition, Component component, Map<String, Object> options) throws Exception {
        super.configureDelegateComponent(definition, component, options);

        if (component instanceof BoxComponent) {
            BoxConfiguration configuration = new BoxConfiguration();
            configuration.setAuthenticationType(authenticationType);
            configuration.setUserName(userName);
            configuration.setUserPassword(userPassword);
            configuration.setClientId(clientId);
            configuration.setClientSecret(clientSecret);
            ((BoxComponent) component).setConfiguration(configuration);
        }
    }

    @Override
    protected Map<String, String> buildEndpointOptions(String remaining, Map<String, Object> options) throws Exception {
        Map<String, String> endpointOptions = super.buildEndpointOptions(remaining, options);
        Stream.of("parentFolderId", "fileId").forEach(key -> {
                ConnectorOptions.extractOptionAndConsume(options, key, (String value) -> endpointOptions.put(key, value));
        });
        return endpointOptions;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
