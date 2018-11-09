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
package io.syndesis.server.endpoint.v1.handler.user;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Let's grab the settings from the 'controllers' section, since
 * they are already defined there.
 *
 */
@Configuration
@ConfigurationProperties("controllers")
public class UserConfigurationProperties {

    // Default values ....
    public static final int UNLIMITED = 0;

    private int maxIntegrationsPerUser = 1;
    private int maxDeploymentsPerUser  = 1;

    public UserConfigurationProperties() {
        super();
    }

    public UserConfigurationProperties(int maxIntegrationsPerUser, int maxDeploymentsPerUser) {
        super();
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
