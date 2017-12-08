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
package io.syndesis.core.json;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class StringTrimmingJsonDeserializerTest {

    private static final DeserializationContext NOT_USED = null;

    private final JsonDeserializer<String> deserializer = new StringTrimmingJsonDeserializer();

    @Test
    public void shouldTrim() throws JsonProcessingException, IOException {
        assertThat(deserializer.deserialize(parserWithString(" a bc \\t"), NOT_USED)).isEqualTo("a bc");
    }

    @Test
    public void shouldTrimNullsToNull() throws JsonProcessingException, IOException {
        assertThat(deserializer.deserialize(parserWithString(null), NOT_USED)).isNull();
    }

    @Test
    public void shouldTrimToNull() throws JsonProcessingException, IOException {
        assertThat(deserializer.deserialize(parserWithString(" "), NOT_USED)).isNull();
        assertThat(deserializer.deserialize(parserWithString(""), NOT_USED)).isNull();
        assertThat(deserializer.deserialize(parserWithString(" \\t \\t"), NOT_USED)).isNull();
    }

    private JsonParser parserWithString(final String value) throws JsonParseException, IOException {
        final JsonParser parser = new ObjectMapper().getFactory()
            .createParser(Optional.ofNullable(value).map(v -> "[\"" + v + "\"]").orElse("[null]"));
        parser.nextToken();// array
        parser.nextToken();// string

        return parser;
    }
}
