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
package io.syndesis.openshift;

import io.fabric8.kubernetes.client.RequestConfig;
import io.fabric8.openshift.api.model.DeploymentConfig;

import java.util.List;
import java.util.Map;

public interface OpenShiftService {

    String REVISION_ID_ANNOTATION = "syndesis.io/revision-id";
    String USERNAME_LABEL = "USERNAME";

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

    /**
     * Returns the {@link DeploymentConfig}s that match the specified labels.
     * @param requestConfig     The configuration of the request.
     * @param labels            The specified labels.
     * @return                  The list of {@link DeploymentConfig}s.
     */
    List<DeploymentConfig> getDeploymentsByLabel(RequestConfig requestConfig, Map<String, String> labels);

    /**
     * Create a Webhook URL which can be used in a github integration
     * @param projectName   The name of the project.
     * @param secret
     */
    String getGitHubWebHookUrl(String projectName, String secret);
}
