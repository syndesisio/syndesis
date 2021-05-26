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
package io.syndesis.server.update.controller.bulletin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.validation.Validator;

import io.syndesis.common.model.ChangeEvent;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.bulletin.IntegrationBulletinBoard;
import io.syndesis.common.model.bulletin.LeveledMessage;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.support.Equivalencer;
import io.syndesis.common.model.validation.TargetWithDomain;
import io.syndesis.common.model.validation.connection.ConnectionWithDomain;
import io.syndesis.common.model.validation.integration.IntegrationWithDomain;
import io.syndesis.common.util.CollectionsUtils;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;

/**
 * This class handles updates on {@link Integration} and related resources and
 * generates related {@link IntegrationBulletinBoard}.
 */
public class IntegrationUpdateHandler extends AbstractResourceUpdateHandler<IntegrationBulletinBoard> {
    private final List<Kind> supportedKinds;

    public IntegrationUpdateHandler(final DataManager dataManager, final EncryptionComponent encryptionComponent, final Validator validator) {
        super(dataManager, encryptionComponent, validator);

        supportedKinds = Arrays.asList(Kind.Integration, Kind.Connector, Kind.Connection, Kind.Extension);
    }

    @Override
    public boolean canHandle(final ChangeEvent event) {
        return event.getKind().map(Kind::from).filter(supportedKinds::contains).isPresent();
    }

    /**
     * Compares the given connection with the connection fetched from the data
     * manager. Adds a message with the missing code if there is no connection
     * available or adds a message with the difference code if the connections
     * differ in their metadata.
     * @param connection the connection to compare
     * @param dbConnection the connection from the data manager
     * @param messages the messages collection to add the message to
     * @param supplier the supplier for generating the message
     * @param missingCode the code if the connection is missing
     * @param differenceCode the code if the connection has different metadata
     */
    private void compareConnection(
        final Connection connection,
        final Connection dbConnection,
        final List<LeveledMessage> messages,
        final Supplier<LeveledMessage.Builder> supplier,
        final LeveledMessage.Code missingCode,
        final LeveledMessage.Code differenceCode) {

        // There's no connection with the given id so this means
        // that the connection has been deleted or it is not present
        if (dbConnection == null) {
            messages.add(
                supplier.get()
                    .level(LeveledMessage.Level.WARN)
                    .code(missingCode)
                    .build());
        } else {
            //
            // Determine whether the integration's nested connection is
            // still equivalent to the connection in the database. If not then
            // flag a warning.
            //
            final Connection withConnectorConnection = includeConnector(dbConnection);
            final Equivalencer equiv = new Equivalencer();
            if (!equiv.equivalent(connection, withConnectorConnection)) {
                final String message = equiv.failureMessage();
                messages.add(
                    supplier.get()
                        .level(LeveledMessage.Level.WARN)
                        .code(differenceCode)
                        .detail(message)
                        .build());
            }
        }
    }

    private Collection<? extends LeveledMessage> computeDeploymentDifferences(final Integration integration,
        final Collection<IntegrationDeployment> allDeployments) {
        final List<IntegrationDeployment> deployments = allDeployments.stream()
            .filter(d -> d.getIntegrationId().equals(integration.getId()))
            .filter(d -> d.getCurrentState().equals(IntegrationDeploymentState.Published))
            .collect(Collectors.toList());

        if (deployments.isEmpty()) {
            // Nothing to do as no deployments have been published
            return Collections.emptyList();
        }

        final List<LeveledMessage> messages = new ArrayList<>();

        for (final IntegrationDeployment deployment : deployments) {
            final Supplier<LeveledMessage.Builder> supplier = () -> new LeveledMessage.Builder().putMetadata("deployment", deployment.getId().get());
            final Integration deployedInteg = deployment.getSpec();

            // **********************
            // Draft Integration
            // **********************

            final Equivalencer equiv = new Equivalencer();
            if (!equiv.equivalent(integration, deployedInteg)) {
                final String message = equiv.failureMessage();
                messages.add(
                    supplier.get()
                        .code(LeveledMessage.Code.SYNDESIS012)
                        .level(LeveledMessage.Level.WARN)
                        .detail(message)
                        .build());

                //
                // Deployment is already stale in comparison to its source
                // integration
                // so no need to check the connections as well and duplicate the
                // messages
                //
                continue;
            }

            // **********************
            // Connections
            // **********************

            for (final Step deployedStep : deployedInteg.getSteps()) {
                deployedStep.getAction()
                    .filter(ConnectorAction.class::isInstance)
                    .map(ConnectorAction.class::cast)
                    .ifPresent(action -> {
                        if (!deployedStep.getConnection().isPresent()) {
                            return;
                        }

                        //
                        // Compare the connection in the deployed integration's
                        // step
                        // with the connection from the data manager and log the
                        // result using the given message codes
                        //
                        final Connection connection = deployedStep.getConnection().get();
                        final Connection dbConnection = getDataManager().fetch(Connection.class, connection.getId().get());

                        compareConnection(
                            connection,
                            dbConnection,
                            messages,
                            supplier,
                            LeveledMessage.Code.SYNDESIS009,
                            LeveledMessage.Code.SYNDESIS012);
                    });
            }
        }

        return Collections.unmodifiableList(messages);
    }

