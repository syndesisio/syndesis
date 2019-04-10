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
package io.syndesis.connector.amqp;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.jms.ConnectionFactory;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.component.amqp.AMQPComponent;
import org.apache.camel.component.extension.ComponentExtension;
import org.apache.camel.util.ObjectHelper;
import org.apache.qpid.jms.JmsConnectionFactory;

import io.syndesis.integration.component.proxy.ComponentDefinition;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;

/**
 * AMQP Proxy Component.
 * @author dhirajsb
 */
class AMQPConnector extends ComponentProxyComponent {

    private String connectionUri;
    private String username;
    private String password;
    private String clientId;
    private String brokerCertificate;
    private String clientCertificate;
    private boolean skipCertificateCheck;

    AMQPConnector(String componentId, String componentScheme) {
        super(componentId, componentScheme);
        // support connection verification
        registerExtension(this::getComponentVerifier);
    }

    public String getConnectionUri() {
        return connectionUri;
    }

    public void setConnectionUri(String connectionUri) {
        this.connectionUri = connectionUri;
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

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getBrokerCertificate() {
        return brokerCertificate;
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

    public boolean isSkipCertificateCheck() {
        return skipCertificateCheck;
    }

    public void setSkipCertificateCheck(boolean skipCertificateCheck) {
        this.skipCertificateCheck = skipCertificateCheck;
    }

    private ComponentExtension getComponentVerifier() {
        return new AMQPVerifierExtension(getComponentId(), getCamelContext());
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Override
    protected Optional<Component> createDelegateComponent(ComponentDefinition definition, Map<String, Object> options) throws Exception {

        AMQPComponent delegate = lookupComponent();

        if (delegate == null) {
            // create and set ConnectionFactory on delegate
            final AMQPUtil.ConnectionParameters connectionParameters = new AMQPUtil.ConnectionParameters(connectionUri, username, password, brokerCertificate, clientCertificate, skipCertificateCheck);
            final JmsConnectionFactory connectionFactory = AMQPUtil.createConnectionFactory(connectionParameters);
            connectionFactory.setClientID(clientId);

            delegate = new AMQPComponent(connectionFactory);
        }

        return Optional.of(delegate);
    }

    private AMQPComponent lookupComponent() {
        final CamelContext context = getCamelContext();
        final List<String> names = context.getComponentNames();

        if (ObjectHelper.isEmpty(names)) {
            return null;
        }

        // lookup existing component with same configuration
        for (String name: names) {

            Component cmp = context.getComponent(name, false, false);
            if (cmp instanceof AMQPComponent) {

                final ConnectionFactory factory;
                try {
                    factory = ((AMQPComponent)cmp).getConfiguration().getConnectionFactory();
                } catch (IllegalArgumentException e) {
                    // ignore components without a connection factory
                    continue;
                }

                if (factory instanceof JmsConnectionFactory) {
                    JmsConnectionFactory jmsConnectionFactory = (JmsConnectionFactory)factory;

                    if (!Objects.equals(connectionUri, jmsConnectionFactory.getRemoteURI())) {
                        continue;
                    }
                    if (!Objects.equals(username, jmsConnectionFactory.getUsername())) {
                        continue;
                    }
                    if (!Objects.equals(password, jmsConnectionFactory.getPassword())) {
                        continue;
                    }
                    if (!Objects.equals(clientId, jmsConnectionFactory.getClientID())) {
                        continue;
                    }

                    return (AMQPComponent) cmp;
                }
            }
        }

        return null;
    }
}
