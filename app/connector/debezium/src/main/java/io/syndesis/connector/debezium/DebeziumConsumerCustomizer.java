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
package io.syndesis.connector.debezium;

import java.io.IOException;
import java.util.Map;

import io.syndesis.connector.kafka.KafkaConnectionCustomizer;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DebeziumConsumerCustomizer implements ComponentProxyCustomizer, CamelContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebeziumConsumerCustomizer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final String DEBEZIUM_OPERATION = "debezium.OPERATION";

    // Debezium is based on Kafka connector
    private final  KafkaConnectionCustomizer kafkaConnectionCustomizer;


    private CamelContext camelContext;

    @Override
    public CamelContext getCamelContext() {
        return this.camelContext;
    }

    @Override
    public final void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public DebeziumConsumerCustomizer(CamelContext context) {
        super();
        setCamelContext(context);
        kafkaConnectionCustomizer = new KafkaConnectionCustomizer(context);
    }

    @Override
    public void customize(final ComponentProxyComponent component, final Map<String, Object> options) {
        kafkaConnectionCustomizer.customize(component, options);
        component.setBeforeConsumer(DebeziumConsumerCustomizer::convertToDebeziumFormat);
    }

    private static void convertToDebeziumFormat(final Exchange exchange) throws IOException {
        final String body = exchange.getMessage().getBody(String.class);
        if (body == null) {
            // we have a tombstone
            LOGGER.debug("Tombstone found with kafka key {}", exchange.getMessage().getHeader("kafka.KEY"));
            exchange.getMessage().setHeader(DEBEZIUM_OPERATION, "TOMBSTONE");
            return;
        }
        final JsonNode root = MAPPER.readTree(body);
        final JsonNode before = root.get("before");
        final JsonNode after = root.get("after");
        final String operation = root.get("op").asText();

        if ("c".equals(operation)) {
            exchange.getMessage().setHeader(DEBEZIUM_OPERATION, "CREATE");
            exchange.getMessage().setBody(after.toString());
        } else if ("d".equals(operation)) {
            exchange.getMessage().setHeader(DEBEZIUM_OPERATION, "DELETE");
            exchange.getMessage().setBody(before.toString());
        } else if ("u".equals(operation)) {
            exchange.getMessage().setHeader(DEBEZIUM_OPERATION, "UPDATE");
            exchange.getMessage().setBody(after.toString());
            exchange.getMessage().setHeader("debezium.BEFORE", before.toString());
        } else {
            LOGGER.warn("Unknown operation {}, providing the after as fallback", operation);
            exchange.getMessage().setBody(after.toString());
        }
    }

}
