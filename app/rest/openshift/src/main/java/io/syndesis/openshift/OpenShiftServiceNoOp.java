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
package io.syndesis.openshift;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.User;
import io.fabric8.openshift.api.model.UserBuilder;

public class OpenShiftServiceNoOp implements OpenShiftService {


    @Override
    public void build(String name, DeploymentData data, InputStream tarInputStream) {
        // Empty no-op just for testing
    }

    @Override
    public void deploy(String name, DeploymentData data) {
        // Empty no-op just for testing
    }

    @Override
    public boolean isDeploymentReady(String name) {
        return true;
    }

    @Override
    public boolean delete(String name) {
        return false;
    }

    @Override
    public boolean exists(String name) {
        return false;
    }

    @Override
    public void scale(String name, int nrReplicas) {
        // Empty no-op just for testing
    }

    @Override
    public boolean isScaled(String name, int nrReplicas) {
        return false;
    }

    @Override
    public List<DeploymentConfig> getDeploymentsByLabel(Map<String, String> labels) {
        return Collections.emptyList();
    }

    @Override
    public User whoAmI(String openShiftToken) {
        return new UserBuilder().withNewMetadata().withName("openshift_noop").and().withFullName("OpenShift NoOp").build();
    }

    @Override
    public boolean isBuildStarted(String name) {
        return false;
    }
}
