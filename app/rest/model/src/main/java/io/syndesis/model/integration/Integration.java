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
package io.syndesis.model.integration;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.model.Kind;
import io.syndesis.model.WithId;
import io.syndesis.model.WithName;
import io.syndesis.model.WithTags;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.user.User;
import io.syndesis.model.validation.UniqueProperty;
import io.syndesis.model.validation.UniquenessRequired;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = Integration.Builder.class)
@UniqueProperty(value = "name", groups = UniquenessRequired.class)
@SuppressWarnings("immutables")
public interface Integration extends WithId<Integration>, WithTags, WithName, Serializable {

    enum Status {
        Draft, Pending, Activated, Deactivated, Deleted
    }

    @Override
    default Kind getKind() {
        return Kind.Integration;
    }

    /**
     * The list of versioned revisions.
     * The items in this list should be versioned and are not meant to be mutated.
     * @return
     */
    Set<IntegrationRevision> getRevisions();

    Optional<IntegrationRevision> getDraftRevision();

    Optional<Integer> getDeployedRevisionId();

    @JsonIgnore
    default Optional<IntegrationRevision> getDeployedRevision() {
        return getDeployedRevisionId().map(i -> getRevisions()
            .stream()
            .filter(r -> r.getVersion().isPresent() && i.equals(r.getVersion().get()))
            .findFirst()
            .orElse(null));
    }

    Optional<String> getConfiguration();

    Optional<String> getIntegrationTemplateId();

    Optional<String> getUserId();

    List<User> getUsers();

    @Value.Default
    default List<Connection> getConnections() {
        return Collections.emptyList();
    }

    @Value.Default
    default List<? extends Step> getSteps() {
        return Collections.emptyList();
    }

    Optional<String> getDescription();

    Optional<Status> getDesiredStatus();

    Optional<Status> getCurrentStatus();

    @Value.Default
    default List<String> getStepsDone() {
        return Collections.emptyList();
    }

    Optional<String> getStatusMessage();

    Optional<Date> getLastUpdated();

    Optional<Date> getCreatedDate();

    Optional<BigInteger> getTimesUsed();

    @JsonIgnore
    default IntegrationRevisionState getStatus() {
        Optional<IntegrationRevision> deployedRevision = getDeployedRevision();

        return deployedRevision.map(r -> r.getCurrentState()).orElse(IntegrationRevisionState.Pending);
    }

    default IntegrationRevision lastRevision() {
        return getRevisions().stream().max(Comparator.comparingInt(r -> r.getVersion().orElse(0))).get();
    }

    class Builder extends ImmutableIntegration.Builder {
        // allow access to ImmutableIntegration.Builder
    }

}
