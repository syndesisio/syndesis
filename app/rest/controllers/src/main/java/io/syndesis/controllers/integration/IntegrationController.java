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
package io.syndesis.controllers.integration;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import io.syndesis.controllers.StateChangeHandler;
import io.syndesis.controllers.StateChangeHandlerProvider;
import io.syndesis.controllers.StateUpdate;
import io.syndesis.core.EventBus;
import io.syndesis.core.Json;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.ChangeEvent;
import io.syndesis.model.Kind;
import io.syndesis.model.integration.Integration;

import io.syndesis.model.integration.IntegrationRevision;
import io.syndesis.model.integration.IntegrationRevisionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This class tracks changes to Integrations and attempts to process them so that
 * their current status matches their desired status.
 */
@Service
public class IntegrationController {
    private static final Logger LOG = LoggerFactory.getLogger(IntegrationController.class);

    private final DataManager dataManager;
    private final EventBus eventBus;
    private final ConcurrentHashMap<IntegrationRevisionState, StateChangeHandler> handlers = new ConcurrentHashMap<>();
    private final Set<String> scheduledChecks = new HashSet<>();
    /* default */ ExecutorService executor;
    /* default */ ScheduledExecutorService scheduler;

    private static final long SCHEDULE_INTERVAL_IN_SECONDS = 60;

    @Autowired
    public IntegrationController(DataManager dataManager, EventBus eventBus, StateChangeHandlerProvider handlerFactory) {
        this.dataManager = dataManager;
        this.eventBus = eventBus;
        for (StateChangeHandler handler : handlerFactory.getStatusChangeHandlers()) {
            for (IntegrationRevisionState state : handler.getTriggerStates()) {
                this.handlers.put(state, handler);
            }
        }
    }

    @PostConstruct
    public void start() {
        executor = Executors.newSingleThreadExecutor();
        scheduler = Executors.newScheduledThreadPool(1);
        scanIntegrationsForWork();

        eventBus.subscribe("integration-revision-controller", getChangeEventSubscription());
    }

    private EventBus.Subscription getChangeEventSubscription() {
        return (event, data) -> {
            // Never do anything that could block in this callback!
            if (event!=null && "change-event".equals(event)) {
                try {
                    ChangeEvent changeEvent = Json.mapper().readValue(data, ChangeEvent.class);
                    if (changeEvent != null) {
                        changeEvent.getId().ifPresent(id -> {
                            changeEvent.getKind()
                                       .map(Kind::from)
                                       .filter(k -> k == Kind.IntegrationRevision)
                                       .ifPresent(k -> {
                                           checkIntegrationStatusIfNotAlreadyInProgress(id);
                                       });
                        });
                    }
                } catch (IOException e) {
                    LOG.error("Error while subscribing to change-event {}", data, e);
                }
            }
        };
    }

    private void checkIntegrationStatusIfNotAlreadyInProgress(String id) {
        executor.execute(() -> {
            IntegrationRevision integrationRevision = dataManager.fetch(IntegrationRevision.class, id);
            if( integrationRevision!=null ) {
                String scheduledKey = getIntegrationMarkerKey(integrationRevision);
                // Don't start check is already a check is running
                if (!scheduledChecks.contains(scheduledKey)) {
                    checkIntegrationStatus(integrationRevision);
                }
            }
        });
    }

    @PreDestroy
    public void stop() {
        eventBus.unsubscribe("integration-controller");
        scheduler.shutdownNow();
        executor.shutdownNow();
    }

    private void scanIntegrationsForWork() {
        executor.execute(() -> {
            dataManager.fetchAll(IntegrationRevision.class).getItems().forEach(integration -> {
                LOG.info("Checking integrations for their status.");
                checkIntegrationStatus(integration);
            });
        });
    }

    private void checkIntegrationStatus(IntegrationRevision integrationRevision) {
        if (integrationRevision == null) {
            return;
        }
        IntegrationRevisionState desiredState = integrationRevision.getTargetState();
        IntegrationRevisionState currentState = integrationRevision.getCurrentState();
        if (!currentState.equals(desiredState)) {
                integrationRevision.getId().ifPresent(integrationRevisionId -> {
                    StateChangeHandler statusChangeHandler = handlers.get(desiredState);
                    if (statusChangeHandler != null) {
                        LOG.info("Integration {} : Desired status \"{}\" != current status \"{}\" --> calling status change handler",
                                               integrationRevisionId, desiredState.toString(), currentState);
                        callStateChangeHandler(statusChangeHandler, integrationRevisionId);
                    }
                });
        } else {
            scheduledChecks.remove(getIntegrationMarkerKey(integrationRevision));
        }
    }

