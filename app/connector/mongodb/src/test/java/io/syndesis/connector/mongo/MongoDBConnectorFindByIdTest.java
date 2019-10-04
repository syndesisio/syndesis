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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.syndesis.common.model.integration.Step;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Test;

public class MongoDBConnectorFindByIdTest extends MongoDBConnectorTestSupport {

    @Override
    protected List<Step> createSteps() {
        return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-find", DATABASE, COLLECTION);
    }

    @Test
    public void mongoFindByIdNumericTest() {
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
        List<String> resultsAsText = template.requestBody("direct:start", "{ \"_id\": { $eq: 1 } }", List.class);
        List<Document> resultsAsDocs = resultsAsText.stream().map(Document::parse).collect(Collectors.toList());
        @SuppressWarnings("unchecked")
        List<String> resultsAsText2 = template.requestBody("direct:start", "{ \"_id\": 2 }", List.class);
        List<Document> resultsAsDocs2 = resultsAsText2.stream().map(Document::parse).collect(Collectors.toList());
        @SuppressWarnings("unchecked")
        List<String> resultsAsText3 = template.requestBody("direct:start", "{ \"_id\": 3 }", List.class);
        List<Document> resultsAsDocs3 = resultsAsText3.stream().map(Document::parse).collect(Collectors.toList());
        // Then
        Assertions.assertThat(resultsAsDocs).hasSize(1);
        Assertions.assertThat(resultsAsDocs).contains(doc);
        Assertions.assertThat(resultsAsDocs2).hasSize(1);
        Assertions.assertThat(resultsAsDocs2).contains(doc2);
        Assertions.assertThat(resultsAsDocs3).hasSize(0);
    }

    @Test
    public void mongoFindByIdTextTest() {
        // When
        String uniqueId = UUID.randomUUID().toString();
        Document doc = new Document();
        doc.append("_id", "test");
        doc.append("unique", uniqueId);
        collection.insertOne(doc);
        // Given
        @SuppressWarnings("unchecked")
        List<String> resultsAsText = template.requestBody("direct:start", "{ \"_id\": \"test\" }", List.class);
        List<Document> resultsAsDocs = resultsAsText.stream().map(Document::parse).collect(Collectors.toList());
        // Then
        Assertions.assertThat(resultsAsDocs).hasSize(1);
        Assertions.assertThat(resultsAsDocs).contains(doc);
    }

    @Test
    public void mongoFindByIdObjectIdTest() {
        // When
        String uniqueId = UUID.randomUUID().toString();
        Document doc = new Document();
        ObjectId objectId = new ObjectId();
        doc.append("_id", objectId);
        doc.append("unique", uniqueId);
        collection.insertOne(doc);
        // Given
        @SuppressWarnings("unchecked")
        List<String> resultsAsText = template.requestBody(
            "direct:start", "{ \"_id\": ObjectId(\"" + objectId.toString() + "\") }",
            List.class);
        List<Document> resultsAsDocs = resultsAsText.stream().map(Document::parse).collect(Collectors.toList());
        // Then

        Assertions.assertThat(resultsAsDocs).contains(doc);
        Assertions.assertThat(resultsAsDocs).hasSize(1);
    }
}