    private Connection includeConnector(final Connection connection) {
        if (connection == null) {
            return connection;
        }

        final Optional<Connector> connectorOptional = connection.getConnector();
        if (connectorOptional != null && connectorOptional.isPresent()) {
            return connection;
        }

        final Connector connector = getDataManager().fetch(Connector.class, connection.getConnectorId());
        if (connector == null) {
            return connection;
        }

        final Connection.Builder builder = new Connection.Builder();
        return builder.createFrom(connection)
            .connector(connector)
            .build();
    }

    /**
     * When a resource is changed, recalculate and redeploy integrations
     * @param event the event.
     * @return the list of integration bulletin boards
     */
    @Override
    protected List<IntegrationBulletinBoard> compute(final ChangeEvent event) {
        final List<IntegrationBulletinBoard> boards = new ArrayList<>();
        final DataManager dataManager = getDataManager();

        /*
         * Assemble all the artifact collections that are going to be checked in
         * the data manager. This does it once rather than repeatedly in the
         * various loops since getting stuff from the DataManager is quite
         * expensive and slow.
         * see #3717
         */
        final List<Integration> integrations = dataManager.fetchAll(Integration.class).getItems();
        final List<IntegrationDeployment> deployments = dataManager.fetchAll(IntegrationDeployment.class).getItems();
        final Set<Integration> deployedIntegrations = deployments.stream()
            .map(IntegrationDeployment::getSpec)
            .collect(Collectors.toSet());
        final List<Connection> connections = dataManager.fetchAll(Connection.class).getItems();

        final Collection<Integration> allIntegrations =
            new ArrayList<>(integrations.size() + deployedIntegrations.size());
        allIntegrations.addAll(integrations);
        allIntegrations.addAll(deployedIntegrations);

        // Now iterate all existing integrations and update them if needed
        for (final Integration integration : integrations) {
            updateIntegration(event, boards, dataManager, deployments,
                                connections, allIntegrations,
                                integration);
        }

        return boards;
    }

    /**
     * For each integration existing in the platform, checks the flow (steps) and update it.
     * The message boards {@see IntegrationBulletinBoard} will be updated with any info, warn or error.
     * @param event change event that triggered this action
     * @param boards list of bulletin boards
     * @param dataManager helper to get data
     * @param deployments list of integration deployments
     * @param connections list of available connections
     * @param allIntegrations list of all integrations
     * @param integration specific integration we want to update
     */
    private void updateIntegration(ChangeEvent event, List<IntegrationBulletinBoard> boards, DataManager dataManager,
                                    List<IntegrationDeployment> deployments, List<Connection> connections,
                                    Collection<Integration> allIntegrations, Integration integration) {

        //Get integration details
        final String id = integration.getId().get();
        final String version = Integer.toString(integration.getVersion());

        //Create a board to show messages to the user
        final IntegrationBulletinBoard board = dataManager.fetchByPropertyValue(IntegrationBulletinBoard.class, "targetResourceId", id).orElse(null);
        final IntegrationBulletinBoard.Builder builder = getBoardBuilder(id, version, board);
        final List<LeveledMessage> messages = new ArrayList<>();

        // Iterates through the flow to process each step with the connectors and actions.
        // If there is no change, this shouldn't change the integration, in theory.
        processAndUpdateFlow(event, dataManager, connections, allIntegrations, messages, integration);
        messages.addAll(computeDeploymentDifferences(integration, deployments));

        //Update the board to show messages to the user
        builder.errors(countMessagesWithLevel(LeveledMessage.Level.ERROR, messages));
        builder.warnings(countMessagesWithLevel(LeveledMessage.Level.WARN, messages));
        builder.notices(countMessagesWithLevel(LeveledMessage.Level.INFO, messages));
        builder.messages(messages);

        //If the board is not already in the list of boards, add it
        final IntegrationBulletinBoard updated = builder.build();
        if (!updated.equals(board)) {
            boards.add(builder.updatedAt(System.currentTimeMillis()).build());
        }
    }

