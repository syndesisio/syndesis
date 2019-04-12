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
package io.syndesis.connector.rest.swagger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

import org.apache.camel.CamelContext;

public class Configuration {

    private final Map<String, Object> configuration = new HashMap<>();

    private final CamelContext context;

    private final ComponentProxyCustomizer customizer;

    private final Map<String, Object> options;

    public Configuration(final ComponentProxyCustomizer customizer, final CamelContext context, final Map<String, Object> options) {
        this(Collections.emptyMap(), customizer, context, options);
    }

    public Configuration(final Map<String, Object> initial, final ComponentProxyCustomizer customizer, final CamelContext context,
        final Map<String, Object> options) {
        configuration.putAll(initial);
        this.customizer = customizer;
        this.context = context;
        this.options = options;
    }

    public boolean booleanOption(final String key) {
        final Boolean value = value(key, Boolean.class);

        if (value == null) {
            return false;
        }

        return value.booleanValue();
    }

    public long longOption(final String key) {
        final Long value = value(key, Long.class);

        if (value == null) {
            return Long.MIN_VALUE;
        }

        return value.longValue();
    }

    public String stringOption(final String key) {
        return value(key, String.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T value(final String key, final Class<T> type) {
        return (T) configuration.computeIfAbsent(key, k -> {
            final AtomicReference<T> value = new AtomicReference<>();
            customizer.consumeOption(context, options, key, type, value::set);

            return value.get();
        });
    }

}
