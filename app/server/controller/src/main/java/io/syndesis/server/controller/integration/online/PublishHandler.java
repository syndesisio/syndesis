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
package io.syndesis.server.controller.integration.online;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import io.syndesis.server.controller.ControllersConfigurationProperties;
import io.syndesis.server.controller.StateChangeHandler;
import io.syndesis.server.controller.StateUpdate;
import io.syndesis.common.util.Labels;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.server.controller.integration.online.customizer.DeploymentDataCustomizer;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.integration.api.IntegrationProjectGenerator;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.server.openshift.DeploymentData;
import io.syndesis.server.openshift.OpenShiftService;

public class PublishHandler extends BaseHandler implements StateChangeHandler {

    private final DataManager dataManager;
    private final IntegrationProjectGenerator projectGenerator;
    private final ControllersConfigurationProperties properties;
    private final List<DeploymentDataCustomizer> customizers;

    @SuppressWarnings("PMD.DefaultPackage")
    PublishHandler(
        DataManager dataManager,
        OpenShiftService openShiftService,
        IntegrationProjectGenerator projectGenerator,
        ControllersConfigurationProperties properties,
        List<DeploymentDataCustomizer> customizers) {

        super(openShiftService);

        this.dataManager = dataManager;
        this.projectGenerator = projectGenerator;
        this.properties = properties;
        this.customizers = customizers;
    }

    @Override
    public Set<IntegrationDeploymentState> getTriggerStates() {
        return Collections.singleton(IntegrationDeploymentState.Published);
    }

    @Override
    public StateUpdate execute(final IntegrationDeployment integrationDeployment) {
        final int maxIntegrationsPerUser = properties.getMaxIntegrationsPerUser();
        if (maxIntegrationsPerUser != ControllersConfigurationProperties.UNLIMITED) {
            int userIntegrations = countActiveIntegrationsOfSameUserAs(integrationDeployment);
            if (userIntegrations >= maxIntegrationsPerUser) {
                //What the user sees.
                return new StateUpdate(IntegrationDeploymentState.Unpublished, integrationDeployment.getStepsDone(), "User has currently " + userIntegrations + " integrations, while the maximum allowed number is " + maxIntegrationsPerUser + ".");
            }
        }

        final int maxDeploymentsPerUser = properties.getMaxDeploymentsPerUser();
        if (maxDeploymentsPerUser != ControllersConfigurationProperties.UNLIMITED) {
            int userDeployments = countDeployments(integrationDeployment);
            if (userDeployments >= maxDeploymentsPerUser) {
                //What we actually want to limit. So even though this should never happen, we still need to make sure.
                return new StateUpdate(IntegrationDeploymentState.Unpublished, integrationDeployment.getStepsDone(), "User has currently " + userDeployments + " deployments, while the maximum allowed number is " + maxDeploymentsPerUser + ".");
            }
        }

        logInfo(integrationDeployment, "Build started: {}, isRunning: {}, Deployment ready: {}",
            isBuildStarted(integrationDeployment), isRunning(integrationDeployment), isReady(integrationDeployment));

        BuildStepPerformer stepPerformer = new BuildStepPerformer(integrationDeployment);
        logInfo(integrationDeployment, "Steps performed so far: " + stepPerformer.getStepsPerformed());

        if (isBuildFailed(integrationDeployment)){
            return new StateUpdate(IntegrationDeploymentState.Error, stepPerformer.getStepsPerformed(), "Error");
        }

        final Integration integration = integrationOf(integrationDeployment);
        try {
            setVersion(integrationDeployment);
            deactivatePreviousDeployments(integrationDeployment);

            DeploymentData deploymentData = createDeploymentData(integration, integrationDeployment);
            String buildLabel = "buildv" + deploymentData.getVersion();
            stepPerformer.perform(buildLabel, this::build, deploymentData);

            deploymentData = new DeploymentData.Builder().createFrom(deploymentData).withImage(stepPerformer.stepsPerformed.get(buildLabel)).build();
            if (hasPublishedDeployments(integrationDeployment)) {
                return new StateUpdate(IntegrationDeploymentState.Unpublished, integrationDeployment.getStepsDone(), "Integration has still active deployments. Will retry shortly");
            }

            stepPerformer.perform("deploy", this::deploy, deploymentData);
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            logError(integrationDeployment, "[ERROR] Activation failure", e);
            // Setting a message to update means implicitly thats in an error state (for the UI)
            return new StateUpdate(IntegrationDeploymentState.Pending, stepPerformer.getStepsPerformed(), e.getMessage());
        }

        // Set status to activate if finally running. Also clears the previous step which has been performed
        if (isRunning(integrationDeployment)) {
            logInfo(integrationDeployment, "[ACTIVATED] bc. integration is running with 1 pod");
            updateDeploymentState(integrationDeployment, IntegrationDeploymentState.Published);
            return new StateUpdate(IntegrationDeploymentState.Published, stepPerformer.getStepsPerformed());
        }

        logInfo(integrationDeployment, "Build started: {}, isRunning: {}, Deployment ready: {}",
            isBuildStarted(integrationDeployment), isRunning(integrationDeployment), isReady(integrationDeployment));
        logInfo(integrationDeployment, "[PENDING] [" + stepPerformer.getStepsPerformed() + "]");

        return new StateUpdate(IntegrationDeploymentState.Pending, stepPerformer.getStepsPerformed());
    }

