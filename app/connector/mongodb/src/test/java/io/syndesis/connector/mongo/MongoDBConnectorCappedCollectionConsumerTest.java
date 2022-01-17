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
package io.syndesis.connector.mongo;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.mongo.embedded.EmbeddedMongoExtension.Mongo;
import io.syndesis.connector.mongo.embedded.EmbeddedMongoExtension.MongoConfiguration;

import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.parallel.Execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;

import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Mongo
@Execution(SAME_THREAD)
public class MongoDBConnectorCappedCollectionConsumerTest extends MongoDBConnectorTestSupport {

    private final static String COLLECTION = "cappedCollection";

    private static int globalId = 0;

    public MongoDBConnectorCappedCollectionConsumerTest(@MongoConfiguration final Map<String, String> configuration) {
        super(configuration);
    }

    @RepeatedTest(2)
    public synchronized void mongoTest(final MongoClient given) throws InterruptedException {
        try (MongoClient client = given) {
            final MongoCollection<Document> collection = client.getDatabase("test").getCollection(COLLECTION);
            // When
            final String unique = UUID.randomUUID().toString();
            final int id = globalId++;
            final MockEndpoint mock = context().getEndpoint("mock:result", MockEndpoint.class);
            // We just retain last message
            mock.setRetainLast(1);
            mock.expectedMessageCount(1);
            mock.expectedMessagesMatches((final Exchange e) -> {
                try {
                    @SuppressWarnings("unchecked")
                    final List<String> doc = e.getMessage().getBody(List.class);
                    final JsonNode jsonNode = MAPPER.readTree(doc.get(0));
                    Assertions.assertThat(jsonNode.get("unique").asText()).isEqualTo(unique);
                    Assertions.assertThat(jsonNode.get("id").asInt()).isEqualTo(id);
                    return true;
                } catch (final IOException ex) {
                    return false;
                }
            });
            // Given
            final Document doc = new Document();
            doc.append("id", id);
            doc.append("unique", unique);
            collection.insertOne(doc);
            // Then
            mock.assertIsSatisfied();
        }
    }

    @Override
    protected List<Step> createSteps() {
        return fromMongoTailToMock("result", "io.syndesis.connector:connector-mongodb-consumer-tail", "test", COLLECTION,
            "id");
    }

    @BeforeAll
    static void setup(final MongoClient given) {
        final CreateCollectionOptions opts = new CreateCollectionOptions().capped(true).sizeInBytes(1024 * 1024);
        try (MongoClient client = given) {
            final MongoDatabase database = client.getDatabase("test");

            database.createCollection(COLLECTION, opts);
        }
    }

    @AfterAll
    static void teardown(final MongoClient given) {
        try (MongoClient client = given) {
            client.getDatabase("test").getCollection(COLLECTION).drop();
        }
    }

}
