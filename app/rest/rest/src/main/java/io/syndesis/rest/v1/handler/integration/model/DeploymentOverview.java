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
package io.syndesis.rest.v1.handler.integration.model;

import java.util.Optional;

import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.IntegrationDeploymentState;

public class DeploymentOverview {

    private final IntegrationDeployment deployment;

    public DeploymentOverview(IntegrationDeployment deployment) {
        this.deployment = deployment;
    }

    public int getIntegrationVersion() {
        return deployment.getSpec().getVersion();
    }

    public int getVersion() {
        return deployment.getVersion();
    }

    public long getCreatedAt() {
        return deployment.getCreatedAt();
    }

    public long getUpdatedAt() {
        return deployment.getUpdatedAt();
    }

    public Optional<String> getId() {
        return deployment.getId();
    }

    public IntegrationDeploymentState getCurrentState() {
        return deployment.getCurrentState();
    }

    public IntegrationDeploymentState getTargetState() {
        return deployment.getTargetState();
    }
}
