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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.mongo.embedded.EmbeddedMongoExtension.Mongo;
import io.syndesis.connector.mongo.embedded.EmbeddedMongoExtension.MongoConfiguration;

import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import com.mongodb.MongoClient;

import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public abstract class MongoDBConnectorFindAllTest extends MongoDBConnectorTestSupport {

    @Mongo
    static class MongoDBConnectorFindAll extends MongoDBConnectorFindAllTest {

        public MongoDBConnectorFindAll(@MongoConfiguration final Map<String, String> configuration) {
            super(configuration);
        }

        @Test
        public void mongoFindAllTest(final MongoClient given) {
            try (MongoClient client = given; ClosableMongoCollection<Document> collection = createCollection(client, "MongoDBConnectorFindAll")) {
                // When
                final String uniqueId = UUID.randomUUID().toString();
                final Document doc = new Document();
                doc.append("_id", 1);
                doc.append("unique", uniqueId);
                collection.insertOne(doc);
                final String uniqueId2 = UUID.randomUUID().toString();
                final Document doc2 = new Document();
                doc2.append("_id", 2);
                doc2.append("unique", uniqueId2);
                collection.insertOne(doc2);
                // Given
                @SuppressWarnings("unchecked")
                final List<String> resultsAsString = template().requestBody("direct:start", null, List.class);
                final List<Document> results = resultsAsString.stream().map(Document::parse).collect(Collectors.toList());
                // Then
                Assertions.assertThat(results).hasSize(2);
                Assertions.assertThat(results).contains(doc, doc2);
            }
        }

        @Override
        protected List<Step> createSteps() {
            return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-find", "test", "MongoDBConnectorFindAll", null, null);
        }

    }

    @Mongo
    static class MongoDBConnectorFindAllWithFilter extends MongoDBConnectorFindAllTest {
        public MongoDBConnectorFindAllWithFilter(@MongoConfiguration final Map<String, String> configuration) {
            super(configuration);
        }

        @Test
        public void findFilterTest(final MongoClient given) {
            try (MongoClient client = given; ClosableMongoCollection<Document> collection = createCollection(client, "MongoDBConnectorFindAllWithFilter")) {
                // When
                final String uniqueId = UUID.randomUUID().toString();
                final Document doc = new Document();
                doc.append("color", "green");
                doc.append("year", 2019);
                doc.append("unique", uniqueId);
                doc.append("text", "something funny");
                collection.insertOne(doc);
                final String uniqueId2 = UUID.randomUUID().toString();
                final Document doc2 = new Document();
                final ObjectId objectId2 = new ObjectId();
                doc2.append("_id", objectId2);
                doc2.append("color", "red");
                doc2.append("year", 2019);
                doc2.append("unique", uniqueId2);
                doc2.append("text", "something funny to test!");
                collection.insertOne(doc2);
                final String uniqueId3 = UUID.randomUUID().toString();
                final Document doc3 = new Document();
                doc3.append("color", "red");
                doc3.append("year", 1990);
                doc3.append("unique", uniqueId3);
                collection.insertOne(doc3);
                // Given
                @SuppressWarnings("unchecked")
                final List<String> resultsAsString = template().requestBody(
                    "direct:start",
                    String.format("{\"c\": \"red\", \"y\": 2019, \"oid\": \"%s\", \"regex\": \"test\"}", objectId2.toHexString()),
                    List.class);
                final List<Document> results = resultsAsString.stream().map(Document::parse).collect(Collectors.toList());
                // Then
                Assertions.assertThat(results).hasSize(1);
                Assertions.assertThat(results).contains(doc2);
            }
        }

        @Override
        protected List<Step> createSteps() {
            final String filter = "{\"color\": \":#c\", \"year\": :#y, \"_id\": ObjectId(\":#oid\"), \"text\": /:#regex/}";
            return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-find", "test", "MongoDBConnectorFindAllWithFilter", null,
                filter);
        }
    }

    @Mongo
    static class MongoDBConnectorFindAllWithFilterFailure extends MongoDBConnectorFindAllTest {
        public MongoDBConnectorFindAllWithFilterFailure(@MongoConfiguration final Map<String, String> configuration) {
            super(configuration);
        }

        @Test
        public void failingFindFilterTest(final MongoClient given) {
            try (MongoClient client = given; ClosableMongoCollection<Document> collection = createCollection(client, "MongoDBConnectorFindAllWithFilter")) {
                // When
                final String uniqueId = UUID.randomUUID().toString();
                final Document doc = new Document();
                doc.append("property", "green");
                doc.append("unique", uniqueId);
                collection.insertOne(doc);
                final String uniqueId2 = UUID.randomUUID().toString();
                final Document doc2 = new Document();
                doc2.append("property", "red");
                doc2.append("unique", uniqueId2);
                collection.insertOne(doc2);
                // Expect
                final Throwable thrown = catchThrowable(() -> {
                    template().requestBody("direct:start", "{\"someOtherProperty\": \"red\"}", List.class);
                });
                Assertions.assertThat(thrown).hasCauseInstanceOf(IllegalArgumentException.class)
                    .hasStackTraceContaining("Missing expected parameter \"c\" in the input source");
            }
        }

        @Override
        protected List<Step> createSteps() {
            final String filter = "{\"color\": \":#c\", \"year\": :#y, \"_id\": ObjectId(\":#oid\"), \"text\": /:#regex/}";
            return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-find", "test", "MongoDBConnectorFindAllWithFilter", null,
                filter);
        }
    }

    public MongoDBConnectorFindAllTest(final Map<String, String> configuration) {
        super(configuration);
    }

}
