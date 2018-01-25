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

import org.apache.qpid.jms.JmsConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for creating AMQP ConnectionFactory.
 *
 * @author dhirajsb
 */
public final class AMQPUtil {

    private static final Logger LOG = LoggerFactory.getLogger(AMQPUtil.class);

    private AMQPUtil() {
        // empty
    }

    public static JmsConnectionFactory createConnectionFactory(ConnectionParameters connectionParameters) {

        final JmsConnectionFactory result;
        if (!connectionParameters.getConnectionUri().contains("amqps:")) {
            result = new JmsConnectionFactory(connectionParameters.getUsername(), connectionParameters.getPassword(), connectionParameters.getConnectionUri());
        } else {
            String newConnectionUri = connectionParameters.getConnectionUri();
            if (connectionParameters.isSkipCertificateCheck()) {
                if (!connectionParameters.getConnectionUri().contains("transport.trustAll")) {
                    LOG.warn("Skipping Certificate check for AMQP Connection {}", connectionParameters
                            .getConnectionUri());
                    newConnectionUri = connectionParameters.getConnectionUri() +
                            (connectionParameters.getConnectionUri().contains("?") ? "&" : "?") +
                            "transport.trustAll=true&transport.verifyHost=false";
                }
                result = new JmsConnectionFactory(connectionParameters.getUsername(), connectionParameters.getPassword(), newConnectionUri);
            } else {
                // TODO add amqps connection support, see CAMEL-11780
                throw new IllegalArgumentException("SSL Not supported, yet!");
            }
        }

        return result;
    }

    public static class ConnectionParameters {
        private final String connectionUri;
        private final String username;
        private final String password;
        private final String brokerCertificate;
        private final String clientCertificate;
        private final boolean skipCertificateCheck;

        ConnectionParameters(String connectionUri, String username, String password, String brokerCertificate, String
                clientCertificate, boolean skipCertificateCheck) {
            this.connectionUri = connectionUri;
            this.username = username;
            this.password = password;
            this.brokerCertificate = brokerCertificate;
            this.clientCertificate = clientCertificate;
            this.skipCertificateCheck = skipCertificateCheck;
        }

        public String getConnectionUri() {
            return connectionUri;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getBrokerCertificate() {
            return brokerCertificate;
        }

        public String getClientCertificate() {
            return clientCertificate;
        }

        public boolean isSkipCertificateCheck() {
            return skipCertificateCheck;
        }
    }
}
