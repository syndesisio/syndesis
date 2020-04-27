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
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.mongo.embedded.EmbedMongoConfiguration;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MongoDBConnectorCappedCollectionConsumerTest extends MongoDBConnectorTestSupport {

    private final static String COLLECTION = "cappedCollection";

    protected MongoCollection<Document> collection;
    @Autowired
    private MongoDatabase database;

    @BeforeClass
    public static void doCollectionSetup() {
        // The feature only works with capped collections!
        CreateCollectionOptions opts = new CreateCollectionOptions().capped(true).sizeInBytes(1024 * 1024);
        EmbedMongoConfiguration.getDB().createCollection(COLLECTION, opts);
    }
    @Override
    protected List<Step> createSteps() {
        return fromMongoTailToMock("result", "io.syndesis.connector:connector-mongodb-consumer-tail", DATABASE, COLLECTION,
            "id");
    }

    @Test
    public void mongoTest() throws Exception {
        collection = database.getCollection(COLLECTION);

        String unique1 = UUID.randomUUID().toString();
        int id1 = 1;
        String unique2 = UUID.randomUUID().toString();
        int id2 = 2;
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(2);
        mock.expectedMessagesMatches(
            exchange -> validateDocument(exchange, unique1, id1),
            exchange -> validateDocument(exchange, unique2, id2)
        );

        Document doc1 = new Document();
        doc1.append("id", id1);
        doc1.append("unique", unique1);
        collection.insertOne(doc1);
        Document doc2 = new Document();
        doc2.append("id", id2);
        doc2.append("unique", unique2);
        collection.insertOne(doc2);

        mock.assertIsSatisfied();
    }

    private static boolean validateDocument(Exchange e, String unique, int id) {
        try {
            @SuppressWarnings("unchecked")
            List<String> doc = e.getMessage().getBody(List.class);
            JsonNode jsonNode = MAPPER.readTree(doc.get(0));
            Assertions.assertThat(jsonNode.get("unique").asText()).isEqualTo(unique);
            Assertions.assertThat(jsonNode.get("id").asInt()).isEqualTo(id);
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

}
