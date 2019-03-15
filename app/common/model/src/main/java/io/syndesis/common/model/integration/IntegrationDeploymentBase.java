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
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.syndesis.common.model.WithModificationTimestamps;
import io.syndesis.common.model.WithResourceId;
import io.syndesis.common.model.WithVersion;
import org.immutables.value.Value;

public interface IntegrationDeploymentBase extends WithResourceId, WithVersion, WithModificationTimestamps {

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

    Optional<String> getStatusMessage();

    IntegrationDeploymentError getError();

    @JsonIgnore
    default boolean hasError() {
        return getError() != null;
    }
}
