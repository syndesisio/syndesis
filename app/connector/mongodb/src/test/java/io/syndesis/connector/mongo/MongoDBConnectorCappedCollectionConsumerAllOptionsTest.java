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
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CreateCollectionOptions;
import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.mongo.embedded.EmbedMongoConfiguration;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MongoDBConnectorCappedCollectionConsumerAllOptionsTest extends MongoDBConnectorTestSupport {

    private final static Logger LOG = LoggerFactory.getLogger(MongoDBConnectorCappedCollectionConsumerAllOptionsTest.class);
    private final static String COLLECTION = "allOptionCappedCollection";
    private final static String COLLECTION_TRACKING = "allOptionCappedCollection_tracking";
    private final static String COLLECTION_TRACKING_FIELD = "someTrackingId";

    protected MongoCollection<Document> collection;

    // JUnit will execute this method after the @BeforeEachClass of the superclass
    @BeforeAll
    public static void doCollectionSetup() {
        // The feature only works with capped collections!
        CreateCollectionOptions opts = new CreateCollectionOptions().capped(true).sizeInBytes(1024 * 1024);
        EmbedMongoConfiguration.DATABASE.createCollection(COLLECTION, opts);
        LOG.debug("Created a capped collection named {}", COLLECTION);
        EmbedMongoConfiguration.DATABASE.createCollection(COLLECTION_TRACKING);
        LOG.debug("Created a tracking collection named {}", COLLECTION_TRACKING);
    }

    /**
     * The test will be interrupted and we do expect to have the valid tracked stored before completion
     */
    @AfterAll
    public static void testTrackingIdValue() {
        List<Document> docsFound = EmbedMongoConfiguration.DATABASE.getCollection(COLLECTION_TRACKING).find().into(new ArrayList<>());
        assertEquals(25, (int) docsFound.get(0).getInteger(COLLECTION_TRACKING_FIELD));
    }

    @BeforeEach
    public void init(){
        collection = EmbedMongoConfiguration.DATABASE.getCollection(COLLECTION);
    }

    @Override
    protected List<Step> createSteps() {
        return fromMongoTailToMock("result", "io.syndesis.connector:connector-mongodb-consumer-tail", DATABASE, COLLECTION,
            COLLECTION_TRACKING_FIELD, true, "idTracker",
            DATABASE, COLLECTION_TRACKING, COLLECTION_TRACKING_FIELD);
    }

    @Test
    public void mongoTest() throws Exception {
        // When
        MockEndpoint mock = context().getEndpoint("mock:result", MockEndpoint.class);
        // We just retain last message
        mock.setRetainLast(1);
        mock.expectedMessageCount(3);
        mock.expectedMessagesMatches((Exchange e) -> {
            try {
                // We just want to validate the output is coming as json well format
                @SuppressWarnings("unchecked")
                List<String> doc = e.getMessage().getBody(List.class);
                JsonNode jsonNode = MAPPER.readTree(doc.get(0));
                Assertions.assertThat(jsonNode).isNotNull();
                Assertions.assertThat(jsonNode.get(COLLECTION_TRACKING_FIELD).asInt()).isEqualTo(25);
            } catch (IOException ex) {
                return false;
            }
            return true;
        });
        // Given
        Document doc = new Document();
        doc.append("someKey", "someValue");
        doc.append(COLLECTION_TRACKING_FIELD, 10);
        collection.insertOne(doc);
        Document doc2 = new Document();
        doc2.append("someKey", "someNewValue");
        doc2.append(COLLECTION_TRACKING_FIELD, 20);
        collection.insertOne(doc2);
        Document doc3 = new Document();
        doc3.append("someKey", "someNewValue");
        doc3.append(COLLECTION_TRACKING_FIELD, 25);
        collection.insertOne(doc3);

        // Then
        mock.assertIsSatisfied();
    }
}
