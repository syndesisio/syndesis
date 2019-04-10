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
package io.syndesis.connector.activemq;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.jms.ConnectionFactory;

import io.syndesis.integration.component.proxy.ComponentDefinition;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.component.sjms.SjmsComponent;
import org.apache.camel.util.ObjectHelper;

class ActiveMQConnector extends ComponentProxyComponent {

    private String brokerUrl;
    private String username;
    private String password;
    private String clientID;
    private boolean skipCertificateCheck;
    private String brokerCertificate;
    private String clientCertificate;

    ActiveMQConnector(String componentId, String componentScheme) {
        super(componentId, componentScheme);
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getBrokerCertificate() {
        return brokerCertificate;
    }

    public boolean isSkipCertificateCheck() {
        return skipCertificateCheck;
    }

    public void setSkipCertificateCheck(boolean skipCertificateCheck) {
        this.skipCertificateCheck = skipCertificateCheck;
    }

    public void setBrokerCertificate(String brokerCertificate) {
        this.brokerCertificate = brokerCertificate;
    }

    public String getClientCertificate() {
        return clientCertificate;
    }

    public void setClientCertificate(String clientCertificate) {
        this.clientCertificate = clientCertificate;
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Override
    protected Optional<Component> createDelegateComponent(ComponentDefinition definition, Map<String, Object> options) throws Exception {
        SjmsComponent component = lookupComponent();

        if (component == null) {
            ConnectionFactory connectionFactory = ActiveMQUtil.createActiveMQConnectionFactory(
                this.brokerUrl,
                this.username,
                this.password,
                this.brokerCertificate,
                this.clientCertificate,
                this.skipCertificateCheck
            );

            component = new SjmsComponent();
            component.setConnectionFactory(connectionFactory);
            component.setConnectionClientId(clientID);
        }

        return Optional.of(component);
    }

    // ************************************
    // Helpers
    // ************************************

    @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
    private SjmsComponent lookupComponent() {
        final CamelContext context = getCamelContext();
        final List<String> names = context.getComponentNames();

        if (ObjectHelper.isEmpty(names)) {
            return null;
        }

        // Try to check if a component with same set-up has already been
        // configured, if so reuse it.
        for (String name: names) {
            Component cmp = context.getComponent(name, false, false);
            if (cmp instanceof SjmsComponent) {
                ConnectionFactory factory = ((SjmsComponent)cmp).getConnectionFactory();
                if (factory instanceof ActiveMQConnectionFactory) {
                    ActiveMQConnectionFactory amqFactory = (ActiveMQConnectionFactory)factory;

                    if (!Objects.equals(brokerUrl, amqFactory.getBrokerURL())) {
                        continue;
                    }
                    if (!Objects.equals(username, amqFactory.getUserName())) {
                        continue;
                    }
                    if (!Objects.equals(password, amqFactory.getPassword())) {
                        continue;
                    }

                    return (SjmsComponent) cmp;
                }
            }
        }

        return null;
    }
}
