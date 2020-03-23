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
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaBrokerServiceImpl implements KafkaBrokerService{

    private static final Logger LOG = LoggerFactory.getLogger(KafkaBrokerServiceImpl.class);

    private final String brokers;
    private final String certificate;
    private final String transportProtocol;

    public KafkaBrokerServiceImpl(String brokers, String transportProtocol){
        this(brokers,transportProtocol,null);
    }

    public KafkaBrokerServiceImpl(String brokers, String transportProtocol, String certificate){
        this.brokers = brokers;
        this.transportProtocol = transportProtocol;
        this.certificate = certificate;
    }

    @Override
    public void ping() throws KafkaBrokerServiceException {
        try {
            this.listTopicsOrThrowException();
        }catch (Exception e){
            LOG.warn("Unable to ping the broker", e);
            throw new KafkaBrokerServiceException(e);
        }
    }

    @Override
    public Set<String> listTopics() {
        try{
            return this.listTopicsOrThrowException();
        }catch (Exception e){
            LOG.error("Unable to read the list of topics on the broker", e);
            return Collections.emptySet();
        }
    }

    private Set<String> listTopicsOrThrowException() throws ExecutionException, InterruptedException {
        // Use a key store helper if a self signed certificate is provided
        KeyStoreHelper brokerKeyStoreHelper = certificate != null ? new KeyStoreHelper(certificate, "brokerCertificate").store() : null;
        Properties properties = getKafkaAdminClientConfiguration(brokers, transportProtocol, brokerKeyStoreHelper);
        try (AdminClient client = KafkaAdminClient.create(properties)) {
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

    private static Properties getKafkaAdminClientConfiguration(String brokers, String transportProtocol, KeyStoreHelper brokerKeyStoreHelper) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", brokers);
        properties.put("connections.max.idle.ms", 10000);
        properties.put("request.timeout.ms", 5000);
        if (!"PLAINTEXT".equals(transportProtocol)) {
            properties.put("security.protocol", "SSL");
            if (brokerKeyStoreHelper != null) {
                properties.put("ssl.endpoint.identification.algorithm", "");
                properties.put("ssl.keystore.location", brokerKeyStoreHelper.getKeyStorePath());
                properties.put("ssl.keystore.password", brokerKeyStoreHelper.getPassword());
                properties.put("ssl.key.password", brokerKeyStoreHelper.getPassword());
                properties.put("ssl.truststore.location", brokerKeyStoreHelper.getKeyStorePath());
                properties.put("ssl.truststore.password", brokerKeyStoreHelper.getPassword());
            }
        }
        return properties;
    }
}
