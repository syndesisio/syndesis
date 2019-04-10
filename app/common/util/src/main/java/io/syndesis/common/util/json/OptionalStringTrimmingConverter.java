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
package io.syndesis.common.util.json;

import java.util.Optional;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

/**
 * Trims {@code Optional<String>}s and converts empty strings to {@code Optional.empty()}
 *
 * You can enable it on a {@code Optional<String>} field like:
 * <pre>
 *   @JsonDeserialize(converter = OptionalStringTrimmingConverter.class)
 *   Optional<String> value;
 * </pre>
 *
 */
public class OptionalStringTrimmingConverter implements Converter<String, Optional<String>> {
    @Override
    public Optional<String> convert(final String text) {
        if( text == null ) {
            return Optional.empty();
        }
        final String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(trimmed);
    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return typeFactory.constructType(String.class);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return typeFactory.constructParametricType(Optional.class, String.class);
    }
}
