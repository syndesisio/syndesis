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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import io.syndesis.common.model.Kind;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.WithKind;
import io.syndesis.common.model.WithResourceId;
import io.syndesis.common.util.IndexedProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@IndexedProperty.Multiple({
    @IndexedProperty("integrationId"),
    @IndexedProperty("currentState"),
    @IndexedProperty("targetState")
})
@Value.Immutable
@JsonDeserialize(builder = IntegrationDeployment.Builder.class)
@SuppressWarnings("immutables")
public interface IntegrationDeployment extends IntegrationDeploymentBase, WithId<IntegrationDeployment>, WithKind, WithResourceId  {

    String COMPOSITE_ID_SEPARATOR = ":";

    static String compositeId(String integrationId, int version) {
        return integrationId + COMPOSITE_ID_SEPARATOR + version;
    }

    @Override
    default Kind getKind() {
        return Kind.IntegrationDeployment;
    }

    @Value.Default
    default Optional<String> getIntegrationId() {
        return getSpec().getId();
    }

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

    default IntegrationDeployment unpublished() {
        Map<String, String> stepsDone = new HashMap<>(getStepsDone());
        stepsDone.remove("deploy");
        return builder().currentState(IntegrationDeploymentState.Unpublished).stepsDone(stepsDone).build();
    }

    default IntegrationDeployment unpublishing() {
        Map<String, String> stepsDone = new HashMap<>(getStepsDone());
        stepsDone.remove("deploy");
        return builder().targetState(IntegrationDeploymentState.Unpublished).stepsDone(stepsDone).build();
    }

    /**
     * @deprecated fully deleted from the data manager in 7.4+
     *      Retained for filtering in existing installations.
     */
    @Deprecated
    default IntegrationDeployment deleted() {
        final Integration integration = new Integration.Builder().createFrom(getSpec()).isDeleted(true).build();
        return builder().spec(integration).build();
    }

}
