/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.model.connection;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import static io.syndesis.model.connection.ActionPropertySuggestions.ActionPropertySuggestion.Builder.of;

import static org.assertj.core.api.Assertions.assertThat;

public class ActionPropertySuggestionsTest {

    private final static JsonNode JSON = parse("/action-property-suggestions.json");

    private final static ObjectMapper MAPPER = new ObjectMapper();

    private final static ActionPropertySuggestions SUGGESTIONS = new ActionPropertySuggestions.Builder()
        .putValue("property1", of("1value1", "1displayValue1"), of("1value2", "1displayValue2"))
        .putValue("property2", of("2value1", "2displayValue1"), of("2value2", "2displayValue2")).build();

    @Test
    public void shouldDeserialize() throws IOException {
        final ActionPropertySuggestions deserialized = MAPPER.readerFor(ActionPropertySuggestions.class)
            .<ActionPropertySuggestions>readValue(JSON);
        assertThat(deserialized).isEqualTo(SUGGESTIONS);
    }

    @Test
    public void shouldSerialize() {
        final JsonNode serialized = MAPPER.valueToTree(SUGGESTIONS);
        assertThat(serialized).isEqualTo(JSON);
    }

    private static JsonNode parse(final String path) {
        try {
            return new ObjectMapper().readTree(ActionPropertySuggestionsTest.class.getResourceAsStream(path));
        } catch (final IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
