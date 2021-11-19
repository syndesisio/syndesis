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
package io.syndesis.connector.mongo.meta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.mongo.MongoDBConnectorTestSupport;
import io.syndesis.connector.mongo.embedded.EmbeddedMongoExtension.Mongo;
import io.syndesis.connector.mongo.embedded.EmbeddedMongoExtension.MongoConfiguration;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;

import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ValidationOptions;

@Mongo
public class MongoDBMetadataTest extends MongoDBConnectorTestSupport {

    private final static String COLLECTION = "metadataCollection";

    private final static String CONNECTOR_ID = "io.syndesis.connector:connector-mongodb-find";

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final static String SCHEME = "mongodb3";
    private final Map<String, String> configuration;

    public MongoDBMetadataTest(@MongoConfiguration final Map<String, String> configuration) {
        super(configuration);
        this.configuration = configuration;
    }

    @Test
    public void verifyMetadataJsonSchemaExpected(final MongoClient given) throws JsonMappingException, JsonProcessingException {
        // Given
        final String collection = "validSchema";
        final Map<String, Object> properties = new HashMap<>();
        properties.put("database", "test");
        properties.put("collection", collection);
        properties.putAll(configuration);
        // When
        final Document jsonSchema = Document.parse("{ \n"
            + "      bsonType: \"object\", \n"
            + "      required: [ \"name\", \"surname\", \"email\" ], \n"
            + "      properties: { \n"
            + "         name: { \n"
            + "            bsonType: \"string\", \n"
            + "            description: \"required and must be a string\" }, \n"
            + "         surname: { \n"
            + "            bsonType: \"string\", \n"
            + "            description: \"required and must be a string\" }, \n"
            + "         email: { \n"
            + "            bsonType: \"string\", \n"
            + "            pattern: \"^.+@.+$\", \n"
            + "            description: \"required and must be a valid email address\" }, \n"
            + "         year_of_birth: { \n"
            + "            bsonType: \"int\", \n"
            + "            minimum: 1900, \n"
            + "            maximum: 2018,\n"
            + "            description: \"the value must be in the range 1900-2018\" }, \n"
            + "         gender: { \n"
            + "            enum: [ \"M\", \"F\" ], \n"
            + "            description: \"can be only M or F\" } \n"
            + "      }}");
        final ValidationOptions collOptions = new ValidationOptions().validator(Filters.eq("$jsonSchema", jsonSchema));
        try (MongoClient client = given) {
            client.getDatabase("test").createCollection(collection,
                new CreateCollectionOptions().validationOptions(collOptions));
        }
        // Then
        final MongoDBMetadataRetrieval metaBridge = new MongoDBMetadataRetrieval();
        final SyndesisMetadata metadata = metaBridge.fetch(
            context(),
            SCHEME,
            CONNECTOR_ID,
            properties);
        // format as json the datashape
        final JsonNode json = OBJECT_MAPPER.readTree(metadata.outputShape.getSpecification());
        Assertions.assertThat(json.get("$schema").asText()).isEqualTo("http://json-schema.org/schema#");
        Assertions.assertThat(json.get("required").isArray()).isTrue();
        Assertions.assertThat(json.get("properties").isObject()).isTrue();
        Assertions.assertThat(json.get("properties").get("name")).isNotNull();
        Assertions.assertThat(json.get("properties").get("surname")).isNotNull();
        Assertions.assertThat(json.get("properties").get("email")).isNotNull();
        Assertions.assertThat(json.get("properties").get("year_of_birth")).isNotNull();
        Assertions.assertThat(json.get("properties").get("gender")).isNotNull();
    }

    @Test
    public void verifyMetadataJsonSchemaMissing(final MongoClient given) {
        // Given
        final String collection = "noSchema";
        final Map<String, Object> properties = new HashMap<>();
        properties.put("database", "test");
        properties.put("collection", collection);
        properties.putAll(configuration);
        // When
        try (MongoClient client = given) {
            client.getDatabase("test").createCollection(collection);
        }
        // Then
        final MongoDBMetadataRetrieval metaBridge = new MongoDBMetadataRetrieval();
        final SyndesisMetadata metadata = metaBridge.fetch(
            context(),
            SCHEME,
            CONNECTOR_ID,
            properties);
        Assertions.assertThat(metadata.outputShape.getKind()).isEqualTo(DataShapeKinds.ANY);
    }

    @Override
    protected List<Step> createSteps() {
        return fromDirectToMongo("start", CONNECTOR_ID, "test", COLLECTION);
    }

}
