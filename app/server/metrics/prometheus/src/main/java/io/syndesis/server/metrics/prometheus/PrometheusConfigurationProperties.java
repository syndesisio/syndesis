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
package io.syndesis.server.metrics.prometheus;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@ConfigurationProperties("prometheus")
@ConditionalOnProperty(value = "metrics.kind", havingValue = "prometheus")
public class PrometheusConfigurationProperties {

    private String service = "syndesis-prometheus";
    private String integrationIdLabel = "syndesis_io_integration_id";
    private String deploymentVersionLabel = "syndesis_io_deployment_version";
    private String componentLabel = "syndesis_io_component";
    private String typeLabel = "type";
    private String metricsHistoryRange = "1d";
    private int topIntegrationsCount = 5;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getIntegrationIdLabel() {
        return integrationIdLabel;
    }

    public void setIntegrationIdLabel(String integrationIdLabel) {
        this.integrationIdLabel = integrationIdLabel;
    }

    public String getDeploymentVersionLabel() {
        return deploymentVersionLabel;
    }

    public void setDeploymentVersionLabel(String deploymentVersionLabel) {
        this.deploymentVersionLabel = deploymentVersionLabel;
    }

    public String getComponentLabel() {
        return componentLabel;
    }

    public void setComponentLabel(String componentLabel) {
        this.componentLabel = componentLabel;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public void setTypeLabel(String typeLabel) {
        this.typeLabel = typeLabel;
    }

    public String getMetricsHistoryRange() {
        return metricsHistoryRange;
    }

    public void setMetricsHistoryRange(String metricsHistoryRange) {
        this.metricsHistoryRange = metricsHistoryRange;
    }

    public int getTopIntegrationsCount() {
        return topIntegrationsCount;
    }

    public void setTopIntegrationsCount(int topIntegrationsCount) {
        this.topIntegrationsCount = topIntegrationsCount;
    }
}