    /**
     *
     * If a board already exists for the give integration ID, then create a builder based on it.
     * If there is no existing board, create a new builder from scratch.
     *
     * @param id identifier of the integration
     * @param version version of the integration
     * @param board integration bulletin board
     * @return a new builder of the integration bulletin board
     */
    private static IntegrationBulletinBoard.Builder getBoardBuilder(String id, String version, IntegrationBulletinBoard board) {
        final IntegrationBulletinBoard.Builder builder;
        if (board != null) {
            builder = new IntegrationBulletinBoard.Builder()
                .createFrom(board);
        } else {
            builder = new IntegrationBulletinBoard.Builder()
                .id(KeyGenerator.createKey())
                .targetResourceId(id)
                .createdAt(System.currentTimeMillis());
        }
        builder.putMetadata("integration-id", id);
        builder.putMetadata("integration-version", version);
        return builder;
    }

    private void processAndUpdateFlow(ChangeEvent event, DataManager dataManager, List<Connection> connections,
                                        Collection<Integration> allIntegrations, List<LeveledMessage> messages,
                                        Integration integration) {

        //For each existing flow in this integration, process all the elements if needed
        final List<Flow> flows = integration.getFlows();

        for (int f = 0; f < flows.size(); f++) {
            final Flow flow = flows.get(f);
            final String flowId = flow.getId().orElse("flow-" + f);
            final List<Step> steps = flow.getSteps();

            // the following code is quite ugly but the integration model
            // duplicates the resources definition in every step so we
            // definitively need to shrink the model and avoid duplication!
            //
            // A step should only include the user defined property and a
            // reference to the resources the properties apply to
            for (int s = 0; s < steps.size(); s++) {
                final Step step = steps.get(s);
                final String stepId = step.getId().orElse("step-" + s);
                final Supplier<LeveledMessage.Builder> supplier =
                    () -> new LeveledMessage.Builder().putMetadata("flow", flowId).putMetadata("step",
                    stepId);

                if (!step.getAction().isPresent()) {
                    continue;
                }

                // **********************
                // Integration
                // **********************

                final TargetWithDomain<Integration> intTarget = new IntegrationWithDomain(integration, allIntegrations);
                messages.addAll(computeValidatorMessages(supplier, intTarget));

                checkAndUpdateExtensions(dataManager, messages, step, supplier);
                checkAndUpdateConnectors(event, dataManager, connections, messages, step, supplier);

            }
        }
    }

