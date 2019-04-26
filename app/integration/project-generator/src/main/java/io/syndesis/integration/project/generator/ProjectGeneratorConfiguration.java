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
package io.syndesis.integration.project.generator;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("generator")
public class ProjectGeneratorConfiguration {

    /**
     * Should activity tracing be enabled?
     */
    private boolean activityTracing;

    private Boolean secretMaskingEnabled = false;
    private String syndesisExtensionPath = "extensions";

    /**
     * Templates configuration.
     */
    private final Templates templates = new Templates();

    public Boolean isSecretMaskingEnabled() {
        return secretMaskingEnabled;
    }

    public void setSecretMaskingEnabled(Boolean secretMaskingEnabled) {
        this.secretMaskingEnabled = secretMaskingEnabled;
    }

    public Boolean getSecretMaskingEnabled() {
        return secretMaskingEnabled;
    }

    public String getSyndesisExtensionPath() {
        return syndesisExtensionPath;
    }

    public void setSyndesisExtensionPath(String syndesisExtensionPath) {
        this.syndesisExtensionPath = syndesisExtensionPath;
    }

    public Templates getTemplates() {
        return templates;
    }

    public boolean isActivityTracing() {
        return activityTracing;
    }

    public void setActivityTracing(boolean activityTracing) {
        this.activityTracing = activityTracing;
    }

    public static class Templates {
        /**
         * The location of override templates relative to the templates dir.
         */
        private String overridePath;

        /**
         * Additional resources to be included, copied as is.
         */
        private final List<Resource> additionalResources = new ArrayList<>();

        public String getOverridePath() {
            return overridePath;
        }

        public void setOverridePath(String overridePath) {
            this.overridePath = overridePath;
        }

        public List<Resource> getAdditionalResources() {
            return additionalResources;
        }

        public static class Resource {
            /**
             * The resource source location.
             */
            private String source;

            /**
             * The resource location in the generated project.
             */
            private String destination;

            public Resource() {
                // Empty constructor for spring boot ioc
            }

            public Resource(String source, String destination) {
                this.source = source;
                this.destination = destination;
            }

            public String getSource() {
                return source;
            }

            public void setSource(String source) {
                this.source = source;
            }

            public String getDestination() {
                return destination;
            }

            public void setDestination(String destination) {
                this.destination = destination;
            }
        }
    }
}
