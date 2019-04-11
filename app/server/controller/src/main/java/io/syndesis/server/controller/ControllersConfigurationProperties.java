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
package io.syndesis.server.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties("controllers")
public class ControllersConfigurationProperties {

    // Default values ....

    public static final int UNLIMITED = 0;

    private int maxIntegrationsPerUser = 1;
    private int maxDeploymentsPerUser = 1;
    private int integrationStateCheckInterval = 60;
    private boolean exposeVia3scale;

    @NestedConfigurationProperty
    private CamelK camelk = new CamelK();

    public int getMaxIntegrationsPerUser() {
        return maxIntegrationsPerUser;
    }

    public void setMaxIntegrationsPerUser(int maxIntegrationsPerUser) {
        this.maxIntegrationsPerUser = maxIntegrationsPerUser;
    }

    public int getMaxDeploymentsPerUser() {
        return maxDeploymentsPerUser;
    }

    public void setMaxDeploymentsPerUser(int maxDeploymentsPerUser) {
        this.maxDeploymentsPerUser = maxDeploymentsPerUser;
    }

    public void setIntegrationStateCheckInterval(int interval) {
        this.integrationStateCheckInterval = interval;
    }

    public int getIntegrationStateCheckInterval() {
        return integrationStateCheckInterval;
    }

    public void setExposeVia3scale(final boolean exposeVia3scale) {
        this.exposeVia3scale = exposeVia3scale;
    }

    public boolean isExposeVia3scale() {
        return exposeVia3scale;
    }

    public CamelK getCamelk() {
        return camelk;
    }

    public static class CamelK {
        private boolean compression;
        private Map<String, String> environment = new HashMap<>();
        private Set<String> customizers = new HashSet<>();

        public Map<String, String> getEnvironment() {
            return environment;
        }

        public boolean isCompression() {
            return compression;
        }

        public void setCompression(boolean compression) {
            this.compression = compression;
        }

        public Set<String> getCustomizers() {
            return customizers;
        }
    }
}
