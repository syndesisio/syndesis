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

public abstract class MongoDBConnectorUpdateTest {

    @Mongo
    static class Multiple extends MongoDBConnectorTestSupport {

        public Multiple(@MongoConfiguration final Map<String, String> configuration) {
            super(configuration);
        }

        @Test
        public void mongoUpdateMultiTest(final MongoClient given) {
            try (MongoClient client = given; ClosableMongoCollection<Document> collection = createCollection(client, "MongoDBConnectorUpdateTest-Multiple")) {
                // When
                final String json = String.format("{\"test\":\"unit\",\"batchNo\":%d}", 33);
                final String json2 = String.format("{\"test\":\"unit2\",\"batchNo\":%d}", 33);
                collection.insertOne(Document.parse(json));
                collection.insertOne(Document.parse(json2));
                // Given
                // { $set: { <field1>: <value1>, ... } }
                final String updateArguments = "{\"batchNo\":33, \"someText\":\"updated!\"}";
                final Long result = template().requestBody("direct:start", updateArguments, Long.class);
                // Then
                final List<Document> docsFound = collection.find(Filters.eq("batchNo", 33)).into(new ArrayList<>());
                Assertions.assertThat(docsFound).hasSize(2);
                docsFound.forEach(document -> Assertions.assertThat(document.getString("test")).isEqualTo("updated!"));
                Assertions.assertThat(result).isEqualTo(2L);
            }
        }

        @Override
        protected List<Step> createSteps() {
            final String filter = "{\"batchNo\": :#batchNo}";
            final String updateExpression = "{$set: {\"test\": \":#someText\"}}";

            return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-update", "test", "MongoDBConnectorUpdateTest-Multiple", null,
                filter, updateExpression);
        }
    }

    @Mongo
    static class Single extends MongoDBConnectorTestSupport {

        public Single(@MongoConfiguration final Map<String, String> configuration) {
            super(configuration);
        }

        @Test
        public void mongoUpdateSingleTest(final MongoClient given) {
            try (MongoClient client = given; ClosableMongoCollection<Document> collection = createCollection(client, "MongoDBConnectorUpdateTest-Single")) {
                // When
                final String json = String.format("{\"test\":\"unit\",\"_id\":%s}", "11");
                final String json2 = String.format("{\"test\":\"unit2\",\"_id\":%s}", "22");
                collection.insertOne(Document.parse(json));
                collection.insertOne(Document.parse(json2));
                // Given
                final String updateArguments = "{\"id\":11, \"someText\":\"updated!\"}";
                final Long result = template().requestBody("direct:start", updateArguments, Long.class);
                // Then
                final List<Document> docsFound = collection.find(Filters.eq("_id", 11)).into(new ArrayList<>());
                Assertions.assertThat(docsFound).hasSize(1);
                Assertions.assertThat(docsFound.get(0).getString("test")).isEqualTo("updated!");
                Assertions.assertThat(result).isEqualTo(1L);
            }
        }

        @Override
        protected List<Step> createSteps() {
            final String filter = "{\"_id\": :#id}";
            final String updateExpression = "{$set: {\"test\": \":#someText\"}}";

            return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-update", "test", "MongoDBConnectorUpdateTest-Single", null,
                filter, updateExpression);
        }
    }

}
