/*
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

package io.syndesis.controllers;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("inspector")
public class ControllersConfigurationProperties {

    public static final int DEFAULT_MAX_INTEGRATIONS_PER_USER = 1;
    public static final int DEFAULT_MAX_DEPLOYMENTS_PER_USER = 1;

    private int maxIntegrationsPerUser = DEFAULT_MAX_INTEGRATIONS_PER_USER;
    private int maxDeploymentsPerUser = DEFAULT_MAX_DEPLOYMENTS_PER_USER;

    public ControllersConfigurationProperties() {
        // behave like a Java bean
    }

    public ControllersConfigurationProperties(int maxIntegrationsPerUser, int maxDeploymentsPerUser) {
        this.maxIntegrationsPerUser = maxIntegrationsPerUser;
        this.maxDeploymentsPerUser = maxDeploymentsPerUser;
    }

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
}
