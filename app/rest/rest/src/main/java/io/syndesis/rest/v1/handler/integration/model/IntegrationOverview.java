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

import static io.syndesis.model.integration.IntegrationDeploymentState.Unpublished;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;

import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.IntegrationDeploymentState;

public class IntegrationOverview {

    private final Integration integration;
    private final Optional<IntegrationDeployment> deployment;

    public IntegrationOverview(Integration integration, Optional<IntegrationDeployment> deployment) {
        this.integration = integration;
        this.deployment = deployment;
    }

    public int getVersion() {
        return integration.getVersion();
    }

    public Optional<String> getId() {
        return integration.getId();
    }

    public String getName() {
        return integration.getName();
    }

    public SortedSet<String> getTags() {
        return integration.getTags();
    }

    public List<StepOverview> getSteps() {
        return integration.getSteps().stream().map(StepOverview::new).collect(Collectors.toList());
    }

    public Optional<String> getDescription() {
        return integration.getDescription();
    }

    public boolean isDraft() {
        return deployment.map(x -> x.getVersion() != integration.getVersion()).orElse(true);
    }

    public IntegrationDeploymentState getCurrentState() {
        return deployment.map(x -> x.getCurrentState()).orElse(Unpublished);
    }

    public IntegrationDeploymentState getTargetState() {
        return deployment.map(x -> x.getTargetState()).orElse(Unpublished);
    }

    public Optional<Integer> getDeploymentVersion() {
        return deployment.map(x->x.getVersion());
    }

    public List<DeploymentOverview> getDeployments() {
        return null;
    }

    public Optional<String> getStatusMessage() {
        return deployment.flatMap(x -> x.getStatusMessage());
    }


}