    // **********************
    // Extension Action
    // **********************
    private void checkAndUpdateExtensions(DataManager dataManager, List<LeveledMessage> messages, Step step,
                                          Supplier<LeveledMessage.Builder> supplier) {
        step.getAction().filter(StepAction.class::isInstance).map(StepAction.class::cast).ifPresent(action -> {
            if (!step.getExtension().isPresent()) {
                return;
            }

            final Extension extension = step.getExtension().get();

            // When an extension is updated a new entity is written
            // to the db so we can't simply lookup by ID but whe need to
            // search for the latest installed extension.
            //
            // This fetchIdsByPropertyValue is not really optimized
            // as it ends up in multiple statements sent to the db, we
            // maybe need to have a better support for this use-case.
            final Set<String> ids = dataManager.fetchIdsByPropertyValue(Extension.class,
                "extensionId", extension.getExtensionId(),
                "status", Extension.Status.Installed.name());

            // No installed extension found, this happen if the
            // extension
            // has been deleted.
            if (ids.isEmpty()) {
                messages.add(
                    supplier.get()
                        .level(LeveledMessage.Level.WARN)
                        .code(LeveledMessage.Code.SYNDESIS004)
                        .build());

                return;
            }

            // More than one installed extension found, this should
            // not happen unless something wrong happen at extension
            // activation phase
            if (ids.size() > 1) {
                messages.add(
                    supplier.get()
                        .level(LeveledMessage.Level.WARN)
                        .code(LeveledMessage.Code.SYNDESIS010)
                        .build());

                return;
            }

            final Extension newExtension = dataManager.fetch(Extension.class, ids.iterator().next());
            if (newExtension == null) {
                messages.add(
                    supplier.get()
                        .level(LeveledMessage.Level.WARN)
                        .code(LeveledMessage.Code.SYNDESIS004)
                        .build());
            } else {
                final Action newAction = newExtension.findActionById(action.getId().get()).orElse(null);
                if (newAction == null) {
                    messages.add(
                        supplier.get()
                            .level(LeveledMessage.Level.WARN)
                            .code(LeveledMessage.Code.SYNDESIS005)
                            .build());
                } else {
                    messages.addAll(computePropertiesDiffMessages(supplier, action.getProperties(), newAction.getProperties()));
                    messages.addAll(computeMissingMandatoryPropertiesMessages(supplier, newAction.getProperties(), step.getConfiguredProperties()));
                    messages.addAll(computeSecretsUpdateMessages(supplier, newAction.getProperties(), step.getConfiguredProperties()));
                }
            }
        });
    }

    // **********************
    // Connector Action
    // **********************
    private void checkAndUpdateConnectors(ChangeEvent event, DataManager dataManager, List<Connection> connections, List<LeveledMessage> messages, Step step, Supplier<LeveledMessage.Builder> supplier) {
        step.getAction().filter(ConnectorAction.class::isInstance).map(ConnectorAction.class::cast).ifPresent(action -> {
            if (!step.getConnection().isPresent()) {
                return;
            }

            final Connection connection = step.getConnection().get();
            final Connection dbConnection = getDataManager().fetch(Connection.class, connection.getId().get());

            if (dbConnection != null &&
                Kind.Integration.equals(event.getKind().map(Kind::from).orElse(null))) {
                /*
                 * An integration event will create, delete or
                 * update it. This has an impact on associated
                 * connections in that their augmented properties,
                 * ie. those appended by the {@link
                 * ConnectionHandler#augmentedWithUsage}, eg. uses,
                 * need to be re-synced by clients. In order to do
                 * that, a change event must be broadcast to all
                 * clients and a call to update() provides such an
                 * event.
                 * see #4008
                 */
                dataManager.update(dbConnection);
            }

            if (connection.getId().isPresent() && (!connection.getConnector().isPresent() || !connection.getConnector().get().getTags().contains("dynamic"))) {
                //
                // Compare the connection in the draft integration's
                // step
                // with the connection from the data manager and log
                // the
                // result using the given message codes
                //
                compareConnection(
                    connection,
                    dbConnection,
                    messages,
                    supplier,
                    LeveledMessage.Code.SYNDESIS009,
                    LeveledMessage.Code.SYNDESIS011);
            }

            final Connector newConnector = dataManager.fetch(Connector.class, connection.getConnectorId());
            if (newConnector == null) {
                messages.add(
                    supplier.get()
                        .level(LeveledMessage.Level.WARN)
                        .code(LeveledMessage.Code.SYNDESIS003)
                        .build());
            } else {
                final Action newAction = newConnector.findActionById(action.getId().get()).orElse(null);
                if (newAction == null) {
                    messages.add(
                        supplier.get()
                            .level(LeveledMessage.Level.WARN)
                            .code(LeveledMessage.Code.SYNDESIS005)
                            .build());
                } else {
                    final Map<String, String> configuredProperties = CollectionsUtils.aggregate(connection.getConfiguredProperties(),
                        step.getConfiguredProperties());

                    final ConnectionWithDomain connTarget = new ConnectionWithDomain(connection, connections);
                    messages.addAll(computeValidatorMessages(supplier, connTarget));
                    messages.addAll(computePropertiesDiffMessages(supplier, action.getProperties(), newAction.getProperties()));
                    messages.addAll(computeMissingMandatoryPropertiesMessages(supplier, newAction.getProperties(), configuredProperties));
                    messages.addAll(computeSecretsUpdateMessages(supplier, newAction.getProperties(), configuredProperties));
                }
            }
        });
    }
}
