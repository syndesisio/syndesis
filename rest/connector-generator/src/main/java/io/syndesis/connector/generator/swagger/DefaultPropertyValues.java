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
package io.syndesis.connector.generator.swagger;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.swagger.models.Swagger;

enum DefaultPropertyValues {

    host {
        @Override
        protected Function<Swagger, String> propertyValueExtractor() {
            return Swagger::getHost;
        }
    };

    private static final Set<String> KNOWN_PROPERTIES = Arrays.stream(values()).map(DefaultPropertyValues::name)
        .collect(Collectors.toSet());

    protected abstract Function<Swagger, String> propertyValueExtractor();

    static Optional<String> fetchValue(final String propertyName, final Swagger swagger) {
        if (!KNOWN_PROPERTIES.contains(propertyName)) {
            return Optional.empty();
        }

        final DefaultPropertyValues defaultPropertyValue = DefaultPropertyValues.valueOf(propertyName);

        final String value = defaultPropertyValue.propertyValueExtractor().apply(swagger);

        return Optional.ofNullable(value);
    }

}
