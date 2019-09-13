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
import org.bson.Document;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MongoDBConnectorFindAllTest extends MongoDBConnectorTestSupport {
    // **************************
    // Set up
    // **************************

    @Override
    protected List<Step> createSteps() {
        return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-producer", DATABASE, COLLECTION,
            "findAll");
    }

    // **************************
    // Tests
    // **************************

    @Test
    public void mongoFindAllTest() throws IOException {
        // When
        String uniqueId = UUID.randomUUID().toString();
        Document doc = new Document();
        doc.append("_id", 1);
        doc.append("unique", uniqueId);
        collection.insertOne(doc);
        String uniqueId2 = UUID.randomUUID().toString();
        Document doc2 = new Document();
        doc2.append("_id", 2);
        doc2.append("unique", uniqueId2);
        collection.insertOne(doc2);
        // Given
        @SuppressWarnings("unchecked")
        List<String> resultsAsString = template.requestBody("direct:start", null, List.class);
        List<Document> results = resultsAsString.stream().map(s -> Document.parse(s)).collect(Collectors.toList());
        // Then
        assertEquals(2, results.size());
        results.forEach(document -> assertTrue(
            (Integer.valueOf(1).equals(document.get("_id")) && uniqueId.equals(document.get("unique"))) ||
                (Integer.valueOf(2).equals(document.get("_id")) && uniqueId2.equals(document.get("unique")))
        ));
    }

    @Test
    public void mongoFindAllFilterTest() throws IOException {
        // When
        String uniqueId = UUID.randomUUID().toString();
        Document doc = new Document();
        doc.append("color", "green");
        doc.append("unique", uniqueId);
        collection.insertOne(doc);
        String uniqueId2 = UUID.randomUUID().toString();
        Document doc2 = new Document();
        doc2.append("color", "red");
        doc2.append("unique", uniqueId2);
        collection.insertOne(doc2);
        // Given
        @SuppressWarnings("unchecked")
        List<String> resultsAsString = template.requestBody("direct:start", "{\"color\": \"red\"}", List.class);
        List<Document> results = resultsAsString.stream().map(s -> Document.parse(s)).collect(Collectors.toList());
        // Then
        assertEquals(1, results.size());
        assertEquals(uniqueId2, results.get(0).getString("unique"));
    }
}
