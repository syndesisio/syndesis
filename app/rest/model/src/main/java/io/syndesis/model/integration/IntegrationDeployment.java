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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.core.SuppressFBWarnings;
import io.syndesis.model.Kind;
import io.syndesis.model.WithId;
import io.syndesis.model.WithKind;
import io.syndesis.model.WithName;
import io.syndesis.model.action.ConnectorAction;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@JsonDeserialize(builder = IntegrationDeployment.Builder.class)
public class IntegrationDeployment implements WithKind, WithId<IntegrationDeployment>, WithName {

    public static final String COMPOSITE_ID_SEPARATOR = ":";

    private final Optional<String> id;
    private final Optional<Integer> version;
    private final Optional<String> integrationId;

    private final String name;
    private final Integer parentVersion;

    private final IntegrationDeploymentSpec spec;
    private final List<String> stepsDone;

    private final IntegrationDeploymentState currentState;
    private final IntegrationDeploymentState targetState;

    private final BigInteger timesUsed;
    private final Date lastUpdated;
    private final Date createdDate;

    public IntegrationDeployment() {
        this(Optional.empty(), Optional.of(1), null, null, null, Collections.emptyList(), null, null, null, null, null);
    }

    public IntegrationDeployment(Optional<String> integrationId, Optional<Integer> version, String name, Integer parentVersion, IntegrationDeploymentSpec spec, List<String> stepsDone, IntegrationDeploymentState currentState, IntegrationDeploymentState targetState, BigInteger timesUsed, Date lastUpdated, Date createdDate) {
        this.id = Optional.of(compositeId(integrationId, version));
        this.integrationId = integrationId;
        this.version = version;
        this.name = name;
        this.parentVersion = parentVersion != null ? parentVersion : 0;
        this.spec = spec;
        this.stepsDone = stepsDone;
        this.currentState = currentState;
        this.targetState = targetState;
        this.timesUsed = timesUsed != null ? timesUsed : BigInteger.ZERO;
        this.lastUpdated = lastUpdated != null ? lastUpdated : new Date();
        this.createdDate = createdDate != null ? lastUpdated : new Date();
    }


    public static String compositeId(String integrationId, Integer version) {
        return compositeId(Optional.of(integrationId), Optional.of(version));
    }

    public static String compositeId(Optional<String> integrationId, Optional<Integer> version) {
        return integrationId.orElse("") + COMPOSITE_ID_SEPARATOR + version.orElse(1);
    }

    @Override
    @JsonProperty("kind")
    public Kind getKind() {
        return Kind.IntegrationDeployment;
    }

    @Override
    @JsonProperty("id")
    public Optional<String> getId() {
        return id;
    }

    @Override
    public IntegrationDeployment withId(String id) {
        throw new IllegalStateException("Specifying an id on IntegrationDeployment is not supported. Specify integrationId and version instead.");
    }

    @JsonProperty("integrationId")
    public Optional<String> getIntegrationId() {
        return integrationId;
    }

    @JsonProperty("version")
    public Optional<Integer> getVersion() {
        return version;
    }

    @JsonProperty("parentVersion")
    public Integer getParentVersion() {
        return parentVersion;
    }

    @Override
    @JsonProperty("name")
    public String getName() {
        return name;
    }


    @JsonProperty("spec")
    public IntegrationDeploymentSpec getSpec() {
        return spec;
    }

    @JsonProperty("stepsDone")
    public List<String> getStepsDone() {
        return stepsDone;
    }

    @JsonProperty("currentState")
    public IntegrationDeploymentState getCurrentState() {
        return currentState;
    }

    @JsonProperty("targetState")
    public IntegrationDeploymentState getTargetState() {
        return targetState;
    }

    @JsonProperty("timesUsed")
    public BigInteger getTimesUsed() {
        return timesUsed;
    }

    @JsonProperty("lastUpdated")
    @SuppressFBWarnings(value="EI_EXPOSE_REP")
    public Date getLastUpdated() {
        return lastUpdated;
    }

    @JsonProperty("createdDate")
    @SuppressFBWarnings(value="EI_EXPOSE_REP")
    public Date getCreatedDate() {
        return createdDate;
    }

    /**
     * Returns that {@link IntegrationDeploymentState}.
     * The state is either the `desired state` or `pending`.
     * @return true, if current state is matching with target, false otherwise.
     */
    @JsonIgnore
    public boolean isPending() {
        return getTargetState() != getCurrentState();
    }


    @JsonIgnore
    public boolean isInactive() {
        return getCurrentState() == IntegrationDeploymentState.Undeployed || getCurrentState() == IntegrationDeploymentState.Inactive
            ? true
            : false;
    }

    @JsonIgnore
    public Set<String> getUsedConnectorIds() {
        return getSpec().getSteps().stream()//
            .map(s -> s.getAction())//
            .filter(Optional::isPresent)//
            .map(Optional::get)//
            .filter(ConnectorAction.class::isInstance)//
            .map(ConnectorAction.class::cast)//
            .map(a -> a.getDescriptor().getConnectorId())//
            .filter(Objects::nonNull)//
            .collect(Collectors.toSet());
    }

    public IntegrationDeployment withCurrentState(IntegrationDeploymentState state) {
        return new IntegrationDeployment.Builder().createFrom(this).currentState(state).build();
    }

    public IntegrationDeployment withTargetState(IntegrationDeploymentState state) {
        return new IntegrationDeployment.Builder().createFrom(this).targetState(state).build();
    }


    public static IntegrationDeployment newDeployment(Integration integration) {
        return newDeployment(integration, Collections.emptyList());
    }

