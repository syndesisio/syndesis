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
package io.syndesis.controllers.integration.online;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import io.syndesis.controllers.ControllersConfigurationProperties;
import io.syndesis.controllers.StateChangeHandler;
import io.syndesis.controllers.StateUpdate;
import io.syndesis.controllers.integration.IntegrationSupport;
import io.syndesis.core.Labels;
import io.syndesis.core.SyndesisServerException;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.dao.manager.EncryptionComponent;
import io.syndesis.integration.api.IntegrationProjectGenerator;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.IntegrationDeploymentState;
import io.syndesis.openshift.DeploymentData;
import io.syndesis.openshift.OpenShiftService;

public class ActivateHandler extends BaseHandler implements StateChangeHandler {

    private final DataManager dataManager;
    private final IntegrationProjectGenerator projectGenerator;
    private final ControllersConfigurationProperties properties;
    private final EncryptionComponent encryptionComponent;

    @SuppressWarnings("PMD.DefaultPackage")
    ActivateHandler(
        DataManager dataManager,
        OpenShiftService openShiftService,
        IntegrationProjectGenerator projectGenerator,
        ControllersConfigurationProperties properties,
        EncryptionComponent encryptionComponent) {

        super(openShiftService);

        this.dataManager = dataManager;
        this.projectGenerator = projectGenerator;
        this.properties = properties;
        this.encryptionComponent = encryptionComponent;
    }

    @Override
    public Set<IntegrationDeploymentState> getTriggerStates() {
        return Collections.singleton(IntegrationDeploymentState.Active);
    }

    @Override
    public StateUpdate execute(IntegrationDeployment integrationDeploymentDefinition) {
        final int maxIntegrationsPerUser = properties.getMaxIntegrationsPerUser();
        if (maxIntegrationsPerUser != ControllersConfigurationProperties.UNLIMITED) {
            int userIntegrations = countActiveIntegrationsOfSameUserAs(integrationDeploymentDefinition);
            if (userIntegrations >= maxIntegrationsPerUser) {
                //What the user sees.
                return new StateUpdate(IntegrationDeploymentState.Inactive, "User has currently " + userIntegrations + " integrations, while the maximum allowed number is " + maxIntegrationsPerUser + ".");
            }
        }

        final int maxDeploymentsPerUser = properties.getMaxDeploymentsPerUser();
        if (maxDeploymentsPerUser != ControllersConfigurationProperties.UNLIMITED) {
            int userDeployments = countDeployments(integrationDeploymentDefinition);
            if (userDeployments >= maxDeploymentsPerUser) {
                //What we actually want to limit. So even though this should never happen, we still need to make sure.
                return new StateUpdate(IntegrationDeploymentState.Inactive, "User has currently " + userDeployments + " deployments, while the maximum allowed number is " + maxDeploymentsPerUser + ".");
            }
        }

        final IntegrationDeployment integrationDeployment = IntegrationSupport.sanitize(integrationDeploymentDefinition, dataManager, encryptionComponent);
        logInfo(integrationDeployment, "Build started: {}, isRunning: {}, Deployment ready: {}",
            isBuildStarted(integrationDeployment), isRunning(integrationDeployment), isReady(integrationDeployment));
        BuildStepPerformer stepPerformer = new BuildStepPerformer(integrationDeployment);
        logInfo(integrationDeployment, "Steps performed so far: " + stepPerformer.getStepsPerformed());

        final Integration integration = integrationOf(integrationDeployment);
        try {

            deactivatePreviousDeployments(integrationDeployment);

            DeploymentData deploymentData = createDeploymentData(integration, integrationDeployment);
            stepPerformer.perform("build", this::build, deploymentData);
            stepPerformer.perform("deploy", this::deploy, deploymentData);
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            logError(integrationDeployment, "[ERROR] Activation failure");
            // Setting a message to update means implicitly thats in an error state (for the UI)
            return new StateUpdate(IntegrationDeploymentState.Pending, e.getMessage());
        }

        // Set status to activate if finally running. Also clears the previous step which has been performed
        if (isRunning(integrationDeployment)) {
            logInfo(integrationDeployment, "[ACTIVATED] bc. integration is running with 1 pod");
            updateDeploymentState(integrationDeployment, IntegrationDeploymentState.Active);
            return new StateUpdate(IntegrationDeploymentState.Active);
        }

        logInfo(integrationDeployment, "Build started: {}, isRunning: {}, Deployment ready: {}",
            isBuildStarted(integrationDeployment), isRunning(integrationDeployment), isReady(integrationDeployment));
        logInfo(integrationDeployment, "[PENDING] [" + stepPerformer.getStepsPerformed() + "]");

        return new StateUpdate(IntegrationDeploymentState.Pending, stepPerformer.getStepsPerformed());
    }

