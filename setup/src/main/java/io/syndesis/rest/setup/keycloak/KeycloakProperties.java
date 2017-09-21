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
package io.syndesis.rest.setup.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("keycloak")
public class KeycloakProperties {

    private boolean enabled = true;

    private String keycloakUrl = "http://syndesis-keycloak:8080/auth";

    private String adminUsername = "admin";

    private String adminPassword;

    private String adminRealm = "master";

    private String adminClientId = "admin-cli";

    private String syndesisRealm = "syndesis";

    private String gitHubIdentityProviderId = "github";

    private String githubIdentityProviderUnsetClientId = "dummy";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getKeycloakUrl() {
        return keycloakUrl;
    }

    public void setKeycloakUrl(String keycloakUrl) {
        this.keycloakUrl = keycloakUrl;
    }

    public String getAdminRealm() {
        return adminRealm;
    }

    public void setAdminRealm(String adminRealm) {
        this.adminRealm = adminRealm;
    }

    public String getAdminClientId() {
        return adminClientId;
    }

    public void setAdminClientId(String adminClientId) {
        this.adminClientId = adminClientId;
    }

    public String getSyndesisRealm() {
        return syndesisRealm;
    }

    public void setSyndesisRealm(String syndesisRealm) {
        this.syndesisRealm = syndesisRealm;
    }

    public String getGitHubIdentityProviderId() {
        return gitHubIdentityProviderId;
    }

    public void setGitHubIdentityProviderId(String gitHubIdentityProviderId) {
        this.gitHubIdentityProviderId = gitHubIdentityProviderId;
    }

    public String getGithubIdentityProviderUnsetClientId() {
        return githubIdentityProviderUnsetClientId;
    }

    public void setGithubIdentityProviderUnsetClientId(String githubIdentityProviderUnsetClientId) {
        this.githubIdentityProviderUnsetClientId = githubIdentityProviderUnsetClientId;
    }
}
