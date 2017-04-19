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
package com.redhat.ipaas.controllers.integration;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.redhat.ipaas.core.EventBus;
import com.redhat.ipaas.core.Json;
import com.redhat.ipaas.dao.manager.DataManager;
import com.redhat.ipaas.model.ChangeEvent;
import com.redhat.ipaas.model.Kind;
import com.redhat.ipaas.model.integration.Integration;
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
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final DataManager dataManager;
    private final EventBus eventBus;
    private final ConcurrentHashMap<Integration.Status, StatusChangeHandlerProvider.StatusChangeHandler> handlers = new ConcurrentHashMap<>();
    private final HashMap<String, String> scheduledChecks = new HashMap<>();
    private ExecutorService executor;
    private ScheduledExecutorService scheduler;
    private long retrySeconds = 60;

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

        eventBus.subscribe("integration-controller", (event, data) -> {
            // Never do anything that could block in this callback!
            if (event!=null && "change-event".equals(event)) {
                try {
                    ChangeEvent changeEvent = Json.mapper().readValue(data, ChangeEvent.class);
                    if (changeEvent != null) {
                        changeEvent.getId().ifPresent(
                            id -> changeEvent.getKind().ifPresent(
                                kind -> {
                                    if (Kind.from(kind) == Kind.Integration) {
                                        enqueueCheckIntegration(id);
                                    }
                                }));
                    }
                } catch (IOException e) {
                    log.error("Error while subscribing to change-event " + data, e);
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
        executor.submit(() -> {
            dataManager.fetchAll(Integration.class).getItems().forEach(integration -> {
                log.info("Checking integrations for their status.");
                checkIntegration(integration);
            });
        });
    }

    private void enqueueCheckIntegration(String id) {
        executor.execute(() -> {
            checkIntegration(dataManager.fetch(Integration.class, id));
        });
    }

    private void checkIntegration(Integration integration) {
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
                        log.info(String.format("Integration %s is not in the desired status %s (current: %s) --> enqueuing",
                                               integrationId, desiredStatus.toString(), current.map(Enum::toString).orElse("[none]")));
                        enqueue(statusChangeHandler, integrationId);
                    }
                }));
        }
    }

    private void enqueue(StatusChangeHandlerProvider.StatusChangeHandler handler, String integrationId) {
        executor.submit(() -> {
            Integration integration = dataManager.fetch(Integration.class, integrationId);
            String scheduledKey = getIntegrationMarkerKey(integration);
            if (stale(handler, integration) || scheduledChecks.containsKey(scheduledKey)) {
                return;
            }

            try {
                log.info("Processing integration " + integrationId + ".");
                StatusChangeHandlerProvider.StatusChangeHandler.StatusUpdate update = handler.execute(integration);
                if (update!=null) {

                    // handler.execute might block for while so refresh our copy of the integration
                    // data before we update the current status

                    // TODO: do this in a single TX.
                    Integration current = dataManager.fetch(Integration.class, integrationId);
                    dataManager.update(
                        new Integration.Builder()
                            .createFrom(current)
                            .currentStatus(update.getNewStatus())
                            .statusMessage(update.getStatusMessage())
                            .lastUpdated(new Date())
                            .build());
                }

            } catch (Exception e) {
                log.error("Error while processing integration status for integration " + integrationId, e);
                // Something went wrong.. lets note it.
                Integration current = dataManager.fetch(Integration.class, integrationId);
                dataManager.update(new Integration.Builder()
                    .createFrom(current)
                    .statusMessage("Error: "+e)
                    .lastUpdated(new Date())
                    .build());

            } finally {
                scheduledChecks.put(scheduledKey, integrationId);
                scheduler.schedule(() -> {
                    scheduledChecks.remove(scheduledKey, integrationId);
                    Integration i = dataManager.fetch(Integration.class, integrationId);
                    checkIntegration(i);
                }, retrySeconds, TimeUnit.SECONDS);
            }

        });
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
