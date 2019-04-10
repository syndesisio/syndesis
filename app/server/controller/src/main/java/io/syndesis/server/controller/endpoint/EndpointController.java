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
package io.syndesis.server.controller.endpoint;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import io.syndesis.common.model.ChangeEvent;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationEndpoint;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.util.EventBus;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.backend.BackendController;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.openshift.OpenShiftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This class keeps the integration endpoint data aligned with the external state.
 */
@Service
public class EndpointController implements BackendController {
    private static final Logger LOG = LoggerFactory.getLogger(EndpointController.class);

    private static final String EVENT_BUS_ID = "integration-endpoint-controller";

    private final OpenShiftService openShiftService;
    private final DataManager dataManager;
    private final EventBus eventBus;

    private ScheduledExecutorService scheduler;

    @Autowired
    public EndpointController(OpenShiftService openShiftService, DataManager dataManager, EventBus eventBus) {
        this.openShiftService = openShiftService;
        this.dataManager = dataManager;
        this.eventBus = eventBus;
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    @PostConstruct
    @Override
    public void start() {
        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor(threadFactory("Endpoint Controller"));
            scheduler.scheduleWithFixedDelay(this::scanIntegrationDeployments, 5, 60, TimeUnit.SECONDS);
            eventBus.subscribe(EVENT_BUS_ID, getChangeEventSubscription());
        }
    }

    @PreDestroy
    @Override
    public void stop() {
        if (scheduler != null) {
            eventBus.unsubscribe(EVENT_BUS_ID);
            scheduler.shutdownNow();
            boolean schedulerShutdown = false;
            do {
                try {
                    schedulerShutdown = scheduler.awaitTermination(10, TimeUnit.SECONDS);
                } catch (final InterruptedException e) {
                    LOG.warn("Unable to cleanly stop: {}", e.getMessage());
                    LOG.debug("Interrupted while stopping", e);
                }
            } while (!schedulerShutdown);

            scheduler = null;
        }
    }

    @SuppressWarnings("PMD.DoNotUseThreads")
    private static ThreadFactory threadFactory(String name) {
        return r -> new Thread(null, r, name);
    }

    private EventBus.Subscription getChangeEventSubscription() {
        return (event, data) -> {
            // Never do anything that could block in this callback!
            if ("change-event".equals(event)) {
                try {
                    ChangeEvent changeEvent = Json.reader().forType(ChangeEvent.class).readValue(data);
                    Optional.ofNullable(changeEvent)
                        .flatMap(ChangeEvent::getId)
                        .ifPresent(id -> changeEvent.getKind()
                            .map(Kind::from)
                            .filter(k -> k == Kind.IntegrationDeployment)
                            .ifPresent(k -> this.scheduler.execute(() -> this.checkIntegrationDeployment(id))));

                } catch (IOException e) {
                    LOG.error("Error while subscribing to change-event {}", data, e);
                }
            }
        };
    }

    private void scanIntegrationDeployments() {
        LOG.debug("Checking exposed endpoints for their status.");
        scheduler.execute(() -> dataManager.fetchIds(IntegrationDeployment.class).forEach(this::checkIntegrationDeployment));
    }

    private void checkIntegrationDeployment(String integrationDeploymentId) {
        if (integrationDeploymentId != null) {
            IntegrationDeployment deployment = dataManager.fetch(IntegrationDeployment.class, integrationDeploymentId);
            if (deployment != null) {
                updateIntegrationEndpoint(deployment);
            }
        }
    }

    private void updateIntegrationEndpoint(IntegrationDeployment deployment) {
        if (deployment.getId().isPresent()) {
            IntegrationEndpoint endpoint = dataManager.fetch(IntegrationEndpoint.class, deployment.getId().get());
            boolean endpointPresent = endpoint != null;

            Optional<String> host = openShiftService.getExposedHost(deployment.getSpec().getName());
            Optional<IntegrationEndpoint> expectedEndpoint = expectedEndpoint(deployment, host);

            // Synchronize internal model
            if (!endpointPresent && expectedEndpoint.isPresent()) {
                LOG.info("Adding endpoint {} to integration deployment {}", expectedEndpoint.get(), deployment.getId().get());
                dataManager.create(expectedEndpoint.get());
            } else if (endpointPresent && expectedEndpoint.isPresent()) {
                if (!endpoint.equals(expectedEndpoint.get())) {
                    LOG.info("Updating endpoint for deployment {} to {}", deployment.getId().get(), expectedEndpoint.get());
                    dataManager.update(expectedEndpoint.get());
                }
            } else if (endpointPresent) {
                LOG.info("Deleting endpoint for deployment {}", deployment.getId().get());
                dataManager.delete(IntegrationEndpoint.class, deployment.getId().get());
            }
        }
    }

    private Optional<IntegrationEndpoint> expectedEndpoint(IntegrationDeployment integrationDeployment, Optional<String> host) {
        if (!host.isPresent()) {
            return Optional.empty();
        }

        Integration integ = integrationDeployment.getSpec();

        return Optional.of(integrationDeployment)
            .map(deployment -> new IntegrationEndpoint.Builder()
                    .id(deployment.getId())
                    .host(host)
                    .protocol("https")
                    .contextPath(join(serverBasePath(integ), contextPath(integ)))
                    .build());
    }

    private Optional<String> serverBasePath(Integration integration) {
        return allSteps(integration)
            .findFirst()
            .flatMap(Step::getAction)
            .flatMap(action -> action.getMetadata("serverBasePath"));
    }

    private Optional<String> contextPath(Integration integration) {
        return allSteps(integration)
            .findFirst()
            .flatMap(step -> step.getAction().flatMap(action -> action.propertyTaggedWith(step.getConfiguredProperties(), "context-path")));
    }

    @SafeVarargs
    private final String join(Optional<String>... paths) {
        StringBuilder res = new StringBuilder();
        for (Optional<String> path : paths) {
            if (path.isPresent()) {
                String part = path.get();
                while (part.startsWith("/")) {
                    part = part.substring(1);
                }

                while (res.length() > 0 && res.lastIndexOf("/") == res.length() - 1) {
                    res.deleteCharAt(res.length() - 1);
                }
                res.append('/').append(part);
            }
        }
        return res.toString();
    }

    private static Stream<Step> allSteps(Integration integration) {
        return integration.getFlows().stream().flatMap(f -> f.getSteps().stream());
    }

}
