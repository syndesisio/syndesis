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
package io.syndesis.connector.jms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

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
public class ActiveMQUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveMQUtil.class);

    public static KeyManager[] createKeyManagers(String clientCertificate) throws GeneralSecurityException, IOException {
        final KeyStore clientKs = createKeyStore("amq-client", clientCertificate);

        // create Key Manager
        KeyManagerFactory kmFactory = KeyManagerFactory.getInstance("PKIX");
        kmFactory.init(clientKs, null);
        return kmFactory.getKeyManagers();
    }

    public static TrustManager[] createTrustManagers(String brokerCertificate) throws GeneralSecurityException,
            IOException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
        trustManagerFactory.init(createKeyStore("amq-server", brokerCertificate));
        return trustManagerFactory.getTrustManagers();
    }

    private static KeyStore createKeyStore(String alias, String certificate) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        final KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        // create client key entry
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        final Certificate generated = factory.generateCertificate(new ByteArrayInputStream
                (certificate.getBytes("UTF-8")));
        keyStore.setCertificateEntry(alias, generated);
        return keyStore;
    }

    public static ActiveMQConnectionFactory createActiveMQConnectionFactory(String brokerUrl, String username, String password, String brokerCertificate, String clientCertificate, boolean skipCertificateCheck) {
        final ActiveMQConnectionFactory connectionFactory;
        if (brokerUrl.contains("ssl:")) {
            if (ObjectHelper.isEmpty(username)) {
                connectionFactory = new ActiveMQSslConnectionFactory(brokerUrl);
            }
            else {
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
                    keyManagers = createKeyManagers(clientCertificate);
                }

                // create client trust manager
                final TrustManager[] trustManagers;
                if (ObjectHelper.isEmpty(brokerCertificate)) {
                    if (skipCertificateCheck) {
                        // use a trust all TrustManager
                        LOG.warn("Skipping Certificate check for Broker " + brokerUrl);
                        trustManagers = new TrustManager[]{new TrustAllTrustManager()};
                    } else {
                        LOG.debug("Using default JVM Trust Manager for Broker " + brokerUrl);
                        trustManagers = null;
                    }
                } else {
                    trustManagers = createTrustManagers(brokerCertificate);
                }

                ((ActiveMQSslConnectionFactory)connectionFactory).setKeyAndTrustManagers(keyManagers, trustManagers, new SecureRandom());

            } catch (GeneralSecurityException | IOException e) {
                throw new IllegalArgumentException("SSL configuration error: " + e.getMessage(), e);
            }
        } else {
            // non-ssl connection
            connectionFactory = ObjectHelper.isEmpty(username) ? new ActiveMQConnectionFactory(brokerUrl) : new ActiveMQConnectionFactory(username, password, brokerUrl);

        }
        return connectionFactory;
    }

}
