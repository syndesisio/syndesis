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

public interface OpenShiftService {

    /**
     * Checks if {@link io.fabric8.openshift.api.model.DeploymentConfig} is ready.
     * @param name  The name of the {@link io.fabric8.openshift.api.model.DeploymentConfig}.
     * @return      True if ready, False otherwise.
     */
    boolean isDeploymentConfigReady(String name);

    /**
     * Scales deployment to the selected number of replicas.
     * @param name      The name of the {@link io.fabric8.openshift.api.model.DeploymentConfig}
     * @param replicas  The number of replicas.
     */
    boolean isDeploymentConfigScaled(String name, int replicas);

    /**
     * Scales deployment to the selected number of replicas.
     * @param name      The name of the {@link io.fabric8.openshift.api.model.DeploymentConfig}
     * @param replicas  The number of replicas.
     */
    void scaleDeploymentConfig(String name, int replicas);

    /**
     * Checks if DeploymentConfig exists.
     * @param name      The name of the {@link io.fabric8.openshift.api.model.DeploymentConfig}
     */
    boolean deploymentConfigExists(String name);

    /**
     * Creates all the requires resources.
     * @param name  The name of the project.
     * @return      Returns True if all resources were deleted, False otherwise.
     */
    boolean deleteResources(String name);

    /**
     * Creates all the requires resources.
     * @param request
     */
    void createOpenShiftResources(CreateResourcesRequest request);



}
