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
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.model.integration.Step;
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

    public IntegrationUpdateHandler(DataManager dataManager, EncryptionComponent encryptionComponent, Validator validator) {
        super(dataManager, encryptionComponent, validator);

        this.supportedKinds = Arrays.asList(Kind.Integration, Kind.Connector, Kind.Connection, Kind.Extension);
    }

    @Override
    public boolean canHandle(ChangeEvent event) {
        return event.getKind().map(Kind::from).filter(supportedKinds::contains).isPresent();
    }

    /**
     * Compares the given connection with the connection fetched from the data manager.
     * Adds a message with the missing code if there is no connection available or adds a
     * message with the difference code if the connections differ in their metadata.
     *
     * @param connection the connection to compare
     * @param messages the messages collection to add the message to
     * @param supplier the supplier for generating the message
     * @param missingCode the code if the connection is missing
     * @param differenceCode the code if the connection has different metadata
     */
    private void compareConnection(Connection connection,
                                                                    List<LeveledMessage> messages, Supplier<LeveledMessage.Builder> supplier,
                                                                    LeveledMessage.Code missingCode, LeveledMessage.Code differenceCode) {
        Connection newConnection = getDataManager().fetch(Connection.class, connection.getId().get());

        // There's no connection with the given id so this means
        // that the connection has been deleted or is is not present
        if (newConnection == null) {
            messages.add(
                supplier.get()
                    .level(LeveledMessage.Level.WARN)
                    .code(missingCode)
                    .build()
            );
        } else {
            //
            // Determine whether the integration's nested connection is
            // still equivalent to the connection in the database. If not then
            // flag a warning.
            //
            newConnection = includeConnector(newConnection);
            if (! connection.equivalent(newConnection)) {
                messages.add(
                         supplier.get()
                         .level(LeveledMessage.Level.WARN)
                         .code(differenceCode)
                         .build()
                );
            }
        }
    }

    private Connection includeConnector(Connection connection) {
        if (connection == null) {
            return connection;
        }

        Optional<Connector> connectorOptional = connection.getConnector();
        if (connectorOptional != null && connectorOptional.isPresent()) {
            return connection;
        }

        Connector connector = getDataManager().fetch(Connector.class, connection.getConnectorId());
        if (connector == null) {
            return connection;
        }

        Connection.Builder builder = new Connection.Builder();
        return builder.createFrom(connection)
                                   .connector(connector)
                                   .build();
    }

    @SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.NPathComplexity"})
    @Override
    protected List<IntegrationBulletinBoard> compute(ChangeEvent event) {
        final List<IntegrationBulletinBoard> boards = new ArrayList<>();
        final DataManager dataManager = getDataManager();
        final List<Integration> integrations = dataManager.fetchAll(Integration.class).getItems();
        final List<LeveledMessage> messages = new ArrayList<>();

        for (int i = 0; i < integrations.size(); i++) {
            final Integration integration = integrations.get(i);
            final List<Step> steps = integration.getSteps();
            final String id = integration.getId().get();

            final IntegrationBulletinBoard board = dataManager.fetchByPropertyValue(IntegrationBulletinBoard.class, "targetResourceId", id).orElse(null);
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

            // reuse messages
            messages.clear();

            // the following code is quite ugly but the integration model duplicates
            // the resources definition in every step so we definitively need to
            // shrink the model and avoid duplication !
            //
            // A step should only include the user defined property and a reference
            // to the resources the properties apply to
            for (int s = 0; s < steps.size(); s++) {
                final int index = s;
                final Step step = steps.get(s);
                final Supplier<LeveledMessage.Builder> supplier = () -> new LeveledMessage.Builder().putMetadata("step", step.getId().orElse("step-" + index));

                if (!step.getAction().isPresent()) {
                    continue;
                }

                // **********************
                // Integration
                // **********************

                messages.addAll(computeValidatorMessages(supplier, integration));

                // **********************
                // Extension Action
                // **********************

                step.getAction().filter(StepAction.class::isInstance).map(StepAction.class::cast).ifPresent(action -> {
                    if (!step.getExtension().isPresent()) {
                        return;
                    }

                    final Extension extension = step.getExtension().get();

                    // When an extension is updated a new entity is written to the db
                    // so we can't simply lookup by ID but whe need to search for the
                    // latest installed extension.
                    //
                    // This fetchIdsByPropertyValue is not really optimized as it
                    // ends up in multiple statements sent to the db, we maybe need
                    // to have a better support for this use-case.
                    Set<String> ids = dataManager.fetchIdsByPropertyValue(Extension.class,
                        "extensionId", extension.getExtensionId(),
                        "status", Extension.Status.Installed.name()
                    );

                    // No installed extension found, this happen if the extension
                    // has been deleted.
                    if (ids.size() == 0) {
                        messages.add(
                            supplier.get()
                                .level(LeveledMessage.Level.WARN)
                                .code(LeveledMessage.Code.SYNDESIS004)
                                .build()
                        );

                        return;
                    }

                    // More than one installed extension found, this should not
                    // happen unless something wrong happen at extension activation
                    // phase
                    if (ids.size() > 1) {
                        messages.add(
                            supplier.get()
                                .level(LeveledMessage.Level.WARN)
                                .code(LeveledMessage.Code.SYNDESIS010)
                                .build()
                        );

                        return;
                    }

                    Extension newExtension = dataManager.fetch(Extension.class, ids.iterator().next());
                    if (newExtension == null) {
                        messages.add(
                            supplier.get()
                                .level(LeveledMessage.Level.WARN)
                                .code(LeveledMessage.Code.SYNDESIS004)
                                .build()
                        );
                    } else {
                        Action newAction = newExtension.findActionById(action.getId().get()).orElse(null);
                        if (newAction == null) {
                            messages.add(
                                supplier.get()
                                    .level(LeveledMessage.Level.WARN)
                                    .code(LeveledMessage.Code.SYNDESIS005)
                                    .build()
                            );
                        } else {
                            messages.addAll(computePropertiesDiffMessages(supplier, action.getProperties(), newAction.getProperties()));
                            messages.addAll(computeMissingMandatoryPropertiesMessages(supplier, newAction.getProperties(), step.getConfiguredProperties()));
                            messages.addAll(computeSecretsUpdateMessages(supplier, newAction.getProperties(), step.getConfiguredProperties()));
                        }
                    }
                });

                // **********************
                // Connector Action
                // **********************

                step.getAction().filter(ConnectorAction.class::isInstance).map(ConnectorAction.class::cast).ifPresent(action -> {
                    if (!step.getConnection().isPresent()) {
                        return;
                    }

                    final Connection connection = step.getConnection().get();

                    if (connection.getId().isPresent()) {
                        //
                        // Compare the connection in the draft integration's step
                        // with the connection from the data manager and log the
                        // result using the given message codes
                        //
                        compareConnection(connection, messages, supplier,
                                                              LeveledMessage.Code.SYNDESIS009, LeveledMessage.Code.SYNDESIS011);
                    }

                    Connector newConnector = dataManager.fetch(Connector.class, connection.getConnectorId());
                    if (newConnector == null) {
                        messages.add(
                            supplier.get()
                                .level(LeveledMessage.Level.WARN)
                                .code(LeveledMessage.Code.SYNDESIS003)
                                .build()
                        );
                    } else {
                        Action newAction = newConnector.findActionById(action.getId().get()).orElse(null);
                        if (newAction == null) {
                            messages.add(
                                supplier.get()
                                    .level(LeveledMessage.Level.WARN)
                                    .code(LeveledMessage.Code.SYNDESIS005)
                                    .build()
                            );
                        } else {
                            Map<String, String> configuredProperties = CollectionsUtils.aggregate(connection.getConfiguredProperties(), step.getConfiguredProperties());

                            messages.addAll(computeValidatorMessages(supplier, connection));
                            messages.addAll(computePropertiesDiffMessages(supplier, action.getProperties(), newAction.getProperties()));
                            messages.addAll(computeMissingMandatoryPropertiesMessages(supplier, newAction.getProperties(), configuredProperties));
                            messages.addAll(computeSecretsUpdateMessages(supplier, newAction.getProperties(), configuredProperties));
                        }
                    }
                });

            }

            // **********************
            // Integration Deployments
            // **********************
            messages.addAll(computeDeploymentDifferences(integration));

            builder.errors(countMessagesWithLevel(LeveledMessage.Level.ERROR, messages));
            builder.warnings(countMessagesWithLevel(LeveledMessage.Level.WARN, messages));
            builder.notices(countMessagesWithLevel(LeveledMessage.Level.INFO, messages));
            builder.putMetadata("integration-id", id);
            builder.putMetadata("integration-version", Integer.toString(integration.getVersion()));
            builder.messages(messages);

            final IntegrationBulletinBoard updated = builder.build();
            if (!updated.equals(board)) {
                boards.add(builder.updatedAt(System.currentTimeMillis()).build());
            }
        }

        return boards;
    }

    private Collection<? extends LeveledMessage> computeDeploymentDifferences(Integration integration) {
        String integrationId = integration.getId().get();

        Set<String> deploymentIds = getDataManager().fetchIdsByPropertyValue(IntegrationDeployment.class, "integrationId", integrationId);
        if (deploymentIds == null || deploymentIds.isEmpty()) {
            // No deployments match this integration
            return Collections.emptyList();
        }

        List<IntegrationDeployment> deployments = deploymentIds.stream()
                .map(i -> getDataManager().fetch(IntegrationDeployment.class, i))
                .filter(r -> r != null)
                .filter(d -> d.getCurrentState().equals(IntegrationDeploymentState.Published))
                .collect(Collectors.toList());

        if (deployments.isEmpty()) {
            // Nothing to do as no deployments have been published
            return Collections.emptyList();
        }

        final List<LeveledMessage> messages = new ArrayList<>();

        for (IntegrationDeployment deployment : deployments) {
            final Supplier<LeveledMessage.Builder> supplier = () -> new LeveledMessage.Builder().putMetadata("deployment", deployment.getId().get());

            Integration deployedInteg = deployment.getSpec();

            // **********************
            // Draft Integration
            // **********************
            if (! integration.equivalent(deployedInteg)) {
                messages.add(
                             supplier.get()
                             .code(LeveledMessage.Code.SYNDESIS012)
                             .level(LeveledMessage.Level.WARN)
                             .build()
                );

                //
                // Deployment is already stale in comparison to its source integration
                // so no need to check the connections as well and duplicate the messages
                //
                continue;
            }

            // **********************
            // Connections
            // **********************
            for (Step deployedStep : deployedInteg.getSteps()) {
                deployedStep.getAction().filter(ConnectorAction.class::isInstance)
                                            .map(ConnectorAction.class::cast)
                                            .ifPresent(action -> {
                                                if (! deployedStep.getConnection().isPresent()) {
                                                    return;
                                                }

                                                //
                                                // Compare the connection in the deployed integration's step
                                                // with the connection from the data manager and log the
                                                // result using the given message codes
                                                //
                                                final Connection connection = deployedStep.getConnection().get();
                                                compareConnection(connection, messages, supplier,
                                                                  LeveledMessage.Code.SYNDESIS009, LeveledMessage.Code.SYNDESIS012);
                                            });
            }
        }

        return messages;
    }
}
