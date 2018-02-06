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
package io.syndesis.connector.generator.swagger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

import io.syndesis.core.Json;

import com.fasterxml.jackson.databind.SerializationFeature;

public final class TestHelper {

    private TestHelper() {
        // utility class
    }

    public static String resource(final String path) throws IOException {
        final String resource;
        try (final InputStream in = requireNonNull(TestHelper.class.getResourceAsStream(path), path);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

            resource = reader.lines().collect(Collectors.joining("\n"));
        }
        return resource;
    }

    /* default */ static String reformatJson(final String json) throws IOException {
        if (json == null) {
            return null;
        }

        final Map<?, ?> tree = Json.reader().forType(Map.class).readValue(json);

        return Json.copyObjectMapperConfiguration().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true).writerWithDefaultPrettyPrinter()
            .writeValueAsString(tree);
    }

    /* default */ static String resource(final String path, final String alternative) throws IOException {
        if (TestHelper.class.getResource(path) != null) {
            return resource(path);
        }

        return resource(alternative);
    }

}
