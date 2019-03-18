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
package io.syndesis.server.controller.integration;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import io.syndesis.common.model.ChangeEvent;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.util.EventBus;
import io.syndesis.common.util.Exceptions;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.backend.BackendController;
import io.syndesis.server.controller.ControllersConfigurationProperties;
import io.syndesis.server.controller.StateChangeHandler;
import io.syndesis.server.controller.StateChangeHandlerProvider;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.openshift.OpenShiftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class tracks changes to Integrations and attempts to process them so that
 * their current status matches their desired status.
 */
public abstract class BaseIntegrationController implements BackendController {
    private static final Logger LOG = LoggerFactory.getLogger(BaseIntegrationController.class);

    private static final String EVENT_BUS_ID = "integration-deployment-controller";
    private final OpenShiftService openShiftService;
    private final DataManager dataManager;
    private final EventBus eventBus;
    private final ConcurrentHashMap<IntegrationDeploymentState, StateChangeHandler> handlers = new ConcurrentHashMap<>();
    private final Set<String> scheduledChecks = ConcurrentHashMap.newKeySet();
    private final ControllersConfigurationProperties properties;

    private ExecutorService executor;
    private ScheduledExecutorService scheduler;

    protected BaseIntegrationController(OpenShiftService openShiftService, DataManager dataManager, EventBus eventBus,
                                     StateChangeHandlerProvider handlerFactory, ControllersConfigurationProperties properties) {
        this.openShiftService = openShiftService;
        this.dataManager = dataManager;
        this.eventBus = eventBus;
        for (StateChangeHandler handler : handlerFactory.getStatusChangeHandlers()) {
            for (IntegrationDeploymentState state : handler.getTriggerStates()) {
                this.handlers.put(state, handler);
            }
        }
        this.properties = properties;
    }

    protected OpenShiftService getOpenShiftService() {
        return openShiftService;
    }

    protected DataManager getDataManager() {
        return dataManager;
    }

    protected EventBus getEventBus() {
        return eventBus;
    }

    protected ControllersConfigurationProperties getProperties() {
        return properties;
    }

    protected Set<String> getScheduledChecks() { return scheduledChecks; }

    protected ScheduledExecutorService getScheduler() { return scheduler; }

    @SuppressWarnings("FutureReturnValueIgnored")
    protected void doStart() {
        executor = Executors.newSingleThreadExecutor(threadFactory("Integration Controller"));
        scheduler = Executors.newScheduledThreadPool(2, threadFactory("Integration Controller Scheduler"));

        scheduler.scheduleAtFixedRate(this::scanIntegrationsForWork, 0, properties.getIntegrationStateCheckInterval(), TimeUnit.SECONDS);
        eventBus.subscribe(EVENT_BUS_ID, getChangeEventSubscription());
    }

    protected void doStop() {
        eventBus.unsubscribe(EVENT_BUS_ID);

        scheduler.shutdownNow();
        executor.shutdownNow();
        try {
            boolean schedulerStopped = false;
            boolean executorStopped = false;

            do {
                schedulerStopped = scheduler.awaitTermination(10, TimeUnit.SECONDS);
                executorStopped = executor.awaitTermination(10, TimeUnit.SECONDS);
            } while (!schedulerStopped && !executorStopped);
        } catch (final InterruptedException e) {
            LOG.warn("Unable to cleanly stop: {}", e.getMessage());
            LOG.debug("Interrupted while stopping", e);
        }
    }

    @SuppressWarnings("PMD.DoNotUseThreads")
    private static ThreadFactory threadFactory(String name) {
        return r -> new Thread(null, r, name);
    }

