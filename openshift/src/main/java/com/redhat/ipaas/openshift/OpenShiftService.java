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
     * Creates the deployment (Deployment and Build configurations, Image Streams etc)
     * @param d A description of the deployment to create.
     */
    void create(OpenShiftDeployment d);

    /**
     * Deletes the deployment (Deployment and Build configurations, Image Streams etc)
     * @param d A description of the deployment to delete.
     * @return          Returns True if all resources were deleted, False otherwise.
     */
    boolean delete(OpenShiftDeployment d);

    /**
     * Checks if the deployment (Deployment and Build configurations, Image Streams etc) exists
     * @param d         A description of the deployment to check.
     * @return          Returns True if all resources were deleted, False otherwise.
     */
    boolean exists(OpenShiftDeployment d);

    /**
     * Scale the deployment (Deployment and Build configurations, Image Streams etc)
     * @param d A description of the deployment to scale.
     */
    void scale(OpenShiftDeployment d);

    /**
     * Checks if the deployment (Deployment and Build configurations, Image Streams etc) is scaled.
     * @param d A description of the deployment to scale.
     * @param d
     */
    boolean isScaled(OpenShiftDeployment d);

}
