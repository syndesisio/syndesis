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
package io.syndesis.server.controller.integration.online.customizer;

import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.server.openshift.DeploymentData;
import io.syndesis.server.openshift.OpenShiftConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Add custom labels to the integration pod from the integration-labels property
 */
@Component
public final class IntegrationLabelsDeploymentDataCustomizer implements DeploymentDataCustomizer {

    private final OpenShiftConfigurationProperties properties;

    public IntegrationLabelsDeploymentDataCustomizer(OpenShiftConfigurationProperties properties) {
        this.properties = properties;
    }

    @Override
    public DeploymentData customize(final DeploymentData data, final IntegrationDeployment integrationDeployment) {
        DeploymentData.Builder builder = new DeploymentData.Builder().createFrom(data);
        properties.getIntegrationLabels().forEach(builder::addLabel);
        return builder.build();
    }
}
