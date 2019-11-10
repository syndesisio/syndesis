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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ValidationOptions;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.mongo.MongoDBConnectorTestSupport;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.bson.Document;
import org.junit.Test;

public class MongoDBMetadataTest extends MongoDBConnectorTestSupport {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final static String CONNECTOR_ID = "io.syndesis.connector:connector-mongodb-find";
    private final static String SCHEME = "mongodb3";

    @Override
    protected List<Step> createSteps() {
        return fromDirectToMongo("start", CONNECTOR_ID, DATABASE, COLLECTION);
    }

    @Test
    public void verifyMetadataJsonSchemaExpected() throws IOException {
        // Given
        String collection = "validSchema";
        Map<String, Object> properties = new HashMap<>();
        properties.put("database", DATABASE);
        properties.put("collection", collection);
        properties.put("host", String.format("%s:%s", HOST, PORT));
        properties.put("user", USER);
        properties.put("password", PASSWORD);
        properties.put("adminDB", ADMIN_DB);
        // When
        Document jsonSchema = Document.parse("{ \n"
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
        ValidationOptions collOptions = new ValidationOptions().validator(Filters.eq("$jsonSchema",jsonSchema));
        mongoClient.getDatabase(DATABASE).createCollection(collection,
            new CreateCollectionOptions().validationOptions(collOptions));
        // Then
        MongoDBMetadataRetrieval metaBridge = new MongoDBMetadataRetrieval();
        SyndesisMetadata metadata = metaBridge.fetch(
            context,
            SCHEME,
            CONNECTOR_ID,
            properties
        );
        // format as json the datashape
        JsonNode json = OBJECT_MAPPER.readTree(metadata.outputShape.getSpecification());
        assertNotNull(json);
        assertNotNull(json.get("properties"));
        assertNotNull(json.get("$schema"));
        assertNotNull(json.get("required"));
        assertEquals("http://json-schema.org/schema#", json.get("$schema").asText());
        assertNotNull(json.get("id"));
        assertNotNull(json.get("type"));
    }

    @Test
    public void verifyMetadataJsonSchemaMissing() throws IOException {
        // Given
        String collection = "noSchema";
        Map<String, Object> properties = new HashMap<>();
        properties.put("database", DATABASE);
        properties.put("collection", collection);
        properties.put("host", String.format("%s:%s", HOST, PORT));
        properties.put("user", USER);
        properties.put("password", PASSWORD);
        properties.put("adminDB", ADMIN_DB);
        // When
        mongoClient.getDatabase(DATABASE).createCollection(collection);
        // Then
        MongoDBMetadataRetrieval metaBridge = new MongoDBMetadataRetrieval();
        SyndesisMetadata metadata = metaBridge.fetch(
            context,
            SCHEME,
            CONNECTOR_ID,
            properties
        );
        assertEquals(DataShapeKinds.JAVA, metadata.inputShape.getKind());
        assertEquals(DataShapeKinds.ANY, metadata.outputShape.getKind());
    }

}
