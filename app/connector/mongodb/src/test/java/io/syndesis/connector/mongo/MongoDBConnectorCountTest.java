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

import io.syndesis.common.model.integration.Step;
import org.apache.camel.ExchangePattern;
import org.bson.Document;
import org.junit.Test;

import java.util.List;

public class MongoDBConnectorCountTest extends MongoDBConnectorTestSupport {

    // **************************
    // Set up
    // **************************

    @Override
    protected List<Step> createSteps() {
        return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-producer", DATABASE, COLLECTION,
            "count");
    }

    // **************************
    // Tests
    // **************************

    @Test
    public void mongoCountSingleTest() {
        // When
        String json = String.format("{\"test\":\"single\",\"_id\":%s}", "11");
        String json2 = String.format("{\"test\":\"single2\",\"_id\":%s}", "22");
        collection.insertOne(Document.parse(json));
        collection.insertOne(Document.parse(json2));
        // Given
        String countArguments = "{\"test\":\"single\"}";
        Document result = Document.parse((String)template.requestBody("direct:start", countArguments, List.class).get(0));
        // Then
        assertEquals(Integer.valueOf(1), result.getInteger("count"));
    }

    @Test
    public void mongoCountMultiTest() {
        // When
        String json = String.format("{\"test\":\"unit\",\"batchNo\":%d}", 33);
        String json2 = String.format("{\"test\":\"unit\",\"batchNo\":%d}", 33);
        String json3 = String.format("{\"test\":\"unit3\",\"batchNo\":%d}", 44);
        collection.insertOne(Document.parse(json));
        collection.insertOne(Document.parse(json2));
        collection.insertOne(Document.parse(json3));
        // Given
        String countArguments = "{\"batchNo\":33}";
        Document result = Document.parse((String)template.requestBody("direct:start", countArguments, List.class).get(0));
        // Then
        assertEquals(Integer.valueOf(2), result.getInteger("count"));
    }

}
