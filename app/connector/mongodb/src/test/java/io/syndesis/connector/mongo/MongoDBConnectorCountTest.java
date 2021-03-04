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

import io.syndesis.common.model.integration.Step;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.jupiter.api.Test;

public class MongoDBConnectorCountTest extends MongoDBConnectorProducerTestSupport {

    private final static String COLLECTION = "countCollection";

    @Override
    public String getCollectionName() {
        return COLLECTION;
    }

    @Override
    protected List<Step> createSteps() {
        return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-count", DATABASE, COLLECTION, null,
            "{\"test\":\":#someText\"}");
    }

    @Test
    public void mongoCountSingleTest() {
        // When
        String json = String.format("{\"test\":\"single\",\"_id\":%s}", "11");
        String json2 = String.format("{\"test\":\"single2\",\"_id\":%s}", "22");
        collection.insertOne(Document.parse(json));
        collection.insertOne(Document.parse(json2));
        // Given
        String countArguments = "{\"someText\":\"single\"}";
        Long result = template().requestBody("direct:start", countArguments, Long.class);
        // Then
        Assertions.assertThat(result).isEqualTo(1L);
    }

    @Test
    public void mongoCountMultiTest() {
        // When
        String json = String.format("{\"test\":\"unit\",\"batchNo\":%d}", 33);
        String json2 = String.format("{\"test\":\"unit\",\"batchNo\":%d}", 35);
        String json3 = String.format("{\"test\":\"unit3\",\"batchNo\":%d}", 44);
        collection.insertOne(Document.parse(json));
        collection.insertOne(Document.parse(json2));
        collection.insertOne(Document.parse(json3));
        // Given
        String countArguments = "{\"someText\": \"unit\"}";
        Long result = template().requestBody("direct:start", countArguments, Long.class);
        // Then
        Assertions.assertThat(result).isEqualTo(2L);
    }

}
