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


import java.util.HashMap;
import java.util.Map;

public class DeploymentData {

    private final Map<String, String> annotations = new HashMap<>();
    private final Map<String, String> labels = new HashMap<>();
    private final Map<String, String> secret = new HashMap<>();

    private String image;
    private int version;

    private Exposure exposure = Exposure.NONE;

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public Map<String, String> getSecret() {
        return secret;
    }

    public int getVersion() {
        return version;
    }

    public String getImage() {
        return image;
    }

    public Exposure getExposure() {
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

        private Builder(DeploymentData deploymentData) {
            this.that = deploymentData;
        }

        public Builder createFrom(DeploymentData deploymentData) {
            return new Builder(deploymentData);
        }

        public DeploymentData build() {
            return that;
        }

        public DeploymentData.Builder addLabel(String name, String value) {
             that.labels.put(name, value);
             return this;
        }

        public DeploymentData.Builder addAnnotation(String name, String value) {
             that.annotations.put(name, value);
             return this;
        }

        public DeploymentData.Builder addSecretEntry(String name, String value) {
             that.secret.put(name, value);
             return this;
        }

        public DeploymentData.Builder withImage(String image) {
            that.image = image;
            return this;
        }

        public DeploymentData.Builder withVersion(int version) {
            that.version = version;
            return this;
        }

        public DeploymentData.Builder withExposure(Exposure exposure) {
            that.exposure = exposure;
            return this;
        }
    }
}
