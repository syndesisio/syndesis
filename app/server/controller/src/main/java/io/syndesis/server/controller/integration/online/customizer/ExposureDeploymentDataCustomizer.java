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

import java.util.EnumSet;

import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.util.Optionals;
import io.syndesis.server.controller.ControllersConfigurationProperties;
import io.syndesis.server.openshift.DeploymentData;
import io.syndesis.server.openshift.Exposure;
import org.springframework.stereotype.Component;

/**
 * Sets the right exposure (e.g. HTTP routes) into the deployment data.
 */
@Component
public final class ExposureDeploymentDataCustomizer implements DeploymentDataCustomizer {

    private final boolean exposeVia3scale;

    public ExposureDeploymentDataCustomizer(ControllersConfigurationProperties properties) {
        exposeVia3scale = properties.isExposeVia3scale();
    }

    @Override
    public DeploymentData customize(final DeploymentData data, final IntegrationDeployment integrationDeployment) {
        return new DeploymentData.Builder()
            .createFrom(data)
            .withExposure(determineExposure(integrationDeployment))
            .build();
    }

    EnumSet<Exposure> determineExposure(final IntegrationDeployment integrationDeployment) {
        if (needsExposure(integrationDeployment)) {
            if (exposeVia3scale && isNotWebhook(integrationDeployment)) {
                return EnumSet.of(Exposure.SERVICE, Exposure._3SCALE);
            }

            return EnumSet.of(Exposure.ROUTE, Exposure.SERVICE);
        }

        return EnumSet.noneOf(Exposure.class);
    }

    static boolean isNotWebhook(IntegrationDeployment integrationDeployment) {
        return !integrationDeployment.getSpec().getUsedConnectorIds().contains("webhook");
    }

    static boolean needsExposure(final IntegrationDeployment integrationDeployment) {
        return integrationDeployment.getSpec()
            .getFlows().stream().flatMap(f -> f.getSteps().stream())
            .flatMap(step -> Optionals.asStream(step.getAction()))
            .flatMap(action -> action.getTags().stream())
            .anyMatch("expose"::equals);
    }
}
