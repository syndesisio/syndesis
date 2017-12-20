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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import io.syndesis.controllers.ControllersConfigurationProperties;
import io.syndesis.controllers.StateChangeHandler;
import io.syndesis.controllers.StateUpdate;
import io.syndesis.dao.manager.EncryptionComponent;
import io.syndesis.controllers.integration.IntegrationSupport;
import io.syndesis.core.Names;
import io.syndesis.core.SyndesisServerException;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.IntegrationRevision;
import io.syndesis.model.integration.IntegrationRevisionState;
import io.syndesis.openshift.DeploymentData;
import io.syndesis.openshift.OpenShiftService;
import io.syndesis.project.converter.ProjectGenerator;

public class ActivateHandler extends BaseHandler implements StateChangeHandler {

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
    public Set<IntegrationRevisionState> getTriggerStates() {
        return Collections.singleton(IntegrationRevisionState.Active);
    }

    @Override
    public StateUpdate execute(IntegrationRevision integrationRevision) {
        Integration integration = integrationOf(integrationRevision);

        final int maxIntegrationsPerUser = properties.getMaxIntegrationsPerUser();
        if (maxIntegrationsPerUser != ControllersConfigurationProperties.UNLIMITED) {
            int userIntegrations = countActiveIntegrationsOfSameUserAs(integration);
            if (userIntegrations >= maxIntegrationsPerUser) {
                //What the user sees.
                return new StateUpdate(IntegrationRevisionState.Inactive, "User has currently " + userIntegrations + " integrations, while the maximum allowed number is " + maxIntegrationsPerUser + ".");
            }
        }

        final int maxDeploymentsPerUser = properties.getMaxDeploymentsPerUser();
        if (maxDeploymentsPerUser != ControllersConfigurationProperties.UNLIMITED) {
            int userDeployments = countDeployments(integration);
            if (userDeployments >= maxDeploymentsPerUser) {
                //What we actually want to limit. So even though this should never happen, we still need to make sure.
                return new StateUpdate(IntegrationRevisionState.Inactive, "User has currently " + userDeployments + " deployments, while the maximum allowed number is " + maxDeploymentsPerUser + ".");
            }
        }

        logInfo(integrationRevision,"Build started: {}, isRunning: {}, Deployment ready: {}",
                isBuildStarted(integrationRevision), isRunning(integrationRevision), isReady(integrationRevision));
        BuildStepPerformer stepPerformer = new BuildStepPerformer(integrationRevision);
        logInfo(integrationRevision, "Steps performed so far: " + stepPerformer.getStepsPerformed());
        try {

            deactivatePreviousRevisions(integrationRevision);

            DeploymentData deploymentData = createDeploymentData(integration, integrationRevision);
            stepPerformer.perform("build", this::build, deploymentData);
            stepPerformer.perform("deploy", this::deploy, deploymentData);
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            logError(integrationRevision,"[ERROR] Activation failure");
            // Setting a message to update means implicitly thats in an error state (for the UI)
            return new StateUpdate(IntegrationRevisionState.Pending, e.getMessage());
        }

        // Set status to activate if finally running. Also clears the previous step which has been performed
        if (isRunning(integrationRevision)) {
            logInfo(integrationRevision, "[ACTIVATED] bc. integration is running with 1 pod");
            updateIntegration(integrationRevision, IntegrationRevisionState.Active);
            return new StateUpdate(IntegrationRevisionState.Active);
        }

        logInfo(integrationRevision,"Build started: {}, isRunning: {}, Deployment ready: {}",
                isBuildStarted(integrationRevision), isRunning(integrationRevision), isReady(integrationRevision));
        logInfo(integrationRevision, "[PENDING] [" + stepPerformer.getStepsPerformed() + "]");

        return new StateUpdate(IntegrationRevisionState.Pending, stepPerformer.getStepsPerformed());
    }

    private DeploymentData createDeploymentData(Integration integration, IntegrationRevision integrationRevision) {
        Properties applicationProperties = IntegrationSupport.buildApplicationProperties(integrationRevision, dataManager, encryptionComponent);

        String username = integration.getUserId().orElseThrow(() -> new IllegalStateException("Couldn't find the user of the integration"));

        return DeploymentData.builder()
            .addLabel(OpenShiftService.REVISION_ID_ANNOTATION, integrationRevision.getVersion().orElse(0).toString())
            .addLabel(OpenShiftService.USERNAME_LABEL, Names.sanitize(username))
            .addSecretEntry("application.properties", propsToString(applicationProperties))
            .build();
    }


