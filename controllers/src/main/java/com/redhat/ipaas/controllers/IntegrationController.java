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
package com.redhat.ipaas.controllers;

import com.redhat.ipaas.core.EventBus;
import com.redhat.ipaas.core.Json;
import com.redhat.ipaas.dao.manager.DataManager;
import com.redhat.ipaas.model.ChangeEvent;
import com.redhat.ipaas.model.Kind;
import com.redhat.ipaas.model.integration.Integration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class tracks changes to Integrations and attempts to process them so that
 * their current status matches their desired status.
 */
@Service
public class IntegrationController {

    private final DataManager dataManager;
    private final EventBus eventBus;
    private final HashMap<Integration.Status, WorkflowHandler> handlers = new HashMap<>();
    private ExecutorService executor;
    private ScheduledExecutorService scheduler;
    private long retrySeconds = 60;

    @Autowired
    public IntegrationController(DataManager dataManager, EventBus eventBus, List<WorkflowHandler> handlers) {
        this.dataManager = dataManager;
        this.eventBus = eventBus;
        for (WorkflowHandler handler : handlers) {
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
            if (event!=null && "change-event".equals(event)) {
                try {
                    ChangeEvent changeEvent = Json.mapper().readValue(data, ChangeEvent.class);
                    if ((changeEvent != null
                        || changeEvent.getKind().isPresent() && changeEvent.getId().isPresent())
                        && (Kind.from(changeEvent.getKind().get()) == Kind.Integration)) {
                        enqueueCheckIntegration(changeEvent.getId().get());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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
        dataManager.fetchAll(Integration.class).getItems().forEach(integration -> {
            checkIntegration(integration);
        });
    }

    private void enqueueCheckIntegration(String id) {
        executor.execute(() -> {
            checkIntegration(dataManager.fetch(Integration.class, id));
        });
    }

    private void checkIntegration(Integration integration) {
        Integration.Status desired = integration.getDesiredStatus().orElse(Integration.Status.Draft);
        Integration.Status current = integration.getCurrentStatus().orElse(Integration.Status.Draft);
        if (!current.equals(desired)) {
            WorkflowHandler workflowHandler = handlers.get(desired);
            if (workflowHandler != null) {
                enqueue(workflowHandler, integration.getId().get());
            }
        }
    }

    private void enqueue(WorkflowHandler handler, String integrationId) {
        executor.submit(() -> {
            Integration integration = dataManager.fetch(Integration.class, integrationId);
            if (stale(handler, integration)) {
                return;
            }

            try {

                Integration.Status currentStatus = handler.execute(integration);
                if (currentStatus == null) {
                    return;
                }

                // handler.execute might block for while so refresh our copy of the integration
                // data before we update the current status
                integration = dataManager.fetch(Integration.class, integrationId);
                integration = new Integration.Builder().currentStatus(currentStatus).build();
                dataManager.update(integration);

            } catch (Exception e) {

                e.printStackTrace();
                // TODO update the integration status

                // Retry in a while
                scheduler.schedule(() -> {
                    enqueue(handler, integrationId);
                }, retrySeconds, TimeUnit.SECONDS);
            }
        });
    }

    private boolean stale(WorkflowHandler workflow, Integration integration) {
        return
            integration == null
            || workflow == null
            || !workflow.getTriggerStatuses().contains(integration.getDesiredStatus());
    }

}
