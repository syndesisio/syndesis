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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import io.syndesis.connector.support.util.CertificateUtil;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for ActiveMQ Connection factory configuration.
 *
 * @author dhirajsb
 */
public final class ActiveMQUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveMQUtil.class);

    private ActiveMQUtil() {
        // utility class
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    public static ActiveMQConnectionFactory createActiveMQConnectionFactory(String brokerUrl, String username, String password, String brokerCertificate, String clientCertificate, boolean skipCertificateCheck) {
        if (brokerUrl.contains("ssl:")) {
            final ActiveMQSslConnectionFactory connectionFactory;

            if (ObjectHelper.isEmpty(username)) {
                connectionFactory = new ActiveMQSslConnectionFactory(brokerUrl);
            } else {
                connectionFactory = new ActiveMQSslConnectionFactory(brokerUrl);
                connectionFactory.setUserName(username);
                connectionFactory.setPassword(password);
            }

            try {
                // create client key manager
                final KeyManager[] keyManagers;
                if (ObjectHelper.isEmpty(clientCertificate)) {
                    keyManagers = null;
                } else {
                    keyManagers = CertificateUtil.createKeyManagers(clientCertificate, "amq-client");
                }

                // create client trust manager
                final TrustManager[] trustManagers;
                if (ObjectHelper.isEmpty(brokerCertificate)) {
                    if (skipCertificateCheck) {
                        // use a trust all TrustManager
                        LOG.warn("Skipping Certificate check for Broker {}", brokerUrl);
                        trustManagers = CertificateUtil.createTrustAllTrustManagers();
                    } else {
                        LOG.debug("Using default JVM Trust Manager for Broker {}", brokerUrl);
                        trustManagers = null;
                    }
                } else {
                    trustManagers = CertificateUtil.createTrustManagers(brokerCertificate, "amq-broker");
                }

                connectionFactory.setKeyAndTrustManagers(keyManagers, trustManagers, new SecureRandom());

                return connectionFactory;
            } catch (GeneralSecurityException | IOException e) {
                throw new IllegalArgumentException("SSL configuration error: " + e.getMessage(), e);
            }
        } else {
            // non-ssl connection
            return ObjectHelper.isEmpty(username)
                ? new ActiveMQConnectionFactory(brokerUrl)
                : new ActiveMQConnectionFactory(username, password, brokerUrl);
        }
    }
}
