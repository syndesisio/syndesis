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
package io.syndesis.model.connection;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DynamicActionMetadataTest {

    private final static JsonNode JSON = parse("/action-property-suggestions.json");

    private final static ObjectMapper MAPPER = new ObjectMapper();

    private final static JsonNode INPUT_SCHEMA = JSON.get("inputSchema");

    private final static JsonNode OUTPUT_SCHEMA = JSON.get("outputSchema");

    private final static DynamicActionMetadata SUGGESTIONS = new DynamicActionMetadata.Builder()
        .inputSchema(INPUT_SCHEMA).outputSchema(OUTPUT_SCHEMA)
        .putProperty("sObjectIdName",
            Arrays.asList(property("Contact ID", "Id"), property("Email", "Email"),
                property("Twitter Screen Name", "TwitterScreenName__c")))
        .putProperty("sObjectName", Arrays.asList(property("Contact", "Contact"))).build();

    @Test
    public void shouldDeserialize() throws IOException {
        final DynamicActionMetadata deserialized = MAPPER.readerFor(DynamicActionMetadata.class)
            .<DynamicActionMetadata>readValue(JSON);
        assertThat(deserialized).isEqualTo(SUGGESTIONS);
    }

    @Test
    public void shouldSerialize() {
        final JsonNode serialized = MAPPER.valueToTree(SUGGESTIONS);
        assertThat(serialized).isEqualTo(JSON);
    }

    private static JsonNode parse(final String path) {
        try {
            return new ObjectMapper().readTree(DynamicActionMetadataTest.class.getResourceAsStream(path));
        } catch (final IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static ImmutableActionPropertySuggestion property(final String displayValue, final String value) {
        return new DynamicActionMetadata.ActionPropertySuggestion.Builder().displayValue(displayValue).value(value)
            .build();
    }

}
