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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import io.syndesis.common.model.ChangeEvent;
import io.syndesis.common.model.Kind;
import io.syndesis.common.util.EventBus;
import io.syndesis.common.util.EventBus.Subscription;
import io.syndesis.common.util.Json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ResourceUpdateController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUpdateController.class);

    final Subscription handler = this::onEvent;

    final AtomicBoolean running;

    ScheduledExecutorService scheduler;

    private final List<ChangeEvent> allEvents;

    private final EventBus eventBus;

    private final List<ResourceUpdateHandler> handlers;

    private final Supplier<ScheduledExecutorService> schedulerCreator;

    @Autowired
    public ResourceUpdateController(final ResourceUpdateConfiguration configuration, final EventBus eventBus, final List<ResourceUpdateHandler> handlers) {
        this.eventBus = eventBus;
        this.handlers = new ArrayList<>(handlers);
        running = new AtomicBoolean(false);

        allEvents = new ArrayList<>();
        for (final Kind kind : Kind.values()) {
            allEvents.add(new ChangeEvent.Builder().kind(kind.getModelName()).build());
        }

        schedulerCreator = schedulerConfiguredFrom(configuration);
        scheduler = schedulerCreator.get();
    }

    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        if (scheduler.isShutdown()) {
            scheduler = schedulerCreator.get();
        }

        LOGGER.debug("Subscribing to EventBus");
        eventBus.subscribe(getClass().getName(), handler);
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        eventBus.unsubscribe(getClass().getName());

        scheduler.shutdownNow();
    }

    // FutureReturnValueIgnored: there should not be any exceptions from logging
    // PMD.InvalidSlf4jMessageFormat: https://github.com/pmd/pmd/issues/939
    @SuppressWarnings({"FutureReturnValueIgnored", "PMD.InvalidSlf4jMessageFormat"})
    void onEvent(final String event, final String data) {
        onEventInternal(event, data)
            .whenComplete((x, t) -> {
                if (t == null) {
                    LOGGER.debug("Processed event: {} with data {}", event, data);
                } else {
                    LOGGER.warn("Failed to process event: {} with data {}", event, data, t);
                }
            });
    }

    CompletableFuture<Void> onEventInternal(final String event, final String data) {
        if (!running.get()) {
            return CompletableFuture.completedFuture(null);
        }

        // Never do anything that could block in this callback!
        if (!Objects.equals(event, EventBus.Type.CHANGE_EVENT)) {
            return CompletableFuture.completedFuture(null);
        }

        final ChangeEvent changeEvent;
        try {
            changeEvent = Json.reader().forType(ChangeEvent.class).readValue(data);
        } catch (final IOException e) {
            LOGGER.error("Error while processing change-event {}", data, e);
            final CompletableFuture<Void> errored = new CompletableFuture<>();
            errored.completeExceptionally(e);

            return errored;
        }

        final CompletableFuture<Void> done = new CompletableFuture<>();
        if (changeEvent != null) {
            scheduler.execute(() -> run(changeEvent, done));
        }

        return done;
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    final Supplier<ScheduledExecutorService> schedulerConfiguredFrom(final ResourceUpdateConfiguration configuration) {
        return () -> {
            final ScheduledExecutorService configuredScheduler = Executors.newScheduledThreadPool(1,
                r -> new Thread(null, r, "ResourceUpdateController (scheduler)"));

            if (configuration.getScheduler().isEnabled()) {
                LOGGER.debug("Register background resource update check task (interval={}, interval-unit={})",
                    configuration.getScheduler().getInterval(),
                    configuration.getScheduler().getIntervalUnit());

                final ScheduledFuture<?> task = configuredScheduler.scheduleWithFixedDelay(this::run, 0, configuration.getScheduler().getInterval(),
                    configuration.getScheduler().getIntervalUnit());

                CompletableFuture.supplyAsync(() -> {
                    try {
                        return task.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new IllegalStateException(e);
                    }
                }).whenComplete((x, t) -> {
                    if (t != null) {
                        LOGGER.warn("Failure in scheduled event processing", t);
                    }
                });
            } else {
                LOGGER.debug("Execute one-time resource update check");
                configuredScheduler.execute(this::run);
            }

            return configuredScheduler;
        };
    }

    private void run() {
        for (int h = 0; h < handlers.size(); h++) {
            final ResourceUpdateHandler handler = handlers.get(h);

            for (int i = 0; i < allEvents.size(); i++) {
                final ChangeEvent event = allEvents.get(i);

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

    private void run(final ChangeEvent event, final CompletableFuture<Void> done) {
        if (!running.get()) {
            done.complete(null);
            return;
        }

        for (int i = 0; i < handlers.size(); i++) {
            final ResourceUpdateHandler handler = handlers.get(i);

            if (handler.canHandle(event)) {
                LOGGER.debug("Trigger handler {} for event {}", handler, event);
                handler.process(event);
            }
        }

        done.complete(null);
    }
}
