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

import java.util.Map;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyFactory;
import org.apache.camel.Endpoint;
import org.apache.camel.component.amqp.AMQPComponent;
import org.apache.camel.component.extension.ComponentExtension;
import org.apache.qpid.jms.JmsConnectionFactory;

/**
 * {@link ComponentProxyFactory} to create AMQP Connectors.
 * @author dhirajsb
 */
public class AMQPConnectorFactory implements ComponentProxyFactory {

    @Override
    public ComponentProxyComponent newInstance(String componentId, String componentScheme) {
        return new AMQPProxyComponent(componentId, componentScheme);
    }

    private static class AMQPProxyComponent extends ComponentProxyComponent {

        private String connectionUri;
        private String username;
        private String password;
        private String brokerCertificate;
        private String clientCertificate;
        private boolean skipCertificateCheck;

        public AMQPProxyComponent(String componentId, String componentScheme) {
            super(componentId, componentScheme);
            // support connection verification
            registerExtension(this::getComponentVerifier);
        }

        private ComponentExtension getComponentVerifier() {
            return new AMQPVerifierExtension(getComponentId(), getCamelContext());
        }

        @Override
        public void setOptions(Map<String, Object> options) {
            // connection parameters
            connectionUri = (String) options.remove("connectionUri");
            username = (String) options.remove("username");
            password = (String) options.remove("password");
            brokerCertificate = (String) options.remove("brokerCertificate");
            clientCertificate = (String) options.remove("clientCertificate");
            skipCertificateCheck = Boolean.TRUE.equals(options.remove("skipCertificateCheck"));

            super.setOptions(options);
        }

        @Override
        protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws
                Exception {

            // create and set ConnectionFactory on delegate
            AMQPComponent delegate = (AMQPComponent) getCamelContext().getComponent(getComponentScheme());
            final AMQPUtil.ConnectionParameters connectionParameters = new AMQPUtil.ConnectionParameters
                    (connectionUri, username, password, brokerCertificate, clientCertificate, skipCertificateCheck);
            final JmsConnectionFactory connectionFactory = AMQPUtil.createConnectionFactory(connectionParameters);
            delegate.setConnectionFactory(connectionFactory);

            return super.createEndpoint(uri, remaining, parameters);
        }
    }
}
