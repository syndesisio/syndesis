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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.syndesis.model.connection.ActionPropertySuggestions.Deserializer;

import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(using = Deserializer.class)
public interface ActionPropertySuggestions {

    @Value.Immutable
    @JsonDeserialize(builder = ActionPropertySuggestion.Builder.class)
    interface ActionPropertySuggestion {

        @SuppressWarnings("PMD.UseUtilityClass")
        final class Builder extends ImmutableActionPropertySuggestion.Builder {

            public static ActionPropertySuggestion of(final String value, final String displayValue) {
                return new ActionPropertySuggestion.Builder().value(value).displayValue(displayValue).build();
            }

        }

        String displayValue();

        String value();

    }

    final class Builder extends ImmutableActionPropertySuggestions.Builder {

        /* default */ Builder putValue(final String property, final ActionPropertySuggestion suggestion1,
            final ActionPropertySuggestion... others) {
            final List<ActionPropertySuggestion> propertySuggestions = new ArrayList<>(1 + others.length);
            propertySuggestions.add(suggestion1);
            propertySuggestions.addAll(Arrays.asList(others));

            putValue(property, propertySuggestions);

            return this;
        }

    }

    final class Deserializer extends JsonDeserializer<ActionPropertySuggestions> {

        private static final TypeReference<Map<String, List<ActionPropertySuggestion>>> TYPE = new TypeReference<Map<String, List<ActionPropertySuggestion>>>() {
            // type token pattern
        };

        @Override
        public ActionPropertySuggestions deserialize(final JsonParser p, final DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
            final Map<String, List<ActionPropertySuggestion>> suggestions = p.readValueAs(TYPE);

            return new Builder().putAllValue(suggestions).build();
        }

    }

    @JsonValue
    Map<String, List<ActionPropertySuggestion>> value();

}
