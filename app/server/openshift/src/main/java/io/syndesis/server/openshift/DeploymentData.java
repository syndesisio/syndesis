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


import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public final class DeploymentData {

    private final Map<String, String> annotations = new HashMap<>();
    private final Map<String, String> labels = new HashMap<>();
    private final Map<String, String> secret = new HashMap<>();
    private final Map<String, String> properties = new HashMap<>();

    private String image;
    private int version;

    private EnumSet<Exposure> exposure = EnumSet.noneOf(Exposure.class);

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public Map<String, String> getSecret() {
        return secret;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public int getVersion() {
        return version;
    }

    public String getImage() {
        return image;
    }

    public EnumSet<Exposure> getExposure() {
        return exposure;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final DeploymentData that;

        public Builder() {
            this(new DeploymentData());
        }

        private Builder(final DeploymentData deploymentData) {
            that = deploymentData;
        }

        public Builder createFrom(final DeploymentData deploymentData) {
            return new Builder(deploymentData);
        }

        public DeploymentData build() {
            return that;
        }

        public DeploymentData.Builder addLabel(final String name, final String value) {
            that.labels.put(name, value);
            return this;
        }

        public DeploymentData.Builder addAnnotation(final String name, final String value) {
            that.annotations.put(name, value);
            return this;
        }

        public DeploymentData.Builder addSecretEntry(final String name, final String value) {
            that.secret.put(name, value);
            return this;
        }

        public DeploymentData.Builder addProperty(final String name, final String value) {
            that.properties.put(name, value);
            return this;
        }

        public DeploymentData.Builder withImage(final String image) {
            that.image = image;
            return this;
        }

        public DeploymentData.Builder withVersion(final int version) {
            that.version = version;
            return this;
        }

        public DeploymentData.Builder withExposure(final EnumSet<Exposure> exposure) {
            that.exposure = exposure;
            return this;
        }
    }
}
