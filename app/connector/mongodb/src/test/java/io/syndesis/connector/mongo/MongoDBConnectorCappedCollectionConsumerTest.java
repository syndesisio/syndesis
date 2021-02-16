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
import com.mongodb.client.model.CreateCollectionOptions;
import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.mongo.embedded.EmbedMongoConfiguration;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBConnectorCappedCollectionConsumerTest extends MongoDBConnectorTestSupport {

    private final static Logger LOG = LoggerFactory.getLogger(MongoDBConnectorCappedCollectionConsumerTest.class);
    private final static String COLLECTION = "cappedCollection";
    private static int globalId = 0;

    protected MongoCollection<Document> collection;

    // JUnit will execute this method after the @BeforeEachClass of the superclass
    @BeforeAll
    public static void doCollectionSetup() {
        // The feature only works with capped collections!
        CreateCollectionOptions opts = new CreateCollectionOptions().capped(true).sizeInBytes(1024 * 1024);
        EmbedMongoConfiguration.DATABASE.createCollection(COLLECTION, opts);
        LOG.debug("Created a capped collection named {}", COLLECTION);
    }

    @Override
    protected List<Step> createSteps() {
        return fromMongoTailToMock("result", "io.syndesis.connector:connector-mongodb-consumer-tail", DATABASE, COLLECTION,
            "id");
    }

    @BeforeEach
    public void init(){
        collection = EmbedMongoConfiguration.DATABASE.getCollection(COLLECTION);
    }

    @Test
    public void mongoTest() throws Exception {
        // When
        String unique = UUID.randomUUID().toString();
        int id = globalId++;
        MockEndpoint mock = context().getEndpoint("mock:result", MockEndpoint.class);
        // We just retain last message
        mock.setRetainLast(1);
        mock.expectedMessageCount(1);
        mock.expectedMessagesMatches((Exchange e) -> {
            try {
                @SuppressWarnings("unchecked")
                List<String> doc = e.getMessage().getBody(List.class);
                JsonNode jsonNode = MAPPER.readTree(doc.get(0));
                Assertions.assertThat(jsonNode.get("unique").asText()).isEqualTo(unique);
                Assertions.assertThat(jsonNode.get("id").asInt()).isEqualTo(id);
                return true;
            } catch (IOException ex) {
                return false;
            }
        });
        // Given
        Document doc = new Document();
        doc.append("id", id);
        doc.append("unique", unique);
        collection.insertOne(doc);
        // Then
        mock.assertIsSatisfied();
    }

    @Test
    public void repeatMongoTest() throws Exception {
        // As we are tracking id, any new insert should trigger the new document only
        mongoTest();
    }

}