    private String getLabel(IntegrationRevision integration) {
        return "Integration " + integration.getIntegrationId().orElse("[none]");
    }

    /* default */ void callStateChangeHandler(StateChangeHandler handler, String integrationRevisionId) {
        executor.execute(() -> {
            IntegrationRevision integrationRevision = dataManager.fetch(IntegrationRevision.class, integrationRevisionId);
            String checkKey = getIntegrationMarkerKey(integrationRevision);
            scheduledChecks.add(checkKey);

            if (stale(handler, integrationRevision)) {
                scheduledChecks.remove(checkKey);
                return;
            }

            try {
                LOG.info("Integration {} : Start processing integration: {}, version: {} with handler:{}", integrationRevision.getIntegrationId().get(), integrationRevision.getVersion().get(), handler.getClass().getSimpleName());
                StateUpdate update = handler.execute(integrationRevision);
                if (update!=null) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("{} : Setting status to {}{}", getLabel(integrationRevision), update.getState(), (update.getStatusMessage() != null ? " (" + update.getStatusMessage() + ")" : ""));
                    }

                    // handler.execute might block for while so refresh our copy of the integration
                    // data before we update the current status

                    // TODO: do this in a single TX.
                    Date now = new Date();
                    IntegrationRevision current = dataManager.fetch(IntegrationRevision.class, integrationRevisionId);
                    IntegrationRevision updated = new IntegrationRevision.Builder()
                        .createFrom(current)
                        //.statusMessage(Optional.ofNullable(update.getStatusMessage()))
                        .currentState(update.getState())
                        .stepsDone(update.getStepsPerformed())
                        .createdDate(IntegrationRevisionState.Active.equals(update.getState()) ? now : integrationRevision.getCreatedDate())
                        .lastUpdated(new Date())
                        .build();

                    /*

                    TODO: This block should be removed. Just keeping it till refactoring is done.

                    Set<IntegrationRevision> revisions = dataManager
                        .fetchIdsByPropertyValue(IntegrationRevision.class, "integrationId", current.getId().get())
                        .stream()
                        .map(i -> dataManager.fetch(IntegrationRevision.class, i))
                        .collect(Collectors.toSet());

                    IntegrationRevision revision = IntegrationRevision.deployedRevision(integrationRevision)
                        .withCurrentState(update.getState());

                    //replace revision
                    revisions.remove(revision);

                    final IntegrationRevision last = integrationRevision.lastRevision();
                    if (update.getState().equals(last.getCurrentState())) {
                        revision = new IntegrationRevision.Builder().createFrom(revision)
                            .version(last.getVersion())
                            .parentVersion(last.getParentVersion())
                            .build();
                    }
                    revisions.add(revision);
                    */

                    dataManager.update(updated);
                }

            } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
                LOG.error("Error while processing integration status for integration {}", integrationRevisionId, e);
                // Something went wrong.. lets note it.
                Integration current = dataManager.fetch(Integration.class, integrationRevisionId);
                dataManager.update(new Integration.Builder()
                    .createFrom(current)
                    .statusMessage("Error: "+e)
                    .lastUpdated(new Date())
                    .build());

            } finally {
                // Add a next check for the next interval
                reschedule(integrationRevisionId);
            }

        });
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    private void reschedule(String integrationId) {
        scheduler.schedule(() -> {
            IntegrationRevision i = dataManager.fetch(IntegrationRevision.class, integrationId);
            checkIntegrationStatus(i);
        }, SCHEDULE_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
    }

    private String getIntegrationMarkerKey(IntegrationRevision integrationRevision) {
        return integrationRevision.getTargetState() +
               ":" +
               integrationRevision.getId().orElseThrow(() -> new IllegalArgumentException("No id set in integration " + integrationRevision));
    }

    private boolean stale(StateChangeHandler handler, IntegrationRevision integrationRevision) {
        if (integrationRevision == null || handler == null) {
            return true;
        }

        IntegrationRevisionState desiredState = integrationRevision.getTargetState();
        return desiredState.equals(integrationRevision.getCurrentState());
    }
}
