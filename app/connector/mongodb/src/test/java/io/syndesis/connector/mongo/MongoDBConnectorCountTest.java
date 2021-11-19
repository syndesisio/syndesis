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

import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.mongo.embedded.EmbeddedMongoExtension.Mongo;
import io.syndesis.connector.mongo.embedded.EmbeddedMongoExtension.MongoConfiguration;

import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import com.mongodb.MongoClient;

public final class MongoDBConnectorCountTest {

    private MongoDBConnectorCountTest() {
    }

    @Mongo
    static class CountMulti extends MongoDBConnectorTestSupport {

        public CountMulti(@MongoConfiguration final Map<String, String> configuration) {
            super(configuration);
        }

        @Test
        public void mongoCountMultiTest(final MongoClient given) {
            try (MongoClient client = given; ClosableMongoCollection<Document> collection = createCollection(client, "MongoDBConnectorCountTest-CountMulti")) {
                // When
                final String json = String.format("{\"test\":\"unit\",\"batchNo\":%d}", 33);
                final String json2 = String.format("{\"test\":\"unit\",\"batchNo\":%d}", 35);
                final String json3 = String.format("{\"test\":\"unit3\",\"batchNo\":%d}", 44);
                collection.insertOne(Document.parse(json));
                collection.insertOne(Document.parse(json2));
                collection.insertOne(Document.parse(json3));
                // Given
                final String countArguments = "{\"someText\": \"unit\"}";
                final Long result = template().requestBody("direct:start", countArguments, Long.class);
                // Then
                Assertions.assertThat(result).isEqualTo(2L);
            }
        }

        @Override
        protected List<Step> createSteps() {
            return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-count", "test", "MongoDBConnectorCountTest-CountMulti", null,
                "{\"test\":\":#someText\"}");
        }

    }

    @Mongo
    static class CountSingle extends MongoDBConnectorTestSupport {

        public CountSingle(@MongoConfiguration final Map<String, String> configuration) {
            super(configuration);
        }

        @Test
        public void mongoCountSingleTest(final MongoClient given) {
            try (MongoClient client = given; ClosableMongoCollection<Document> collection = createCollection(client, "MongoDBConnectorCountTest-CountSingle")) {
                // When
                final String json = String.format("{\"test\":\"single\",\"_id\":%s}", "11");
                final String json2 = String.format("{\"test\":\"single2\",\"_id\":%s}", "22");
                collection.insertOne(Document.parse(json));
                collection.insertOne(Document.parse(json2));
                // Given
                final String countArguments = "{\"someText\":\"single\"}";
                final Long result = template().requestBody("direct:start", countArguments, Long.class);
                // Then
                Assertions.assertThat(result).isEqualTo(1L);
            }
        }

        @Override
        protected List<Step> createSteps() {
            return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-count", "test", "MongoDBConnectorCountTest-CountSingle", null,
                "{\"test\":\":#someText\"}");
        }
    }

}
