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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import io.syndesis.common.model.WithName;
import io.syndesis.common.model.WithProperties;

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

    @JsonIgnore
    @Value.Default
    default Optional<InputStream> getSpecification() {
        final String configuredSpecification = getConfiguredProperties().get("specification");

        if (configuredSpecification != null) {
            return Optional.of(new ByteArrayInputStream(configuredSpecification.getBytes(StandardCharsets.UTF_8)));
        }

        return Optional.empty();
    }

}
