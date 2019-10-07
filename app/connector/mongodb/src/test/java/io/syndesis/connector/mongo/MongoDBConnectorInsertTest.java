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

import com.mongodb.client.model.Filters;
import io.syndesis.common.model.integration.Step;
import org.bson.Document;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;

public class MongoDBConnectorInsertTest extends MongoDBConnectorTestSupport {

    // **************************
    // Set up
    // **************************

    @Override
    protected List<Step> createSteps() {
        return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-producer", DATABASE, COLLECTION,
            "insert");
    }

    // **************************
    // Tests
    // **************************

    @Test
    public void mongoInsertTest() {
        // When
        String uniqueId = UUID.randomUUID().toString();
        String message = String.format("{\"test\":\"unit\",\"uniqueId\":\"%s\"}", uniqueId);
        Document doc = Document.parse(message);
        // Given
        Document result = Document.parse((String)template.requestBody("direct:start", message, List.class).get(0));
        // Then
        assertEquals(doc.getString("test"), result.getString("test"));
        assertEquals(doc.getString("uniqueId"), result.getString("uniqueId"));

        List<Document> docsFound = collection.find(Filters.eq("uniqueId", uniqueId)).into(new ArrayList<Document>());
        assertEquals(1, docsFound.size());
        assertEquals(result, docsFound.get(0));
    }

    @Test
    public void mongoInsertMultipleDocuments() {
        // When
        int iteration = 10;
        int batchId = 432;
        List<Document> batchMessage = formatBatchMessageDocument(iteration, batchId);
        // Given
        @SuppressWarnings("unchecked")
        List<String> resultsAsString = template.requestBody("direct:start", batchMessage, List.class);
        List<Document> result = resultsAsString.stream().map(s -> Document.parse(s)).collect(Collectors.toList());
        // Then
        List<Document> docsFound = collection.find(Filters.eq("batchNo", batchId)).into(new ArrayList<Document>());
        assertEquals(iteration, docsFound.size());
        assertThat(result, containsInAnyOrder(docsFound.toArray()));
    }

    private List<Document> formatBatchMessageDocument(int nDocs, int batchNo) {
        List<Document> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Document next = new Document();
            next.put("test", "test" + i);
            next.put("batchNo", batchNo);
            list.add(next);
        }
        return list;
    }

    @Test
    public void mongoInsertMultipleJsonTexts() {
        // When
        int iteration = 10;
        int batchId = 654;
        List<Document> batchMessage = formatBatchMessageDocument(iteration, batchId);
        List<String> jsonStrings = batchMessage.stream().map(Document::toJson).collect(Collectors.toList());
        // Given
        @SuppressWarnings("unchecked")
        List<String> resultsAsString = template.requestBody("direct:start", jsonStrings, List.class);
        List<Document> result = resultsAsString.stream().map(s -> Document.parse(s)).collect(Collectors.toList());
        // Then
        List<Document> docsFound = collection.find(Filters.eq("batchNo", batchId)).into(new ArrayList<Document>());
        assertEquals(iteration, docsFound.size());
        assertThat(result, containsInAnyOrder(docsFound.toArray()));
    }
}
