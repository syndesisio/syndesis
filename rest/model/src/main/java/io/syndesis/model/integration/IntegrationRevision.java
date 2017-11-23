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

import java.math.BigInteger;
import java.util.Date;
import java.util.Optional;

@Value.Immutable
@JsonDeserialize(builder = IntegrationRevision.Builder.class)
public abstract class IntegrationRevision {

    /**
     * The revision number. This is unique per {@link Integration}.
     * Once an {@IntegrationRevision} gets a version, it should not be mutated anymore.
     * @return
     */
    public abstract Optional<Integer> getVersion();

    /**
     * The version of the integration revision which was the origin of this revision.
     * @return 0 if this is the first revision of an integration, the parent version otherwise.
     */
    public abstract Integer getParentVersion();

    public abstract IntegrationRevisionSpec getSpec();

    /**
     * The desired state of the revision.
     * @return
     */
    public abstract IntegrationRevisionState getTargetState();

    /**
     * The current state of the revision.
     * @return
     */
     public abstract IntegrationRevisionState getCurrentState();

    /**
     * Message describing the currentState further (e.g. error message)
     * @return
     */
    public abstract Optional<String> getCurrentMessage();

    /**
     * Message which should become the currentMessage after reconciliation
     * @return
     */
    public abstract Optional<String> getTargetMessage();

    public abstract Optional<BigInteger> getTimesUsed();

    public abstract Optional<Date> getLastUpdated();

    public abstract Optional<Date> getCreatedDate();

    /**
     * Returns that {@link IntegrationRevisionState}.
     * The state is either the `desired state` or `pending`.
     * @return true, if current state is matching with target, false otherwise.
     */
    @JsonIgnore
    public boolean isPending() {
        return getTargetState() != getCurrentState();
    }

    public IntegrationRevision withCurrentState(IntegrationRevisionState state) {
        return new IntegrationRevision.Builder().createFrom(this).currentState(state).build();
    }

    public IntegrationRevision withTargetState(IntegrationRevisionState state) {
        return new IntegrationRevision.Builder().createFrom(this).targetState(state).build();
    }

    @Override
    public int hashCode() {
        return getVersion().orElse(1).hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof IntegrationRevision)) {
            return false;
        }
        IntegrationRevision revision = (IntegrationRevision) other;
        return getVersion().orElse(1).equals(revision.getVersion().orElse(1));
    }

    public static IntegrationRevision createNewRevision(Integration integration) {
        int version = integration.getRevisions()
            .stream()
            .map(i -> i.getVersion().orElse(0))
            .reduce(Integer::max)
            .orElse(0);

        BigInteger totalTimesUsed = integration.getTimesUsed().orElse(BigInteger.ZERO);
        BigInteger parentUses =  integration.getRevisions()
            .stream()
            .map(i -> i.getTimesUsed().orElse(BigInteger.ZERO))
            .reduce( (n1,n2) -> n1.add(n2))
            .orElse(BigInteger.ZERO);


        return new IntegrationRevision.Builder()
            .version(version + 1)
            .parentVersion(version)
            .spec(new IntegrationRevisionSpec.Builder()
                        .configuration(integration.getConfiguration())
                        .connections(integration.getConnections())
                        .steps(integration.getSteps())
                        .build())
            .currentState(IntegrationRevisionState.from(integration.getCurrentStatus().orElse(Integration.Status.Draft)))
            .currentMessage(integration.getStatusMessage())
            .targetState(IntegrationRevisionState.from(integration.getDesiredStatus().orElse(Integration.Status.Draft)))
            .timesUsed(Optional.of(totalTimesUsed.subtract(parentUses)))
            //We retain the information found on the integration and we override when needed, why?
            //Because this is not called just when we want to create a new revision,
            //but can be used when editing the current one.
            .createdDate(integration.getCreatedDate())
            .lastUpdated(integration.getLastUpdated())
            .build();
    }

    public static IntegrationRevision deployedRevision(Integration integration) {
        BigInteger totalTimesUsed = integration.getTimesUsed().orElse(BigInteger.ZERO);
        BigInteger parentUses =  integration.getRevisions()
            .stream()
            .map(i -> i.getTimesUsed().orElse(BigInteger.ZERO))
            .reduce( (n1,n2) -> n1.add(n2))
            .orElse(BigInteger.ZERO);


        return new IntegrationRevision.Builder()
            .createFrom(integration.getDeployedRevision().orElseGet(() -> IntegrationRevision.createNewRevision(integration)))
            .timesUsed(Optional.of(totalTimesUsed.subtract(parentUses)))
            .lastUpdated(new Date())
            .build();

    }

    public static class Builder extends ImmutableIntegrationRevision.Builder {
        // allow access to ImmutableIntegrationRevision.Builder
    }

}