    private EventBus.Subscription getChangeEventSubscription() {
        return (event, data) -> {
            // Never do anything that could block in this callback!
            if (EventBus.Type.CHANGE_EVENT.equals(event)) {
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

    protected void checkIntegrationStatusIfNotAlreadyInProgress(String id) {
        executor.execute(() -> {
            IntegrationDeployment integrationDeployment = dataManager.fetch(IntegrationDeployment.class, id);
            if( integrationDeployment != null) {
                String scheduledKey = getIntegrationMarkerKey(integrationDeployment);

                LOG.debug("Check if IntegrationStatus {} is already in progress for key: {} (keys: {})", id, scheduledKey, scheduledChecks);
                // Don't start check is already a check is running
                if (!scheduledChecks.contains(scheduledKey)) {
                    checkIntegrationStatus(integrationDeployment);
                } else {
                    LOG.debug("A check for IntegrationDeployment {} is already configured with key {}", id, scheduledKey);
                }
            } else {
                LOG.debug("No IntegrationDeployment with id: {}", id);
            }
        });
    }

    private void scanIntegrationsForWork() {
        LOG.info("Checking integrations for their status.");
        executor.execute(
            () -> dataManager.fetchIds(IntegrationDeployment.class).forEach(this::checkIntegrationStatusIfNotAlreadyInProgress)
        );
    }

    private void checkIntegrationStatus(IntegrationDeployment integrationDeployment) {
        if (integrationDeployment == null) {
            return;
        }
        IntegrationDeployment reconciled = reconcileDeployment(integrationDeployment);
        IntegrationDeploymentState desiredState = reconciled.getTargetState();
        IntegrationDeploymentState currentState = reconciled.getCurrentState();
        if (currentState != IntegrationDeploymentState.Error && currentState != desiredState) {
                reconciled.getId().ifPresent(integrationDeploymentId -> {
                    StateChangeHandler statusChangeHandler = handlers.get(desiredState);
                    if (statusChangeHandler != null) {
                        LOG.info("Integration {} : Desired status \"{}\" != current status \"{}\" --> calling status change handler",
                            integrationDeploymentId, desiredState.toString(), currentState);
                        callStateChangeHandler(statusChangeHandler, reconciled);
                    }
                });
        } else if (reconciled.getCurrentState() !=  integrationDeployment.getCurrentState()) {
            dataManager.update(reconciled);
            scheduledChecks.remove(getIntegrationMarkerKey(reconciled));
        } else {
            scheduledChecks.remove(getIntegrationMarkerKey(reconciled));
        }
    }

    private String getLabel(IntegrationDeployment integrationDeployment) {
        return "Integration " + integrationDeployment.getIntegrationId().orElse("[none]");
    }

    void callStateChangeHandler(StateChangeHandler handler, IntegrationDeployment integrationDeployment) {
        String integrationDeploymentId = integrationDeployment.getId().get();
        executor.execute(() -> {
            String checkKey = getIntegrationMarkerKey(integrationDeployment);
            scheduledChecks.add(checkKey);

            if (stale(handler, integrationDeployment)) {
                scheduledChecks.remove(checkKey);
                return;
            }

            try {
                final String integrationId = integrationDeployment.getIntegrationId().get();
                final int deploymentVersion = integrationDeployment.getVersion();
                LOG.info("IntegrationDeploymentId {} Integration {} : Start processing integration: {}, version: {} with handler: {}", integrationDeploymentId, integrationId, integrationId, deploymentVersion, handler.getClass().getSimpleName());
                handler.execute(integrationDeployment, update-> {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("{} : Setting status to {}{}",
                            getLabel(integrationDeployment),
                            update.getState(),
                            Optional.ofNullable(update.getStatusMessage()).map(x->" ("+x+")").orElse(""));
                    }

                    // handler.execute might block for while so refresh our copy of the integration
                    // data before we update the current status
                    IntegrationDeployment current = dataManager.fetch(IntegrationDeployment.class, integrationDeploymentId);
                    final IntegrationDeployment updated = current.builder()
                        .statusMessage(Optional.ofNullable(update.getStatusMessage()))
                        .currentState(update.getState())
                        .error(update.getError())
                        .stepsDone(update.getStepsPerformed())
                        .build();
                    LOG.trace("Updated {} , Current {}", updated, current);
                    if (!updated.equals(current)) {
                        LOG.debug("IntegrationDeploymentId {} Integration {} , version: {} : jsonDB state update from {} to {}", integrationDeploymentId, integrationId, deploymentVersion, current.getCurrentState(), updated.getCurrentState());
                        dataManager.update(updated.builder().updatedAt(System.currentTimeMillis()).build());
                    }
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
                reschedule(integrationDeploymentId, checkKey);
            }

        });
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    protected void reschedule(String integrationId, String checkKey) {
        LOG.debug("Reschedule IntegrationDeployment check, id:{}, keys: {}", integrationId, scheduledChecks);
        scheduler.schedule(() -> {
                IntegrationDeployment i = dataManager.fetch(IntegrationDeployment.class, integrationId);
                LOG.debug("Trigger checkIntegrationStatus, id:{}", integrationId);
                checkIntegrationStatus(i);
            },
            properties.getIntegrationStateCheckInterval(),
            TimeUnit.SECONDS
        );
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


    protected abstract IntegrationDeploymentState determineState(IntegrationDeployment integrationDeployment);

    /**
     * Re-conciliates the state of the deployment between the database and Openshift.
     * This is mostly needed for cases where an active integration has been tampered with external tools.
     * For example: Deleted or Scaled down DeploymentConfig.
     */
    private IntegrationDeployment reconcileDeployment(IntegrationDeployment integrationDeployment) {
        IntegrationDeploymentState actualState = determineState(integrationDeployment);
        LOG.debug("Actual state: {}, Persisted state: {}, Desired state: {}", actualState, integrationDeployment.getCurrentState(), integrationDeployment.getTargetState());

        // Make sure to not loose error state, only overwrite when actual state in Openshift is published for some reason
        if (integrationDeployment.getCurrentState() == IntegrationDeploymentState.Error) {
            if (actualState == IntegrationDeploymentState.Published) {
                return integrationDeployment.withCurrentState(actualState);
            } else {
                return integrationDeployment;
            }
        }

        //We also need to compare with current state to make sure we only call the unpublished() once. Calling it multiple times, might loose the steps performed, causing an infinite loop.
        if (actualState == IntegrationDeploymentState.Unpublished && actualState != integrationDeployment.getCurrentState()) {
            return integrationDeployment.unpublished();
        } else {
            return integrationDeployment.withCurrentState(actualState);
        }
    }
}
