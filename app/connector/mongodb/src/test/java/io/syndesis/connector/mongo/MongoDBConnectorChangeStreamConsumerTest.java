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

import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.mongo.embedded.EmbeddedMongoExtension.Mongo;
import io.syndesis.connector.mongo.embedded.EmbeddedMongoExtension.MongoConfiguration;

import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

@Mongo
public class MongoDBConnectorChangeStreamConsumerTest extends MongoDBConnectorTestSupport {

    private static final String COLLECTION = "changeStreamCollection";

    public MongoDBConnectorChangeStreamConsumerTest(@MongoConfiguration final Map<String, String> configuration) {
        super(configuration);
    }

    @Test
    public void singleInsertTest(final MongoClient given) throws InterruptedException {
        try (MongoClient client = given) {
            final MongoCollection<Document> collection = client.getDatabase("test").getCollection(COLLECTION);
            // When
            final MockEndpoint mock = context().getEndpoint("mock:result", MockEndpoint.class);
            mock.setRetainLast(1);
            mock.expectedMessageCount(2);
            mock.expectedMessagesMatches((final Exchange e) -> {
                try {
                    // We just want to validate the output is coming as json
                    // well format
                    @SuppressWarnings("unchecked")
                    final List<String> doc = e.getMessage().getBody(List.class);
                    final JsonNode jsonNode = MAPPER.readTree(doc.get(0));
                    Assertions.assertThat(jsonNode).isNotNull();
                    Assertions.assertThat(jsonNode.get("test").asText()).isEqualTo("junit2");
                } catch (final IOException ex) {
                    return false;
                }
                return true;
            });
            // Given
            final Document doc = new Document();
            doc.append("someKey", "someValue");
            doc.append("test", "junit");
            collection.insertOne(doc);
            final Document doc2 = new Document();
            doc2.append("someKey", "someValue2");
            doc2.append("test", "junit2");
            collection.insertOne(doc2);
            // Then
            mock.assertIsSatisfied();
        }
    }

    @Override
    protected List<Step> createSteps() {
        return fromMongoChangeStreamToMock("result", "io.syndesis.connector:connector-mongodb-consumer-changestream", "test", COLLECTION);
    }

    @SuppressWarnings("resource") // created collection is dropped in teardown
    @BeforeAll
    static void setup(final MongoClient given) {
        try (MongoClient client = given) {
            createCollection(client, COLLECTION);
        }
    }

    @AfterAll
    static void teardown(final MongoClient given) {
        try (MongoClient client = given) {
            client.getDatabase("test").getCollection(COLLECTION).drop();
        }
    }
}
