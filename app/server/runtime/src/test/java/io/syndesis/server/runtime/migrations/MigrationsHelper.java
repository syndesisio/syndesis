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
package io.syndesis.server.runtime.migrations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.syndesis.common.util.Json;
import io.syndesis.server.jsondb.JsonDB;

final class MigrationsHelper {

    static <T> List<T> load(final JsonDB jsondb, final String path, final Class<T> type) throws IOException {
        final List<T> items = new ArrayList<>();
        final String raw = jsondb.getAsString(path);

        Json.reader().readTree(raw).fieldNames().forEachRemaining(id -> {
            try {
                final String itemRaw = jsondb.getAsString(path + "/" + id);
                final T item = Json.reader().forType(type).readValue(itemRaw);

                items.add(item);
            } catch (final IOException e) {
                throw new RuntimeException();
            }
        });

        return items;
    }

}
