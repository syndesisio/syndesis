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
package io.syndesis.server.update.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import io.syndesis.common.model.ChangeEvent;
import io.syndesis.common.model.Kind;
import io.syndesis.common.util.EventBus;
import io.syndesis.common.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

public class ResourceUpdateController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUpdateController.class);

    private final ResourceUpdateConfiguration configuration;
    private final EventBus eventBus;
    private final List<ResourceUpdateHandler> handlers;
    private final AtomicBoolean running;
    private final List<ChangeEvent> allEvents;

    @SuppressWarnings("PMD.AvoidUsingVolatile")
    private volatile ScheduledExecutorService scheduler;

    @Autowired
    public ResourceUpdateController(ResourceUpdateConfiguration configuration, EventBus eventBus, List<ResourceUpdateHandler> handlers) {
        this.configuration = configuration;
        this.eventBus = eventBus;
        this.handlers = new ArrayList<>(handlers);
        this.running = new AtomicBoolean(false);

        this.allEvents = new ArrayList<>();
        for (Kind kind : Kind.values()) {
            allEvents.add(new ChangeEvent.Builder().kind(kind.getModelName()).build());
        }
    }

    public void start() {
        if (configuration.isEnabled()) {
            running.set(true);

            LOGGER.debug("Subscribing to EventBus");
            eventBus.subscribe(getClass().getName(), this::onEvent);
        }
    }

    public void stop() {
        running.set(false);

        eventBus.unsubscribe(getClass().getName());

        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private void onEvent(String event, String data) {
        if (!running.get()) {
            return;
        }

        // Never do anything that could block in this callback!
        if (Objects.equals(event, EventBus.Type.CHANGE_EVENT)) {
            try {
                ChangeEvent changeEvent = Json.reader().forType(ChangeEvent.class).readValue(data);
                if (changeEvent != null) {
                    scheduler.execute(() -> run(changeEvent));
                }
            } catch (IOException e) {
                LOGGER.error("Error while processing change-event {}", data, e);
            }
        }
    }

    private void run() {
        for (int h = 0; h < handlers.size(); h++) {
            ResourceUpdateHandler handler = handlers.get(h);

            for (int i = 0; i < allEvents.size(); i++) {
                ChangeEvent event = allEvents.get(i);

                if (handler.canHandle(event)) {
                    LOGGER.debug("Trigger handler {}", handler);
                    handler.process(event);

                    // At the moment, handlers are not selective but they scan
                    // resources every time process is invoked so we do not need
                    // to trigger the handler multiple time.
                    break;
                }
            }
        }
    }

    private void run(ChangeEvent event) {
        if (!running.get()) {
            return;
        }

        for (int i = 0; i < handlers.size(); i++) {
            ResourceUpdateHandler handler = handlers.get(i);

            if (handler.canHandle(event)) {
                LOGGER.debug("Trigger handler {} for event {}", handler, event);
                handler.process(event);
            }
        }
    }

    @SuppressWarnings({"FutureReturnValueIgnored", "PMD.DoNotUseThreads"})
    @EventListener
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        if (configuration.isEnabled()) {
            scheduler = Executors.newScheduledThreadPool(1, r -> new Thread(null, r, "ResourceUpdateController (scheduler)"));

            if (configuration.getScheduler().isEnabled()) {
                LOGGER.debug("Register background resource update check task (interval={}, interval-unit={})",
                    configuration.getScheduler().getInterval(),
                    configuration.getScheduler().getIntervalUnit()
                );

                scheduler.scheduleWithFixedDelay(this::run, 0, configuration.getScheduler().getInterval(), configuration.getScheduler().getIntervalUnit());
            } else {
                LOGGER.debug("Execute one-time resource update check");
                scheduler.execute(this::run);
            }
        }
    }
}
