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
package com.redhat.ipaas.controllers;

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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import io.fabric8.funktion.model.StepKinds;

@Component
public class ActivateHandler implements WorkflowHandler {

    private static final String WEBHOOK_FORMAT = "%s/namespaces/%s/buildconfigs/%s/webhooks/%s/github";

    public static final Set<Integration.Status> DESIRED_STATE_TRIGGERS =
        Collections.unmodifiableSet(new HashSet<>(Arrays.asList(Integration.Status.Activated)));

    @Value("${openshift.apiBaseUrl}")
    private String openshiftApiBaseUrl;

    @Value("${openshift.namespace}")
    private String namespace;

    private final DataManager dataManager;
    private final OpenShiftService openShiftService;
    private final GitHubService gitHubService;
    private final ProjectGenerator projectConverter;

    public ActivateHandler(DataManager dataManager, OpenShiftService openShiftService, GitHubService gitHubService, ProjectGenerator projectConverter) {
        this.dataManager = dataManager;
        this.openShiftService = openShiftService;
        this.gitHubService = gitHubService;
        this.projectConverter = projectConverter;
    }

    public Set<Integration.Status> getTriggerStatuses() {
        return DESIRED_STATE_TRIGGERS;
    }

    @Override
    public Optional<Integration.Status> execute(Integration integration)  {
        String token = integration.getToken().get();
        Tokens.setAuthenticationToken(token);

        OpenShiftDeployment deployment = OpenShiftDeployment
            .builder()
            .name(integration.getName())
            .replicas(1)
            .token(token)
            .build();

        Integration.Status currentStatus = openShiftService.isScaled(deployment)
            ? Integration.Status.Activated
            : Integration.Status.Pending;

        String secret = createSecret();
        String gitCloneUrl = ensureGitHubSetup(integration, secret);

        Integration updatedIntegration = new Integration.Builder()
            .createFrom(integration).gitRepo(gitCloneUrl)
            .currentStatus(currentStatus)
            .lastUpdated(new Date())
            .build();

        ensureOpenShiftResources(updatedIntegration, secret);
        return integration.getDesiredStatus();
    }

    private String ensureGitHubSetup(Integration integration, String secret) {
        try {
            Integration integrationWithGitRepoName = ensureGitRepoName(integration);
            String repoName = integrationWithGitRepoName.getGitRepo().orElseThrow(() -> new IllegalArgumentException("Missing git repo in integration"));

            GenerateProjectRequest request = ImmutableGenerateProjectRequest
                .builder()
                .integration(integrationWithGitRepoName)
                .connectors(connectorsMap())
                .build();

            Map<String, byte[]> fileContents = projectConverter.generate(request);

            // Secret to be used in the build trigger
            String webHookUrl = createWebHookUrl(repoName, secret);

            // Do all github stuff at once
            return gitHubService.createOrUpdateProjectFiles(repoName, integration.getToken().get(), generateCommitMessage(), fileContents, webHookUrl);

        } catch (IOException e) {
            throw IPaasServerException.launderThrowable(e);
        }
    }

    private String createWebHookUrl(String bcName, String secret) {
        return String.format(WEBHOOK_FORMAT, openshiftApiBaseUrl, namespace, bcName, secret);
    }

    private Integration ensureGitRepoName(Integration integration) {
        Optional<String> repoNameOptional = integration.getGitRepo();
        if (!repoNameOptional.isPresent()) {
            String generatedRepoName = Names.sanitize(integration.getName());
            return new Integration.Builder()
                .createFrom(integration).gitRepo(generatedRepoName)
                .build();
        }
        return integration;
    }

    private String generateCommitMessage() {
        // TODO Let's generate some nice message...
        return "Updated";
    }

    private String createSecret() {
        return UUID.randomUUID().toString();
    }

    private Map<String, Connector> connectorsMap() {
        return dataManager.fetchAll(Connector.class).getItems().stream().collect(Collectors.toMap(o -> o.getId().get(), o -> o));
    }

    private void ensureOpenShiftResources(Integration integration, String webHookSecret) {
        integration.getGitRepo().ifPresent(gitRepo -> openShiftService.create(ImmutableOpenShiftDeployment.builder()
            .name(integration.getName())
            .gitRepository(gitRepo)
            .webhookSecret(webHookSecret)
            .secretData(extractSecretsFrom(integration))
            .build()));
    }

    private Map<String, String> extractSecretsFrom(Integration integration) {
        Map<String, String> secrets = new HashMap<>();
        Map<String, Connector> connectorMap = connectorsMap();

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
                            secrets.putAll(connector.filterSecrets(step.getConfiguredProperties()));
                        });
                    });
                    continue;
                }
            }
        });
        return secrets;
    }
}
