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

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

public interface WithDynamicProperties {

    @Value.Immutable
    @JsonDeserialize(builder = ActionPropertySuggestion.Builder.class)
    interface ActionPropertySuggestion {

        final class Builder extends ImmutableActionPropertySuggestion.Builder {
            public static ActionPropertySuggestion of(final String value, final String displayValue) {
                return new ActionPropertySuggestion.Builder().value(value).displayValue(displayValue).build();
            }
        }

        String displayValue();

        String value();
    }

    Map<String, List<ActionPropertySuggestion>> properties();
}
