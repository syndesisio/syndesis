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
package io.syndesis.rest.dblogging.controller;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.syndesis.core.Json;
import io.syndesis.core.KeyGenerator;
import io.syndesis.jsondb.GetOptions;
import io.syndesis.jsondb.JsonDB;
import io.syndesis.openshift.OpenShiftService;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * This class tracks pod controller and ingests them into our DB.
 */
@Service
public class LogsController implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(LogsController.class);

    private final DBI dbi;
    private final KubernetesClient client;
    private final Map<String, PodLogHandler> podHandlers = new HashMap<>();
    private final JsonDB jsonDB;
    private final KubernetesSupport kubernetesSupport;
    private ScheduledExecutorService scheduler;

    protected ExecutorService executor;
    protected final LinkedBlockingDeque<BatchOperation> eventQueue = new LinkedBlockingDeque<>(1000);
    protected final AtomicBoolean stopped = new AtomicBoolean();

    private Duration retention = Duration.ofDays(1);
    private Duration cleanUpInterval = Duration.ofMinutes(15);
    private Duration startupDelay = Duration.ofSeconds(15);

    @Autowired
    public LogsController(JsonDB jsonDB, DBI dbi, KubernetesClient client) {
        this.jsonDB = jsonDB;
        this.dbi = dbi;
        this.client = client;
        this.kubernetesSupport = new KubernetesSupport(client);
    }

    @PostConstruct
    @SuppressWarnings("FutureReturnValueIgnored")
    public void open() {
        scheduler = Executors.newScheduledThreadPool(1, threadFactory("Logs Controller Scheduler"));
        executor =  Executors.newCachedThreadPool(threadFactory("Logs Controller"));
        executor.execute(this::processEventQueue);
        scheduler.scheduleWithFixedDelay(()->{ executor.execute(this::pollPods);}, startupDelay.getSeconds(), 5, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(()->{ executor.execute(this::cleanupLogs);}, startupDelay.toMillis(), cleanUpInterval.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void cleanupLogs() {
        try {
            LOG.info("Purging old controller");
            long until = System.currentTimeMillis() - retention.toMillis();
            String untilKey = KeyGenerator.recreateKey(until, 0, 0);

            @SuppressWarnings("unchecked")
            Map<String, Object> hashMap = dbGet(HashMap.class, "/logs/integrations");
            if( hashMap!=null ) {
                for (String integrationId : hashMap.keySet()) {
                    String integrationPath = "/logs/exchanges/" + integrationId + "/";
                    int count = deleteFieldsLT(integrationPath, untilKey);
                    LOG.info("deleted {} transactions for integration: {}", count, integrationId);
                }
            }
        } catch (Exception e) {
            LOG.error("Unexpected Error occurred.", e);
        }
    }

    /**
     * TODO: push this into the jsondb API.
     *
     * @param path
     * @param field
     */
    private int deleteFieldsLT(String path, String field) {
        return dbi.inTransaction((conn, status) -> {
            StringBuilder sql = new StringBuilder("DELETE from jsondb where path LIKE ? and path < ?");
            return conn.update(sql.toString(), path+"%", path + field);
        });
    }

    @Override
    @PreDestroy
    public void close() {
        stopped.set(true);
        scheduler.shutdownNow();
        executor.shutdown();
    }


    /**
     * This controller can potentially spin up lots of threads, at last one for each
     * pod that's being processed.  Lets reduce thread stack size since we don't need
     * a very large stack to do log processing.
     *
     * @param name
     * @return
     */
    private static ThreadFactory threadFactory(String name) {
        return r -> new Thread(null, r, name, 1024);
    }

    private void pollPods() {

        try {
            // clear the marks
            for (PodLogHandler handler : podHandlers.values()) {
                handler.markInOpenshift.set(false);
            }


            PodList podList = listPods();
            for (Pod pod : podList.getItems()) {

                // We are only looking for running containers.
                if (!"Running".equals(pod.getStatus().getPhase())) {
                    continue;
                }

                String name = pod.getMetadata().getName();
                PodLogHandler handler = podHandlers.get(name);

                if (handler == null) {
                    // create a new handler.
                    try {
                        handler = new PodLogHandler(this, pod);
                        handler.start();

                        LOG.info("Created handler for pod: {}", handler.podName);
                        podHandlers.put(name, handler);
                    } catch (IOException e) {
                        LOG.error("Unexpected Error", e);
                    }
                } else {
                    // mark existing handlers as being used.
                    handler.markInOpenshift.set(true);
                }
            }

            // Remove items from the map which are no longer in openshift
            Iterator<Map.Entry<String, PodLogHandler>> iterator = podHandlers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, PodLogHandler> next = iterator.next();
                if (!next.getValue().markInOpenshift.get()) {
                    LOG.info("Pod not tracked by openshift anymore: {}", next.getValue().podName);
                    next.getValue().keepTrying.set(false);
                    iterator.remove();
                }
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> pods = dbGet(HashMap.class, "/logs/pods");
            if (pods != null) {
                pods.keySet().removeAll(podHandlers.keySet());
                for (String o : pods.keySet()) {
                    jsonDB.delete("/logs/pods/" + o);
                    LOG.info("Pod state removed from db: {}", o);
                }
            }
        } catch (Throwable e) {
            LOG.error("Unexpected Error occurred.", e);
        }

    }

    protected PodList listPods() {
        return client.pods().withLabel(OpenShiftService.COMPONENT_LABEL, "integration").list();
    }

    protected boolean isPodRunning(String name) {
        Pod pod = client.pods().withName(name).get();
        if (pod == null) {
            return false;
        }
        return "Running".equals(pod.getStatus().getPhase());
    }

    protected void watchLog(String podName, Consumer<InputStream> handler, String sinceTime) throws IOException {
        kubernetesSupport.watchLog(podName, handler, sinceTime, executor);
    }

    public void deletePodLogState(String podName) throws IOException {
        jsonDB.delete("/logs/pods/" + podName);
    }

    public void setPodLogState(String podName, PodLogState state) throws IOException {
        jsonDB.set("/logs/pods/" + podName, Json.mapper().writeValueAsBytes(state));
    }

    public PodLogState getPodLogState(String podName) throws IOException {
        return dbGet(PodLogState.class, "/logs/pods/" + podName);
    }

    private <T> T dbGet(Class<T> type, String path) throws IOException {
        return dbGet(type, path, null);
    }

    private <T> T dbGet(Class<T> type, String path, GetOptions options) throws IOException {
        byte[] data = jsonDB.getAsByteArray(path, options);
        if (data == null) {
            return null;
        }
        return Json.mapper().readValue(data, type);
    }

    private void processEventQueue() {
        try {
            LOG.info("Batch ingestion work thread started.");
            while (!stopped.get()) {

                // Using a timeout so that if queue is empty, we break out periodically to
                // check if we are stopped.
                BatchOperation event = eventQueue.pollFirst(1, TimeUnit.SECONDS);
                if (event == null) {
                    continue;
                }

                // Start a batch..
                HashMap<String, Object> batch = new HashMap<>();
                long batchStartTime = System.currentTimeMillis();
                int eventCounter = 0;

                try {

                    while (!stopped.get() && event != null) {
                        eventCounter++;
                        event.apply(batch);

                        // Once the batch gets big enough, or we are taking too long on this batch..
                        long remaining = 1000 - (System.currentTimeMillis() - batchStartTime);
                        if (batch.size() >= 1000 || remaining <= 0) {
                            event = null;
                        } else {
                            // try to get more for the batch
                            event = eventQueue.poll(remaining, TimeUnit.MILLISECONDS);
                        }
                    }

                    // Write the batch..
                    jsonDB.update("/logs", Json.mapper().writeValueAsBytes(batch));
                    LOG.info("Batch ingested {} log events", eventCounter);

                } catch (IOException e) {
                    LOG.error("Unexpected Error", e);
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Interrupted", e);
        }
        LOG.info("Batch ingestion work thread done.");
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    protected void schedule(Runnable command, long delay, TimeUnit unit) {
        scheduler.schedule(()->{ executor.execute(command); }, delay, unit);
    }

    @Value("${controllers.dblogging.retention:1 day}")
    public void setRetention(String retention) {
        this.retention = new DurationConverter().convert(retention);
    }

    @Value("${controllers.dblogging.cleanUpPeriod:15 minutes}")
    public void setCleanUpInterval(String cleanUpInterval) {
        this.cleanUpInterval = new DurationConverter().convert(cleanUpInterval);
    }

    @Value("${controllers.dblogging.startupDelay:15 seconds}")
    public void setStartupDelay(String startupDelay) {
        this.startupDelay = new DurationConverter().convert(startupDelay);
    }

}
