/*
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonDeserialize(builder = IntegrationRevision.Builder.class)
public interface IntegrationRevision {

    /**
     * The revision number. This is unique per {@link Integration}.
     * Once an {@IntegrationRevision} gets a version, it should not be mutated anymore.
     * @return
     */
    Optional<Integer> getVersion();

    /**
     * The version of the integration revision which was the origin of this revision.
     * @return 0 if this is the first revision of an integration, the parent version otherwise.
     */
    Integer getParentVersion();


    IntegrationRevisionSpec getSpec();

    /**
     * The desired state of the revision.
     * @return
     */
    IntegrationRevisionState getTargetState();

    /**
     * The current state of the revision.
     * @return
     */
    IntegrationRevisionState getCurrentState();

    /**
     * Message describing the currentState further (e.g. error message)
     * @return
     */
    Optional<String> getCurrentMessage();


    /**
     * Message which should become the currentMessage after reconciliation
     * @return
     */
    Optional<String> getTargetMessage();

    /**
     * Returns that {@link IntegrationRevisionState}.
     * The state is either the `desired state` or `pending`.
     * @return true, if current state is matching with target, false otherwise.
     */
    @JsonIgnore
    default boolean isPending() {
        return getTargetState() != getCurrentState();
    }

    default IntegrationRevision withVersion(Integer version) {
        return new IntegrationRevision.Builder().createFrom(this).version(version).build();
    }

    default IntegrationRevision withCurrentState(IntegrationRevisionState state) {
        return new IntegrationRevision.Builder().createFrom(this).currentState(state).build();
    }

    default IntegrationRevision withCurrentState(IntegrationRevisionState state, String message) {
        return new IntegrationRevision.Builder().createFrom(this)
            .currentState(state)
            .currentMessage(message)
            .build();
    }

    default IntegrationRevision withTargetState(IntegrationRevisionState state) {
        return new IntegrationRevision.Builder().createFrom(this).targetState(state).build();
    }

    default IntegrationRevision.Builder newIntegrationRevisionBuilder() {
        return new IntegrationRevision.Builder()
            .currentState(IntegrationRevisionState.Draft)
            .targetState(IntegrationRevisionState.Draft)
            .createFrom(this).version(Optional.empty())
            .parentVersion(this.getVersion().orElse(0));
    }

    class Builder extends ImmutableIntegrationRevision.Builder {
    }

}
