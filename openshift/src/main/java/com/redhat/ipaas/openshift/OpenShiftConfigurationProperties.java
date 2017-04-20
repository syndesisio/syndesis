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
package com.redhat.ipaas.openshift;

import io.fabric8.openshift.client.OpenShiftConfig;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("openshift")
public class OpenShiftConfigurationProperties {

    public static final String SERVICE_CA_CERT_FILE = "/var/run/secrets/kubernetes.io/serviceaccount/service-ca.crt";

    private boolean enabled;

    private String openShiftHost = "https://ipaas-openshift-proxy." + new OpenShiftConfigBuilder().build().getNamespace() + ".svc";

    private OpenShiftConfig openShiftClientConfig = new OpenShiftConfigBuilder().withMasterUrl(openShiftHost).withCaCertFile(SERVICE_CA_CERT_FILE).build();

    private String builderImage = "fabric8/s2i-java:2.0.0";

    private String openshiftApiBaseUrl;

    private String namespace;

    private String integrationServiceAccount = "ipaas-integration";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getOpenShiftHost() {
        return openShiftHost;
    }

    public void setOpenShiftHost(String openShiftHost) {
        this.openShiftHost = openShiftHost;
    }

    public OpenShiftConfig getOpenShiftClientConfiguration() {
        return openShiftClientConfig;
    }

    public String getBuilderImage() {
        return builderImage;
    }

    public void setBuilderImage(String builderImage) {
        this.builderImage = builderImage;
    }

    public String getOpenshiftApiBaseUrl() {
        return openshiftApiBaseUrl;
    }

    public void setOpenshiftApiBaseUrl(String openshiftApiBaseUrl) {
        this.openshiftApiBaseUrl = openshiftApiBaseUrl;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getIntegrationServiceAccount() {
        return integrationServiceAccount;
    }
}
