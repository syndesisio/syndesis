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
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConnectorOptions {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectorOptions.class);

    /**
     * Gets the value mapped to the given key,
     * converts to {@link String} & returns or defaultValue otherwise.
     *
     * Note. Don't use this if the value is not expected to a {@link String}
     *
     * @param options
     * @param key
     * @param defaultValue
     * @return {@link String} object belonging to the given key in the options map
     */
    public static String extractOption(Map<String, ?> options, String key, String defaultValue) {
        if (options == null) {
            return defaultValue;
        }

        return Optional.ofNullable(options.get(key))
            .map(Object::toString)
            .filter(v -> v.length() > 0)
            .orElse(defaultValue);
    }

    /**
     * Gets the value mapped to the given key & converts to {@link String} if present,
     * or null otherwise.
     *
     * @param options
     * @param key
     * @return {@link String} object belonging to the given key in the options map
     */
    public static String extractOption(Map<String, ?> options, String key) {
        return extractOption(options, key, null);
    }

    /**
     * Gets & removes the value mapped to the given key & converts to {@link String} if present,
     * or null otherwise.
     *
     * @param options
     * @param key
     * @return {@link String} object that used to belong to the given key in the options map
     */
    public static String popOption(Map<String, ?> options, String key) {
        if (options == null) {
            return null;
        }

        return Optional.ofNullable(options.remove(key))
            .map(Object::toString)
            .filter(v -> v.length() > 0)
            .orElse(null);
    }

    /**
     * Gets the value mapped to the given key, converts to {@link String} if present,
     * then applies the given mapping function to the {@link String} value. Either
     * returns the resulting mapped object or null.
     *
     * Note. This will only work for values that can be converted to strings so Strings and primitives.
     *            The mappingFn should be able to handle null values.
     *
     * @param options
     * @param key
     * @return Mapped object converted from value belonging to the given key in the options map
     */
    public static <T> T extractOptionAndMap(Map<String, ?> options, String key,
                                                                    Function<? super String, T> mappingFn, T defaultValue) {
        if (options == null) {
            return defaultValue;
        }

        try {
            return Optional.ofNullable(options.get(key))
                        .map(Object::toString)
                        .filter(v -> v.length() > 0)
                        .map(mappingFn)
                        .orElse(defaultValue);
        } catch (Exception ex) {
            LOG.error("Evaluation of option '" + key + "' failed. Returning default value.", ex);
            return defaultValue;
        }
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
     * @throws any exception that may result from the mapping function
     */
    public static <T> T extractOptionAndMap(Map<String, ?> options, String key,
                                                                    Function<? super String, T> mappingFn) throws IllegalArgumentException {
        if (options == null) {
            return null;
        }

        try {
            return Optional.ofNullable(options.get(key))
                        .map(Object::toString)
                        .filter(v -> v.length() > 0)
                        .map(mappingFn)
                        .orElse(null);
        } catch (Exception ex) {
            LOG.error("Evaluation of option '" + key + "' failed.", ex);
            throw new IllegalArgumentException(ex);
        }
    }

    public static void extractOptionAndConsume(Map<String, ?> options, String key,
                                                                   Consumer<String> consumer) {
        if (options == null) {
            return;
        }

        try {
            Optional.ofNullable(options.get(key))
                .map(Object::toString)
                .ifPresent(consumer);
        } catch (Exception ex) {
            LOG.error("Evaluation of option '" + key + "' failed.", ex);
        }
    }

    /**
     * Gets the value mapped to the given key, checks it is the required class
     * and returns. Otherwise return defaultValue.
     *
     * @param options
     * @param key
     * @param requiredClass
     * @param defaultValue
     *
     * @return value from options
     */
    public static <T> T extractOptionAsType(Map<String, ?> options,
                                                            String key, Class<T> requiredClass, T defaultValue) {
        if (options == null) {
            return defaultValue;
        }

        return Optional.ofNullable(options.get(key))
            .filter(requiredClass::isInstance)
            .map(requiredClass::cast)
            .orElse(defaultValue);
    }

    /**
     * Gets the value mapped to the given key, checks it is the required class
     * and returns. Otherwise return null.
     *
     * @param options
     * @param key
     * @param requiredClass
     * @return value from options
     */
    public static <T> T extractOptionAsType(Map<String, ?> options,
                                                            String key, Class<T> requiredClass) {
        return extractOptionAsType(options, key, requiredClass, null);
    }
}
