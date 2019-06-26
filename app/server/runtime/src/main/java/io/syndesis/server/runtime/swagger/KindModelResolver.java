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
package io.syndesis.server.runtime.swagger;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.swagger.jackson.ModelResolver;
import io.swagger.models.properties.StringProperty;
import io.swagger.util.Json;
import io.syndesis.common.model.Kind;

/**
 * We're using {@link Kind#modelName} as value for the {@link Kind} enum values.
 * The OpenAPI document generation has no knowledge of that so this
 * {@link ModelResolver} sets {@code enum} values to the values of the
 * {@code modelName}.
 */
public final class KindModelResolver extends ModelResolver {

    private static final List<String> KINDS;

    static {
        KINDS = Stream.of(Kind.values())
            .map(k -> k.modelName)
            .collect(Collectors.toList());
    }

    public KindModelResolver() {
        super(Json.mapper());
    }

    @Override
    protected void _addEnumProps(final Class<?> propClass, final StringProperty property) {
        if (Kind.class.equals(propClass)) {
            property._enum(KINDS);
        } else {
            super._addEnumProps(propClass, property);
        }
    }
}
