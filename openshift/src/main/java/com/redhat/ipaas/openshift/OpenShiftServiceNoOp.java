/**
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
package com.redhat.ipaas.openshift;

public class OpenShiftServiceNoOp implements OpenShiftService {

    @Override
    public boolean isDeploymentConfigReady(String name) {
        return false;
    }

    @Override
    public boolean isDeploymentConfigScaled(String name, int replicas) {
        return false;
    }

    @Override
    public void scaleDeploymentConfig(String name, int replicas) {

    }

    @Override
    public boolean deploymentConfigExists(String name) {
        return false;
    }

    @Override
    public boolean deleteResources(String name) {
        return false;
    }

    @Override
    public void createOpenShiftResources(CreateResourcesRequest request) {
        // Empty no-op just for testing
    }
}
