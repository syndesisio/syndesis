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
package io.syndesis.server.logging.jsondb.controller;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.syndesis.common.util.DurationConverter;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.server.jsondb.GetOptions;
import io.syndesis.server.jsondb.JsonDB;
import io.syndesis.server.jsondb.impl.JsonRecordSupport;
import io.syndesis.server.jsondb.impl.SqlJsonDB;
import io.syndesis.server.openshift.OpenShiftService;

/**
 * This class tracks pod controller and ingests them into our DB.
 */
@Service
@ConditionalOnProperty(value = "controllers.dblogging.enabled", havingValue = "true", matchIfMissing = true)
@SuppressWarnings({"PMD.DoNotUseThreads", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.CyclomaticComplexity", "PMD.GodClass"})
public class ActivityTrackingController implements Closeable {

    static final String IDLE_THREAD_NAME = "Logs Controller [idle]";

    private static final Logger LOG = LoggerFactory.getLogger(ActivityTrackingController.class);

    private final DBI dbi;
    private final KubernetesClient client;
    private final Map<String, PodLogMonitor> podHandlers = new HashMap<>();
    private final JsonDB jsondb;
    private ScheduledExecutorService scheduler;
    private ExecutorService executor;

    final KubernetesSupport kubernetesSupport;

    protected final LinkedBlockingDeque<BatchOperation> eventQueue = new LinkedBlockingDeque<>(1000);
    protected final AtomicBoolean stopped = new AtomicBoolean();

    private Duration retention = Duration.ofDays(1);
    private Duration cleanUpInterval = Duration.ofMinutes(15);
    private Duration startupDelay = Duration.ofSeconds(15);
    private SqlJsonDB.DatabaseKind databaseKind;

    @Autowired
    public ActivityTrackingController(JsonDB jsondb, DBI dbi, KubernetesClient client) {
        this.jsondb = jsondb;
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
        scheduler.scheduleWithFixedDelay(this::pollPods, startupDelay.getSeconds(), 5, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(this::cleanupLogs, startupDelay.toMillis(), cleanUpInterval.toMillis(), TimeUnit.MILLISECONDS);

        // Lets find out the type of DB we are working with.
        dbi.inTransaction((x, status) -> {
            try {
                String dbName = x.getConnection().getMetaData().getDatabaseProductName();
                databaseKind = SqlJsonDB.DatabaseKind.valueOf(dbName);

                // CockroachDB uses the PostgreSQL driver.. so need to look a little closer.
                if( databaseKind == SqlJsonDB.DatabaseKind.PostgreSQL ) {
                    String version = x.createQuery("SELECT VERSION()").mapTo(String.class).first();
                    if( version.startsWith("CockroachDB") ) {
                        databaseKind = SqlJsonDB.DatabaseKind.CockroachDB;
                    }
                }
                return null;
            } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
                throw new IllegalStateException("Could not determine the database type", e);
            }
        });
    }

    public void cleanupLogs() {
        Thread.currentThread().setName("Logs Controller Scheduler [running]: cleanupLogs");

        try {
            LOG.info("Purging old controller");
            long until = System.currentTimeMillis() - retention.toMillis();
            String untilKey = KeyGenerator.recreateKey(until, 0, 0);

            @SuppressWarnings("unchecked")
            Map<String, Object> hashMap = dbGet(HashMap.class, "/activity/integrations"); //NOPMD
            if( hashMap!=null ) {
                for (String integrationId : hashMap.keySet()) {
                    String integrationPath = "/activity/exchanges/" + integrationId + "/";
                    int count = deleteFieldsLT(integrationPath, untilKey);
                    LOG.info("deleted {} transactions for integration: {}", count, integrationId);
                }
            }
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            LOG.error("Unexpected Error occurred.", e);
        } finally {
            Thread.currentThread().setName("Logs Controller Scheduler [idle]");
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

    private void writeBatch(Map<String, Object> batch) {
        dbi.inTransaction((conn, status) -> {
            String sql = "INSERT into jsondb (path, value, ovalue) values (:path, :value, :ovalue)";
            if( databaseKind == SqlJsonDB.DatabaseKind.PostgreSQL ) {
                // Lets update if the record exists.
                sql += " ON CONFLICT (path) DO UPDATE SET value = :value, ovalue = :ovalue"; // NOPMD
            }

            PreparedBatch insert = conn.prepareBatch(sql);
            for (Map.Entry<String, Object> entry : batch.entrySet()) {
                String key = "/activity" + entry.getKey() + "/";
                String value = null;
                String ovalue = null;
                if( key.startsWith("/activity/exchanges" )) {
                    value = JsonRecordSupport.STRING_VALUE_PREFIX + (String)entry.getValue();
                } else if ( key.startsWith("/activity/integrations" )) {
                    ovalue = "true";
                    value = String.valueOf(JsonRecordSupport.TRUE_VALUE_PREFIX);
                } else if ( key.startsWith("/activity/pods" )) {
                    PodLogState p = (PodLogState) entry.getValue();
                    key += "time/"; //NOPMD
                    value = JsonRecordSupport.STRING_VALUE_PREFIX+p.time;
                }
                insert
                    .bind("path", key)
                    .bind("value", value)
                    .bind("ovalue", ovalue)
                    .add();
            }
            return insert.execute();
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
    @SuppressWarnings("PMD.InvalidSlf4jMessageFormat") // false positive
    private static ThreadFactory threadFactory(String name) {
        return r -> {
            Thread thread = new Thread(null, r, name, 1024);
            thread.setUncaughtExceptionHandler((where, throwable) -> LOG.error("Failure running activity tracking task on thread: {}", where.getName(), throwable));

            return thread;
        };
    }

    private void pollPods() {
        Thread.currentThread().setName("Logs Controller Scheduler [running]: pollPods");
        try {
            // clear the marks
            for (PodLogMonitor handler : podHandlers.values()) {
                handler.markInOpenshift.set(false);
            }


            PodList podList = listPods();
            for (Pod pod : podList.getItems()) {

                // We are only looking for running containers.
                if (!"Running".equals(pod.getStatus().getPhase())) {
                    continue;
                }

                String name = pod.getMetadata().getName();
                PodLogMonitor handler = podHandlers.get(name);

                if (handler == null) {
                    // create a new handler.
                    try {
                        handler = createLogMonitor(pod);
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
            Iterator<Map.Entry<String, PodLogMonitor>> iterator = podHandlers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, PodLogMonitor> next = iterator.next();
                if (!next.getValue().markInOpenshift.get()) {
                    LOG.info("Pod not tracked by openshift anymore: {}", next.getValue().podName);
                    next.getValue().keepTrying.set(false);
                    iterator.remove();
                }
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> pods = dbGet(HashMap.class, "/activity/pods"); //NOPMD
            if (pods != null) {
                pods.keySet().removeAll(podHandlers.keySet());
                for (String o : pods.keySet()) {
                    jsondb.delete("/activity/pods/" + o);
                    LOG.info("Pod state removed from db: {}", o);
                }
            }
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") RuntimeException | IOException e) {
            LOG.error("Unexpected Error occurred.", e);
        } finally {
            Thread.currentThread().setName("Logs Controller Scheduler [idle]");
        }

    }

    protected PodLogMonitor createLogMonitor(Pod pod) throws IOException {
        return new PodLogMonitor(this, pod);
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

    public void deletePodLogState(String podName) {
        jsondb.delete("/activity/pods/" + podName);
    }

    public void setPodLogState(String podName, PodLogState state) throws IOException {
        jsondb.set("/activity/pods/" + podName, Json.writer().writeValueAsBytes(state));
    }

    public PodLogState getPodLogState(String podName) throws IOException {
        return dbGet(PodLogState.class, "/activity/pods/" + podName);
    }

    private <T> T dbGet(Class<T> type, String path) throws IOException {
        return dbGet(type, path, null);
    }

    private <T> T dbGet(Class<T> type, String path, GetOptions options) throws IOException {
        byte[] data = jsondb.getAsByteArray(path, options);
        if (data == null) {
            return null;
        }
        return Json.reader().forType(type).readValue(data);
    }

    private void processEventQueue() {
        Thread.currentThread().setName("Logs Controller [running]: processEventQueue");
        try {
            LOG.info("Batch ingestion work thread started.");
            while (!stopped.get()) {

                // Using a timeout so that if queue is empty, we break out periodically to
                // check if we are stopped.
                BatchOperation event = eventQueue.pollFirst(1, TimeUnit.SECONDS);
                if (event == null) {
                    continue;
                }

                // Start a batch..  We use a tree map so that records are sorted
                // to help avoid deadlocks.
                TreeMap<String, Object> batch = new TreeMap<>();
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
                    writeBatch(batch);

                    LOG.debug("Batch ingested {} log events", eventCounter);

                } catch (IOException e) {
                    LOG.error("Unexpected Error", e);
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Interrupted", e);
        } finally {
            Thread.currentThread().setName(IDLE_THREAD_NAME);
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

    public Duration getRetention() {
        return retention;
    }

    void execute(String podName, Runnable task) {
        executor.execute(() -> {
            Thread.currentThread().setName("Logs Controller [running], pod: " + podName);
            try {
                task.run();
            } finally {
                Thread.currentThread().setName(IDLE_THREAD_NAME);
            }
        });
    }
}
