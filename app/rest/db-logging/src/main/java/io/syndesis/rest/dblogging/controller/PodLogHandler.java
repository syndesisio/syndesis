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
import io.syndesis.core.Json;
import io.syndesis.jsondb.JsonDBException;
import io.syndesis.openshift.OpenShiftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static io.syndesis.jsondb.impl.JsonRecordSupport.validateKey;
import static java.lang.String.format;

/**
 *
 */
class PodLogHandler implements Consumer<InputStream> {

    private static final Logger LOG = LoggerFactory.getLogger(LogsController.class);

    private final LogsController logsController;
    protected final AtomicBoolean markInOpenshift = new AtomicBoolean(true);
    protected final AtomicBoolean keepTrying = new AtomicBoolean(true);
    protected final String podName;
    protected final String integrationId;
    protected final String deploymentId;
    protected PodLogState state;

    public PodLogHandler(LogsController logsController, Pod pod) {
        this.logsController = logsController;
        this.podName = pod.getMetadata().getName();
        Map<String, String> annotations = pod.getMetadata().getAnnotations();
        this.integrationId = annotations.get(OpenShiftService.INTEGRATION_ID_ANNOTATION);
        Map<String, String> labels = pod.getMetadata().getLabels();
        this.deploymentId = labels.get(OpenShiftService.DEPLOYMENT_ID_ANNOTATION);
    }

    public void start() throws IOException {

        // We are just getting started, means we are in the openshift pod
        // list and we need to track pod log state.
        state = logsController.getPodLogState(podName);
        if (state == null) {
            state = new PodLogState();
            logsController.setPodLogState(podName, state);
        }
        LOG.info("Recovered state: {}", state);
        logsController.executor.execute(this::run);
    }

    public void run() {

        if( logsController.stopped.get() || !keepTrying.get() || !logsController.isPodRunning(podName) ) {
            // Seems we don't need to keep trying, lets bail.
            return;
        }

        LOG.info("Getting controller for pod: {}", podName);
        try {
            logsController.watchLog(this.podName, this, this.state.time);
        } catch (IOException e) {
            LOG.info("Failure occurred while processing controller for pod: {}", podName, e);
            logsController.schedule(this::run, 5, TimeUnit.SECONDS);
        }
    }

    @Override
    public void accept(InputStream is) {
        if( is !=null ) {
            try {
                try {
                    processLogStream(is);
                } finally {
                    is.close();
                }
            } catch (InterruptedException | IOException e) {
                LOG.info("Failure occurred while processing controller for pod: {}", podName, e);
                logsController.schedule(this::run, 5, TimeUnit.SECONDS);
            }
        } else {
            logsController.schedule(this::run, 5, TimeUnit.SECONDS);
        }
    }

    private void processLogStream(final InputStream is) throws IOException, InterruptedException {
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        int c;

        while (!logsController.stopped.get()) {
            c = is.read();
            if (c < 0) {
                break;
            }

            line.write(c);
            if (c == '\n') {
                processLine(line.toByteArray());
                line.reset();
            }

            // drop really long lines to avoid blowing up our memory.
            if (line.size() > 1024 * 4) {
                line.reset();
            }
        }

        if( !logsController.stopped.get() ) {
            if ( logsController.isPodRunning(podName) ) {
                // odd, why did our stream end??  try to resume processing..
                LOG.info("End of Log stream for running pod: {}", podName);
                logsController.schedule(this::run, 5, TimeUnit.SECONDS);
            } else {
                // Seems like the normal case where stream ends because pod is stopped.
                LOG.info("End of Log stream for terminated pod: {}", podName);
            }
        }
    }

    private void processLine(byte[] line) throws IOException {
        // Could it be a data of json structured output?

        // Log lines look like:
        // 2018-01-12T21:22:02.068338027Z { ..... }
        if (
            line.length < 32 // not long enough
            || line[30] != ' ' // expecting space
            || line[31] != '{' // expecting the json data starting here.
            ) {
            return;
        }

        String time = new String(line, 0, 30, StandardCharsets.US_ASCII);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> json = Json.mapper().readValue(line, 31, line.length-31, HashMap.class);

            // are the required fields set?
            String exchange = validate((String) json.get("exchange"));
            String id = validate((String) json.get("id"));
            String step = validate((String) json.get("step"));
            String status = validate((String) json.get("status"));

            if (id != null && exchange != null && step != null) {

                // Looks like a step level logging event.
                String stepPath = format("/exchanges/%s/%s/steps/%s/%s", integrationId, exchange, step, id);
                json.put("logts", time);

                // Drop bits encoded into the key, to reduce size of entry...
                json.remove("exchange");
                json.remove("id");
                json.remove("step");

                logsController.eventQueue.put(batch -> {
                    // Do as little as possible in here, single thread processes the event queue.
                    batch.put(stepPath, json);
                    trackState(time, batch);
                });

            } else if (exchange != null && status!=null) {

                // Looks like a exchange level logging event.

                String transactionPath = format("/exchanges/%s/%s", integrationId, exchange);
                json.put("pod", podName);
                json.put("ver", deploymentId);
                if( "begin".equals(status) ) {
                    json.put("logts", time);
                }

                // Drop bits encoded into the key, to reduce size of entry...
                json.remove("exchange");

                // Protect this field.. use by step level logging events.
                json.remove("steps");

                logsController.eventQueue.put(batch -> {
                    // Do as little as possible in here, single thread processes the event queue.

                    for (Map.Entry<String, Object> entry : json.entrySet()) {
                        batch.put(transactionPath + "/" +entry.getKey(), entry.getValue());
                    }
                    trackState(time, batch);
                });

            }

        } catch (JsonDBException | ClassCastException | IOException ignore) {
            /// log record not in the expected format.
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
    }

    private void trackState(String time, Map<String, Object> batch) {
        state.time = time;
        String podStatPath = "/pods/" + podName;
        batch.put(podStatPath, state);
        batch.put("/integrations/" + integrationId, Boolean.TRUE);
    }

    private String validate(String value) {
        if( value == null ) {
            return null;
        }
        return validateKey(value);
    }


}
