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

package io.syndesis.dv.openshift;

/**
 * Holder for the latest build and deployment status for a virtualization.
 */
public class VirtualizationStatus {

    private BuildStatus buildStatus;
    private DeploymentStatus deploymentStatus;

    public VirtualizationStatus(BuildStatus buildStatus, DeploymentStatus deploymentStatus) {
        this.buildStatus = buildStatus;
        this.deploymentStatus = deploymentStatus;
    }

    public DeploymentStatus getDeploymentStatus() {
        return deploymentStatus;
    }

    public BuildStatus getBuildStatus() {
        return buildStatus;
    }

}
