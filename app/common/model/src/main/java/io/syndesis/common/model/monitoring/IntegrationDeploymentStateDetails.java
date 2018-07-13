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
package io.syndesis.common.model.monitoring;

import java.io.Serializable;
import java.util.Optional;

import org.immutables.value.Value;

import io.syndesis.common.model.Kind;
import io.syndesis.common.model.WithId;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Detailed {@link io.syndesis.common.model.integration.IntegrationDeploymentState} description.
 * Contains details on sub-states so to speak when publishing/unpublishing an integration.
 */
@Value.Immutable
@JsonDeserialize(builder = IntegrationDeploymentStateDetails.Builder.class)
@SuppressWarnings("immutables")
public interface IntegrationDeploymentStateDetails extends WithId<IntegrationDeploymentStateDetails>, Serializable {

    @Override
    default Kind getKind() {
        return Kind.IntegrationDeploymentStateDetails;
    }

    /**
     * Integration Id of the related {@link io.syndesis.common.model.integration.IntegrationDeployment}.
     * @return integration id.
     */
    String getIntegrationId();

    /**
     * Deployment version of the related {@link io.syndesis.common.model.integration.IntegrationDeployment}.
     * @return integration deployment version.
     */
    int getDeploymentVersion();

    /**
     * Integration state detailed status, including step count and total steps.
     * @return detailed state.
     */
    IntegrationDeploymentDetailedState getDetailedState();

    /**
     * OpenShift namespace for (build/deployment) pod being monitored.
     * @return OpenShift namespace.
     */
    Optional<String> getNamespace();

    /**
     * POD name of pod (build/deployment) being monitored.
     * @return currently monitored pod name.
     */
    Optional<String> getPodName();

    /**
     * Type of log link being returned, i.e. whether events url or logs url should be displayed.
     * @return
     */
    Optional<LinkType> getLinkType();

    class Builder extends ImmutableIntegrationDeploymentStateDetails.Builder {
        // allow access to IntegrationDeploymentStateDetails.Builder
    }
}
