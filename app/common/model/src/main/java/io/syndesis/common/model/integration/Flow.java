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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.WithMetadata;
import io.syndesis.common.model.WithName;
import io.syndesis.common.model.WithTags;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.util.json.OptionalStringTrimmingConverter;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = Flow.Builder.class)
@SuppressWarnings("immutables")
public interface Flow extends WithName, WithId<Flow>, WithTags, WithSteps, WithMetadata, Serializable {

    class Builder extends ImmutableFlow.Builder {
        // allow access to ImmutableIntegration.Builder
    }

    enum FlowType {
        PRIMARY,
        API_PROVIDER,
        ALTERNATE;
    }

    @Value.Default
    default FlowType getType() {
        return FlowType.PRIMARY;
    }

    @Value.Default
    default List<Connection> getConnections() {
        return Collections.emptyList();
    }

    Optional<Scheduler> getScheduler();

    @JsonDeserialize(converter = OptionalStringTrimmingConverter.class)
    Optional<String> getDescription();

    default Flow.Builder builder() {
        return new Flow.Builder().createFrom(this);
    }

    default Optional<Step> findStepById(String stepId) {
        if (getSteps() == null) {
            return Optional.empty();
        }

        return getSteps().stream()
            .filter(WithId.class::isInstance)
            .filter(step -> step.getId().isPresent())
            .filter(step -> stepId.equals(step.getId().get()))
            .findFirst();
    }

    @JsonIgnore
    default Set<String> getConnectionIds() {
        return Stream.concat(
            getConnections().stream()
                .map(Connection::getId),

            getSteps().stream()
                .map(s -> s.getConnectionId()))

            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
    }

    @JsonIgnore
    default Set<String> getExtensionIds() {
        return getSteps().stream()
            .flatMap(s -> s.getExtensionIds().stream())
            .collect(Collectors.toSet());
    }

    @JsonIgnore
    default boolean isPrimary() {
        return FlowType.PRIMARY == getType();
    }

    @JsonIgnore
    default boolean isApiProvider() {
        return FlowType.API_PROVIDER == getType();
    }

    @JsonIgnore
    default boolean isAlternate() {
        return FlowType.ALTERNATE == getType();
    }
}
