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
package io.syndesis.server.monitoring;

import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.server.dao.manager.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Monitor Integrations based on their deployment state.
 * Used for PUBLISHING status updates. Can be utilized in the future for other purposes.
 * @author dhirajsb
 */
@Service
@ConditionalOnProperty(value = "features.monitoring.enabled", havingValue = "true")
@SuppressWarnings("PMD.DoNotUseThreads")
public class DeploymentStateMonitorController implements Runnable, Closeable, DeploymentStateMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentStateMonitorController.class);

    private final MonitoringConfiguration configuration;
    private final DataManager dataManager;
    private final Map<IntegrationDeploymentState, List<StateHandler>> stateHandlers;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService executor = Executors.newCachedThreadPool(new CustomizableThreadFactory("state-monitor"));

    @Autowired
    public DeploymentStateMonitorController(MonitoringConfiguration configuration, DataManager dataManager) {
        this.configuration = configuration;
        this.dataManager = dataManager;
        this.stateHandlers = new ConcurrentHashMap<>();
    }

    @PostConstruct
    @SuppressWarnings("FutureReturnValueIgnored")
    public void open() {
        LOGGER.info("Starting deployment state monitor.");
        scheduler.scheduleWithFixedDelay(this, configuration.getInitialDelay(), configuration.getPeriod(), TimeUnit.SECONDS);
    }

    @Override
    public void close() throws IOException {
        LOGGER.info("Stopping state monitor.");
        close(scheduler);
        close(executor);
    }

    @Override
    public void run() {
        LOGGER.debug("Processing states for integration deployments.");
        try {
            for (Map.Entry<IntegrationDeploymentState, List<StateHandler>> e : this.stateHandlers.entrySet()) {
                final IntegrationDeploymentState currentState = e.getKey();
                final List<StateHandler> handlers = e.getValue();

                // Indexed search
                final Stream<IntegrationDeployment> deployments = dataManager.fetchAllByPropertyValue(IntegrationDeployment.class, "currentState", currentState.name());
                deployments.forEach(deployment -> {
                    for (StateHandler handler : handlers) {
                        try {
                            handler.accept(deployment);
                        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception ex) {
                            if (LOGGER.isErrorEnabled()) {
                                final String msg = String.format("Error handling state [%s] with handler [%s]", currentState, handler.getDescription());
                                LOGGER.error(msg, ex);
                            }
                        }
                    }
                });
            }
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception ex) {
            LOGGER.error("Error while iterating integration deployments.", ex);
        }

    }

    private static List<Runnable> close(ExecutorService service) throws IOException {
        service.shutdown();
        try {
            if (service.awaitTermination(1, TimeUnit.MINUTES)) {
                return service.shutdownNow();
            } else {
                return Collections.emptyList();
            }
        } catch (InterruptedException e) {
            return service.shutdownNow();
        }
    }

    @Override
    public void register(IntegrationDeploymentState state, StateHandler stateHandler) {
        final List<StateHandler> handlers = this.stateHandlers.computeIfAbsent(state,
                k -> Collections.synchronizedList(new ArrayList<>()));
        handlers.add(stateHandler);
    }
}
