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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
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
                        Connection newConnection = dataManager.fetch(Connection.class, connection.getId().get());

                        // There's not connection with the given id so this means
                        // that the connection has been deleted or is is not present
                        // in the imported extension
                        if (newConnection == null) {
                            messages.add(
                                supplier.get()
                                    .level(LeveledMessage.Level.WARN)
                                    .code(LeveledMessage.Code.SYNDESIS009)
                                    .build()
                            );
                        }
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
}
