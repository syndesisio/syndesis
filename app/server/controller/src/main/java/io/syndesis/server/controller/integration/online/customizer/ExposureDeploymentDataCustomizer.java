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
