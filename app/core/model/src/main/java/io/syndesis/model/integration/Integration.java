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
package io.syndesis.model.integration;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.syndesis.core.json.OptionalStringTrimmingConverter;
import io.syndesis.model.Kind;
import io.syndesis.model.ResourceIdentifier;
import io.syndesis.model.WithId;
import io.syndesis.model.WithModificationTimestamps;
import io.syndesis.model.WithName;
import io.syndesis.model.WithTags;
import io.syndesis.model.WithVersion;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.validation.UniqueProperty;
import io.syndesis.model.validation.UniquenessRequired;

@Value.Immutable
@JsonDeserialize(builder = Integration.Builder.class)
@UniqueProperty(value = "name", groups = UniquenessRequired.class)
@SuppressWarnings("immutables")
public interface Integration extends WithId<Integration>, WithVersion, WithModificationTimestamps, WithTags, WithName, Serializable {

    @Override
    default Kind getKind() {
        return Kind.Integration;
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @Value.Default
    default boolean isDeleted() {
        return false;
    }

    @Value.Default
    default List<Connection> getConnections() {
        return Collections.emptyList();
    }

    @Value.Default
    default List<Step> getSteps() {
        return Collections.emptyList();
    }

    @Value.Default
    default List<ResourceIdentifier> getResources() {
        return Collections.emptyList();
    }

    @JsonDeserialize(converter = OptionalStringTrimmingConverter.class)
    Optional<String> getDescription();

    class Builder extends ImmutableIntegration.Builder {
        // allow access to ImmutableIntegration.Builder
    }

    @JsonIgnore
    default Set<String> getUsedConnectorIds() {
        return getSteps().stream()//
                .map(s -> s.getAction())//
                .filter(Optional::isPresent)//
                .map(Optional::get)//
                .filter(ConnectorAction.class::isInstance)//
                .map(ConnectorAction.class::cast)//
                .map(a -> a.getDescriptor().getConnectorId())//
                .filter(Objects::nonNull)//
                .collect(Collectors.toSet());
    }

}
