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
        final String topic = ConnectorOptions.extractOption(parameters, "topic");
        final String brokers = ConnectorOptions.extractOption(parameters, "brokers");
        final String certificate = ConnectorOptions.extractOption(parameters, "brokerCertificate");
        final String transportProtocol = ConnectorOptions.extractOption(parameters, "transportProtocol");
        LOG.debug("Getting metadata from Kafka connection to {} with protocol {}", brokers, transportProtocol);

        if( topic == null ) {
            if (ObjectHelper.isNotEmpty(brokers)) {
                KafkaBrokerService kafkaBrokerService = new KafkaBrokerServiceImpl(brokers, transportProtocol, certificate);
                return Optional.of(
                    MetaDataBuilder.on(getCamelContext())
                        .withAttribute(MetaData.CONTENT_TYPE, "text/plain")
                        .withAttribute(MetaData.JAVA_TYPE, String.class)
                        .withPayload(kafkaBrokerService.listTopics())
                        .build()
                );
            } else {
                throw new IllegalStateException("brokers property must have a value.");
            }
        } else {
            LOG.debug("Topic property already set nothing to do, just return what we got in topic property: [{}].", topic);
            Set<String> topicsNames = new HashSet<>();
            topicsNames.add(topic);
            topicsNames = Collections.unmodifiableSet(topicsNames);
            return Optional.of(
                MetaDataBuilder.on(getCamelContext())
                    .withAttribute(MetaData.CONTENT_TYPE, "text/plain")
                    .withAttribute(MetaData.JAVA_TYPE, String.class)
                    .withPayload(topicsNames)
                    .build()
                );
        }
    }
}
