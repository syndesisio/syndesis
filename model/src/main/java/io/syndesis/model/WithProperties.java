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
package io.syndesis.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.Connector;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface WithProperties {

    Map<String, ConfigurationProperty> getProperties();

    @JsonIgnore
    default Predicate<Map.Entry<String, String>> isEndpointProperty() {
        return e -> this.getProperties() != null && this.getProperties().containsKey(e.getKey()) && Boolean.FALSE.equals(this.getProperties().get(e.getKey()).getComponentProperty());
    }

    @JsonIgnore
    default Predicate<Map.Entry<String, String>> isSecretEndpointProperty() {
        return isEndpointProperty().and(isSecret());
    }

    @JsonIgnore
    default Predicate<Map.Entry<String, String>> isComponentProperty() {
        return e -> this.getProperties() != null && this.getProperties().containsKey(e.getKey()) && Boolean.TRUE.equals(this.getProperties().get(e.getKey()).getComponentProperty());
    }

    @JsonIgnore
    default Predicate<Map.Entry<String, String>> isSecretComponentProperty() {
        return isComponentProperty().and(isSecret());
    }

    @JsonIgnore
    default Predicate<Map.Entry<String, String>> isSecret() {
        return e -> this.getProperties() != null && this.getProperties().containsKey(e.getKey()) && Boolean.TRUE.equals(this.getProperties().get(e.getKey()).getSecret());

    }

    /**
     * Filters the specified properties, using the specified {@link Predicate} and value converter {@link Function}.
     *
     * @param properties        The configuration.
     * @param predicate         The {@link Predicate}.
     * @param keyConverter    The {@link Function} to apply to keys.
     * @param valueConverter    The {@link Function} to apply to values.
     * @return                  A map with the properties that match the {@link Predicate}, and converted values.
     */
    default Map<String, String> filterProperties(Map<String, String> properties, Predicate<Map.Entry<String, String>> predicate,
                                                 Function<Map.Entry<String, String>, String> keyConverter,
                                                 Function<Map.Entry<String, String>, String> valueConverter) {
        return properties.entrySet()
            .stream()
            .filter(predicate)
            .collect(Collectors.toMap(keyConverter, valueConverter));
    }

    /**
     * Filters the specified properties, using the specified {@link Predicate} and value converter {@link Function}.
     *
     * @param properties        The configuration.
     * @param predicate         The {@link Predicate}.

     * @return                  A map with the properties that match the {@link Predicate}, and converted values.
     */
    default Map<String, String> filterProperties(Map<String, String> properties, Predicate<Map.Entry<String, String>> predicate) {
        return filterProperties(properties, predicate, e -> e.getKey(),  e -> e.getValue());
    }

    /**
     * Filters the properties that the {@link Connector} considers sensitive / secret.
     * @param properties        The specified configuration.
     * @return                  A map with just the sensitive data.
     */
    default Map<String, String> filterSecrets(Map<String, String> properties) {
        return filterSecrets(properties, e -> e.getValue());
    }

    /**
     * Filters the properties that the {@link Connector} considers sensitive / secret.
     * @param properties        The specified configuration.
     * @param valueConverter    A {@link Function} that is applies to each {@link Map.Entry} of the configuration.
     * @return                  A map with just the sensitive data.
     */
    default Map<String, String> filterSecrets(Map<String, String> properties, Function<Map.Entry<String, String>, String> valueConverter) {
        return properties.entrySet()
            .stream()
            .filter(isSecret())
            .collect(Collectors.toMap(e -> e.getKey(), e -> valueConverter.apply(e)));
    }
}
