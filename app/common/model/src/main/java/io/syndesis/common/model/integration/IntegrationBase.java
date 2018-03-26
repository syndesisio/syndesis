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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.common.model.ResourceIdentifier;
import io.syndesis.common.model.ToJson;
import io.syndesis.common.model.WithModificationTimestamps;
import io.syndesis.common.model.WithName;
import io.syndesis.common.model.WithResourceId;
import io.syndesis.common.model.WithTags;
import io.syndesis.common.model.WithVersion;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.util.json.OptionalStringTrimmingConverter;
import org.immutables.value.Value;

public interface IntegrationBase extends WithResourceId, WithVersion, WithModificationTimestamps, WithTags, WithName, ToJson, Serializable {

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

    Optional<Scheduler> getScheduler();

    @JsonDeserialize(converter = OptionalStringTrimmingConverter.class)
    Optional<String> getDescription();

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
