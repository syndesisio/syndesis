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
package io.syndesis.common.model.connection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import io.syndesis.common.util.json.JsonUtils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConnectorSettingsTest {

    @Test
    public void shouldLoadSpecificationProvidedStream() throws IOException {
        try (InputStream given = new ByteArrayInputStream("expected".getBytes(StandardCharsets.US_ASCII))) {
            final ConnectorSettings settings = new ConnectorSettings.Builder()
                .specification(given)
                .build();

            try (InputStream specification = settings.getSpecification().get()) {
                assertThat(specification).hasContent("expected");
            }
        }
    }

    @Test
    public void shouldLoadSpecificationStreamFromConfiguredProperties() throws IOException {
        final ConnectorSettings settings = new ConnectorSettings.Builder()
            .putConfiguredProperty("specification", "expected")
            .build();

        try (InputStream specification = settings.getSpecification().get()) {
            assertThat(specification).hasContent("expected");
        }
    }

    @Test
    public void shouldNotDeserializeFromJson() throws IOException {
        final ConnectorSettings settings = JsonUtils.reader().readValue("{\"specification\": \"not expected\"}", ConnectorSettings.class);

        assertThat(settings.getSpecification()).isEmpty();
    }

    @Test
    public void shouldNotSerializeToJson() throws IOException {
        try (InputStream given = new ByteArrayInputStream("expected".getBytes(StandardCharsets.US_ASCII))) {
            final ConnectorSettings settings = new ConnectorSettings.Builder()
                .specification(given)
                .build();

            assertThat(JsonUtils.toString(settings)).isEqualTo("{}");
        }
    }

    @Test
    public void shouldThrowExceptionWhenNoSpecificationGivenAndRequested() {
        assertThat(new ConnectorSettings.Builder().build().getSpecification()).isEmpty();
    }
}
