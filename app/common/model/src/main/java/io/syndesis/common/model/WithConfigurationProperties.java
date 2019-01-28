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
package io.syndesis.common.model;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;

@FunctionalInterface
public interface WithConfigurationProperties {

    @SuppressFBWarnings("SE_BAD_FIELD")
    Map<String, ConfigurationProperty> getProperties();

    @JsonIgnore
    default boolean isEndpointProperty(Entry<String, String> e) {
        return this.getProperties() != null && this.getProperties().containsKey(e.getKey()) && !this.getProperties().get(e.getKey()).componentProperty();
    }

    @JsonIgnore
    default Predicate<Entry<String, String>> isEndpointProperty() {
        return e -> this.isEndpointProperty(e);
    }

    @JsonIgnore
    default boolean isComponentProperty(Entry<String, String> e) {
        return this.getProperties() != null && this.getProperties().containsKey(e.getKey()) && this.getProperties().get(e.getKey()).componentProperty();
    }

    @JsonIgnore
    default Predicate<Entry<String, String>> isComponentProperty() {
        return e -> this.isComponentProperty(e);
    }

    // ***************************
    // Secret
    // ***************************

    @JsonIgnore
    default boolean isSecret(String key) {
        return this.getProperties() != null && this.getProperties().containsKey(key) && this.getProperties().get(key).secret();
    }

    @JsonIgnore
    default boolean isSecret(Entry<String, String> e) {
        return this.isSecret(e.getKey());
    }

    @JsonIgnore
    default Predicate<Entry<String, String>> isSecret() {
        return e -> this.isSecret(e.getKey());
    }

    @JsonIgnore
    default boolean isSecretEndpointProperty(Entry<String, String> e) {
        return this.isEndpointProperty(e) && this.isSecret(e);
    }

    @JsonIgnore
    default Predicate<Entry<String, String>> isSecretEndpointProperty() {
        return e -> isSecretEndpointProperty(e);
    }

    @JsonIgnore
    default boolean isSecretComponentProperty(Entry<String, String> e) {
        return this.isComponentProperty(e) && this.isSecret(e);
    }

    @JsonIgnore
    default Predicate<Entry<String, String>> isSecretComponentProperty() {
        return e -> this.isSecretComponentProperty(e);
    }

    @JsonIgnore
    default boolean isSecretOrComponentProperty(Entry<String, String> e) {
        return this.isComponentProperty(e) || this.isSecret(e);
    }

    @JsonIgnore
    default Predicate<Entry<String, String>> isSecretOrComponentProperty() {
        return e -> isSecretOrComponentProperty(e);
    }

    // ***************************
    // Raw
    // ***************************

    @JsonIgnore
    default boolean isRaw(String key) {
        return this.getProperties() != null && this.getProperties().containsKey(key) && this.getProperties().get(key).raw();
    }

    @JsonIgnore
    default boolean isRaw(Entry<String, String> e) {
        return this.isRaw(e.getKey());
    }

    @JsonIgnore
    default Predicate<Entry<String, String>> isRaw() {
        return e -> this.isRaw(e.getKey());
    }

    @JsonIgnore
    default boolean isRawEndpointProperty(Entry<String, String> e) {
        return this.isEndpointProperty(e) && this.isRaw(e);
    }

    @JsonIgnore
    default Predicate<Entry<String, String>> isRawEndpointProperty() {
        return e -> isRawEndpointProperty(e);
    }

    @JsonIgnore
    default boolean isRawComponentProperty(Entry<String, String> e) {
        return this.isComponentProperty(e) && this.isRaw(e);
    }

    @JsonIgnore
    default Predicate<Entry<String, String>> isRawComponentProperty() {
        return e -> this.isRawComponentProperty(e);
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
    default Map<String, String> filterProperties(Map<String, String> properties, Predicate<Entry<String, String>> predicate,
                                                 Function<Entry<String, String>, String> keyConverter,
                                                 Function<Entry<String, String>, String> valueConverter) {
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
    default Map<String, String> filterProperties(Map<String, String> properties, Predicate<Entry<String, String>> predicate) {
        return filterProperties(properties, predicate, Map.Entry::getKey, Map.Entry::getValue);
    }

    default Map<String, String> filterEndpointProperties(WithConfiguredProperties withConfigurationProperties) {
        return filterProperties(withConfigurationProperties.getConfiguredProperties(), this::isEndpointProperty);
    }

    default Map<String, String> filterEndpointProperties(Map<String, String> properties) {
        return filterProperties(properties, this::isEndpointProperty);
    }

    default Map<String, String> filterComponentProperties(WithConfiguredProperties withConfigurationProperties) {
        return filterProperties(withConfigurationProperties.getConfiguredProperties(), this::isComponentProperty);
    }

    default Map<String, String> filterComponentProperties(Map<String, String> properties) {
        return filterProperties(properties, this::isComponentProperty);
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
     * @param valueConverter    A {@link Function} that is applies to each {@link Entry} of the configuration.
     * @return                  A map with just the sensitive data.
     */
    default Map<String, String> filterSecrets(Map<String, String> properties, Function<Entry<String, String>, String> valueConverter) {
        return properties.entrySet()
            .stream()
            .filter(isSecret())
            .collect(Collectors.toMap(e -> e.getKey(), e -> valueConverter.apply(e)));
    }

    /**
     * Returns first property entry tagged with the given tag.
     *
     * @param tag               The looked after tag
     * @return                  First property tagged with the supplied tag, if any
     */
    default Optional<Entry<String, ConfigurationProperty>> propertyEntryTaggedWith(final String tag) {
        return getProperties().entrySet().stream().filter(entry -> entry.getValue().getTags().contains(tag))
            .findFirst();
    }

    /**
     * Returns first property tagged with the given tag.
     *
     * @param properties        The properties
     * @param tag               The looked after tag
     * @return                  First property tagged with the supplied tag, if any
     */
    default Optional<String> propertyTaggedWith(final Map<String, String> properties, final String tag) {
        return propertyEntryTaggedWith(tag).map(entry -> properties.get(entry.getKey()));
    }

}
