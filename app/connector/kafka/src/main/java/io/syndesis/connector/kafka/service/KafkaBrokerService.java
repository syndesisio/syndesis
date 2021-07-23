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

import java.util.Set;

/**
 * Used to recover helper information from a Kafka Broker
 */
public interface KafkaBrokerService {

    // property names defined in kafka.json
    String BROKERS = "brokers";
    String BROKER_CERTIFICATE = "brokerCertificate";
    String TRANSPORT_PROTOCOL = "transportProtocol";
    String SASL_MECHANISM = "saslMechanism";
    String SASL_LOGIN_CALLBACK_HANDLER_CLASS = "saslLoginCallbackHandlerClass";
    String USERNAME = "username";
    String PASSWORD = "password";
    String OAUTH_TOKEN_ENDPOINT_URI = "oauthTokenEndpointURI";
    String TOPIC = "topic";
    String OAUTHBEARER = "OAUTHBEARER";
    String PLAIN = "PLAIN";
    String SASL_SSL = "SASL_SSL";

    /**
     * Check if a cluster is up and running
     */
    void ping() throws KafkaBrokerServiceException;

    /**
     * Connect to a cluster and recover the list of topics
     * @return the list of topics
     */
    Set<String> listTopics();

    void setSaslMechanism(String saslMechanism);

    void setSaslLoginCallbackHandlerClass(String saslLoginCallbackHandlerClass);

    void setUsername(String username);

    void setPassword(String password);

    void setOauthTokenEndpointURI(String oauthTokenEndpointURI);
}
