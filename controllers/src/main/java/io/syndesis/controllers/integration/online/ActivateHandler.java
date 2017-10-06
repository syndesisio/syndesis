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
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.syndesis.controllers.ControllersConfigurationProperties;
import io.syndesis.controllers.integration.StatusChangeHandlerProvider;
import io.syndesis.core.Names;
import io.syndesis.core.SyndesisServerException;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.integration.model.steps.Endpoint;
import io.syndesis.model.WithConfigurationProperties;
import io.syndesis.model.connection.Action;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.IntegrationRevision;
import io.syndesis.model.integration.Step;
import io.syndesis.openshift.DeploymentData;
import io.syndesis.openshift.OpenShiftService;
import io.syndesis.project.converter.GenerateProjectRequest;
import io.syndesis.project.converter.ProjectGenerator;

public class ActivateHandler extends BaseHandler implements StatusChangeHandlerProvider.StatusChangeHandler {

    private final DataManager dataManager;
    private final ProjectGenerator projectConverter;
    private final ControllersConfigurationProperties properties;

    /* default */ ActivateHandler(DataManager dataManager, OpenShiftService openShiftService,
                                  ProjectGenerator projectConverter, ControllersConfigurationProperties properties) {
        super(openShiftService);
        this.dataManager = dataManager;
        this.projectConverter = projectConverter;
        this.properties = properties;
    }

    @Override
    public Set<Integration.Status> getTriggerStatuses() {
        return Collections.singleton(Integration.Status.Activated);
    }

    @Override
    public StatusUpdate execute(Integration integration) {

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

        logInfo(integration,"Build started: {}, isRunning: {}, Deployment ready: {}",
                isBuildStarted(integration), isRunning(integration), isReady(integration));
        BuildStepPerformer stepPerformer = new BuildStepPerformer(integration);
        logInfo(integration, "Steps performed so far: " + stepPerformer.getStepsPerformed());
        try {
            stepPerformer.perform("setup", this::setup);
            stepPerformer.perform("build", this::build);
            stepPerformer.perform("deploy", this::deploy);
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            logError(integration,"[ERROR] Activation failure");
            // Setting a message to update means implictely thats in an error state (for the UI)
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


    // =============================================================================
    // Various steps to perform:

    private void setup(Integration integration) throws IOException {
        Properties applicationProperties = extractApplicationPropertiesFrom(integration);
        IntegrationRevision revision = IntegrationRevision.createNewRevision(integration);
        String username = integration.getUserId().orElseThrow(() -> new IllegalStateException("Couldn't find the user of the integration"));
        String name = integration.getName();

        DeploymentData deploymentData = DeploymentData.builder()
                                                      .addLabel(OpenShiftService.REVISION_ID_ANNOTATION, revision.getVersion().orElse(0).toString())
                                                      .addAnnotation(OpenShiftService.USERNAME_LABEL, username)
                                                      .addSecretEntry("application.properties", propsToString(applicationProperties))
                                                      .build();

        openShiftService().setup(name, deploymentData);
        logInfo(integration, "Ensured OpenShift resources");
    }

    private void build(Integration integration) throws IOException {
        InputStream tarInputStream = createProjectFiles(integration);
        logInfo(integration, "Created project files and starting build");
        openShiftService().build(integration.getName(), tarInputStream);
    }

    private void deploy(Integration integration) throws IOException {
        logInfo(integration, "Starting deployment");
        openShiftService().deploy(integration.getName());
        logInfo(integration, "Deployment done");
    }


    // =================================================================================

    private boolean isBuildStarted(Integration integration) {
        return openShiftService().isBuildStarted(integration.getName());
    }

    private boolean isReady(Integration integration) {
        return openShiftService().isReady(integration.getName());
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
            GenerateProjectRequest request = new GenerateProjectRequest.Builder()
                .integration(integration)
                .connectors(fetchConnectorsMap())
                .build();
            return projectConverter.generate(request);
        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
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
        final Properties secrets = new Properties();
        final Map<String, Connector> connectorMap = fetchConnectorsMap(dataManager);
        final Map<Step, String> connectorIdMap = buildConnectorSuffixMap(integration);

        integration.getSteps().ifPresent(steps -> {
            steps.stream()
                .filter(step -> step.getStepKind().equals(Endpoint.KIND))
                .filter(step -> step.getAction().isPresent())
                .filter(step -> step.getConnection().isPresent())
                .forEach(step -> {
                    final Action action = step.getAction().get();
                    final Connection connection = step.getConnection().get();
                    final String connectorId = connection.getConnectorId().orElseGet(action::getConnectorId);

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
                    //
                    // The real connector id is calculated from the
                    // number of instances a connector should be
                    // instantiated.
                    //
                    // Example:
                    //
                    // if the twitter-search-connector is used twice,
                    // secrets will be in the form
                    //
                    //     twitter-search-connector.configurations.twitter-search-connector-1.propertyName
                    //
                    // otherwise it fallback to
                    //
                    //     twitter-search-connector.propertyName
                    if (hasComponentProperties(properties, connector, action) && connectorIdMap.containsKey(step)) {
                        componentKeyConverter = e -> String.join(".", connectorPrefix, "configurations", connectorPrefix + "-" + connectorIdMap.get(step), e.getKey()).toString();
                    } else {
                        componentKeyConverter = e -> String.join(".", connectorPrefix, e.getKey()).toString();
                    }

                    // Secrets does not follow the component convention so
                    // the property is always flattered at connector level
                    //
                    // Example:
                    //
                    //     twitter-search-connector-1.propertyName
                    //
                    // otherwise it fallback to
                    //
                    //     witter-search-connector.propertyName
                    //
                    if (connectorIdMap.containsKey(step)) {
                        secretKeyConverter = e -> String.join(".", connectorPrefix + "-" + connectorIdMap.get(step), e.getKey()).toString();
                    } else {
                        secretKeyConverter = e -> String.join(".", connectorPrefix, e.getKey()).toString();
                    }

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
            }
        );

        return secrets;
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

    // ===============================================================================
    // Some helper method to conditional execute certain steps
    @FunctionalInterface
    public interface IoCheckedFunction<T> {
         void apply(T t) throws IOException;
    }

    private class BuildStepPerformer {
        private final List<String> stepsPerformed;
        private final Integration integration;

        BuildStepPerformer(Integration integration) {
            this.integration = integration;
            this.stepsPerformed = integration.getStepsDone().isPresent() ?
                new ArrayList<>(integration.getStepsDone().get()) : new ArrayList<>();
        }

        /* default */ void perform(String step, IoCheckedFunction<Integration> callable) throws IOException {
            if (!stepsPerformed.contains(step)) {
                callable.apply(integration);
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
