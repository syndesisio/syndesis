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

import org.apache.camel.util.ObjectHelper;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syndesis.connector.support.util.KeyStoreHelper;

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
        final String connectionUri = connectionParameters.getConnectionUri();
        if (!connectionUri.contains("amqps:")) {
            result = new JmsConnectionFactory(connectionParameters.getUsername(), connectionParameters.getPassword(), connectionUri);
        } else {
            StringBuilder newConnectionUri = new StringBuilder(connectionUri);
            if (connectionParameters.isSkipCertificateCheck()) {
                if (!connectionUri.contains("transport.trustAll")) {
                    LOG.warn("Skipping Certificate check for AMQP Connection {}", connectionUri);
                    newConnectionUri.append(connectionUri.contains("?") ? "&" : "?")
                            .append("transport.trustAll=true&transport.verifyHost=false");
                }
            } else {
                // add amqps connection certificates
                final String clientCertificate = connectionParameters.getClientCertificate();
                if (!ObjectHelper.isEmpty(clientCertificate)) {
                    // copy client certificate to a keystore using a random key
                    addKeyStoreParam(newConnectionUri, clientCertificate, "amqp-client", "key");
                    newConnectionUri.append("&transport.keyAlias=amqp-client");
                }
                final String brokerCertificate = connectionParameters.getBrokerCertificate();
                if (!ObjectHelper.isEmpty(brokerCertificate)) {
                    // copy broker certificate to a keystore using a random key
                    addKeyStoreParam(newConnectionUri, brokerCertificate, "amqp-broker", "trust");
                    // possibly expose this property from connector in the future?
                    newConnectionUri.append("&transport.verifyHost=false");
                }
            }
            result = new JmsConnectionFactory(connectionParameters.getUsername(), connectionParameters.getPassword(), newConnectionUri.toString());
        }

        return result;
    }

    private static void addKeyStoreParam(StringBuilder connectionUri, String certificate, String alias, String storePrefix) {
        KeyStoreHelper keyStoreHelper = new KeyStoreHelper(certificate, alias).store();
        connectionUri.append(connectionUri.indexOf("?") != -1 ? "&" : "?")
                .append("transport.").append(storePrefix).append("StoreLocation=").append(keyStoreHelper.getKeyStorePath())
                .append("&transport.").append(storePrefix).append("StorePassword=").append(keyStoreHelper.getPassword());
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
