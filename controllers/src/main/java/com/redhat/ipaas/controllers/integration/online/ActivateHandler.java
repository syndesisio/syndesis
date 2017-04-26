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

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
import io.fabric8.funktion.model.StepKinds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivateHandler implements StatusChangeHandlerProvider.StatusChangeHandler {

    // Step used which should be performed only once per integration
    static final String STEP_GITHUB = "github-setup";
    static final String STEP_OPENSHIFT = "openshift-setup";

    private final DataManager dataManager;
    private final OpenShiftService openShiftService;
    private final GitHubService gitHubService;
    private final ProjectGenerator projectConverter;

    private final static Logger log = LoggerFactory.getLogger(ActivateHandler.class);

    ActivateHandler(DataManager dataManager, OpenShiftService openShiftService,
                    GitHubService gitHubService, ProjectGenerator projectConverter) {
        this.dataManager = dataManager;
        this.openShiftService = openShiftService;
        this.gitHubService = gitHubService;
        this.projectConverter = projectConverter;
    }

    public Set<Integration.Status> getTriggerStatuses() {
        return Collections.singleton(Integration.Status.Activated);
    }

    @Override
    public StatusUpdate execute(Integration integration) {
        if (!integration.getToken().isPresent()) {
            return new StatusUpdate(integration.getCurrentStatus().orElse(null), "No token present");
        }


        if (isTokenExpired(integration)) {
            log.info("{} : Token is expired", getLabel(integration));
            return new StatusUpdate(integration.getCurrentStatus().orElse(null), "Token is expired");
        }
        String token = storeToken(integration);

        Properties applicationProperties = extractApplicationPropertiesFrom(integration);

        OpenShiftDeployment deployment = OpenShiftDeployment
            .builder()
            .name(integration.getName())
            .replicas(1)
            .token(token)
            .applicationProperties(applicationProperties)
            .build();

        String secret = createSecret();

        // TODO: Verify Token and refresh if expired ....

        List<String> stepsPerformed = integration.getStepsDone().orElse(new ArrayList<>());
        try {
            String gitCloneUrl = null;
            if (!stepsPerformed.contains(STEP_GITHUB)) {
                String gitHubUser = getGitHubUser();
                log.info("{} : Looked up GitHub user {}", getLabel(integration), gitHubUser);
                Map<String, byte[]> projectFiles = createProjectFiles(gitHubUser, integration);
                log.info("{} : Created project files", getLabel(integration));

                gitCloneUrl = ensureGitHubSetup(integration, getWebHookUrl(deployment, secret), projectFiles);
                log.info("{} : Updated GitHub repo {}", getLabel(integration), gitCloneUrl);
                stepsPerformed.add(STEP_GITHUB);
            }

            if (!stepsPerformed.contains(STEP_OPENSHIFT)) {
                if (gitCloneUrl==null) {
                    gitCloneUrl = getCloneURL(integration);
                }
                createOpenShiftResources(integration.getName(), gitCloneUrl, secret, applicationProperties);
                log.info("{} : Created OpenShift resources", getLabel(integration));
                stepsPerformed.add(STEP_OPENSHIFT);
            }

            if (openShiftService.isScaled(deployment)) {
                return new StatusUpdate(Integration.Status.Activated, stepsPerformed);
            }
        } catch (Exception e) {
            log.info("{} : Failure", getLabel(integration), e);
        }
        return new StatusUpdate(Integration.Status.Pending, stepsPerformed);

    }

    protected String getCloneURL(Integration integration)  {
        try {
            return gitHubService.getCloneURL(Names.sanitize(integration.getName()));
        } catch (IOException e) {
            throw IPaasServerException.launderThrowable(e);
        }
    }

    private boolean isTokenExpired(Integration integration) {
        return integration.getToken().isPresent() &&
               Tokens.isTokenExpired(integration.getToken().get());
    }

    private String storeToken(Integration integration) {
        String token = integration.getToken().orElse(null);
        Tokens.setAuthenticationToken(token);
        return token;
    }

    private String getWebHookUrl(OpenShiftDeployment deployment, String secret) {
        return openShiftService.getGitHubWebHookUrl(deployment, secret);
    }

    private String ensureGitHubSetup(Integration integration, String webHookUrl, Map<String, byte[]> projectFiles) {
        try {
            // Do all github stuff at once
            String gitHubRepoName = Names.sanitize(integration.getName());
            String gitCloneUrl = gitHubService.createOrUpdateProjectFiles(gitHubRepoName, generateCommitMessage(), projectFiles, webHookUrl);

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

    private Map<String, byte[]> createProjectFiles(String gitHubUser, Integration integration) {
        try {
            GenerateProjectRequest request = ImmutableGenerateProjectRequest
                .builder()
                .integration(integration)
                .connectors(fetchConnectorsMap())
                .gitHubRepoName(Names.sanitize(integration.getName()))
                .gitHubUser(gitHubUser)
                .build();
            return projectConverter.generate(request);
        } catch (IOException e) {
            throw IPaasServerException.launderThrowable(e);
        }
    }

    private String getGitHubUser() {
        try {
            return gitHubService.getApiUser();
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

    private void createOpenShiftResources(String integrationName, String gitCloneUrl, String webHookSecret, Properties applicationProperties) {
        openShiftService.create(
            ImmutableOpenShiftDeployment.builder()
                                        .name(integrationName)
                                        .gitRepository(gitCloneUrl)
                                        .webhookSecret(webHookSecret)
                                        .applicationProperties(applicationProperties)
                                        .build());
    }

    /**
     * Creates a {@link Map} that contains all the configuration that corresponds to application.properties.
     * The configuration should include:
     *  i) component properties
     *  ii) sensitive endpoint properties that should be masked.
     * @param integration
     * @return
     */
    private Properties extractApplicationPropertiesFrom(Integration integration) {
        Properties secrets = new Properties();
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
                            String prefix = action.getCamelConnectorPrefix();
                            Connector connector = connectorMap.get(connectorId);

                            //Handle component data
                            secrets.putAll(connector.filterProperties(connection.getConfiguredProperties(),
                                connector.isComponentProperty(),
                                e -> prefix + "." + e.getKey(),
                                e -> e.getValue()));

                            secrets.putAll(connector.filterProperties(step.getConfiguredProperties().orElse(new HashMap<String,String>()),
                                connector.isComponentProperty(),
                                e -> prefix + "." + e.getKey(),
                                e -> e.getValue()));

                            //Handle sensitive data
                            secrets.putAll(connector.filterProperties(connection.getConfiguredProperties(),
                                connector.isSecret(),
                                e -> prefix + "." + e.getKey(),
                                e -> e.getValue()));

                            secrets.putAll(connector.filterProperties(step.getConfiguredProperties().orElse(new HashMap<String,String>()),
                                connector.isSecret(),
                                e -> prefix + "." + e.getKey(),
                                e -> e.getValue()));
                        });
                    });
                    continue;
                }
            }
        });
        return secrets;
    }

    private String getLabel(Integration integration) {
        return "Integration " + integration.getId().orElse("[none]");
    }

}
