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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import io.syndesis.controllers.StateChangeHandler;
import io.syndesis.controllers.StateChangeHandlerProvider;
import io.syndesis.controllers.StateUpdate;
import io.syndesis.core.EventBus;
import io.syndesis.core.Json;
import io.syndesis.core.util.Exceptions;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.ChangeEvent;
import io.syndesis.model.Kind;

import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.IntegrationDeploymentState;
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
    private final ConcurrentHashMap<IntegrationDeploymentState, StateChangeHandler> handlers = new ConcurrentHashMap<>();
    private final Set<String> scheduledChecks = new HashSet<>();
    /* default */ ExecutorService executor;
    /* default */ ScheduledExecutorService scheduler;

    private static final long SCHEDULE_INTERVAL_IN_SECONDS = 60;

    @Autowired
    public IntegrationController(DataManager dataManager, EventBus eventBus, StateChangeHandlerProvider handlerFactory) {
        this.dataManager = dataManager;
        this.eventBus = eventBus;
        for (StateChangeHandler handler : handlerFactory.getStatusChangeHandlers()) {
            for (IntegrationDeploymentState state : handler.getTriggerStates()) {
                this.handlers.put(state, handler);
            }
        }
    }

    @PostConstruct
    public void start() {
        executor = Executors.newSingleThreadExecutor();
        scheduler = Executors.newScheduledThreadPool(1);
        scanIntegrationsForWork();

        eventBus.subscribe("integration-deployment-controller", getChangeEventSubscription());
    }

    private EventBus.Subscription getChangeEventSubscription() {
        return (event, data) -> {
            // Never do anything that could block in this callback!
            if (event!=null && "change-event".equals(event)) {
                try {
                    ChangeEvent changeEvent = Json.reader().forType(ChangeEvent.class).readValue(data);
                    if (changeEvent != null) {
                        changeEvent.getId().ifPresent(id -> {
                            changeEvent.getKind()
                                       .map(Kind::from)
                                       .filter(k -> k == Kind.IntegrationDeployment)
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
            IntegrationDeployment integrationDeployment = dataManager.fetch(IntegrationDeployment.class, id);
            if( integrationDeployment!=null ) {
                String scheduledKey = getIntegrationMarkerKey(integrationDeployment);
                // Don't start check is already a check is running
                if (!scheduledChecks.contains(scheduledKey)) {
                    checkIntegrationStatus(integrationDeployment);
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
            dataManager.fetchAll(IntegrationDeployment.class).getItems().forEach(integration -> {
                LOG.info("Checking integrations for their status.");
                checkIntegrationStatus(integration);
            });
        });
    }

    private void checkIntegrationStatus(IntegrationDeployment integrationDeployment) {
        if (integrationDeployment == null) {
            return;
        }
        IntegrationDeploymentState desiredState = integrationDeployment.getTargetState();
        IntegrationDeploymentState currentState = integrationDeployment.getCurrentState();
        if (!currentState.equals(desiredState)) {
                integrationDeployment.getId().ifPresent(integrationDeploymentId -> {
                    StateChangeHandler statusChangeHandler = handlers.get(desiredState);
                    if (statusChangeHandler != null) {
                        LOG.info("Integration {} : Desired status \"{}\" != current status \"{}\" --> calling status change handler",
                            integrationDeploymentId, desiredState.toString(), currentState);
                        callStateChangeHandler(statusChangeHandler, integrationDeploymentId);
                    }
                });
        } else {
            scheduledChecks.remove(getIntegrationMarkerKey(integrationDeployment));
        }
    }

    private String getLabel(IntegrationDeployment integrationDeployment) {
        return "Integration " + integrationDeployment.getIntegrationId().orElse("[none]");
    }

    /* default */ void callStateChangeHandler(StateChangeHandler handler, String integrationDeploymentId) {
        executor.execute(() -> {
            IntegrationDeployment integrationDeployment = dataManager.fetch(IntegrationDeployment.class, integrationDeploymentId);
            String checkKey = getIntegrationMarkerKey(integrationDeployment);
            scheduledChecks.add(checkKey);

            if (stale(handler, integrationDeployment)) {
                scheduledChecks.remove(checkKey);
                return;
            }

            try {
                LOG.info("Integration {} : Start processing integration: {}, version: {} with handler:{}", integrationDeployment.getIntegrationId().get(), integrationDeployment.getVersion(), handler.getClass().getSimpleName());
                handler.execute(integrationDeployment, update->{
                    if (LOG.isInfoEnabled()) {
                        LOG.info("{} : Setting status to {}{}",
                            getLabel(integrationDeployment),
                            update.getState(),
                            Optional.ofNullable(update.getStatusMessage()).map(x->" ("+x+")").orElse(""));
                    }

                    // handler.execute might block for while so refresh our copy of the integration
                    // data before we update the current status
                    IntegrationDeployment current = dataManager.fetch(IntegrationDeployment.class, integrationDeploymentId);
                    dataManager.update(current.builder()
                        .statusMessage(Optional.ofNullable(update.getStatusMessage()))
                        .currentState(update.getState())
                        .stepsDone(update.getStepsPerformed())
                        .updatedAt(System.currentTimeMillis())
                        .build());
                });
            } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
                LOG.error("Error while processing integration status for integration {}", integrationDeploymentId, e);
                // Something went wrong.. lets note it.
                IntegrationDeployment current = dataManager.fetch(IntegrationDeployment.class, integrationDeploymentId);
                dataManager.update(new IntegrationDeployment.Builder()
                    .createFrom(current)
                    .currentState(IntegrationDeploymentState.Error)
                    .statusMessage(Exceptions.toString(e))
                    .updatedAt(System.currentTimeMillis())
                    .build());

            } finally {
                // Add a next check for the next interval
                reschedule(integrationDeploymentId);
            }

        });
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    private void reschedule(String integrationId) {
        scheduler.schedule(() -> {
            IntegrationDeployment i = dataManager.fetch(IntegrationDeployment.class, integrationId);
            checkIntegrationStatus(i);
        }, SCHEDULE_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
    }

    private String getIntegrationMarkerKey(IntegrationDeployment integrationDeployment) {
        return integrationDeployment.getTargetState() +
               ":" +
               integrationDeployment.getId().orElseThrow(() -> new IllegalArgumentException("No id set in integration " + integrationDeployment));
    }

    private boolean stale(StateChangeHandler handler, IntegrationDeployment integrationDeployment) {
        if (integrationDeployment == null || handler == null) {
            return true;
        }

        IntegrationDeploymentState desiredState = integrationDeployment.getTargetState();
        return desiredState.equals(integrationDeployment.getCurrentState());
    }
}
