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
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBConnectorCappedCollectionConsumerAllOptionsTest extends MongoDBConnectorTestSupport {

    private final static Logger LOG = LoggerFactory.getLogger(MongoDBConnectorCappedCollectionConsumerAllOptionsTest.class);
    private final static String COLLECTION = "allOptionCappedCollection";
    private final static String COLLECTION_TRACKING = "allOptionCappedCollection_tracking";
    private final static String COLLECTION_TRACKING_FIELD = "someTrackingId";

    protected MongoCollection<Document> collection;

    @Override
    protected List<Step> createSteps() {
        List<Step> steps = fromMongoTailToMock("result",
            "io.syndesis.connector:connector-mongodb-consumer-tail",
            DATABASE,
            COLLECTION,
            COLLECTION_TRACKING_FIELD, true, "idTracker",
            DATABASE, COLLECTION_TRACKING, COLLECTION_TRACKING_FIELD);
        return steps;
    }

    // JUnit will execute this method after the @BeforeClass of the superclass
    @BeforeClass
    public static void doCollectionSetup() {
        // The feature only works with capped collections!
        CreateCollectionOptions opts = new CreateCollectionOptions().capped(true).sizeInBytes(1024 * 1024);
        EmbedMongoConfiguration.getDB().createCollection(COLLECTION, opts);
        LOG.debug("Created a capped collection named {}", COLLECTION);
        EmbedMongoConfiguration.getDB().createCollection(COLLECTION_TRACKING);
        LOG.debug("Created a tracking collection named {}", COLLECTION_TRACKING);
    }


    @Test
    public void mongoTest() throws Exception {
        collection = EmbedMongoConfiguration.getDB().getCollection(COLLECTION);
        // When
        MockEndpoint mock = getMockEndpoint("mock:result");
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

        List<Document> docsFound =
            EmbedMongoConfiguration.getDB().getCollection(COLLECTION_TRACKING).find().into(new ArrayList<>());
        assertEquals(25, (int) docsFound.get(0).getInteger(COLLECTION_TRACKING_FIELD));
    }
}
