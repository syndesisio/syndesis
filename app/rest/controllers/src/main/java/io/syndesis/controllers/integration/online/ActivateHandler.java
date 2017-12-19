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
import io.syndesis.dao.manager.EncryptionComponent;
import io.syndesis.controllers.integration.IntegrationSupport;
import io.syndesis.controllers.integration.StatusChangeHandlerProvider;
import io.syndesis.core.Names;
import io.syndesis.core.SyndesisServerException;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.IntegrationRevision;
import io.syndesis.model.integration.IntegrationRevisionState;
import io.syndesis.openshift.DeploymentData;
import io.syndesis.openshift.OpenShiftService;
import io.syndesis.project.converter.ProjectGenerator;

public class ActivateHandler extends BaseHandler implements StatusChangeHandlerProvider.StatusChangeHandler {

    private final DataManager dataManager;
    private final ProjectGenerator projectGenerator;
    private final ControllersConfigurationProperties properties;
    private final EncryptionComponent encryptionComponent;

    @SuppressWarnings("PMD.DefaultPackage")
    ActivateHandler(
            DataManager dataManager,
            OpenShiftService openShiftService,
            ProjectGenerator projectGenerator,
            ControllersConfigurationProperties properties,
            EncryptionComponent encryptionComponent) {

        super(openShiftService);

        this.dataManager = dataManager;
        this.projectGenerator = projectGenerator;
        this.properties = properties;
        this.encryptionComponent = encryptionComponent;
    }

    @Override
    public Set<Integration.Status> getTriggerStatuses() {
        return Collections.singleton(Integration.Status.Activated);
    }

    @Override
    public StatusUpdate execute(Integration integration) {

        final int maxIntegrationsPerUser = properties.getMaxIntegrationsPerUser();
        if (maxIntegrationsPerUser != ControllersConfigurationProperties.UNLIMITED) {
            int userIntegrations = countActiveIntegrationsOfSameUserAs(integration);
            if (userIntegrations >= maxIntegrationsPerUser) {
                //What the user sees.
                return new StatusUpdate(Integration.Status.Deactivated, "User has currently " + userIntegrations + " integrations, while the maximum allowed number is " + maxIntegrationsPerUser + ".");
            }
        }

        final int maxDeploymentsPerUser = properties.getMaxDeploymentsPerUser();
        if (maxDeploymentsPerUser != ControllersConfigurationProperties.UNLIMITED) {
            int userDeployments = countDeployments(integration);
            if (userDeployments >= maxDeploymentsPerUser) {
                //What we actually want to limit. So even though this should never happen, we still need to make sure.
                return new StatusUpdate(Integration.Status.Deactivated, "User has currently " + userDeployments + " deployments, while the maximum allowed number is " + maxDeploymentsPerUser + ".");
            }
        }

        logInfo(integration,"Build started: {}, isRunning: {}, Deployment ready: {}",
                isBuildStarted(integration), isRunning(integration), isReady(integration));
        BuildStepPerformer stepPerformer = new BuildStepPerformer(integration);
        logInfo(integration, "Steps performed so far: " + stepPerformer.getStepsPerformed());
        try {

            DeploymentData deploymentData = createDeploymentData(integration);
            stepPerformer.perform("build", this::build, deploymentData);
            stepPerformer.perform("deploy", this::deploy, deploymentData);
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            logError(integration,"[ERROR] Activation failure");
            // Setting a message to update means implicitly thats in an error state (for the UI)
            return new StatusUpdate(Integration.Status.Pending, e.getMessage());
        }

        // Set status to activate if finally running. Also clears the previous step which has been performed
        if (isRunning(integration)) {
            logInfo(integration, "[ACTIVATED] bc. integration is running with 1 pod");
            return new StatusUpdate(Integration.Status.Activated);
        }

        logInfo(integration,"Build started: {}, isRunning: {}, Deployment ready: {}",
                isBuildStarted(integration), isRunning(integration), isReady(integration));
        logInfo(integration, "[PENDING] [" + stepPerformer.getStepsPerformed() + "]");
        return new StatusUpdate(Integration.Status.Pending, stepPerformer.getStepsPerformed());
    }

