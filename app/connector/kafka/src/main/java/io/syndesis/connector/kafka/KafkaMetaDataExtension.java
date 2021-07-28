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
package io.syndesis.connector.kafka;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.syndesis.connector.kafka.service.KafkaBrokerService;
import io.syndesis.connector.kafka.service.KafkaBrokerServiceImpl;
import io.syndesis.connector.support.util.ConnectorOptions;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.metadata.AbstractMetaDataExtension;
import org.apache.camel.component.extension.metadata.MetaDataBuilder;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaMetaDataExtension extends AbstractMetaDataExtension {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaMetaDataExtension.class);

    KafkaMetaDataExtension(CamelContext context) {
        super(context);
    }

    @Override
    public Optional<MetaData> meta(Map<String, Object> parameters) {
        final String topic = ConnectorOptions.extractOption(parameters, KafkaBrokerService.TOPIC);
        final String brokers = ConnectorOptions.extractOption(parameters, KafkaBrokerService.BROKERS);
        final String certificate = ConnectorOptions.extractOption(parameters, KafkaBrokerService.BROKER_CERTIFICATE);
        final String securityProtocol = ConnectorOptions.extractOption(parameters, KafkaBrokerService.TRANSPORT_PROTOCOL);
        final String saslMechanism = ConnectorOptions.extractOption(parameters, KafkaBrokerService.SASL_MECHANISM);
        final String saslLoginCallbackHandlerClass = ConnectorOptions.extractOption(parameters, KafkaBrokerService.SASL_LOGIN_CALLBACK_HANDLER_CLASS);
        final String clientId = ConnectorOptions.extractOption(parameters, KafkaBrokerService.USERNAME);
        final String clientSecret = ConnectorOptions.extractOption(parameters, KafkaBrokerService.PASSWORD);
        final String oauthTokenEndpointURI = ConnectorOptions.extractOption(parameters, KafkaBrokerService.OAUTH_TOKEN_ENDPOINT_URI);

        LOG.debug("Getting metadata from Kafka connection to {} with protocol {}", brokers, securityProtocol);

        Set<String> topicsNames = new HashSet<>();
        if (topic != null) {
            // Adding topic to appear at the top
            topicsNames.add(topic);
        }
        try {
            if (ObjectHelper.isNotEmpty(brokers)) {
                LOG.trace("Calling the brokerService to collect topics");
                KafkaBrokerService kafkaBrokerService = new KafkaBrokerServiceImpl(brokers, securityProtocol, certificate);
                kafkaBrokerService.setUsername(clientId);
                kafkaBrokerService.setPassword(clientSecret);
                kafkaBrokerService.setSaslMechanism(saslMechanism);
                kafkaBrokerService.setSaslLoginCallbackHandlerClass(saslLoginCallbackHandlerClass);
                kafkaBrokerService.setOauthTokenEndpointURI(oauthTokenEndpointURI);
                topicsNames.addAll(kafkaBrokerService.listTopics());
                topicsNames = Collections.unmodifiableSet(topicsNames);
            } else {
                throw new IllegalStateException("brokers property must have a value.");
            }
        } catch (Exception e) {
            LOG.error("Couldn't fill the topics from the broker");
        }

        return Optional.of(
            MetaDataBuilder.on(getCamelContext())
                .withAttribute(MetaData.CONTENT_TYPE, "text/plain")
                .withAttribute(MetaData.JAVA_TYPE, String.class)
                .withPayload(topicsNames)
                .build()
        );

    }
}
