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
    private final ConcurrentHashMap<Integration.Status, StatusChangeHandlerProvider.StatusChangeHandler> handlers = new ConcurrentHashMap<>();
    private final Set<String> scheduledChecks = new HashSet<>();
    /* default */ ExecutorService executor;
    /* default */ ScheduledExecutorService scheduler;

    private static final long SCHEDULE_INTERVAL_IN_SECONDS = 60;

    @Autowired
    public IntegrationController(DataManager dataManager, EventBus eventBus, StatusChangeHandlerProvider handlerFactory) {
        this.dataManager = dataManager;
        this.eventBus = eventBus;
        for (StatusChangeHandlerProvider.StatusChangeHandler handler : handlerFactory.getStatusChangeHandlers()) {
            for (Integration.Status status : handler.getTriggerStatuses()) {
                this.handlers.put(status, handler);
            }
        }
    }

    @PostConstruct
    public void start() {
        executor = Executors.newSingleThreadExecutor();
        scheduler = Executors.newScheduledThreadPool(1);
        scanIntegrationsForWork();

        eventBus.subscribe("integration-controller", getChangeEventSubscription());
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
                                       .filter(k -> k == Kind.Integration)
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
            Integration integration = dataManager.fetch(Integration.class, id);
            if( integration!=null ) {
                String scheduledKey = getIntegrationMarkerKey(integration);
                // Don't start check is already a check is running
                if (!scheduledChecks.contains(scheduledKey)) {
                    checkIntegrationStatus(integration);
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
            dataManager.fetchAll(Integration.class).getItems().forEach(integration -> {
                LOG.info("Checking integrations for their status.");
                checkIntegrationStatus(integration);
            });
        });
    }

    private void checkIntegrationStatus(Integration integration) {
        if (integration == null) {
            return;
        }
        Optional<Integration.Status> desired = integration.getDesiredStatus();
        Optional<Integration.Status> current = integration.getCurrentStatus();
        if (!current.equals(desired)) {
            desired.ifPresent(desiredStatus ->
                integration.getId().ifPresent(integrationId -> {
                    StatusChangeHandlerProvider.StatusChangeHandler statusChangeHandler = handlers.get(desiredStatus);
                    if (statusChangeHandler != null) {
                        LOG.info("Integration {} : Desired status \"{}\" != current status \"{}\" --> calling status change handler",
                                               integrationId, desiredStatus.toString(), current.map(Object::toString).orElse("[none]"));
                        callStatusChangeHandler(statusChangeHandler, integrationId);
                    }
                }));
        } else {
            // When the desired state is reached remove the marker so that a next change trigger a check again
            // Doesn't harm when no such key exists
            desired.ifPresent(d -> scheduledChecks.remove(getIntegrationMarkerKey(integration)));
        }
    }

    private String getLabel(Integration integration) {
        return "Integration " + integration.getId().orElse("[none]");
    }

    /* default */ void callStatusChangeHandler(StatusChangeHandlerProvider.StatusChangeHandler handler, String integrationId) {
        executor.execute(() -> {
            Integration integration = dataManager.fetch(Integration.class, integrationId);
            String checkKey = getIntegrationMarkerKey(integration);
            scheduledChecks.add(checkKey);

            if (stale(handler, integration)) {
                scheduledChecks.remove(checkKey);
                return;
            }

            try {
                LOG.info("Integration {} : Start processing integration with {}", integrationId, handler.getClass().getSimpleName());
                StatusChangeHandlerProvider.StatusChangeHandler.StatusUpdate update = handler.execute(integration);
                if (update!=null) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("{} : Setting status to {}{}", getLabel(integration), update.getStatus(), (update.getStatusMessage() != null ? " (" + update.getStatusMessage() + ")" : ""));
                    }

                    // handler.execute might block for while so refresh our copy of the integration
                    // data before we update the current status

                    // TODO: do this in a single TX.
                    Date now = new Date();
                    Integration current = dataManager.fetch(Integration.class, integrationId);
                    Integration updated = new Integration.Builder()
                        .createFrom(current)
                        .currentStatus(update.getStatus()) // Status must not be null
                        .statusMessage(Optional.ofNullable(update.getStatusMessage()))
                        .stepsDone(update.getStepsPerformed())
                        .createdDate(Integration.Status.Activated.equals(update.getStatus()) ? now : integration.getCreatedDate().get())
                        .lastUpdated(new Date())
                        .build();

                    Set<IntegrationRevision> revisions = new HashSet<>(integration.getRevisions());
                    IntegrationRevision revision = IntegrationRevision.deployedRevision(integration)
                        .withCurrentState(IntegrationRevisionState.from(update.getStatus()));

                    //replace revision
                    revisions.remove(revision);

                    final IntegrationRevision last = integration.lastRevision();
                    if (IntegrationRevisionState.from(update.getStatus()).equals(last.getCurrentState())) {
                        revision = new IntegrationRevision.Builder().createFrom(revision)
                            .version(last.getVersion())
                            .parentVersion(last.getParentVersion())
                            .build();
                    }
                    revisions.add(revision);

                    dataManager.update(new Integration.Builder()
                        .createFrom(updated)
                        .revisions(revisions)
                        .build());
                }

            } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
                LOG.error("Error while processing integration status for integration {}", integrationId, e);
                // Something went wrong.. lets note it.
                Integration current = dataManager.fetch(Integration.class, integrationId);
                dataManager.update(new Integration.Builder()
                    .createFrom(current)
                    .statusMessage("Error: "+e)
                    .lastUpdated(new Date())
                    .build());

            } finally {
                // Add a next check for the next interval
                reschedule(integrationId);
            }

        });
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    private void reschedule(String integrationId) {
        scheduler.schedule(() -> {
            Integration i = dataManager.fetch(Integration.class, integrationId);
            checkIntegrationStatus(i);
        }, SCHEDULE_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
    }

    private String getIntegrationMarkerKey(Integration integration) {
        return integration.getDesiredStatus().orElseThrow(() -> new IllegalArgumentException("No desired status set on " + integration)).toString() +
               ":" +
               integration.getId().orElseThrow(() -> new IllegalArgumentException("No id set in integration " + integration));
    }

    private boolean stale(StatusChangeHandlerProvider.StatusChangeHandler handler, Integration integration) {
        if (integration == null || handler == null) {
            return true;
        }

        Optional<Integration.Status> desiredStatus = integration.getDesiredStatus();
        return !desiredStatus.isPresent()
               || desiredStatus.equals(integration.getCurrentStatus())
               || !handler.getTriggerStatuses().contains(desiredStatus.get());
    }
}
