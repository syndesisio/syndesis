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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import io.syndesis.common.model.WithName;
import io.syndesis.common.model.WithProperties;
import io.syndesis.common.util.IOStreams;
import io.syndesis.common.util.Strings;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Value.Immutable
@JsonDeserialize(builder = ConnectorSettings.Builder.class)
@SuppressWarnings("immutables")
public interface ConnectorSettings extends WithName, WithProperties {

    class Builder extends ImmutableConnectorSettings.Builder {
        // make ImmutableConnectorSettings accessible

        public final ConnectorSettings.Builder specification(InputStream specification) {
            return specification(Optional.ofNullable(specification));
        }
    }

    String getConnectorTemplateId();

    String getDescription();

    String getIcon();

    /**
     * This property will either be backed by an InputStream set via the
     * {@link Builder} or it will try to read from the specification property in
     * the configured properties. Two cases are supported, either the
     * specification property contains an URL or the specification holds the
     * specification itself.
     */
    @SuppressWarnings("resource") // we're returning a stream
    @JsonIgnore
    @Value.Default
    default Optional<InputStream> getSpecification() {
        final String configuredSpecification = getConfiguredProperties().get("specification");

        if (Strings.isEmptyOrBlank(configuredSpecification)) {
            return Optional.empty();
        }

        return Optional.of(determineSpecificationStream(configuredSpecification));
    }

    static InputStream determineSpecificationStream(final String configuredSpecification) {
        if (configuredSpecification.startsWith("http")) {
            try {
                URI uri = URI.create(configuredSpecification);

                URL url = uri.toURL();

                try (InputStream in = url.openStream()) {
                    return IOStreams.fullyBuffer(in);
                }
            } catch (@SuppressWarnings("unused") IllegalArgumentException | MalformedURLException ignore) {
                // it's not a valid URL
                return streamSpecificationContent(configuredSpecification);
            } catch (IOException e) {
                // looks like a valid URL, but can't be fetched
                throw new UncheckedIOException(e);
            }
        }

        return streamSpecificationContent(configuredSpecification);
    }

    static ByteArrayInputStream streamSpecificationContent(final String configuredSpecification) {
        return new ByteArrayInputStream(configuredSpecification.getBytes(StandardCharsets.UTF_8));
    }

}
