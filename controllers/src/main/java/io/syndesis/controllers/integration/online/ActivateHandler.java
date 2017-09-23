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
package io.syndesis.controllers.integration.online;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.fabric8.kubernetes.client.RequestConfigBuilder;
import io.syndesis.controllers.ControllersConfigurationProperties;
import io.syndesis.controllers.integration.StatusChangeHandlerProvider;
import io.syndesis.core.Names;
import io.syndesis.core.SyndesisServerException;
import io.syndesis.core.Tokens;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.github.GitHubService;
import io.syndesis.github.GithubRequest;
import io.syndesis.integration.model.steps.Endpoint;
import io.syndesis.model.WithConfigurationProperties;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.IntegrationRevision;
import io.syndesis.model.integration.Step;
import io.syndesis.openshift.OpenShiftDeployment;
import io.syndesis.openshift.OpenShiftService;
import io.syndesis.project.converter.GenerateProjectRequest;
import io.syndesis.project.converter.ProjectGenerator;
import org.eclipse.egit.github.core.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivateHandler implements StatusChangeHandlerProvider.StatusChangeHandler {

    // Step used which should be performed only once per integration
    /* default */ static final String STEP_GITHUB = "github-setup";
    /* default */ static final String STEP_OPENSHIFT = "openshift-setup";

    private final DataManager dataManager;
    private final OpenShiftService openShiftService;
    private final GitHubService gitHubService;
    private final ProjectGenerator projectConverter;
    private final ControllersConfigurationProperties properties;

    private static final Logger LOG = LoggerFactory.getLogger(ActivateHandler.class);

    /* default */ ActivateHandler(DataManager dataManager, OpenShiftService openShiftService,
                                  GitHubService gitHubService, ProjectGenerator projectConverter, ControllersConfigurationProperties properties) {
        this.dataManager = dataManager;
        this.openShiftService = openShiftService;
        this.gitHubService = gitHubService;
        this.projectConverter = projectConverter;
        this.properties = properties;
    }

    @Override
    public Set<Integration.Status> getTriggerStatuses() {
        return Collections.singleton(Integration.Status.Activated);
    }

    @Override
    public StatusUpdate execute(Integration integration) {
        if (!integration.getToken().isPresent()) {
            return new StatusUpdate(integration.getCurrentStatus().orElse(null), "No token present");
        }

        if (isTokenExpired(integration)) {
            LOG.info("{} : Token is expired", getLabel(integration));
            return new StatusUpdate(integration.getCurrentStatus().orElse(null), "Token is expired");
        }

        int userIntegrations = countIntegrations(integration);
        if (userIntegrations >= properties.getMaxIntegrationsPerUser()) {
            //What the user sees.
            return new StatusUpdate(Integration.Status.Deactivated, "User has currently " + userIntegrations + " integrations, while the maximum allowed number is " + properties.getMaxIntegrationsPerUser() + ".");
        }

        int userDeployments = countDeployments(integration);
        if (userDeployments >= properties.getMaxDeploymentsPerUser()) {
            //What we actually want to limit. So even though this should never happen, we still need to make sure.
            return new StatusUpdate(Integration.Status.Deactivated, "User has currently " + userDeployments + " deployments, while the maximum allowed number is " + properties.getMaxDeploymentsPerUser() + ".");
        }


        String projectName = Names.sanitize(integration.getName());
        String token = storeToken(integration);
        Properties applicationProperties = extractApplicationPropertiesFrom(integration);
        IntegrationRevision revision = IntegrationRevision.createNewRevision(integration);
        String username = integration.getUserId().orElseThrow(() -> new IllegalStateException("Couldn't find the user of the integration"));
        String secret = createSecret();
        String gitCloneUrl = getCloneURL(integration);
        // TODO: Verify Token and refresh if expired ....

        List<String> stepsPerformed = integration.getStepsDone().orElseGet(ArrayList::new);
        try {
            if (!stepsPerformed.contains(STEP_GITHUB)) {
                User gitHubUser = getGitHubUser();
                String githubUsername = gitHubUser.getLogin();
                LOG.info("{} : Looked up GitHub user {}", getLabel(integration), githubUsername);
                Map<String, byte[]> projectFiles = createProjectFiles(githubUsername, integration);
                LOG.info("{} : Created project files", getLabel(integration));

                gitCloneUrl = ensureGitHubSetup(integration, gitHubUser, openShiftService.getGitHubWebHookUrl(projectName, secret), projectFiles);
                LOG.info("{} : Updated GitHub repo {}", getLabel(integration), gitCloneUrl);
                stepsPerformed.add(STEP_GITHUB);
            }

             OpenShiftDeployment deployment = OpenShiftDeployment
                    .builder()
                    .name(integration.getName())
                    .username(username)
                    .revisionId(revision.getVersion().get())
                    .replicas(1)
                    .token(token)
                    .gitRepository(Optional.ofNullable(gitCloneUrl))
                    .webhookSecret(secret)
                    .applicationProperties(applicationProperties)
                    .build();

            if (!stepsPerformed.contains(STEP_OPENSHIFT)) {
                openShiftService.create(deployment);

                LOG.info("{} : Created OpenShift resources", getLabel(integration));
                stepsPerformed.add(STEP_OPENSHIFT);
            }

            if (openShiftService.isScaled(deployment)) {
                return new StatusUpdate(Integration.Status.Activated, stepsPerformed);
            }
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            LOG.error("{} : Failure", getLabel(integration), e);
        }
        return new StatusUpdate(Integration.Status.Pending, stepsPerformed);
    }

    protected String getCloneURL(Integration integration)  {
        try {
            return gitHubService.getCloneURL(Names.sanitize(integration.getName()));
        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable(e);
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


    /**
     * Count the integrations (in DB) of the owner of the specified integration.
     * @param integration   The specified integration.
     * @return              The number of integrations (excluding the current).
     */
    private int countIntegrations(Integration integration) {
        String id = integration.getId().orElse(null);
        String username = integration.getUserId().orElseThrow(() -> new IllegalStateException("Couldn't find the user of the integration"));

        return (int) dataManager.fetchAll(Integration.class).getItems()
            .stream()
            .filter(i -> i.getId().isPresent() && !i.getId().get().equals(id)) //The "current" integration will already be in the database.
            .filter(i -> username.equals(i.getUserId().orElse(null)))
            .filter(i -> i.getStatus().isPresent() && (Integration.Status.Activated.equals(i.getCurrentStatus().orElse(null))) ||
                                                       Integration.Status.Activated.equals(i.getCurrentStatus().orElse(null)))
            .count();
    }

    /**
     * Count the deployments of the owner of the specified integration.
     * @param integration   The specified integration.
     * @return              The number of deployed integrations (excluding the current).
     */
    private int countDeployments(Integration integration) {
        String name = integration.getName();
        String token = integration.getToken().orElse(null);
        String username = integration.getUserId().orElseThrow(() -> new IllegalStateException("Couldn't find the user of the integration"));

        Map<String, String> labels = new HashMap<>();
        labels.put(OpenShiftService.USERNAME_LABEL, Names.sanitize(username));

        return (int) openShiftService.getDeploymentsByLabel(new RequestConfigBuilder().withOauthToken(token).build(), labels)
            .stream()
            .filter(d -> !Names.sanitize(name).equals(d.getMetadata().getName())) //this is also called on updates (so we need to exclude)
            .filter(d -> d.getSpec().getReplicas() > 0)
            .count();
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod") // PMD false positive
    private String ensureGitHubSetup(Integration integration, User githubUser, String webHookUrl, Map<String, byte[]> projectFiles) {
        // Do all github stuff at once
        String gitHubRepoName = Names.sanitize(integration.getName());
        GithubRequest request = new GithubRequest.Builder()
            .repoName(gitHubRepoName)
            .author(githubUser)
            .commitMessage(generateCommitMessage())
            .fileContents(projectFiles)
            .webHookUrl(webHookUrl)
            .build();

        String gitCloneUrl = gitHubService.createOrUpdateProjectFiles(request);

        // Update integration within DB. Maybe re-read it before updating the URL ? Best: Add a dedicated 'updateGitRepo()'
        // method to the backend
        Integration updatedIntegration = new Integration.Builder()
            .createFrom(integration)
            .gitRepo(gitCloneUrl)
            .build();
        dataManager.update(updatedIntegration);
        return gitCloneUrl;
    }

    private Map<String, byte[]> createProjectFiles(String username, Integration integration) {
        try {
            Map<String, Connector> connectorMap = fetchConnectorsMap();

            GenerateProjectRequest request = new GenerateProjectRequest.Builder()
                .integration(integration)
                .connectors(connectorMap)
                .gitHubRepoName(Names.sanitize(integration.getName()))
                .gitHubUserLogin(username)
                .build();
            return projectConverter.generate(request);
        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    private User getGitHubUser() {
        try {
            return gitHubService.getApiUser();
        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable(e);
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
                if (step.getStepKind().equals(Endpoint.KIND)) {
                    step.getAction().ifPresent(action -> {
                        step.getConnection().ifPresent(connection -> {
                            String connectorId = connection.getConnectorId().orElseGet(action::getConnectorId);
                            if (!connectorMap.containsKey(connectorId)) {
                                throw new IllegalStateException("Connector:[" + connectorId + "] not found.");
                            }

                            final String connectorPrefix = action.getCamelConnectorPrefix();
                            final Connector connector = connectorMap.get(connectorId);
                            final Map<String, String> properties = aggregate(connection.getConfiguredProperties(), step.getConfiguredProperties().orElseGet(Collections::emptyMap));

                            final Function<Map.Entry<String, String>, String> componentKeyConverter;
                            final Function<Map.Entry<String, String>, String> secretKeyConverter;

                            // Enable configuration aliases only if the connector
                            // has component options otherwise it does not get
                            // configured by camel.
                            if (hasComponentProperties(properties, connector, action)) {
                                // The connector id is marked as optional thus if the
                                // id is not provided it is also not possible to create
                                // a connector configuration alias.
                                //
                                // if th id is set this generate something like:
                                //
                                //     twitter-search.configurations.twitter-search-1.propertyName
                                //
                                // otherwise it fallback to
                                //
                                //     twitter-search.propertyName
                                //
                                // NOTE: model should be more clear about what fields
                                //       should be there and what are effectively
                                //       optional so it maybe useful to leverage
                                //       annotation such as @NotNull, @Nullable.
                                componentKeyConverter = e -> connection.getId().map(
                                    id -> String.join(".", connectorPrefix, "configurations", connectorPrefix + "-" + id, e.getKey()).toString()
                                ).orElseGet(
                                    () -> String.join(".", connectorPrefix, e.getKey()).toString()
                                );
                            } else {
                                componentKeyConverter = e -> String.join(".", connectorPrefix, e.getKey()).toString();
                            }

                            // Secrets does not follow the component convention so
                            // the property is always flattered at connector level
                            //
                            // if th id is set this generate something like:
                            //
                            //     twitter-search-1.propertyName
                            //
                            // otherwise it fallback to
                            //
                            //     twitter-search.propertyName
                            //
                            secretKeyConverter = e -> connection.getId().map(
                                id -> String.join(".", connectorPrefix + "-" + id, e.getKey()).toString()
                            ).orElseGet(
                                () -> String.join(".", connectorPrefix, e.getKey()).toString()
                            );

                            // Merge properties set on connection and step and
                            // create secrets for component options or for sensitive
                            // information.
                            //
                            // NOTE: if an option is both a component option and
                            //       a sensitive information it is then only added
                            //       to the component configuration to avoid dups
                            //       and possible error at runtime.
                            properties.entrySet().stream()
                                .filter(or(connector::isSecretOrComponentProperty, action::isSecretOrComponentProperty))
                                .distinct()
                                .forEach(
                                    e -> {
                                        if (connector.isComponentProperty(e) || action.isComponentProperty(e)) {
                                            secrets.put(componentKeyConverter.apply(e), e.getValue());
                                        } else if (connector.isSecret(e) || action.isSecret(e)) {
                                            secrets.put(secretKeyConverter.apply(e), e.getValue());
                                        }
                                    }
                                );
                        });
                    });
                }
            }
        });
        return secrets;
    }

    private String getLabel(Integration integration) {
        return "Integration " + integration.getId().orElse("[none]");
    }

    private static <K, V> Map<K, V> aggregate(Map<K, V> ... maps) {
        return Stream.of(maps)
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue));
    }

    private static <T> Predicate<T> or(Predicate<T>... predicates) {
        Predicate<T> predicate = predicates[0];

        for (int i = 1; i < predicates.length; i++) {
            predicate = predicate.or(predicates[i]);
        }

        return predicate;
    }

    private static boolean hasComponentProperties(Map<String, String> properties, WithConfigurationProperties... withConfigurationProperties) {
        for (WithConfigurationProperties wcp : withConfigurationProperties) {
            if (properties.entrySet().stream().anyMatch(wcp::isComponentProperty)) {
                return true;
            }
        }

        return false;
    }
}
