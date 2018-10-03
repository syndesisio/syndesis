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
package io.syndesis.common.model.integration;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.syndesis.common.model.ToJson;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.WithModificationTimestamps;
import io.syndesis.common.model.WithName;
import io.syndesis.common.model.WithProperties;
import io.syndesis.common.model.WithResourceId;
import io.syndesis.common.model.WithResources;
import io.syndesis.common.model.WithTags;
import io.syndesis.common.model.WithVersion;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.util.json.OptionalStringTrimmingConverter;

import org.immutables.value.Value;

public interface IntegrationBase extends WithProperties, WithResourceId, WithVersion, WithModificationTimestamps, WithTags, WithName, WithSteps, ToJson, Serializable, WithResources {

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @Value.Default
    default boolean isDeleted() {
        return false;
    }

    /**
     * @deprecated steps have been superseded by flows
     */
    @Override
    @Deprecated
    @Value.Default
    default List<Step> getSteps() {
        return Collections.emptyList();
    }

    @Value.Default
    default List<Flow> getFlows() {
        return Collections.emptyList();
    }

    @Value.Default
    default List<Connection> getConnections() {
        return Collections.emptyList();
    }

    @JsonDeserialize(converter = OptionalStringTrimmingConverter.class)
    Optional<String> getDescription();

    @JsonIgnore
    default Set<String> getUsedConnectorIds() {
        return Stream.concat(
            getFlows().stream()
                .flatMap(f -> f.getSteps().stream())
                .map(Step::getConnection)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Connection::getConnectorId),

            getFlows().stream()
                .flatMap(f -> f.getSteps().stream())
                .map(Step::getAction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ConnectorAction.class::isInstance)
                .map(ConnectorAction.class::cast)
                .map(ConnectorAction::getDescriptor)
                .filter(Objects::nonNull)
                .map(ConnectorDescriptor::getConnectorId)
            )
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    }

    default Optional<Connection> findConnectionById(String connectionId) {
        if (getConnections() == null) {
            return Optional.empty();
        }

        return getConnections().stream()
            .filter(WithId.class::isInstance)
            .filter(connection -> connection.getId().isPresent())
            .filter(connection -> connectionId.equals(connection.getId().get()))
            .findFirst();
    }

    default Optional<Flow> findFlowById(String id) {
        if (getSteps() == null) {
            return Optional.empty();
        }

        return getFlows().stream()
            .filter(WithId.class::isInstance)
            .filter(flow -> flow.getId().isPresent())
            .filter(flow -> id.equals(flow.getId().get()))
            .findFirst();
    }
}
