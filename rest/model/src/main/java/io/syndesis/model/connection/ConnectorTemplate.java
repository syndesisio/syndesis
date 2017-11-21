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
package io.syndesis.model.connection;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.syndesis.model.Kind;
import io.syndesis.model.WithId;
import io.syndesis.model.WithName;
import io.syndesis.model.WithProperties;

import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = ConnectorTemplate.Builder.class)
public interface ConnectorTemplate extends WithId<ConnectorTemplate>, WithName, WithProperties, Serializable {

    class Builder extends ImmutableConnectorTemplate.Builder implements WithPropertiesBuilder<Builder> {
        // make ImmutableConnectorTemplate.Builder accessible
    }

    default Builder builder() {
        return new Builder().createFrom(this);
    }

    String getCamelConnectorGAV();

    String getCamelConnectorPrefix();

    Optional<ConnectorGroup> getConnectorGroup();

    Map<String, ConfigurationProperty> getConnectorProperties();

    String getDescription();

    String getIcon();

    @Override
    @Value.Default
    default Kind getKind() {
        return Kind.ConnectorTemplate;
    }

    @Override
    default ConnectorTemplate withId(final String id) {
        return builder().id(id).build();
    }

}
