/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.ipaas.controllers.integration.online;

import com.redhat.ipaas.controllers.integration.StatusChangeHandlerProvider;
import com.redhat.ipaas.core.IPaasServerException;
import com.redhat.ipaas.core.Names;
import com.redhat.ipaas.core.Tokens;
import com.redhat.ipaas.dao.manager.DataManager;
import com.redhat.ipaas.github.GitHubService;
import com.redhat.ipaas.model.connection.Connector;
import com.redhat.ipaas.model.integration.Integration;
import com.redhat.ipaas.model.integration.Step;
import com.redhat.ipaas.openshift.ImmutableOpenShiftDeployment;
import com.redhat.ipaas.openshift.OpenShiftDeployment;
import com.redhat.ipaas.openshift.OpenShiftService;
import com.redhat.ipaas.project.converter.GenerateProjectRequest;
import com.redhat.ipaas.project.converter.ImmutableGenerateProjectRequest;
import com.redhat.ipaas.project.converter.ProjectGenerator;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import io.fabric8.funktion.model.StepKinds;

public class ActivateHandler implements StatusChangeHandlerProvider.StatusChangeHandler {

    private final DataManager dataManager;
    private final OpenShiftService openShiftService;
    private final GitHubService gitHubService;
    private final ProjectGenerator projectConverter;

    ActivateHandler(DataManager dataManager, OpenShiftService openShiftService, GitHubService gitHubService, ProjectGenerator projectConverter) {
        this.dataManager = dataManager;
        this.openShiftService = openShiftService;
        this.gitHubService = gitHubService;
        this.projectConverter = projectConverter;
    }

    public Set<Integration.Status> getTriggerStatuses() {
        return Collections.singleton(Integration.Status.Activated);
    }

    @Override
    public StatusUpdate execute(Integration integration)  {
        if( !integration.getToken().isPresent() ) {
            return new StatusUpdate(integration.getCurrentStatus(), "No token present");
        }

        String token = integration.getToken().get();
        Tokens.setAuthenticationToken(token);

        OpenShiftDeployment deployment = OpenShiftDeployment
            .builder()
            .name(integration.getName())
            .replicas(1)
            .token(token)
            .build();

        String secret = createSecret();
        String gitCloneUrl = ensureGitHubSetup(integration, getWebHookUrl(deployment, secret));
        ensureOpenShiftResources(integration.getName(), gitCloneUrl, secret, extractSecretsFrom(integration));

        Integration.Status currentStatus = openShiftService.isScaled(deployment)
            ? Integration.Status.Activated
            : Integration.Status.Pending;

        return new StatusUpdate(currentStatus);
    }

    private String getWebHookUrl(OpenShiftDeployment deployment, String secret) {
        return openShiftService.getGitHubWebHookUrl(deployment, secret);
    }

    private String ensureGitHubSetup(Integration integration, String webHookUrl) {
        try {
            String gitHubRepoName = Names.sanitize(integration.getName());

            GenerateProjectRequest request = ImmutableGenerateProjectRequest
                .builder()
                .integration(integration)
                .connectors(fetchConnectorsMap())
                .gitHubRepoName(gitHubRepoName)
                .gitHubUser(gitHubService.getApiUser())
                .build();

            Map<String, byte[]> fileContents = projectConverter.generate(request);

            // Do all github stuff at once
            String gitCloneUrl = gitHubService.createOrUpdateProjectFiles(gitHubRepoName, generateCommitMessage(), fileContents, webHookUrl);

            // Update integration within DB. Maybe re-read it before updating the URL ? Best: Add a dedicated 'updateGitRepo()'
            // method to the backend
            Integration updatedIntegration = new Integration.Builder()
                .createFrom(integration)
                .gitRepo(gitCloneUrl)
                .build();
            dataManager.update(updatedIntegration);

            return gitCloneUrl;
        } catch (IOException e) {
            throw IPaasServerException.launderThrowable(e);
        }
    }

    private String generateCommitMessage() {
        // TODO Let's generate some nice message...
        return "Updated";
    }

    private String createSecret() {
        return UUID.randomUUID().toString();
    }

    private Map<String, Connector> fetchConnectorsMap() {
        return dataManager.fetchAll(Connector.class).getItems().stream().collect(Collectors.toMap(o -> o.getId().get(), o -> o));
    }

    private void ensureOpenShiftResources(String integrationName, String gitCloneUrl, String webHookSecret, Map<String, String> secretData) {
        openShiftService.create(
            ImmutableOpenShiftDeployment.builder()
                                        .name(integrationName)
                                        .gitRepository(gitCloneUrl)
                                        .webhookSecret(webHookSecret)
                                        .secretData(secretData)
                                        .build());
    }

    private Map<String, String> extractSecretsFrom(Integration integration) {
        Map<String, String> secrets = new HashMap<>();
        Map<String, Connector> connectorMap = fetchConnectorsMap();

        integration.getSteps().ifPresent(steps -> {
            for (Step step : steps) {
                if (step.getStepKind().equals(StepKinds.ENDPOINT)) {
                    step.getAction().ifPresent(action -> {
                        step.getConnection().ifPresent(connection -> {
                            String connectorId = step.getConnection().get().getConnectorId().orElse(action.getConnectorId());
                            if (!connectorMap.containsKey(connectorId)) {
                                throw new IllegalStateException("Connector:[" + connectorId + "] not found.");
                            }
                            Connector connector = connectorMap.get(connectorId);
                            secrets.putAll(connector.filterSecrets(connection.getConfiguredProperties()));
                            secrets.putAll(connector.filterSecrets(step.getConfiguredProperties().orElse(new HashMap<String,String>())));
                        });
                    });
                    continue;
                }
            }
        });
        return secrets;
    }
}
