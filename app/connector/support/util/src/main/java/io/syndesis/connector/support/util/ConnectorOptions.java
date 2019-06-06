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
package io.syndesis.connector.support.util;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class ConnectorOptions {

    /**
     * Gets the value mapped to the given key & converts to {@link String} if present,
     * or null otherwise.
     *
     * @param options
     * @param key
     * @return {@link String} object belonging to the given key in the options map
     */
    public static String extractOption(Map<String, Object> options, String key) {
        return Optional.ofNullable(options.get(key))
                        .map(Object::toString)
                        .orElse(null);
    }

    /**
     * Gets the value mapped to the given key, converts to {@link String} if present,
     * then applies the given mapping function to the {@link String} value. Either
     * returns the resulting mapped object or null.
     *
     * Note. mappingFn should be able to handle null values.
     *
     * @param options
     * @param key
     * @return Mapped object converted from value belonging to the given key in the options map
     */
    public static <T> T extractOptionAndMap(Map<String, Object> options, String key,
                                                                    Function<? super String, ? extends T> mappingFn) {
        return Optional.ofNullable(options.get(key))
                        .map(Object::toString)
                        .map(mappingFn)
                        .orElse(null);
    }

}
