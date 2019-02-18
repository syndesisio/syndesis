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
package io.syndesis.server.controller.integration;

import java.util.HashMap;
import java.util.Map;

import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.util.Labels;
import io.syndesis.server.controller.ControllersConfigurationProperties;
import io.syndesis.server.controller.StateUpdate;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.openshift.OpenShiftService;
import org.springframework.stereotype.Component;

/**
 * @author roland
 * @since 2019-02-11
 */
@Component
public class IntegrationPublishValidator {

    ControllersConfigurationProperties properties;
    DataManager dataManager;
    OpenShiftService openShiftService;

    public IntegrationPublishValidator(ControllersConfigurationProperties properties, DataManager dataManager, OpenShiftService openShiftService) {
        this.properties = properties;
        this.dataManager = dataManager;
        this.openShiftService = openShiftService;
    }

    public StateUpdate validate(IntegrationDeployment deployment) {
        final int maxIntegrationsPerUser = properties.getMaxIntegrationsPerUser();
        if (maxIntegrationsPerUser != ControllersConfigurationProperties.UNLIMITED) {
            int userIntegrations = countActiveIntegrationsOfSameUserAs(deployment);
            if (userIntegrations >= maxIntegrationsPerUser) {
                //What the user sees.
                return new StateUpdate(IntegrationDeploymentState.Unpublished, deployment.getStepsDone(), "User has currently " + userIntegrations + " integrations, while the maximum allowed number is " + maxIntegrationsPerUser + ".");
            }
        }

        final int maxDeploymentsPerUser = properties.getMaxDeploymentsPerUser();
        if (maxDeploymentsPerUser != ControllersConfigurationProperties.UNLIMITED) {
            int userDeployments = countDeployments(deployment);
            if (userDeployments >= maxDeploymentsPerUser) {
                //What we actually want to limit. So even though this should never happen, we still need to make sure.
                return new StateUpdate(IntegrationDeploymentState.Unpublished, deployment.getStepsDone(), "User has currently " + userDeployments + " deployments, while the maximum allowed number is " + maxDeploymentsPerUser + ".");
            }
        }
        return null;
    }

    /**
     * Count the deployments of the owner of the specified integration.
     *
     * @param deployment The specified IntegrationDeployment.
     * @return The number of deployed integrations (excluding the current).
     */
    private int countDeployments(IntegrationDeployment deployment) {
        Integration integration = deployment.getSpec();
        String id = Labels.validate(integration.getId().orElseThrow(() -> new IllegalStateException("Couldn't find the id of the integration")));
        String username = deployment.getUserId().orElseThrow(() -> new IllegalStateException("Couldn't find the user of the integration"));

        Map<String, String> labels = new HashMap<>();
        labels.put(OpenShiftService.USERNAME_LABEL, Labels.sanitize(username));

        return (int) openShiftService.getDeploymentsByLabel(labels)
            .stream()
            .filter(d -> !id.equals(d.getMetadata().getLabels().get(OpenShiftService.INTEGRATION_ID_LABEL)))
            .filter(d -> d.getSpec().getReplicas() > 0)
            .count();
    }


    /**
     * Counts active integrations (in DB) of the owner of the specified integration.
     *
     * @param deployment The specified IntegrationDeployment.
     * @return The number of integrations (excluding the current).
     */
    private int countActiveIntegrationsOfSameUserAs(IntegrationDeployment deployment) {
        Integration integration = deployment.getSpec();
        String integrationId = integration.getId().orElseThrow(() -> new IllegalStateException("Couldn't find the id of the integration."));
        String username = deployment.getUserId().orElseThrow(() -> new IllegalStateException("Couldn't find the user of the integration"));

        return (int) dataManager.fetchAll(IntegrationDeployment.class).getItems()
            .stream()
            .filter(i -> !i.getIntegrationId().get().equals(integrationId)) //The "current" integration will already be in the database.
            .filter(i -> IntegrationDeploymentState.Published == i.getCurrentState())
            .filter(i -> i.getUserId().map(username::equals).orElse(Boolean.FALSE))
            .count();
    }
}
