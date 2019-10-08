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
import org.bson.types.ObjectId;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class MongoDBConnectorFindByIdTest extends MongoDBConnectorTestSupport {
    // **************************
    // Set up
    // **************************

    @Override
    protected List<Step> createSteps() {
        return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-producer", DATABASE, COLLECTION,
            "findById");
    }

    // **************************
    // Tests
    // **************************

    @Test
    public void mongoFindByIdTest() throws IOException {
        // When
        String uniqueId = UUID.randomUUID().toString();
        Document doc = new Document();
        doc.append("_id", "1");
        doc.append("unique", uniqueId);
        collection.insertOne(doc);
        String uniqueId2 = UUID.randomUUID().toString();
        Document doc2 = new Document();
        doc2.append("_id", "2");
        doc2.append("unique", uniqueId2);
        collection.insertOne(doc2);
        Document doc3 = new Document();
        String uniqueId3 = UUID.randomUUID().toString();
        ObjectId objectId = new ObjectId();
        doc3.append("_id", objectId);
        doc3.append("unique", uniqueId3);
        collection.insertOne(doc3);
        // Given
        Document result = Document.parse((String)template.requestBody("direct:start", "1", List.class).get(0));
        Document result2 = Document.parse((String)template.requestBody("direct:start", 2, List.class).get(0));
        Document result3 = Document.parse((String)template.requestBody("direct:start", objectId, List.class).get(0));
        // Then
        assertEquals(uniqueId, result.get("unique"));
        assertEquals(uniqueId2, result2.get("unique"));
        assertEquals(uniqueId3, result3.get("unique"));
    }

}
