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
package io.syndesis.connector.kafka.service;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import io.syndesis.connector.support.util.KeyStoreHelper;
import org.apache.camel.util.ObjectHelper;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaBrokerServiceImpl implements KafkaBrokerService {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaBrokerServiceImpl.class);

    private final String brokers;
    private final String certificate;
    private final String transportProtocol;
    private String saslMechanism;
    private String saslLoginCallbackHandlerClass;
    private String username;
    private String password;
    private String oauthTokenEndpointURI;

    public KafkaBrokerServiceImpl(String brokers, String transportProtocol, String certificate){
        this.brokers = brokers;
        this.transportProtocol = transportProtocol;
        this.certificate = certificate;
    }

    @Override
    public void ping() throws KafkaBrokerServiceException {
        try {
            this.listTopicsOrThrowException();
        } catch (Exception e){
            LOG.warn("Unable to ping the broker", e);
            throw new KafkaBrokerServiceException(e);
        }
    }

    @Override
    public Set<String> listTopics() {
        try {
            return this.listTopicsOrThrowException();
        } catch (Exception e){
            LOG.error("Unable to read the list of topics on the broker", e);
            return Collections.emptySet();
        }
    }

    private Set<String> listTopicsOrThrowException() throws ExecutionException, InterruptedException {
        // Use a key store helper if a self signed certificate is provided
        KeyStoreHelper brokerKeyStoreHelper = certificate != null ? new KeyStoreHelper(certificate, KafkaBrokerService.BROKER_CERTIFICATE).store() : null;
        Properties properties = getKafkaAdminClientConfiguration(brokers, transportProtocol, brokerKeyStoreHelper);
        try (Admin client = Admin.create(properties)) {
            ListTopicsResult topics = client.listTopics();
            return topics.names().get();
        } finally {
            if (brokerKeyStoreHelper != null) {
                // Clean up temporary resources used by key store
                boolean keystoreDeleted = brokerKeyStoreHelper.clean();
                if (!keystoreDeleted) {
                    LOG.warn("Impossible to delete temporary keystore located at " + brokerKeyStoreHelper.getKeyStorePath());
                }
            }
        }
    }

    private Properties getKafkaAdminClientConfiguration(String brokers, String transportProtocol, KeyStoreHelper brokerKeyStoreHelper) {
        Properties properties = new Properties();
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, brokers);
        properties.put(CommonClientConfigs.CONNECTIONS_MAX_IDLE_MS_CONFIG, 10000);
        properties.put(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG, 5000);
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, transportProtocol);
        properties.put(CommonClientConfigs.DEFAULT_API_TIMEOUT_MS_CONFIG, 5000);
        // use certificate to authenticate to kafka server
        if (brokerKeyStoreHelper != null) {
            properties.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
            properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, brokerKeyStoreHelper.getKeyStorePath());
            properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, brokerKeyStoreHelper.getPassword());
            properties.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, brokerKeyStoreHelper.getPassword());
            properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, brokerKeyStoreHelper.getKeyStorePath());
            properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, brokerKeyStoreHelper.getPassword());
        } else if (ObjectHelper.isNotEmpty(username) && ObjectHelper.isNotEmpty(password)) {
            properties.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
            if (OAUTHBEARER.equals(saslMechanism)) {
                String template = "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required " +
                    "oauth.client.id=\"%s\" " +
                    "oauth.client.secret=\"%s\" " +
                    "oauth.token.endpoint.uri=\"%s\" ;";
                String config = String.format(template, username, password, oauthTokenEndpointURI);
                properties.put(SaslConfigs.SASL_JAAS_CONFIG, config);
                if (ObjectHelper.isNotEmpty(saslLoginCallbackHandlerClass)) {
                    properties.put(SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS, saslLoginCallbackHandlerClass);
                }
            } else if (PLAIN.equals(saslMechanism)) {
                String template = "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                    "username=\"%s\" " +
                    "password=\"%s\" ;";
                String config = String.format(template, username, password);
                properties.put(SaslConfigs.SASL_JAAS_CONFIG, config);
            }
        }
        return properties;
    }

    @Override
    public void setSaslMechanism(String saslMechanism) {
        this.saslMechanism = saslMechanism;
    }

    @Override
    public void setSaslLoginCallbackHandlerClass(String saslLoginCallbackHandlerClass) {
        this.saslLoginCallbackHandlerClass = saslLoginCallbackHandlerClass;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void setOauthTokenEndpointURI(String oauthTokenEndpointURI) {
        this.oauthTokenEndpointURI = oauthTokenEndpointURI;
    }
}
