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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.ToJson;
import io.syndesis.common.model.WithDependencies;
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
import io.syndesis.common.util.Optionals;
import io.syndesis.common.util.json.OptionalStringTrimmingConverter;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public interface IntegrationBase
    extends WithProperties, WithResourceId, WithVersion, WithModificationTimestamps, WithTags, WithName, WithSteps, ToJson, WithResources, WithDependencies {

    default Optional<Connection> findConnectionById(final String connectionId) {
        if (getConnections() == null) {
            return Optional.empty();
        }

        return getConnections().stream()
            .filter(WithId.class::isInstance)
            .filter(connection -> connection.getId().isPresent())
            .filter(connection -> connectionId.equals(connection.getId().get()))
            .findFirst();
    }

    default Optional<Flow> findFlowBy(final Predicate<Flow> p) {
        return getFlows().stream()
            .filter(p)
            .findFirst();
    }

    default Optional<Flow> findFlowById(final String id) {
        if (getSteps() == null) {
            return Optional.empty();
        }

        return getFlows().stream()
            .filter(WithId.class::isInstance)
            .filter(flow -> flow.getId().isPresent())
            .filter(flow -> id.equals(flow.getId().get()))
            .findFirst();
    }

    @JsonIgnore
    default Set<String> getConnectionIds() {
        return Stream.concat(
            getConnections()
                .stream()
                .map(Connection::getId)
                .filter(Optional::isPresent)
                .map(Optional::get),

            getFlows().stream()
                .flatMap(f -> f.getConnectionIds().stream()))

            .collect(Collectors.toSet());
    }

    @Value.Default
    default List<Connection> getConnections() {
        return Collections.emptyList();
    }

    /**
     * Map of target environment ids and continuous delivery states. Names are
     * created/deleted on the fly in the UI (since it's just a string). Managed
     * by release tag service and used by CD export and import service.
     *
     * @return
     */
    @Value.Default
    default Map<String, ContinuousDeliveryEnvironment> getContinuousDeliveryState() {
        return Collections.emptyMap();
    }

    @JsonDeserialize(converter = OptionalStringTrimmingConverter.class)
    Optional<String> getDescription();

    @JsonDeserialize(converter = OptionalStringTrimmingConverter.class)
    Optional<String> getExposure();


    @JsonIgnore
    default Set<String> getExtensionIds() {
        Set<String> extensionIds = new HashSet<>();
        extensionIds.addAll(
            getDependencies().stream()
                .filter(Dependency::isExtensionTag)
                .map(Dependency::getId)
                .collect(Collectors.toSet())
        );
        extensionIds.addAll(
            getFlows().stream()
                .flatMap(f-> f.getDependencies().stream()
                    .filter(Dependency::isExtensionTag)
                    .map(Dependency::getId)
                )
                .collect(Collectors.toSet())
        );
        extensionIds.addAll(
            getFlows().stream()
                .flatMap(f -> f.getExtensionIds().stream())
                .collect(Collectors.toSet())
        );
        return extensionIds;
    }

    @Value.Default
    default List<Flow> getFlows() {
        return Collections.emptyList();
    }

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
                .map(ConnectorDescriptor::getConnectorId))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    @JsonIgnore
    default boolean isExposable() {
        return getFlows().stream().flatMap(f -> f.getSteps().stream())
            .flatMap(step -> Optionals.asStream(step.getAction()))
            .flatMap(action -> action.getTags().stream())
            .anyMatch("expose"::equals);
    }

}
