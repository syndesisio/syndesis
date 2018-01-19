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
package io.syndesis.rest.metrics.collector;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.readiness.Readiness;
import io.syndesis.core.Json;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.jsondb.GetOptions;
import io.syndesis.jsondb.JsonDB;
import io.syndesis.model.ListResult;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.metrics.IntegrationMetricsSummary;

@Service
public class MetricsCollector implements Runnable, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsCollector.class);
    private static final String HISTORY = "HISTORY";

    private final KubernetesClient kubernetes;
    private final RawMetricsHandler rawMetricsHandler;
    private final DataManager dataManager;
    private final JsonDB jsonDB;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService executor = Executors.newCachedThreadPool(new CustomizableThreadFactory("metrics-collector"));

    @Autowired
    public MetricsCollector(DataManager dataManager, JsonDB jsonDB, KubernetesClient kubernetes) {
        this.dataManager = dataManager;
        this.jsonDB = jsonDB;
        this.kubernetes = kubernetes;
        this.rawMetricsHandler = new PersistRawMetrics(jsonDB);
    }


    @PostConstruct
    @SuppressWarnings("FutureReturnValueIgnored")
    public void open() {
        LOGGER.info("Starting metrics collector.");
        scheduler.scheduleAtFixedRate(this, 10, 5, TimeUnit.SECONDS);
    }


    @Override
    public void close() throws IOException {
        LOGGER.info("Stopping metrics collector.");
        close(scheduler);
        close(executor);
    }


    @Override
    public void run() {
        LOGGER.info("Collecting metrics for active integration pods.");
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
                            p.getMetadata().getLabels().get("integration"),
                            p.getMetadata().getLabels().get("syndesis.io/integration-id"),
                            p.getMetadata().getLabels().get("syndesis.io/deployment-id"),
                            rawMetricsHandler))
            );

            ListResult<Integration> integrationList = dataManager.fetchAll(Integration.class);
            Set<String> activeIntegrationIds = new HashSet<>();
            for (Integration integration : integrationList.getItems()) {
                String name = integration.getName();
                String integrationId = integration.getId().get();
                LOGGER.info("IntegrationId=" + integrationId);
                activeIntegrationIds.add(name);

                Map<String,RawMetrics> rawMetrics = getRawMetrics(name);
                IntegrationMetricsSummary currentSummary =
                        computeIntegrationSummary(integrationId, rawMetrics, livePods);
                IntegrationMetricsSummary existingSummary =
                        dataManager.fetch(IntegrationMetricsSummary.class, integrationId);
                if (existingSummary == null) {
                    dataManager.create(currentSummary);
                } else if (existingSummary.hashCode() != currentSummary.hashCode()) {
                    //only write to the DB when the new metrics differs to unnecessary
                    //and expensive writes to the DB
                    dataManager.update(currentSummary);
                }
                curateDeadPodMetrics(integrationId, name, rawMetrics, livePods);
            }
            curateDeletedIntegrationMetrics(activeIntegrationIds);

        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") IOException ex) {
            LOGGER.error("Error while iterating integration pods.", ex);
        }

    }

    /**
     * Obtains all RawMetrics entries in the DB for the current integration
     *
     * @param integrationId - the integrationId for which we are obtaining the metrics
     * @return a Map containing all RawMetrics entries for the current integration,
     * the key is either HISTORY or the podName.
     * @throws IOException
     */
    /*default*/ Map<String,RawMetrics> getRawMetrics(String integrationId) throws IOException {
        //try to obtain metrics in this integration
        Map<String,RawMetrics> metrics = new HashMap<>();
        String json = jsonDB.getAsString(path(integrationId));
        LOGGER.debug("JSON: " + json);
        if (json != null) {
            metrics = Json.mapper().readValue(json, new TypeReference<Map<String,RawMetrics>>() {});
        }
        return metrics;
    }

    /**
     * Computes the IntegrationMetricsSummary from the RawMetrics available for the
     * current integration.
     *
     * @param integrationId
     * @param metrics
     * @param livePodIds
     * @return
     */
    /*default*/ IntegrationMetricsSummary computeIntegrationSummary(
            String integrationId,
            Map<String,RawMetrics> metrics,
            Set<String> livePodIds) {

        Long messages = 0L;
        Long errors = 0L;
        Optional<Date> lastProcessed = Optional.empty();
        Optional<Date> startDate = Optional.empty(); //we may have no more live pods for this integration

        for (RawMetrics raw:metrics.values()) {
            messages += raw.getMessages();
            errors += raw.getErrors();
            //Let's simply grab the oldest living pod, we will need to revisit when doing rolling upgrades etc
            if (livePodIds.contains(raw.getPod())) {
                if (startDate.isPresent()) {
                    if (raw.getStartDate().get().before(startDate.get())) {
                        startDate = raw.getStartDate();
                    }
                } else {
                    startDate = raw.getStartDate();
                }
            }
            if (raw.getLastProcessed().isPresent()) {
                if (lastProcessed.isPresent()) {
                    lastProcessed = raw.getLastProcessed().get().after(lastProcessed.get()) ? raw.getLastProcessed() : lastProcessed;
                } else {
                    lastProcessed = raw.getLastProcessed();
                }
            }
        }

        return new IntegrationMetricsSummary.Builder()
                .id(integrationId)
                .messages(messages)
                .errors(errors)
                .start(startDate)
                .lastProcessed(lastProcessed)
                .build();
    }

    /**
     * Adds the RawMetrics of dead pods to a special HISTORY bucket. Each
     * Integration should only have 1 HISTORY bucket and 1 bucket per live
     * pod.
     *
     * @param integrationId
     * @param metrics
     * @param livePodIds
     * @throws IOException
     */
    /*default*/ void curateDeadPodMetrics(
            String integrationId,
            String integration,
            Map<String,RawMetrics> metrics,
            Set<String> livePodIds) throws IOException {

        for (Map.Entry<String, RawMetrics> entry : metrics.entrySet()) {
            if (! entry.getKey().equals(HISTORY) && ! livePodIds.contains(entry.getKey())) { //dead pod check
                if (metrics.containsKey(HISTORY)) {
                    //add to existing history
                    RawMetrics history = metrics.get(HISTORY);
                    RawMetrics dead = entry.getValue();
                    Date lastProcessed = history.getLastProcessed().orElse(new Date(0)).after(dead.getLastProcessed().orElse(new Date(0)))
                            ? history.getLastProcessed().orElse(null) : dead.getLastProcessed().orElse(null);
                    RawMetrics updatedHistoryMetrics = new RawMetrics.Builder()
                            .integrationId(integrationId)
                            .integration(integration)
                            .pod(history.getIntegrationId() + ":" + dead.getPod())
                            .messages(history.getMessages() + dead.getMessages())
                            .errors(history.getErrors() + dead.getErrors())
                            .startDate(Optional.empty())
                            .resetDate(Optional.empty())
                            .lastProcessed(Optional.ofNullable(lastProcessed))
                            .build();
                    String json = Json.mapper().writeValueAsString(updatedHistoryMetrics);
                    jsonDB.update(path(integrationId,HISTORY), json);
                } else {
                    //create history bucket, first time we find a dead pod for this integration
                    String json = Json.mapper().writeValueAsString(metrics.get(entry.getKey()));
                    jsonDB.set(path(integrationId,HISTORY), json);
                }
                //delete the dead pod metrics since it has been added to the history
                jsonDB.delete(path(integrationId,entry.getKey()));
            }
        }
    }

    /**
     * If Integrations get deleted we should also delete their metrics
     *
     * @param activeIntegrationIds
     * @throws IOException
     * @throws JsonMappingException
     */
    /*default*/ void curateDeletedIntegrationMetrics(Set<String> activeIntegrationIds) throws IOException, JsonMappingException {

        //1. Loop over all RawMetrics
        String json = jsonDB.getAsString(path(), new GetOptions().depth(1));
        Map<String,Boolean> metricsMap = Json.mapper().readValue(json, new TypeReference<Map<String,Boolean>>() {});
        Set<String> rawIntegrationIds = metricsMap.keySet();
        for (String rawIntId : rawIntegrationIds) {
            if (! activeIntegrationIds.contains(rawIntId)) {
                jsonDB.delete(path(rawIntId));
            }
        }
        //2. Loop over all IntegrationMetricsSummary
        ListResult<IntegrationMetricsSummary> intSummaries= dataManager.fetchAll(IntegrationMetricsSummary.class);
        Iterator<IntegrationMetricsSummary> iterator = intSummaries.getItems().iterator();
        while (iterator.hasNext()) {
            IntegrationMetricsSummary summary = iterator.next();
            if (! activeIntegrationIds.contains(summary.getId().get())) {
                dataManager.delete(IntegrationMetricsSummary.class, summary.getId().get());
            }
        }
    }

    /*default*/ static String path(String integrationId, String podName) {
        return String.format("%s/integrations/%s/pods/%s", RawMetrics.class.getSimpleName(), integrationId, podName);
    }

    /*default*/ static String path(String integrationId) {
        return String.format("%s/integrations/%s/pods", RawMetrics.class.getSimpleName(), integrationId);
    }

    /*default*/ static String path() {
        return String.format("%s/integrations", RawMetrics.class.getSimpleName());
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
