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
package io.syndesis.server.endpoint.v1.handler.setup;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.syndesis.common.model.WithId;
import io.syndesis.common.model.WithName;
import io.syndesis.common.model.WithProperties;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.credential.Credentials;

import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = OAuthApp.Builder.class)
@SuppressWarnings("immutables")
public interface OAuthApp extends WithId<OAuthApp>, WithName, WithProperties {

    String HOST_TAG = "host";

    /**
     * Set of tags that we expect the UI to show and the users to configure.
     */
    Set<String> OAUTH_TAGS = Collections.unmodifiableSet(new HashSet<>(
        Arrays.asList(Credentials.CLIENT_ID_TAG, Credentials.CLIENT_SECRET_TAG, Credentials.AUTHORIZATION_URL_TAG,
            Credentials.AUTHENTICATION_URL_TAG, Credentials.ACCESS_TOKEN_URL_TAG, Credentials.SCOPE_TAG, HOST_TAG)));

    class Builder extends ImmutableOAuthApp.Builder {

        Builder withTaggedPropertyFrom(final Connector connector, final String tag) {
            final Optional<Entry<String, ConfigurationProperty>> maybeProperty = connector.propertyEntryTaggedWith(tag);

            if (maybeProperty.isPresent()) {
                final Entry<String, ConfigurationProperty> property = maybeProperty.get();

                final ConfigurationProperty configuration = property.getValue();
                if ("hidden".equals(configuration.getType())) {
                    return this;
                }

                final String propertyName = property.getKey();
                putProperty(propertyName, configuration);

                final Optional<String> maybeValue = connector.propertyTaggedWith(tag);
                if (maybeValue.isPresent()) {
                    putConfiguredProperty(propertyName, maybeValue.get());
                }
            }

            return this;
        }

    }

    default OAuthApp clearValues() {
        final Map<String, String> current = getConfiguredProperties();
        final Map<String, String> replacement = new HashMap<>();

        for (final Entry<String, ConfigurationProperty> property : getProperties().entrySet()) {
            final ConfigurationProperty configurationProperty = property.getValue();
            final SortedSet<String> tags = configurationProperty.getTags();

            if (Collections.disjoint(tags, OAUTH_TAGS)) {
                final String propertyName = property.getKey();
                final String value = current.get(propertyName);
                if (value != null) {
                    replacement.put(propertyName, value);
                }
            }
        }

        return new Builder().createFrom(this).configuredProperties(replacement).build();
    }

    String getIcon();

    @Value.Derived
    default boolean isDerived() {
        getConfiguredProperties();
        final Optional<String> maybeClientId = propertyTaggedWith(Credentials.CLIENT_ID_TAG);
        final Optional<String> maybeClientSecret = propertyTaggedWith(Credentials.CLIENT_SECRET_TAG);

        return maybeClientId.isPresent() && maybeClientSecret.isPresent();
    }

    default Optional<String> propertyTaggedWith(final String tag) {
        return propertyTaggedWith(getConfiguredProperties(), tag);
    }

    default Connector update(final Connector connector) {
        final Connector.Builder updating = new Connector.Builder().createFrom(connector);

        final Map<String, String> configuredProperties = getConfiguredProperties();

        final Map<String, String> current = connector.getConfiguredProperties();
        final Map<String, String> updated = new HashMap<>(current);

        for (final String tag : OAUTH_TAGS) {
            final Optional<Entry<String, ConfigurationProperty>> maybeProperty = connector.propertyEntryTaggedWith(tag);

            if (maybeProperty.isPresent()) {
                final Entry<String, ConfigurationProperty> property = maybeProperty.get();
                final ConfigurationProperty propertyDefinition = property.getValue();
                final String propertyName = property.getKey();

                final String configuredValue = configuredProperties.get(propertyName);
                final String value;
                if (configuredValue == null && "hidden".equals(propertyDefinition.getType())) {
                    final ConfigurationProperty configuration = propertyDefinition;

                    final String currentValue = current.get(propertyName);
                    if (currentValue == null) {
                        value = configuration.getDefaultValue();
                    } else {
                        value = currentValue;
                    }
                } else {
                    value = configuredValue;
                }

                if (value == null) {
                    updated.remove(propertyName);
                } else {
                    updated.put(propertyName, value);
                }
            }
        }

        return updating.configuredProperties(updated).build();
    }

    static OAuthApp fromConnector(final Connector connector) {
        final Builder builder = new Builder()//
            .id(connector.getId())//
            .name(connector.getName())//
            .icon(connector.getIcon());

        for (final String tag : OAUTH_TAGS) {
            builder.withTaggedPropertyFrom(connector, tag);
        }

        return builder.build();
    }
}