    private DeploymentData createDeploymentData(Integration integration, IntegrationDeployment integrationDeployment) {
        Properties applicationProperties = projectGenerator.generateApplicationProperties(integration);

        String username = integrationDeployment.getUserId().orElseThrow(() -> new IllegalStateException("Couldn't find the user of the integration"));

        return DeploymentData.builder()
            .addLabel(OpenShiftService.INTEGRATION_ID_LABEL, Labels.sanitize(integrationDeployment.getIntegrationId().orElseThrow(() -> new IllegalStateException("IntegrationDeployment should have an integrationId"))))
            .addLabel(OpenShiftService.DEPLOYMENT_ID_LABEL,  Integer.toString(integrationDeployment.getVersion()))
            .addLabel(OpenShiftService.USERNAME_LABEL, Labels.sanitize(username))
            .addAnnotation(OpenShiftService.INTEGRATION_ID_ANNOTATION, integrationDeployment.getIntegrationId().get())
            .addAnnotation(OpenShiftService.DEPLOYMENT_VERSION_ANNOTATION, Integer.toString(integrationDeployment.getVersion()))
            .addSecretEntry("application.properties", propsToString(applicationProperties))
            .build();
    }


    // =============================================================================
    // Various steps to perform:

    private void build(IntegrationDeployment integration, DeploymentData data) throws IOException {
        InputStream tarInputStream = createProjectFiles(integration.getSpec());
        logInfo(integration, "Created project files and starting build");
        openShiftService().build(integration.getSpec().getName(), data, tarInputStream);
    }

    private void deploy(IntegrationDeployment integration, DeploymentData data) throws IOException {
        logInfo(integration, "Starting deployment");
        openShiftService().deploy(integration.getSpec().getName(), data);
        logInfo(integration, "Deployment done");
    }

    private void deactivatePreviousDeployments(IntegrationDeployment integrationDeployment) {
        dataManager.fetchIdsByPropertyValue(IntegrationDeployment.class, "integrationId", integrationDeployment.getIntegrationId().get(), "currentState", "Active")
            .stream()
            .map(id -> dataManager.fetch(IntegrationDeployment.class, id))
            .filter(r -> r.getVersion() >= integrationDeployment.getVersion())
            .map(r -> r.withCurrentState(IntegrationDeploymentState.Undeployed))
            .forEach(r -> dataManager.update(r));
    }

    private void updateDeploymentState(IntegrationDeployment integrationDeployment, IntegrationDeploymentState state) {
        IntegrationDeployment d = dataManager.fetch(IntegrationDeployment.class, integrationDeployment.getId().get());
        dataManager.update(d.withCurrentState(state));
    }


    // =================================================================================

    private boolean isBuildStarted(IntegrationDeployment integrationDeployment) {
        return openShiftService().isBuildStarted(integrationDeployment.getSpec().getName());
    }

    private boolean isReady(IntegrationDeployment integrationDeployment) {
        return openShiftService().isDeploymentReady(integrationDeployment.getSpec().getName());
    }

    public boolean isRunning(IntegrationDeployment integrationDeployment) {
        return openShiftService().isScaled(integrationDeployment.getSpec().getName(), 1);
    }

    private static String propsToString(Properties data) {
        if (data == null) {
            return "";
        }
        try {
            StringWriter w = new StringWriter();
            data.store(w, "");
            return w.toString();
        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }


    private Integration integrationOf(IntegrationDeployment integrationDeployment) {
        return dataManager.fetch(Integration.class, integrationDeployment.getIntegrationId().orElseThrow(() -> new IllegalStateException("IntegrationDeployment doesn't have integration id.")));
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
            .filter(i -> IntegrationDeploymentState.Active == i.getCurrentState())
            .filter(i -> i.getUserId().map(username::equals).orElse(Boolean.FALSE))
            .count();
    }

    /**
     * Count the deployments of the owner of the specified integration.
     *
     * @param deployment The specified IntegrationDeployment.
     * @return The number of deployed integrations (excluding the current).
     */
    private int countDeployments(IntegrationDeployment deployment) {
        Integration integration = deployment.getSpec();
        String id = Labels.sanitize(integration.getId().orElseThrow(() -> new IllegalStateException("Couldn't find the id of the integration")));
        String username = deployment.getUserId().orElseThrow(() -> new IllegalStateException("Couldn't find the user of the integration"));

        Map<String, String> labels = new HashMap<>();
        labels.put(OpenShiftService.USERNAME_LABEL, Labels.sanitize(username));

        return (int) openShiftService().getDeploymentsByLabel(labels)
            .stream()
            .filter(d -> !id.equals(d.getMetadata().getLabels().get(OpenShiftService.INTEGRATION_ID_LABEL)))
            .filter(d -> d.getSpec().getReplicas() > 0)
            .count();
    }

    private InputStream createProjectFiles(Integration integrationDeployment) {
        try {
            return projectGenerator.generate(integrationDeployment);
        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    // ===============================================================================
    // Some helper method to conditional execute certain steps
    @FunctionalInterface
    public interface IoCheckedFunction<T> {
        void apply(T t, DeploymentData data) throws IOException;
    }

    private class BuildStepPerformer {
        private final List<String> stepsPerformed;
        private final IntegrationDeployment integrationDeployment;

        BuildStepPerformer(IntegrationDeployment integrationDeployment) {
            this.integrationDeployment = integrationDeployment;
            this.stepsPerformed = new ArrayList<>(integrationDeployment.getStepsDone());
        }

        void perform(String step, IoCheckedFunction<IntegrationDeployment> callable, DeploymentData data) throws IOException {
            if (!stepsPerformed.contains(step)) {
                callable.apply(integrationDeployment, data);
                stepsPerformed.add(step);
            } else {
                logInfo(integrationDeployment, "Skipped step {} because already performed", step);
            }
        }

        List<String> getStepsPerformed() {
            return stepsPerformed;
        }
    }
}
