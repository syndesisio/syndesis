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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.mongo.embedded.EmbeddedMongoExtension.Mongo;
import io.syndesis.connector.mongo.embedded.EmbeddedMongoExtension.MongoConfiguration;

import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;

public final class MongoDBConnectorInsertTest {

    public MongoDBConnectorInsertTest() {
    }

    @Mongo
    static class Multiple extends MongoDBConnectorTestSupport {

        public Multiple(@MongoConfiguration final Map<String, String> configuration) {
            super(configuration);
        }

        @Test
        public void mongoInsertMultipleDocuments(final MongoClient given) {
            try (MongoClient client = given; ClosableMongoCollection<Document> collection = createCollection(client, "MongoDBConnectorInsertTest-Multiple")) {
                // When
                final int iteration = 10;
                final int batchId = 432;
                final List<Document> batchMessage = formatBatchMessageDocument(batchId);
                // Given
                @SuppressWarnings("unchecked")
                final List<String> resultsAsString = template().requestBody("direct:start", batchMessage, List.class);
                final List<Document> result = resultsAsString.stream().map(Document::parse).collect(Collectors.toList());
                // Then
                final List<Document> docsFound = collection.find(Filters.eq("batchNo", batchId)).into(new ArrayList<>());
                Assertions.assertThat(docsFound).hasSize(iteration);
                Assertions.assertThat(result).containsAll(docsFound);
            }
        }

        @Override
        protected List<Step> createSteps() {
            return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-insert", "test", "MongoDBConnectorInsertTest-Multiple");
        }

    }

    @Mongo
    static class MultipleJson extends MongoDBConnectorTestSupport {

        public MultipleJson(@MongoConfiguration final Map<String, String> configuration) {
            super(configuration);
        }

        @Test
        public void mongoInsertMultipleJsonTexts(final MongoClient given) {
            try (MongoClient client = given;
                ClosableMongoCollection<Document> collection = createCollection(client, "MongoDBConnectorInsertTest-MultipleJson")) {
                // When
                final int iteration = 10;
                final int batchId = 654;
                final List<Document> batchMessage = formatBatchMessageDocument(batchId);
                final List<String> jsonStrings = batchMessage.stream().map(Document::toJson).collect(Collectors.toList());
                // Given
                @SuppressWarnings("unchecked")
                final List<String> resultsAsString = template().requestBody("direct:start", jsonStrings, List.class);
                final List<Document> result = resultsAsString.stream().map(Document::parse).collect(Collectors.toList());
                // Then
                final List<Document> docsFound = collection.find(Filters.eq("batchNo", batchId)).into(new ArrayList<>());
                Assertions.assertThat(docsFound).hasSize(iteration);
                Assertions.assertThat(result).containsAll(docsFound);
            }
        }

        @Override
        protected List<Step> createSteps() {
            return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-insert", "test", "MongoDBConnectorInsertTest-MultipleJson");
        }

    }

    @Mongo
    static class Single extends MongoDBConnectorTestSupport {

        public Single(@MongoConfiguration final Map<String, String> configuration) {
            super(configuration);
        }

        @Test
        public void mongoInsertSingleTest(final MongoClient given) {
            try (MongoClient client = given; ClosableMongoCollection<Document> collection = createCollection(client, "MongoDBConnectorInsertTest-Single")) {
                // When
                final String uniqueId = UUID.randomUUID().toString();
                final String message = String.format("{\"test\":\"unit\",\"uniqueId\":\"%s\"}", uniqueId);
                final Document doc = Document.parse(message);
                // Given
                @SuppressWarnings("unchecked")
                final List<String> resultsAsString = template().requestBody("direct:start", message, List.class);
                // Then
                Assertions.assertThat(resultsAsString).hasSize(1);
                final Document result = resultsAsString.stream().map(Document::parse).collect(Collectors.toList()).get(0);
                Assertions.assertThat(doc.getString("test")).isEqualTo(result.getString("test"));
                Assertions.assertThat(doc.getString("uniqueId")).isEqualTo(result.getString("uniqueId"));

                final List<Document> docsFound = collection.find(Filters.eq("uniqueId", uniqueId)).into(new ArrayList<>());
                Assertions.assertThat(docsFound).hasSize(1);
                Assertions.assertThat(docsFound).contains(result);
            }
        }

        @Override
        protected List<Step> createSteps() {
            return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-insert", "test", "MongoDBConnectorInsertTest-Single");
        }
    }

    private static List<Document> formatBatchMessageDocument(final int batchNo) {
        final List<Document> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final Document next = new Document();
            next.put("test", "test" + i);
            next.put("batchNo", batchNo);
            list.add(next);
        }
        return list;
    }
}
