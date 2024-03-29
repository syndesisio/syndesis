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

import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.mongo.embedded.EmbeddedMongoExtension.Mongo;
import io.syndesis.connector.mongo.embedded.EmbeddedMongoExtension.MongoConfiguration;

import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class MongoDBConnectorRemoveTest {

    private MongoDBConnectorRemoveTest() {
    }

    @Mongo
    static class Multiple extends MongoDBConnectorTestSupport {

        public Multiple(@MongoConfiguration final Map<String, String> configuration) {
            super(configuration);
        }

        @Test
        public void mongoRemoveMultiTest(final MongoClient given) {
            try (MongoClient client = given; ClosableMongoCollection<Document> collection = createCollection(client, "MongoDBConnectorRemoveTest-Multiple")) {
                // When
                final String json = String.format("{\"test\":\"unit\",\"batchNo\":%d}", 33);
                final String json2 = String.format("{\"test\":\"unit\",\"batchNo\":%d}", 33);
                final String json3 = String.format("{\"test\":\"unit3\",\"batchNo\":%d}", 33);
                collection.insertOne(Document.parse(json));
                collection.insertOne(Document.parse(json2));
                collection.insertOne(Document.parse(json3));
                List<Document> docsFound = collection.find(Filters.eq("batchNo", 33)).into(new ArrayList<>());
                assertEquals(3, docsFound.size());
                // Given
                final String removeArguments = "{\"filter\":\"unit\"}";
                // Need the header to enable multiple updates!
                final Long result = template().requestBody("direct:start", removeArguments, Long.class);
                // Then
                docsFound = collection.find(Filters.eq("batchNo", 33)).into(new ArrayList<>());
                Assertions.assertThat(docsFound).hasSize(1);
                Assertions.assertThat(docsFound.get(0).getString("test")).isEqualTo("unit3");
                Assertions.assertThat(result).isEqualTo(2L);
            }
        }

        @Override
        protected List<Step> createSteps() {
            return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-delete", "test", "MongoDBConnectorRemoveTest-Multiple", null,
                "{\"test\":\":#filter\"}");
        }

    }

    @Mongo
    static class Single extends MongoDBConnectorTestSupport {

        public Single(@MongoConfiguration final Map<String, String> configuration) {
            super(configuration);
        }

        @Test
        public void mongoRemoveSingleTest(final MongoClient given) {
            try (MongoClient client = given; ClosableMongoCollection<Document> collection = createCollection(client, "MongoDBConnectorRemoveTest-Single")) {
                // When
                final String json = String.format("{\"test\":\"unit\",\"_id\":%s}", "11");
                final String json2 = String.format("{\"test\":\"unit2\",\"_id\":%s}", "22");
                collection.insertOne(Document.parse(json));
                collection.insertOne(Document.parse(json2));
                List<Document> docsFound = collection.find(Filters.eq("_id", 11)).into(new ArrayList<>());
                assertEquals(1, docsFound.size());
                // Given
                final String removeArguments = "{\"filter\":\"unit\"}";
                final Long result = template().requestBody("direct:start", removeArguments, Long.class);
                // Then
                docsFound = collection.find(Filters.eq("_id", 11)).into(new ArrayList<>());
                Assertions.assertThat(docsFound).hasSize(0);
                Assertions.assertThat(result).isEqualTo(1L);
            }
        }

        @Override
        protected List<Step> createSteps() {
            return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-delete", "test", "MongoDBConnectorRemoveTest-Single", null,
                "{\"test\":\":#filter\"}");
        }

    }

}
