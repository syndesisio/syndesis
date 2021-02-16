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

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.MongoCollection;
import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.mongo.embedded.EmbedMongoConfiguration;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBConnectorChangeStreamConsumerTest extends MongoDBConnectorTestSupport {

    private final static Logger LOG = LoggerFactory.getLogger(MongoDBConnectorChangeStreamConsumerTest.class);
    private final static String COLLECTION = "changeStreamCollection";

    protected MongoCollection<Document> collection;

    @BeforeAll
    public static void doCollectionSetup() {
        EmbedMongoConfiguration.DATABASE.createCollection(COLLECTION);
        LOG.debug("Created a change stream collection named {}", COLLECTION);
    }

    @BeforeEach
    public void before(){
        collection = EmbedMongoConfiguration.DATABASE.getCollection(COLLECTION);
    }

    @AfterEach
    public void after(){
        collection.drop();
    }

    @Override
    protected List<Step> createSteps() {
        return fromMongoChangeStreamToMock("result", "io.syndesis.connector:connector-mongodb-consumer-changestream", DATABASE, COLLECTION);
    }

    @Test
    public void singleInsertTest() throws Exception {
        // When
        MockEndpoint mock = context().getEndpoint("mock:result", MockEndpoint.class);
        mock.setRetainLast(1);
        mock.expectedMessageCount(2);
        mock.expectedMessagesMatches((Exchange e) -> {
            try {
                // We just want to validate the output is coming as json well format
                @SuppressWarnings("unchecked")
                List<String> doc = e.getMessage().getBody(List.class);
                JsonNode jsonNode = MAPPER.readTree(doc.get(0));
                Assertions.assertThat(jsonNode).isNotNull();
                Assertions.assertThat(jsonNode.get("test").asText()).isEqualTo("junit2");
            } catch (IOException ex) {
                return false;
            }
            return true;
        });
        // Given
        Document doc = new Document();
        doc.append("someKey", "someValue");
        doc.append("test", "junit");
        collection.insertOne(doc);
        Document doc2 = new Document();
        doc2.append("someKey", "someValue2");
        doc2.append("test", "junit2");
        collection.insertOne(doc2);
        // Then
        mock.assertIsSatisfied();
    }
}
