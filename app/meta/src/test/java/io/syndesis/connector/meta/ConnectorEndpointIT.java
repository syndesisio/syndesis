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
package io.syndesis.connector.meta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.connector.support.verifier.api.MetadataRetrieval;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;

import org.apache.camel.CamelContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.banner-mode = off",
        "debug = true"
    })
public class ConnectorEndpointIT {

    @MockBean(name = "test-connector-adapter")
    private MetadataRetrieval adapter;

    private final TestRestTemplate api;

    @Autowired
    public ConnectorEndpointIT(final TestRestTemplate api, final ObjectMapper objectMapper) {
        this.api = api;
        objectMapper.addMixIn(SyndesisMetadata.class, SyndesisMetadataDeserializer.class);
        objectMapper.addMixIn(PropertyPair.class, PropertyPairDeserializer.class);
    }

    @Test
    void shouldFetchActionMetadata() {
        final Map<String, Object> configuredProperties = new HashMap<>();
        configuredProperties.put("p1", "v1");

        final Map<String, List<PropertyPair>> properties = new HashMap<>();
        properties.put("prop1", Arrays.asList(new PropertyPair("val1", "Value 1"), new PropertyPair("val2", "Value 2")));

        final SyndesisMetadata expected = new SyndesisMetadata(properties, new DataShape.Builder().kind(DataShapeKinds.JAVA).build(),
            new DataShape.Builder().kind(DataShapeKinds.JSON_INSTANCE).build());
        when(adapter.fetch(any(CamelContext.class), eq("test-connector"), eq("action1"), eq(configuredProperties))).thenReturn(expected);

        final SyndesisMetadata metadata = api.postForObject("/api/v1/connectors/test-connector/actions/action1", configuredProperties,
            SyndesisMetadata.class);

        assertThat(metadata).isEqualToComparingFieldByField(expected);
    }

    @Test
    void shouldFetchConnectorPropertiesMetadata() {
        final Map<String, Object> configuredProperties = new HashMap<>();
        configuredProperties.put("p1", "v1");

        final Map<String, List<PropertyPair>> properties = new HashMap<>();
        properties.put("prop1", Arrays.asList(new PropertyPair("val1", "Value 1"), new PropertyPair("val2", "Value 2")));

        final SyndesisMetadata expected = new SyndesisMetadata(properties, new DataShape.Builder().kind(DataShapeKinds.JAVA).build(),
            new DataShape.Builder().kind(DataShapeKinds.JSON_INSTANCE).build());
        when(adapter.fetchProperties(any(CamelContext.class), eq("test-connector"), eq(configuredProperties))).thenReturn(expected);

        final SyndesisMetadata metadata = api.postForObject("/api/v1/connectors/test-connector/properties/meta", configuredProperties,
            SyndesisMetadata.class);

        assertThat(metadata).isEqualToComparingFieldByField(expected);
    }

}