    private DeploymentData createDeploymentData(Integration integration, IntegrationDeployment integrationDeployment) {
        Properties applicationProperties = projectGenerator.generateApplicationProperties(integration);

        String username = integrationDeployment.getUserId().orElseThrow(() -> new IllegalStateException("Couldn't find the user of the integration"));

        String integrationId = integrationDeployment.getIntegrationId().orElseThrow(() -> new IllegalStateException("IntegrationDeployment should have an integrationId"));
        String version = Integer.toString(integrationDeployment.getVersion());
        DeploymentData data = DeploymentData.builder()
            .withVersion(integrationDeployment.getVersion())
            .addLabel(OpenShiftService.INTEGRATION_ID_LABEL, Labels.validate(integrationId))
            .addLabel(OpenShiftService.DEPLOYMENT_VERSION_LABEL, version)
            .addLabel(OpenShiftService.USERNAME_LABEL, Labels.sanitize(username))
            .addAnnotation(OpenShiftService.INTEGRATION_NAME_ANNOTATION, integration.getName())
            .addAnnotation(OpenShiftService.INTEGRATION_ID_LABEL, integrationId)
            .addAnnotation(OpenShiftService.DEPLOYMENT_VERSION_LABEL, version)
            .addSecretEntry("application.properties", propsToString(applicationProperties))
            .build();

        if (this.customizers != null && !this.customizers.isEmpty()) {
            for (DeploymentDataCustomizer customizer : customizers) {
                data = customizer.customize(data, integrationDeployment);
            }
        }

        return data;
    }


    // =============================================================================
    // Various steps to perform:

    private String build(IntegrationDeployment integration, DeploymentData data) throws IOException  {
        InputStream tarInputStream = createProjectFiles(integration.getSpec());
        logInfo(integration, "Created project files and starting build");
        try {
            return openShiftService().build(integration.getSpec().getName(), data, tarInputStream);
        } catch (InterruptedException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    private String deploy(IntegrationDeployment integration, DeploymentData data) throws IOException {
        logInfo(integration, "Starting deployment");
        String revision = openShiftService().deploy(integration.getSpec().getName(), data);
        logInfo(integration, "Deployment done");
        return revision;
    }

    private void setVersion(IntegrationDeployment integrationDeployment) {
        Integration integration = integrationDeployment.getIntegrationId().map(i -> dataManager.fetch(Integration.class, i)).orElseThrow(()-> new IllegalStateException("Integration not found!"));
        dataManager.update(new Integration.Builder().createFrom(integration).version(integrationDeployment.getVersion()).build());
    }

    private void deactivatePreviousDeployments(IntegrationDeployment integrationDeployment) {
        dataManager.fetchIdsByPropertyValue(IntegrationDeployment.class, "integrationId", integrationDeployment.getIntegrationId().get(), "targetState", IntegrationDeploymentState.Published.name())
            .stream()
            .map(id -> dataManager.fetch(IntegrationDeployment.class, id))
            .filter(r -> r.getVersion() != integrationDeployment.getVersion())
            .map(r -> r.unpublishing())
            .forEach(r -> {
                dataManager.update(r);
            });
    }

    private void updateDeploymentState(IntegrationDeployment integrationDeployment, IntegrationDeploymentState state) {
        IntegrationDeployment d = dataManager.fetch(IntegrationDeployment.class, integrationDeployment.getId().get());
        dataManager.update(d.withCurrentState(state));
    }


    // =================================================================================

    private boolean isBuildStarted(IntegrationDeployment integrationDeployment) {
        return openShiftService().isBuildStarted(integrationDeployment.getSpec().getName());
    }

    private boolean isBuildFailed(IntegrationDeployment integrationDeployment) {
        return openShiftService().isBuildFailed(integrationDeployment.getSpec().getName());
    }

    private boolean isReady(IntegrationDeployment integrationDeployment) {
        return openShiftService().isDeploymentReady(integrationDeployment.getSpec().getName());
    }

    public boolean isRunning(IntegrationDeployment integrationDeployment) {
        Map<String, String> labels = new HashMap<>();
        labels.put(OpenShiftService.INTEGRATION_ID_LABEL, Labels.validate(integrationDeployment.getIntegrationId().get()));
        labels.put(OpenShiftService.DEPLOYMENT_VERSION_LABEL, String.valueOf(integrationDeployment.getVersion()));
        return openShiftService().isScaled(integrationDeployment.getSpec().getName(), 1, labels);
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

    private static Integration integrationOf(IntegrationDeployment integrationDeployment) {
        return integrationDeployment.getSpec();
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

        return (int) openShiftService().getDeploymentsByLabel(labels)
            .stream()
            .filter(d -> !id.equals(d.getMetadata().getLabels().get(OpenShiftService.INTEGRATION_ID_LABEL)))
            .filter(d -> d.getSpec().getReplicas() > 0)
            .count();
    }

    /**
     * Check if Integration has active deployments.
     * @param deployment The specified {@link IntegrationDeployment}.
     * @return  The true if there are, false otherwise.
     */
    private boolean hasPublishedDeployments(IntegrationDeployment deployment) {
        Integration integration = deployment.getSpec();
        String id = Labels.validate(integration.getId().orElseThrow(() -> new IllegalStateException("Couldn't find the id of the integration")));
        String version = String.valueOf(integration.getVersion());

        Map<String, String> labels = new HashMap<>();
        labels.put(OpenShiftService.INTEGRATION_ID_LABEL, id);

        return (int) openShiftService().getDeploymentsByLabel(labels)
            .stream()
            .filter(d -> !version.equals(d.getMetadata().getLabels().get(OpenShiftService.DEPLOYMENT_VERSION_LABEL)))
            .filter(d -> d.getSpec().getReplicas() > 0)
            .count() > 0;
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
        String apply(T t, DeploymentData data) throws IOException;
    }

    private class BuildStepPerformer {
        private final Map<String, String> stepsPerformed;
        private final IntegrationDeployment integrationDeployment;

        BuildStepPerformer(IntegrationDeployment integrationDeployment) {
            this.integrationDeployment = integrationDeployment;
            this.stepsPerformed = new HashMap<>(integrationDeployment.getStepsDone());
        }

        void perform(String step, IoCheckedFunction<IntegrationDeployment> callable, DeploymentData data) throws IOException {
            if (!stepsPerformed.containsKey(step)) {
                    stepsPerformed.put(step, callable.apply(integrationDeployment, data));
                } else {
                    logInfo(integrationDeployment, "Skipped step {} because already performed", step);
                }
            }

        Map<String, String> getStepsPerformed() {
            return stepsPerformed;
        }
    }
}