    public static IntegrationDeployment newDeployment(Integration integration, Collection<IntegrationDeployment> deployments) {
        BigInteger totalTimesUsed = integration.getTimesUsed().orElse(BigInteger.ZERO);
        BigInteger parentUses =  deployments
            .stream()
            .map(i -> i.getTimesUsed())
            .reduce( (n1,n2) -> n1.add(n2))
            .orElse(BigInteger.ZERO);

        return new IntegrationDeployment.Builder()
            .integrationId(integration.getId())
            .name(integration.getName())
            .version(1)
            .parentVersion(0)
            .spec(new IntegrationDeploymentSpec.Builder()
                        .configuration(integration.getConfiguration())
                        .connections(integration.getConnections())
                        .steps(integration.getSteps())
                        .build())
            .currentState(integration.getCurrentStatus().orElse(IntegrationDeploymentState.Draft))
            .targetState(integration.getDesiredStatus().orElse(IntegrationDeploymentState.Draft))
            .timesUsed(Optional.of(totalTimesUsed.subtract(parentUses)))
            //We retain the information found on the integration and we override when needed, why?
            //Because this is not called just when we want to create a new revision,
            //but can be used when editing the current one.
            .createdDate(integration.getCreatedDate())
            .lastUpdated(integration.getLastUpdated())
            .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IntegrationDeployment that = (IntegrationDeployment) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static final class Builder {

        private Optional<Integer> version = Optional.of(1);
        private Optional<String> integrationId = Optional.empty();
        private String name;
        private Integer parentVersion = 0;
        private IntegrationDeploymentSpec spec = new IntegrationDeploymentSpec.Builder().build();
        private List<String> stepsDone = Collections.emptyList();
        private IntegrationDeploymentState currentState;
        private IntegrationDeploymentState targetState;
        private BigInteger timesUsed = BigInteger.ZERO;
        private Date lastUpdated = new Date();
        private Date createdDate = new Date();

        public Builder() {
        }

        public Builder createFrom(IntegrationDeployment revision) {
            return this
                .integrationId(revision.integrationId)
                .version(revision.version)
                .name(revision.name)
                .parentVersion(revision.parentVersion)
                .spec(revision.spec)
                .stepsDone(revision.stepsDone)
                .currentState(revision.currentState)
                .targetState(revision.targetState)
                .timesUsed(revision.timesUsed)
                .lastUpdated(revision.lastUpdated)
                .createdDate(revision.createdDate);

        }

        @JsonProperty("integrationId")
        public Builder integrationId(String integrationId) {
            this.integrationId = Optional.of(integrationId);
            return this;
        }

        @JsonProperty("version")
        public Builder version(Integer version) {
            this.version = Optional.ofNullable(version);
            return this;
        }

        @JsonProperty("name")
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        @JsonProperty("parentVersion")
        public Builder parentVersion(Integer parentVersion) {
            this.parentVersion = parentVersion;
            return this;
        }

        @JsonProperty("spec")
        public Builder spec(IntegrationDeploymentSpec spec) {
            this.spec = spec;
            return this;
        }

        @JsonProperty("stepsDone")
        public Builder stepsDone(List<String> stepsDone) {
            this.stepsDone = stepsDone;
            return this;
        }

        @JsonProperty("currentState")
        public Builder currentState(IntegrationDeploymentState currentState) {
            this.currentState = currentState;
            return this;
        }

        @JsonProperty("targetState")
        public Builder targetState(IntegrationDeploymentState targetState) {
            this.targetState = targetState;
            return this;
        }

        @JsonProperty("timesUsed")
        public Builder timesUsed(BigInteger timesUsed) {
            this.timesUsed = timesUsed;
            return this;
        }

        @SuppressFBWarnings(value="EI_EXPOSE_REP")
        @JsonProperty("lastUpdated")
        public Builder lastUpdated(Date lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        @SuppressFBWarnings(value="EI_EXPOSE_REP")
        @JsonProperty("createdDate")
        public Builder createdDate(Date createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public IntegrationDeployment build() {
            return new IntegrationDeployment(integrationId, version, name, parentVersion, spec, stepsDone, currentState, targetState, timesUsed, lastUpdated, createdDate);
        }

        //Getters for Jackson



        public Optional<Integer> getVersion() {
            return version;
        }


        public Optional<String> getIntegrationId() {
            return integrationId;
        }


        public String getName() {
            return name;
        }


        public Integer getParentVersion() {
            return parentVersion;
        }


        public IntegrationDeploymentSpec getSpec() {
            return spec;
        }


        public List<String> getStepsDone() {
            return stepsDone;
        }


        public IntegrationDeploymentState getCurrentState() {
            return currentState;
        }


        public IntegrationDeploymentState getTargetState() {
            return targetState;
        }


        public BigInteger getTimesUsed() {
            return timesUsed;
        }

        @SuppressFBWarnings(value="EI_EXPOSE_REP")
        public Date getLastUpdated() {
            return lastUpdated;
        }

        @SuppressFBWarnings(value="EI_EXPOSE_REP")
        public Date getCreatedDate() {
            return createdDate;
        }

        //For compatibility
        public Builder integrationId(Optional<String> integrationId) {
            this.integrationId = integrationId;
            return this;
        }

        public Builder version(Optional<Integer> version) {
            this.version = version;
            return this;
        }

        public Builder timesUsed(Optional<BigInteger> timesUsed) {
            this.timesUsed = timesUsed.orElse(BigInteger.ZERO);
            return this;
        }

        public Builder createdDate(Optional<Date> createdDate) {
            this.createdDate = createdDate.orElse(new Date());
            return this;
        }

        public Builder lastUpdated(Optional<Date> lastUpdated) {
            this.lastUpdated = lastUpdated.orElse(new Date());
            return this;
        }

    }
}
