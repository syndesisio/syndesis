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
package io.syndesis.connector.debezium.metadata;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.connector.support.util.KeyStoreHelper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebeziumMySQLDatashapeStrategy implements DebeziumDatashapeStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebeziumMySQLDatashapeStrategy.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern PATTERN = Pattern.compile("`(.+)` ([^,]+),");

    @Override
    public DataShape getDatashape(Map<String, Object> params) {
        final String brokers = ConnectorOptions.extractOption(params, "brokers");
        final String topicSelected = ConnectorOptions.extractOption(params, "topic");
        final String topicSchemaChange = ConnectorOptions.extractOption(params, "schemaChange");
        final String certificate = ConnectorOptions.extractOption(params, "brokerCertificate");
        final String transportProtocol = ConnectorOptions.extractOption(params, "transportProtocol");

        final String topicTableName = topicSelected.split("\\.", -1)[2];
        String ddlTableExpected = null;

        final Properties properties = kafkaConsumerProperties(brokers, transportProtocol, certificate);
        LOGGER.debug("Calling kafka consumer with params {}", properties);
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
            ddlTableExpected = pollDDLTableSchema(consumer, topicSchemaChange, topicTableName);
        } catch (final IOException e) {
            LOGGER.error("Issue while parsing a record", e);
        }

        if (ddlTableExpected == null) {
            LOGGER.warn("No DDL found to match with topic {}", topicSelected);
            return null;
        }

        LOGGER.debug("The following DDL matches the topic {}", ddlTableExpected);

        final String jsonSchema = convertDDLtoJsonSchema(ddlTableExpected, topicTableName);
        LOGGER.debug("Converted to {}", jsonSchema);

        return new DataShape.Builder()
            .name("Filter parameters")
            .kind(DataShapeKinds.JSON_SCHEMA)
            .specification(jsonSchema)
            .build();
    }

    static String buildJsonSchema(final String tableName, final List<String> properties) {
        final StringBuilder jsonSchemaBuilder = new StringBuilder("{\"$schema\": \"http://json-schema.org/draft-07/schema#\",\"title\": \"")
            .append(tableName)
            .append("\",\"type\": \"object\",\"properties\": {");

        final StringJoiner joiner = new StringJoiner(",");
        for (final String property : properties) {
            joiner.add(property);
        }

        return jsonSchemaBuilder.append(joiner.toString())
            .append("}}")
            .toString();
    }

    static String convertDDLtoJsonSchema(final String ddl, final String tableName) {
        int firstParentheses = ddl.indexOf('(');
        final Matcher matcher = PATTERN.matcher(ddl.substring(firstParentheses));
        final List<String> properties = new ArrayList<>();
        while (matcher.find()) {
            final String field = matcher.group(1);
            final String type = getType(matcher.group(2));
            properties.add("\"" + field + "\":{\"type\":\"" + type + "\"}");
        }
        return buildJsonSchema(tableName, properties);
    }

    private static String pollDDLTableSchema(KafkaConsumer<String, String> consumer, String topicSchemaChange, String topicTableName) throws JsonProcessingException {
        String ddlTableExpected = null;
        // Seek the offset to the beginning in case any offset was committed
        // previously
        consumer.subscribe(Collections.singletonList(topicSchemaChange));
        consumer.seekToBeginning(consumer.assignment());
        // We assume we get the structure query in one poll
        final ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(15));
        for (final ConsumerRecord<String, String> record : records) {
            final String ddl = MAPPER.readTree(record.value()).get("ddl").asText();
            final String matchingDDL = String.format("CREATE TABLE `%s`", topicTableName);
            if (ddl.startsWith(matchingDDL)) {
                ddlTableExpected = ddl;
            }
        }
        return ddlTableExpected;
    }

    private static Properties kafkaConsumerProperties(String brokers, String transportProtocol, String certificate) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", brokers);
        properties.put("group.id", "syndesis-x");
        properties.put("enable.auto.commit", "false");
        properties.put("auto.offset.reset", "earliest");
        properties.put("key.deserializer", StringDeserializer.class.getName());
        properties.put("value.deserializer", StringDeserializer.class.getName());
        // SSL support
        if (!"PLAINTEXT".equals(transportProtocol)) {
            KeyStoreHelper brokerKeyStoreHelper = certificate != null ? new KeyStoreHelper(certificate, "brokerCertificate").store() : null;
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

    private static String getType(final String type) {
        if (type.contains("char")) {
            return "string";
        } else if (type.contains("int")) {
            return "integer";
        }
        // default
        return "string";
    }
}
