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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.syndesis.common.util.IndexedProperty;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.WithKind;
import io.syndesis.common.model.WithModificationTimestamps;
import io.syndesis.common.model.WithVersion;

@IndexedProperty.Multiple({
        @IndexedProperty("integrationId"),
        @IndexedProperty("currentState")
})
@Value.Immutable
@JsonDeserialize(builder = IntegrationDeployment.Builder.class)
@SuppressWarnings("immutables")
public interface IntegrationDeployment extends WithVersion, WithModificationTimestamps, WithKind, WithId<IntegrationDeployment> {

    String COMPOSITE_ID_SEPARATOR = ":";

    static String compositeId(String integrationId, int version) {
        return integrationId + COMPOSITE_ID_SEPARATOR + version;
    }

    @Override
    default Kind getKind() {
        return Kind.IntegrationDeployment;
    }

    Optional<String> getUserId();

    @Value.Default
    default IntegrationDeploymentState getCurrentState() {
        return IntegrationDeploymentState.Pending;
    }

    @Value.Default
    default IntegrationDeploymentState getTargetState() {
        return IntegrationDeploymentState.Published;
    }

    @Value.Default
    default Map<String, String> getStepsDone() {
        return Collections.emptyMap();
    }

    @Value.Default
    default Optional<String> getIntegrationId() {
        return getSpec().getId();
    }

    Optional<String> getStatusMessage();

    Integration getSpec();

    class Builder extends ImmutableIntegrationDeployment.Builder {
        // allow access to ImmutableIntegrationDeployment.Builder
    }

    default Builder builder() {
        return new Builder().createFrom(this);
    }

    default IntegrationDeployment withCurrentState(IntegrationDeploymentState state) {
        return builder().currentState(state).build();
    }

    default IntegrationDeployment withTargetState(IntegrationDeploymentState state) {
        return builder().targetState(state).build();
    }

    default IntegrationDeployment unpublishing() {
        Map<String, String> stepsDone = new HashMap<>(getStepsDone());
        stepsDone.remove("deploy");
        return builder().targetState(IntegrationDeploymentState.Unpublished).stepsDone(stepsDone).build();
    }

}
