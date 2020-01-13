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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebeziumConsumerCustomizer implements ComponentProxyCustomizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebeziumConsumerCustomizer.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        component.setBeforeConsumer(DebeziumConsumerCustomizer::convertToDebeziumFormat);
    }

    private static void convertToDebeziumFormat(Exchange exchange) throws IOException {
        String body = exchange.getMessage().getBody(String.class);
        if (body == null) {
            // we have a tombstone
            LOGGER.info("Tombstone found with kafka key {}", exchange.getMessage().getHeader("kafka.KEY"));
            exchange.getMessage().setHeader("debezium.OPERATION", "TOMBSTONE");
            return;
        }
        JsonNode root = MAPPER.readTree(body);
        JsonNode before = root.get("before");
        JsonNode after = root.get("after");
        String operation = root.get("op").asText();

        if ("c".equals(operation)) {
            exchange.getMessage().setHeader("debezium.OPERATION", "CREATE");
            exchange.getMessage().setBody(after.toString());
        } else if ("d".equals(operation)) {
            exchange.getMessage().setHeader("debezium.OPERATION", "DELETE");
            exchange.getMessage().setBody(before.toString());
        } else if ("u".equals(operation)) {
            exchange.getMessage().setHeader("debezium.OPERATION", "UPDATE");
            exchange.getMessage().setBody(after.toString());
            exchange.getMessage().setHeader("debezium.BEFORE", before.toString());
        } else {
            LOGGER.error("Unknown operation {}, providing the after as fallback", operation);
            exchange.getMessage().setBody(after.toString());
        }
    }

}
