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
import java.util.UUID;
import java.util.stream.Collectors;

import com.mongodb.client.model.Filters;
import io.syndesis.common.model.integration.Step;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.Test;

public class MongoDBConnectorInsertTest extends MongoDBConnectorTestSupport {

    @Override
    protected List<Step> createSteps() {
        return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-insert", DATABASE, COLLECTION);
    }

    @Test
    public void mongoInsertSingleTest() {
        // When
        String uniqueId = UUID.randomUUID().toString();
        String message = String.format("{\"test\":\"unit\",\"uniqueId\":\"%s\"}", uniqueId);
        Document doc = Document.parse(message);
        // Given
        @SuppressWarnings("unchecked")
        List<String> resultsAsString = template.requestBody("direct:start", message, List.class);
        // Then
        Assertions.assertThat(resultsAsString).hasSize(1);
        Document result = resultsAsString.stream().map(Document::parse).collect(Collectors.toList()).get(0);
        Assertions.assertThat(doc.getString("test")).isEqualTo(result.getString("test"));
        Assertions.assertThat(doc.getString("uniqueId")).isEqualTo(result.getString("uniqueId"));

        List<Document> docsFound = collection.find(Filters.eq("uniqueId", uniqueId)).into(new ArrayList<>());
        Assertions.assertThat(docsFound).hasSize(1);
        Assertions.assertThat(docsFound).contains(result);
    }

    @Test
    public void mongoInsertMultipleDocuments() {
        // When
        int iteration = 10;
        int batchId = 432;
        List<Document> batchMessage = formatBatchMessageDocument(batchId);
        // Given
        @SuppressWarnings("unchecked")
        List<String> resultsAsString = template.requestBody("direct:start", batchMessage, List.class);
        List<Document> result = resultsAsString.stream().map(Document::parse).collect(Collectors.toList());
        // Then
        List<Document> docsFound = collection.find(Filters.eq("batchNo", batchId)).into(new ArrayList<>());
        Assertions.assertThat(docsFound).hasSize(iteration);
        Assertions.assertThat(result).containsAll(docsFound);
    }

    private static List<Document> formatBatchMessageDocument(int batchNo) {
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
        List<Document> batchMessage = formatBatchMessageDocument(batchId);
        List<String> jsonStrings = batchMessage.stream().map(Document::toJson).collect(Collectors.toList());
        // Given
        @SuppressWarnings("unchecked")
        List<String> resultsAsString = template.requestBody("direct:start", jsonStrings, List.class);
        List<Document> result = resultsAsString.stream().map(Document::parse).collect(Collectors.toList());
        // Then
        List<Document> docsFound = collection.find(Filters.eq("batchNo", batchId)).into(new ArrayList<>());
        Assertions.assertThat(docsFound).hasSize(iteration);
        Assertions.assertThat(result).containsAll(docsFound);
    }
}
