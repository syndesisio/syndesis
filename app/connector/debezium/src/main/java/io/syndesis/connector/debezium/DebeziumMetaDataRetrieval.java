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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.connector.kafka.KafkaMetaDataRetrieval;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD")
public class DebeziumMetaDataRetrieval extends KafkaMetaDataRetrieval {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebeziumMetaDataRetrieval.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DataShape ANY = new DataShape.Builder().kind(DataShapeKinds.ANY).build();

    @Override
    protected SyndesisMetadata adapt(CamelContext context, String componentId, String actionId, Map<String, Object> properties, MetaDataExtension.MetaData metadata) {
        // Retrieve the list of topics
        @SuppressWarnings("unchecked")
        Set<String> topicsNames = (Set<String>) metadata.getPayload();
        List<PropertyPair> topicsResult = new ArrayList<>();
        topicsNames.stream().forEach(
            t -> topicsResult.add(new PropertyPair(t, t))
        );
        // Retrieve dynamically from list of topics provided by Kafka
        Map<String, List<PropertyPair>> dynamicProperties = new HashMap<>();
        dynamicProperties.put("topic", topicsResult);
        dynamicProperties.put("schemaChange", topicsResult);

        final String brokers = ConnectorOptions.extractOption(properties, "brokers");
        final String topicSelected = ConnectorOptions.extractOption(properties, "topic");
        final String schemaChangeSelected = ConnectorOptions.extractOption(properties, "schemaChange");

        DataShape outputDataShape = topicSelected != null ? getDatashape(brokers, topicSelected, schemaChangeSelected) : ANY;
        return new SyndesisMetadata(
            dynamicProperties,
            null,
            outputDataShape
        );
    }

    @SuppressWarnings("PMD")
    private static DataShape getDatashape(String brokers, String topicSelected, String topicSchemaChange) {
        String topicTableName = topicSelected.split("\\.",-1)[2];
        String ddlTableExpected = null;

        Properties properties = new Properties();
        properties.put("bootstrap.servers", brokers);
        properties.put("group.id", "syndesis-x");
        properties.put("enable.auto.commit", "false");
        properties.put("auto.offset.reset", "earliest");
        properties.put("key.deserializer", StringDeserializer.class.getName());
        properties.put("value.deserializer", StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
            // Seek the offset to the beginning in case any offset was committed previously
            consumer.subscribe(Arrays.asList(topicSchemaChange));
            consumer.seekToBeginning(consumer.assignment());
            // We assume we get the structure query in one poll
            ConsumerRecords<String, String> records = consumer.poll(15000);
            for (ConsumerRecord<String, String> record : records) {
                String ddl = MAPPER.readTree(record.value()).get("ddl").asText();
                String matchingDDL = String.format("CREATE TABLE `%s`", topicTableName);
                if (ddl.startsWith(matchingDDL)) {
                    ddlTableExpected = ddl;
                }
            }
        } catch (IOException e) {
            LOGGER.error("Issue while parsing a record", e);
        }

        if (ddlTableExpected == null) {
            LOGGER.warn("No DDL found to match with topic {}", topicSelected);
            return null;
        } else {
            LOGGER.info("The following DDL matches the topic {}", ddlTableExpected);
        }

        String jsonSchema = convertDDLtoJsonSchema(ddlTableExpected, topicTableName);
        LOGGER.info("Converted to {}", jsonSchema);

        return new DataShape.Builder()
            .name("Filter parameters")
            .kind(DataShapeKinds.JSON_SCHEMA)
            .specification(jsonSchema)
            .build();
    }

    private static String convertDDLtoJsonSchema(String ddl, String tableName) {
        Pattern pattern = Pattern.compile("`(.+)` (.+)\\(");
        Matcher matcher = pattern.matcher(ddl);
        List<String> properties = new ArrayList<>();
        while(matcher.find()){
            String field = matcher.group(1);
            String type = getType(matcher.group(2));
            properties.add( String.format( "    \"%s\": { \"type\": \"%s\" }", field, type));
        }
        return buildJsonSchema(tableName, properties);
    }

    private static String buildJsonSchema(String tableName, List<String> properties) {
        StringBuilder jsonSchemaBuilder = new StringBuilder(
            "{\n" +
                "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "  \"title\": \""+tableName+"\",\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n"
        );
        StringJoiner joiner = new StringJoiner(",\n");
        for(String property:properties){
            joiner.add(property);
        }
        jsonSchemaBuilder.append(joiner.toString());
        jsonSchemaBuilder.append(
            "\n  }\n" +
                "}"
        );
        return jsonSchemaBuilder.toString();
    }

    private static String getType(String type) {
        if(type.contains("char")){
            return "string";
        } else if (type.contains("int")){
            return "integer";
        }
        // default
        return "string";
    }

}
