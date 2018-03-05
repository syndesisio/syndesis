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

import java.util.HashMap;
import java.util.Map;

import io.syndesis.common.model.WithProperties;

public interface WithPropertiesBuilder<T extends WithPropertiesBuilder<T>> {

    WithProperties build();

    /**
     * Either sets or removes the property depending on if the given value is
     * {@code null}.
     *
     * @param properties The properties
     * @param key Property key
     * @param value The value to set the property to, if null property will be
     *            removed
     * @return The previous value of the property, if any
     */
    default String putOrRemoveProperty(final Map<String, String> properties, final String key, final String value) {
        if (value == null) {
            return properties.remove(key);
        }

        return properties.put(key, value);
    }

    /**
     * Sets property tagged with the given tag to the supplied value
     *
     * @param properties The properties
     * @param tag The looked after tag
     * @param value The value to set the property to
     * @return The previous value of the property, if any
     */
    default T putOrRemoveConfiguredPropertyTaggedWith(final WithPropertiesBuilder<T> builder, final String tag,
        final String value) {
        final WithProperties withProperties = builder.build();
        final Map<String, String> configuredProperties = withProperties.getConfiguredProperties();

        final Map<String, String> mutableCopy = new HashMap<>(configuredProperties);

        withProperties.propertyEntryTaggedWith(tag)
            .map(entry -> putOrRemoveProperty(mutableCopy, entry.getKey(), value));

        return builder.configuredProperties(mutableCopy);
    }

    T configuredProperties(Map<String, ? extends String> properties);
}