    // =============================================================================
    // Various steps to perform:

    private void build(IntegrationRevision integrationRevision, DeploymentData data) throws IOException {
        Integration integration = integrationOf(integrationRevision);
        InputStream tarInputStream = createProjectFiles(integration, integrationRevision);
        logInfo(integrationRevision, "Created project files and starting build");
        openShiftService().build(integrationRevision.getName(), data, tarInputStream);
    }

    private void deploy(IntegrationRevision integration, DeploymentData data) throws IOException {
        logInfo(integration, "Starting deployment");
        openShiftService().deploy(integration.getName(), data);
        logInfo(integration, "Deployment done");
    }

    private void deactivatePreviousRevisions(IntegrationRevision revision) {
        dataManager.fetchIdsByPropertyValue(IntegrationRevision.class, "integrationId", revision.getIntegrationId().get(), "currentState", "Active")
            .stream()
            .map(id -> dataManager.fetch(IntegrationRevision.class, id))
            .filter(r -> r.getVersion().orElse(0) >= revision.getVersion().orElse(0))
            .map(r -> r.withCurrentState(IntegrationRevisionState.Undeployed))
            .forEach(r -> dataManager.update(r));
    }

    private void updateIntegration(IntegrationRevision revision, IntegrationRevisionState state) {
        Integration current = dataManager.fetch(Integration.class, revision.getIntegrationId().orElseThrow(() -> new IllegalStateException("IntegrationRevision should have an integrationId")));

        //Set the deployed revision id.
        Integration updated = new Integration.Builder().createFrom(current)
            .deployedRevisionId(revision.getVersion().get())
            .currentStatus(state)
            .build();

        dataManager.update(updated);
    }


    // =================================================================================

    private boolean isBuildStarted(IntegrationRevision integrationRevision) {
        return openShiftService().isBuildStarted(integrationRevision.getName());
    }

    private boolean isReady(IntegrationRevision integrationRevision) {
        return openShiftService().isDeploymentReady(integrationRevision.getName());
    }

    public boolean isRunning(IntegrationRevision integrationRevision) {
        return openShiftService().isScaled(integrationRevision.getName(),1);
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


    private Integration integrationOf(IntegrationRevision integrationRevision) {
        return  dataManager.fetch(Integration.class, integrationRevision.getIntegrationId().orElseThrow(() -> new IllegalStateException("Integration Revision doesn't have integration id.")));
    }

    /**
     * Counts active integrations (in DB) of the owner of the specified integration.
     * @param integration   The specified integration.
     * @return              The number of integrations (excluding the current).
     */
    private int countActiveIntegrationsOfSameUserAs(Integration integration) {
        String id = integration.getId().orElse(null);
        String username = integration.getUserId().orElseThrow(() -> new IllegalStateException("Couldn't find the user of the integration"));

        return (int) dataManager.fetchAll(IntegrationRevision.class).getItems()
            .stream()
            .filter(i -> !i.getIntegrationId().get().equals(id)) //The "current" integration will already be in the database.
            .filter(i -> IntegrationRevisionState.Active == i.getCurrentState())
            .map(i -> i.getIntegrationId().get())
            .distinct()
            .map(i -> dataManager.fetch(Integration.class, i))
            .filter(i -> i.getUserId().map(username::equals).orElse(Boolean.FALSE))
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

    private InputStream createProjectFiles(Integration integration, IntegrationRevision integrationRevision) {
        try {
            return projectGenerator.generate(integration, integrationRevision);
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
        private final IntegrationRevision integrationRevision;

        BuildStepPerformer(IntegrationRevision integrationRevision) {
            this.integrationRevision = integrationRevision;
            this.stepsPerformed = new ArrayList<>(integrationRevision.getStepsDone());
        }

        /* default */ void perform(String step, IoCheckedFunction<IntegrationRevision> callable, DeploymentData data) throws IOException {
            if (!stepsPerformed.contains(step)) {
                callable.apply(integrationRevision, data);
                stepsPerformed.add(step);
            } else {
                logInfo(integrationRevision, "Skipped step {} because already performed", step);
            }
        }

        /* default */ List<String> getStepsPerformed() {
            return stepsPerformed;
        }
    }
}
