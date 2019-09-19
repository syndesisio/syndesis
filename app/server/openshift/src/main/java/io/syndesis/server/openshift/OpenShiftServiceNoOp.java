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
package io.syndesis.server.openshift;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Doneable;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.User;
import io.fabric8.openshift.api.model.UserBuilder;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class OpenShiftServiceNoOp implements OpenShiftService {

    @Override
    public List<HasMetadata> createOrReplaceCRD(InputStream cdrYamlStream) {
        return Collections.emptyList();
    }

    @Override
    public CustomResourceDefinition createOrReplaceCRD(CustomResourceDefinition crd) {
        // Empty no-op just for testing
        return null;
    }

    @Override
    public Optional<CustomResourceDefinition> getCRD(String crdName) {
        return Optional.empty();
    }

    @Override
    public <T extends HasMetadata, L extends KubernetesResourceList<T>, D extends Doneable<T>> T createOrReplaceCR(CustomResourceDefinition crd, Class<T> resourceType, Class<L> resourceListType, Class<D> doneableResourceType, T customResource) {
        // Empty no-op just for testing
        return null;
    }

    @Override
    public <T extends HasMetadata, L extends KubernetesResourceList<T>, D extends Doneable<T>> T getCR(CustomResourceDefinition crd, Class<T> resourceType, Class<L> resourceListType, Class<D> doneableResourceType, String customResourceName) {
        // Empty no-op just for testing
        return null;
    }

    @Override
    public <T extends HasMetadata, L extends KubernetesResourceList<T>, D extends Doneable<T>> List<T> getCRBylabel(CustomResourceDefinition crd, Class<T> resourceType, Class<L> resourceListType, Class<D> doneableResourceType, Map<String, String> labels) {
        return null;
    }

    @Override
    public <T extends HasMetadata, L extends KubernetesResourceList<T>, D extends Doneable<T>> Watch watchCR(CustomResourceDefinition crd, Class<T> resourceType, Class<L> resourceListType, Class<D> doneableResourceType, BiConsumer<Watcher.Action, T> watcher) {
        return null;
    }

    @Override
    public <T extends HasMetadata, L extends KubernetesResourceList<T>, D extends Doneable<T>> boolean deleteCR(CustomResourceDefinition crd, Class<T> resourceType, Class<L> resourceListType, Class<D> doneableResourceType, String customResourceName, boolean cascading) {
        return false;
    }

    @Override
    public <T extends HasMetadata, L extends KubernetesResourceList<T>, D extends Doneable<T>> boolean deleteCR(CustomResourceDefinition crd, Class<T> resourceType, Class<L> resourceListType, Class<D> doneableResourceType, String customResourceName) {
        return false;
    }

    @Override
    public String build(String name, DeploymentData data, InputStream tarInputStream) {
        // Empty no-op just for testing
        return null;
    }

    @Override
    public String deploy(String name, DeploymentData data) {
        // Empty no-op just for testing
        return null;
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
    public void scale(String name, Map<String, String> lables, int desiredReplicas, long amount, TimeUnit timeUnit)  {
        // Empty no-op just for testing
    }

    @Override
    public boolean isScaled(String name, int desiredMinimumReplicas, Map<String, String> labels) {
        return false;
    }

    @Override
    public boolean isBuildFailed(String name) {
        return false;
    }

    @Override
    public List<DeploymentConfig> getDeploymentsByLabel(Map<String, String> labels) {
        return Collections.emptyList();
    }

    @Override
    public User whoAmI(String username) {
        return new UserBuilder().withNewMetadata().withName("openshift_noop").and().withFullName("OpenShift NoOp").build();
    }

    @Override
    public boolean isBuildStarted(String name) {
        return false;
    }

    @Override
    public Optional<String> getExposedHost(String name) {
        return Optional.empty();
    }

    @Override
    public void createOrReplaceSecret(Secret secret) {
        // ... to make PMD happy
    }

    @Override
    public ConfigMap createOrReplaceConfigMap(ConfigMap configMap) {
        return null;
    }
}
