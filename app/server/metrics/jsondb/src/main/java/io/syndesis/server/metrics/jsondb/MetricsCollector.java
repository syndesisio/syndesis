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
package io.syndesis.server.metrics.jsondb;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.readiness.Readiness;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.jsondb.JsonDB;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.metrics.IntegrationMetricsSummary;

@Service
@ConditionalOnProperty(value = "metrics.kind", havingValue = "sql")
@SuppressWarnings("PMD.DoNotUseThreads")
public class MetricsCollector implements Runnable, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsCollector.class);

    private final KubernetesClient kubernetes;
    private final DataManager dataManager;
    private final RawMetricsHandler rmh;
    private final IntegrationMetricsHandler imh;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService executor = Executors.newCachedThreadPool(new CustomizableThreadFactory("metrics-collector"));

    @Autowired
    public MetricsCollector(DataManager dataManager, JsonDB jsonDB, KubernetesClient kubernetes) {
        this.dataManager = dataManager;
        this.kubernetes = kubernetes;
        this.rmh = new JsonDBRawMetrics(jsonDB);
        this.imh = new IntegrationMetricsHandler(dataManager);
    }


    @PostConstruct
    @SuppressWarnings("FutureReturnValueIgnored")
    public void open() {
        LOGGER.info("Starting metrics collector.");
        scheduler.scheduleAtFixedRate(this, 10, 10, TimeUnit.SECONDS);
    }


    @Override
    public void close() throws IOException {
        LOGGER.info("Stopping metrics collector.");
        close(scheduler);
        close(executor);
    }


    @Override
    public void run() {
        LOGGER.debug("Collecting metrics for active integration pods.");
        try {
            List<Pod> integrationPodList = kubernetes.pods().withLabel("integration").list().getItems();
            Set<String> livePods = new HashSet<>();
            for (Pod pod : integrationPodList) {
                livePods.add(pod.getMetadata().getName());
            }

            integrationPodList
                .stream()
                .filter(p -> Readiness.isReady(p))
                .forEach(p ->
                    executor.execute(new PodMetricsReader(
                            kubernetes,
                            p.getMetadata().getName(),
                            p.getMetadata().getAnnotations().get("syndesis.io/integration-name"),
                            p.getMetadata().getLabels().get("syndesis.io/integration-id"),
                            p.getMetadata().getLabels().get("syndesis.io/deployment-version"),
                            rmh))
            );

            Set<String> activeIntegrationIds = dataManager.fetchIds(Integration.class);
            for (String integrationId : activeIntegrationIds) {
                LOGGER.debug("Computing metrics for IntegrationId: {}",integrationId);

                Map<String,RawMetrics> rawMetrics = rmh.getRawMetrics(integrationId);
                IntegrationMetricsSummary imSummary = imh.compute(
                                integrationId,
                                rawMetrics,
                                livePods);
                imh.persist(imSummary);
                rmh.curate(integrationId, rawMetrics, livePods);
            }

            rmh.curate(activeIntegrationIds);
            imh.curate(activeIntegrationIds);

        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception ex) {
            LOGGER.error("Error while iterating integration pods.", ex);
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
}
