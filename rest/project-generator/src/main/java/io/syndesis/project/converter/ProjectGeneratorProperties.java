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
package io.syndesis.project.converter;

import java.util.ArrayList;
import java.util.List;

import io.syndesis.core.MavenProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("generator")
public class ProjectGeneratorProperties {

    private Boolean secretMaskingEnabled = false;

    private String extensionLoaderHome = "${JAVA_DATA_DIR}/extensions";

    /**
     * Templates configuration.
     */
    private final Templates templates = new Templates();

    private final MavenProperties mavenProperties;

    public ProjectGeneratorProperties(final MavenProperties mavenProperties) {
        this.mavenProperties = mavenProperties;
    }

    public Boolean isSecretMaskingEnabled() {
        return secretMaskingEnabled;
    }

    public void setSecretMaskingEnabled(Boolean secretMaskingEnabled) {
        this.secretMaskingEnabled = secretMaskingEnabled;
    }

    public Boolean getSecretMaskingEnabled() {
        return secretMaskingEnabled;
    }

    public String getExtensionLoaderHome() {
        return extensionLoaderHome;
    }

    public void setExtensionLoaderHome(String extensionLoaderHome) {
        this.extensionLoaderHome = extensionLoaderHome;
    }

    public Templates getTemplates() {
        return templates;
    }

    public MavenProperties getMavenProperties() {
        return mavenProperties;
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
