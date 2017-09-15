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

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OpenShiftServiceNoOp implements OpenShiftService {

    @Override
    public void create(OpenShiftDeployment d) {
        // Empty no-op just for testing
    }

    @Override
    public boolean delete(OpenShiftDeployment d) {
        return false;
    }

    @Override
    public boolean exists(OpenShiftDeployment d) {
        return false;
    }

    @Override
    public void scale(OpenShiftDeployment d) {
        // Empty no-op just for testing
    }

    @Override
    public boolean isScaled(OpenShiftDeployment d) {
        return false;
    }

    @Override
    public List<DeploymentConfig> getDeploymentsByLabel(RequestConfig requestConfig, Map<String, String> labels) {
        return Collections.emptyList();
    }

    @Override
    public String getGitHubWebHookUrl(OpenShiftDeployment d, String secret) {
        return "";
    }
}
