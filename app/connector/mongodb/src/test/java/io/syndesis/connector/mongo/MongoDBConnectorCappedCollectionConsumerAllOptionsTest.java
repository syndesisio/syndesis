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

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.model.CreateCollectionOptions;
import io.syndesis.common.model.integration.Step;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MongoDBConnectorCappedCollectionConsumerAllOptionsTest extends MongoDBConnectorTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDBConnectorCappedCollectionConsumerAllOptionsTest.class);

    // **************************
    // Set up
    // **************************

    // JUnit will execute this method after the @BeforeClass of the superclass
    @BeforeClass
    public static void doCollectionSetup() {
        // The feature only works with capped collections!
        CreateCollectionOptions opts = new CreateCollectionOptions().capped(true).sizeInBytes(1024 * 1024);
        database.createCollection("test", opts);
        LOG.debug("Created a capped collection named test");
        database.createCollection("tracking");
        LOG.debug("Created a tracking collection named tracking");
    }

    /**
     * The test will be interrupted and we do expect to have the valid tracked stored before completion
     */
    @AfterClass
    public static void testTrackingIdValue() throws InterruptedException {
        List<Document> docsFound = database.getCollection("tracking").find().into(new ArrayList<Document>());
        System.out.println("Docs found" + docsFound);
        assertEquals(25, (int) docsFound.get(0).getInteger("someTrackingId"));
    }

    // **************************
    // Tests
    // **************************

    @Override
    protected List<Step> createSteps() {
        return fromMongoToMock("result", "io.syndesis.connector:connector-mongodb-consumer", DATABASE, COLLECTION,
            "someTrackingId", true, "idTracker",
            "test", "tracking", "someTrackingId");
    }

    @Test
    public void mongoTest() throws Exception {
        // When
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(3);
        mock.expectedMessagesMatches((Exchange e) -> {
            try {
                // We just want to validate the output is coming as json well format
                @SuppressWarnings("unchecked")
                List<String> doc = e.getMessage().getBody(List.class);
                JsonNode jsonNode = MAPPER.readTree(doc.get(0));
            } catch (IOException ex) {
                log.error("Test failed because: ",ex);
                return false;
            }
            return true;
        });
        // Given
        Document doc = new Document();
        doc.append("someKey", "someValue");
        doc.append("someTrackingId", 10);
        collection.insertOne(doc);
        Document doc2 = new Document();
        doc2.append("someKey", "someNewValue");
        doc2.append("someTrackingId", 20);
        collection.insertOne(doc2);
        Document doc3 = new Document();
        doc3.append("someKey", "someNewValue");
        doc3.append("someTrackingId", 25);
        collection.insertOne(doc3);

        // Then
        mock.assertIsSatisfied();
    }
}
