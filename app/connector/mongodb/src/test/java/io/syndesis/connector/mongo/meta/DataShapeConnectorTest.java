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
package io.syndesis.connector.mongo.meta;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataShapeConnectorTest {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void verifyMetadataEmptyFilter() {
        DataShape criteriaStatic = MongoDBMetadataRetrieval.criteria("{test: \"static\"}");
        DataShape criteriaNull = MongoDBMetadataRetrieval.criteria(null);
        Assertions.assertThat(criteriaStatic.getKind()).isEqualTo(DataShapeKinds.NONE);
        Assertions.assertThat(criteriaNull.getKind()).isEqualTo(DataShapeKinds.NONE);
    }

    @Test
    public void verifyMetadataFilter() throws IOException {
        DataShape criteriaParams = MongoDBMetadataRetrieval.criteria("{test: \":#someText\", xyz.moreTest: /:#more/}");
        JsonNode json = OBJECT_MAPPER.readTree(criteriaParams.getSpecification());
        Assertions.assertThat(criteriaParams.getKind()).isEqualTo(DataShapeKinds.JSON_SCHEMA);
        Assertions.assertThat(json.get("properties").get("someText")).isNotNull();
        Assertions.assertThat(json.get("properties").get("more")).isNotNull();
    }

    @Test
    public void verifyMetadataFindProducer() {
        // Given
        DataShape schema = new DataShape.Builder().name("fake").build();
        Map<String, Object> properties = new HashMap<>();
        properties.put("filter", "{fake: :#fake}");
        MongoDBMetadataRetrieval metaBridge = new MongoDBMetadataRetrieval();
        // When
        SyndesisMetadata meta = metaBridge.buildDatashape("io.syndesis.connector:connector-mongodb-find",schema,properties);
        // Then
        Assertions.assertThat(meta.inputShape.getKind()).isEqualTo(DataShapeKinds.JSON_SCHEMA);
        Assertions.assertThat(meta.outputShape).isEqualTo(schema);
    }

    @Test
    public void verifyMetadataInsertProducer() {
        // Given
        DataShape schema = new DataShape.Builder().name("fake").build();
        MongoDBMetadataRetrieval metaBridge = new MongoDBMetadataRetrieval();
        // When
        SyndesisMetadata meta = metaBridge.buildDatashape("io.syndesis.connector:connector-mongodb-insert",schema,null);
        // Then
        Assertions.assertThat(meta.inputShape).isEqualTo(schema);
        Assertions.assertThat(meta.outputShape).isEqualTo(schema);
    }

    @Test
    public void verifyMetadataUpsertProducer() {
        // Given
        DataShape schema = new DataShape.Builder().name("fake").build();
        Map<String, Object> properties = new HashMap<>();
        properties.put("filter", "{fake: :#fake}");
        MongoDBMetadataRetrieval metaBridge = new MongoDBMetadataRetrieval();
        // When
        SyndesisMetadata meta = metaBridge.buildDatashape("io.syndesis.connector:connector-mongodb-upsert",schema,properties);
        // Then
        Assertions.assertThat(meta.inputShape.getKind()).isEqualTo(DataShapeKinds.JSON_SCHEMA);
        Assertions.assertThat(meta.outputShape).isEqualTo(schema);
    }

    @Test
    public void verifyMetadataUpdate() {
        // Given
        DataShape schema = new DataShape.Builder().name("fake").build();
        Map<String, Object> properties = new HashMap<>();
        properties.put("filter", "{fake: :#fakeFilter}");
        properties.put("updateExpression", "{fakeExpression: :#fakeExpression}");
        MongoDBMetadataRetrieval metaBridge = new MongoDBMetadataRetrieval();
        // When
        SyndesisMetadata meta = metaBridge.buildDatashape("io.syndesis.connector:connector-mongodb-update",schema,properties);
        // Then
        Assertions.assertThat(meta.inputShape.getKind()).isEqualTo(DataShapeKinds.JSON_SCHEMA);
        Assertions.assertThat(meta.inputShape.getSpecification()).contains("fakeFilter");
        Assertions.assertThat(meta.inputShape.getSpecification()).contains("fakeExpression");
        Assertions.assertThat(meta.outputShape).isEqualTo(schema);
    }

    @Test
    public void verifyMetadataDelete() {
        // Given
        DataShape schema = new DataShape.Builder().name("fake").build();
        Map<String, Object> properties = new HashMap<>();
        properties.put("filter", "{fake: :#fake}");
        MongoDBMetadataRetrieval metaBridge = new MongoDBMetadataRetrieval();
        // When
        SyndesisMetadata meta = metaBridge.buildDatashape("io.syndesis.connector:connector-mongodb-delete",schema,properties);
        // Then
        Assertions.assertThat(meta.inputShape.getKind()).isEqualTo(DataShapeKinds.JSON_SCHEMA);
        Assertions.assertThat(meta.outputShape).isEqualTo(schema);
    }

    @Test
    public void verifyMetadataCount() {
        // Given
        DataShape schema = new DataShape.Builder().name("fake").build();
        Map<String, Object> properties = new HashMap<>();
        properties.put("filter", "{fake: :#fake}");
        MongoDBMetadataRetrieval metaBridge = new MongoDBMetadataRetrieval();
        // When
        SyndesisMetadata meta = metaBridge.buildDatashape("io.syndesis.connector:connector-mongodb-count",schema,properties);
        // Then
        Assertions.assertThat(meta.inputShape.getKind()).isEqualTo(DataShapeKinds.JSON_SCHEMA);
        Assertions.assertThat(meta.outputShape).isEqualTo(schema);
    }

    @Test
    public void verifyMetadataTailableConsumer() {
        // Given
        DataShape schema = new DataShape.Builder().name("fake").build();
        MongoDBMetadataRetrieval metaBridge = new MongoDBMetadataRetrieval();
        // When
        SyndesisMetadata meta = metaBridge.buildDatashape("io.syndesis.connector:connector-mongodb-consumer-tail",schema,null);
        // Then
        Assertions.assertThat(meta.inputShape.getKind()).isEqualTo(DataShapeKinds.NONE);
        Assertions.assertThat(meta.outputShape).isEqualTo(schema);
    }

    @Test
    public void verifyMetadataChangeStreamConsumer() {
        // Given
        DataShape schema = new DataShape.Builder().name("fake").build();
        MongoDBMetadataRetrieval metaBridge = new MongoDBMetadataRetrieval();
        // When
        SyndesisMetadata meta = metaBridge.buildDatashape("io.syndesis.connector:connector-mongodb-consumer-changestream",schema,null);
        // Then
        Assertions.assertThat(meta.inputShape.getKind()).isEqualTo(DataShapeKinds.NONE);
        Assertions.assertThat(meta.outputShape).isEqualTo(schema);
    }
}
