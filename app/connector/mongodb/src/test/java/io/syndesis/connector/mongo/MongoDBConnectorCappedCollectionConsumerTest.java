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
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class MongoDBConnectorCappedCollectionConsumerTest extends MongoDBConnectorTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDBConnectorCappedCollectionConsumerTest.class);
    private static int ID = 1;

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
    }

    @Override
    protected List<Step> createSteps() {
        return fromMongoToMock("result", "io.syndesis.connector:connector-mongodb-consumer", DATABASE, COLLECTION,
            "id");
    }

    // **************************
    // Tests
    // **************************

    @Test
    public void mongoTest() throws Exception {
        // When
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.expectedMessagesMatches((Exchange e) -> {
            try {
                @SuppressWarnings("unchecked")
                List<String> doc = e.getMessage().getBody(List.class);
                JsonNode jsonNode = MAPPER.readTree(doc.get(0));
                int id = jsonNode.get("id").asInt();
                String value = jsonNode.get("someKey").asText();
                return id <= ID && "someValue".equals(value);
            } catch (IOException ex) {
                log.error("Test failed because: ",ex);
                return false;
            }
        });
        // Given
        Document doc = new Document();
        doc.append("id", ID++);
        doc.append("someKey", "someValue");
        collection.insertOne(doc);
        // Then
        mock.assertIsSatisfied();
    }

    @Test
    public void repeatMongoTest() throws Exception {
        // As we are tracking _id, any new insert should trigger the new document only
        mongoTest();
    }

}