    private DeploymentData createDeploymentData(Integration integration) {
        Properties applicationProperties = IntegrationSupport.buildApplicationProperties(integration, dataManager, encryptionComponent);
        IntegrationRevision revision = IntegrationRevision.createNewRevision(integration);
        String username = integration.getUserId().orElseThrow(() -> new IllegalStateException("Couldn't find the user of the integration"));

        return DeploymentData.builder()
            .addLabel(OpenShiftService.REVISION_ID_ANNOTATION, revision.getVersion().orElse(0).toString())
            .addLabel(OpenShiftService.USERNAME_LABEL, Names.sanitize(username))
            .addSecretEntry("application.properties", propsToString(applicationProperties))
            .build();
    }


    // =============================================================================
    // Various steps to perform:

    private void build(Integration integration, DeploymentData data) throws IOException {
        InputStream tarInputStream = createProjectFiles(integration);
        logInfo(integration, "Created project files and starting build");
        openShiftService().build(integration.getName(), data, tarInputStream);
    }

    private void deploy(Integration integration, DeploymentData data) throws IOException {
        logInfo(integration, "Starting deployment");
        openShiftService().deploy(integration.getName(), data);
        logInfo(integration, "Deployment done");
    }


    // =================================================================================

    private boolean isBuildStarted(Integration integration) {
        return openShiftService().isBuildStarted(integration.getName());
    }

    private boolean isReady(Integration integration) {
        return openShiftService().isDeploymentReady(integration.getName());
    }

    public boolean isRunning(Integration integration) {
        return openShiftService().isScaled(integration.getName(),1);
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

    /**
     * Counts active integrations (in DB) of the owner of the specified integration.
     * @param integration   The specified integration.
     * @return              The number of integrations (excluding the current).
     */
    private int countActiveIntegrationsOfSameUserAs(Integration integration) {
        String id = integration.getId().orElse(null);
        String username = integration.getUserId().orElseThrow(() -> new IllegalStateException("Couldn't find the user of the integration"));

        return (int) dataManager.fetchAll(Integration.class).getItems()
            .stream()
            .filter(i -> !i.idEquals(id)) //The "current" integration will already be in the database.
            .filter(i -> i.getUserId().map(username::equals).orElse(Boolean.FALSE))
            .filter(i -> IntegrationRevisionState.Active == i.getStatus())
            .count();
    }

    /**
     * Count the deployments of the owner of the specified integration.
     * @param integration   The specified integration.
     * @return              The number of deployed integrations (excluding the current).
     */
    private int countDeployments(Integration integration) {
        String name = integration.getName();
        String username = integration.getUserId().orElseThrow(() -> new IllegalStateException("Couldn't find the user of the integration"));

        Map<String, String> labels = new HashMap<>();
        labels.put(OpenShiftService.USERNAME_LABEL, Names.sanitize(username));

        return (int) openShiftService().getDeploymentsByLabel(labels)
            .stream()
            .filter(d -> !Names.sanitize(name).equals(d.getMetadata().getName())) //this is also called on updates (so we need to exclude)
            .filter(d -> d.getSpec().getReplicas() > 0)
            .count();
    }

    private InputStream createProjectFiles(Integration integration) {
        try {
            return projectGenerator.generate(integration);
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
        private final Integration integration;

        BuildStepPerformer(Integration integration) {
            this.integration = integration;
            this.stepsPerformed = new ArrayList<>(integration.getStepsDone());
        }

        /* default */ void perform(String step, IoCheckedFunction<Integration> callable, DeploymentData data) throws IOException {
            if (!stepsPerformed.contains(step)) {
                callable.apply(integration, data);
                stepsPerformed.add(step);
            } else {
                logInfo(integration, "Skipped step {} because already performed", step);
            }
        }

        /* default */ List<String> getStepsPerformed() {
            return stepsPerformed;
        }
    }
}
