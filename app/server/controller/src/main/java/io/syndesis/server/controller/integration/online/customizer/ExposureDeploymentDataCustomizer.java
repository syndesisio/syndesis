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
import io.syndesis.common.util.Optionals;
import io.syndesis.server.openshift.DeploymentData;
import io.syndesis.server.openshift.Exposure;

/**
 * Sets the right exposure (e.g. HTTP routes) into the deployment data.
 */
public class ExposureDeploymentDataCustomizer implements DeploymentDataCustomizer {

    @Override
    public DeploymentData customize(DeploymentData data, IntegrationDeployment integrationDeployment) {
        return new DeploymentData.Builder()
            .createFrom(data)
            .withExposure(getExposure(integrationDeployment))
            .build();
    }

    private Exposure getExposure(IntegrationDeployment integrationDeployment) {
        Exposure exposure = Exposure.NONE;

        boolean needsDirectExposure = integrationDeployment.getSpec()
            .getSteps()
            .stream()
            .flatMap(step -> Optionals.asStream(step.getAction()))
            .flatMap(action -> action.getTags().stream())
            .anyMatch("expose"::equals);

        if (needsDirectExposure) {
            exposure = Exposure.DIRECT;
        }

        return exposure;
    }
}
