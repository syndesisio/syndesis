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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class MongoDBConnectorUpdateTest extends MongoDBConnectorTestSupport {

    @Override
    protected List<Step> createSteps() {
        return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-update", DATABASE, COLLECTION);
    }

    @Test
    public void mongoUpdateSingleTest() {
        // When
        String json = String.format("{\"test\":\"unit\",\"_id\":%s}", "11");
        String json2 = String.format("{\"test\":\"unit2\",\"_id\":%s}", "22");
        collection.insertOne(Document.parse(json));
        collection.insertOne(Document.parse(json2));
        // Given
        // { $set: { <field1>: <value1>, ... } }
        String updateArguments = "[{\"_id\":11},{$set: {\"test\":\"updated!\"}}]";
        Long result = template.requestBody("direct:start", updateArguments, Long.class);
        // Then
        List<Document> docsFound = collection.find(Filters.eq("_id", 11)).into(new ArrayList<Document>());
        assertThat(docsFound, hasSize(1));
        assertThat(docsFound.get(0).getString("test"), equalTo("updated!"));
        assertThat(docsFound.get(0).getString("test"), equalTo("updated!"));
        assertThat(result, equalTo(1L));
    }

    @Test
    public void mongoUpdateMultiTest() {
        // When
        String json = String.format("{\"test\":\"unit\",\"batchNo\":%d}", 33);
        String json2 = String.format("{\"test\":\"unit2\",\"batchNo\":%d}", 33);
        collection.insertOne(Document.parse(json));
        collection.insertOne(Document.parse(json2));
        // Given
        // { $set: { <field1>: <value1>, ... } }
        String updateArguments = "[{\"batchNo\":33},{$set: {\"test\":\"updated!\"}}]";
        Long result = template.requestBody("direct:start", updateArguments, Long.class);
        // Then
        List<Document> docsFound = collection.find(Filters.eq("batchNo", 33)).into(new ArrayList<Document>());
        assertThat(docsFound, hasSize(2));
        assertEquals(2, docsFound.size());
        docsFound.forEach(document -> assertThat(document.getString("test"), equalTo("updated!")));
        assertThat(result, equalTo(2L));
    }

}
