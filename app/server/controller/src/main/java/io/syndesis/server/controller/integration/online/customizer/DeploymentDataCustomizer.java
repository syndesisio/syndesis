package io.syndesis.server.controller.integration.online.customizer;

import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.server.openshift.DeploymentData;

@FunctionalInterface
public interface DeploymentDataCustomizer {

    DeploymentData customize(DeploymentData data, IntegrationDeployment integrationDeployment);

}
