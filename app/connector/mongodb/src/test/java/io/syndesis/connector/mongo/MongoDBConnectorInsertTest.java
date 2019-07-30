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

import org.bson.Document;
import org.junit.Test;

import com.mongodb.client.model.Filters;

import io.syndesis.common.model.integration.Step;

@SuppressWarnings({ "PMD.SignatureDeclareThrowsException", "PMD.JUnitTestsShouldIncludeAssert" })
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
        // Given
        String uniqueId = UUID.randomUUID().toString();
        String message = String.format("{\"test\":\"unit\",\"uniqueId\":\"%s\"}", uniqueId);
        template().sendBody("direct:start", message);
        // Then
        List<Document> docsFound = collection.find(Filters.eq("uniqueId", uniqueId)).into(new ArrayList<Document>());
        assertEquals(1, docsFound.size());
    }

}
