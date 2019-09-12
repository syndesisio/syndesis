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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.kubernetes.api.model.Pod;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.server.endpoint.v1.handler.activity.Activity;
import io.syndesis.server.endpoint.v1.handler.activity.ActivityStep;
import io.syndesis.server.jsondb.JsonDBException;
import io.syndesis.server.openshift.OpenShiftService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.syndesis.server.jsondb.impl.JsonRecordSupport.validateKey;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

@SuppressWarnings("PMD.GodClass")
class PodLogMonitor implements Consumer<InputStream> {

    private static final Logger LOG = LoggerFactory.getLogger(ActivityTrackingController.class);

    // matches log lines like: 2018-06-06T21:54:36.30603486Z {"exchange":"i-LEM51uGKc6IuIjvR95Vz","status":"begin"}
    private static final Pattern LOG_LINE_REGEX = Pattern.compile("^(\\d\\d\\d\\d\\-\\d\\d\\-\\d\\dT\\d\\d:\\d\\d:\\d\\d\\.\\d+Z) (\\{.*\\})\\s*");

    private AtomicBoolean running = new AtomicBoolean(false);
    private final ActivityTrackingController logsController;
    protected final AtomicBoolean markInOpenshift = new AtomicBoolean(true);
    protected final AtomicBoolean keepTrying = new AtomicBoolean(true);
    protected final String podName;
    protected final String integrationId;
    protected final String deploymentVersion;
    protected PodLogState state;
    protected HashMap<String, InflightData> inflightActivities = new HashMap<>();

    PodLogMonitor(ActivityTrackingController logsController, Pod pod) {
        this.logsController = logsController;
        this.podName = pod.getMetadata().getName();
        if (this.podName == null) {
            throw new IllegalStateException("Could not determine the pod name");
        }

        Map<String, String> labels = pod.getMetadata().getLabels();
        this.integrationId = labels.get(OpenShiftService.INTEGRATION_ID_LABEL);
        if (this.integrationId == null) {
            throw new IllegalStateException("Could not determine the integration id that is being run on the pod: " + this.podName);
        }

        this.deploymentVersion = labels.get(OpenShiftService.DEPLOYMENT_VERSION_LABEL);
        if (this.deploymentVersion == null) {
            throw new IllegalStateException("Could not determine the deployment version that is being run on the pod: " + this.podName);
        }
    }

    public void start() throws IOException {
        if (running.get()) {
            return;
        }

        // We are just getting started, means we are in the openshift pod
        // list and we need to track pod log state.
        state = logsController.getPodLogState(podName);
        if (state == null) {
            state = new PodLogState();
            logsController.setPodLogState(podName, state);
        }
        LOG.info("Recovered state: {}", state);
        logsController.execute(podName, this::run);
    }

    public void run() {

        if (logsController.stopped.get() || !keepTrying.get() || !logsController.isPodRunning(podName)) {
            // Seems we don't need to keep trying, lets bail.
            return;
        }

        if (running.compareAndSet(false, true)) {
            LOG.info("Getting controller for pod: {}", podName);
            try {
                logsController.watchLog(this.podName, this, this.state.time);
            } catch (IOException e) {
                LOG.info("Failure occurred while processing controller for pod: {}", podName, e);
            }
        }
    }

    @Override
    public void accept(InputStream is) {
        try {
            if (is == null) {
                return;
            }

            processLogStream(is);
        } catch (SocketTimeoutException | EOFException e) {
            LOG.info("Streaming ended for pod {} due to: {}", podName, message(e));
            LOG.debug("Streaming ended for pod {}", podName, e);
        } catch (InterruptedException | IOException e) {
            LOG.info("Failure occurred while processing controller for pod: {}", podName, e);
        } finally {
            running.set(false);
            IOUtils.closeQuietly(is);
        }
    }

    void processLogStream(final InputStream is) throws IOException, InterruptedException {
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        int c;

        while (!logsController.stopped.get()) {
            c = is.read();
            if (c < 0) {
                break;
            }

            line.write(c);
            // cut really long lines to avoid blowing up our memory.
            if (line.size() > 1024 * 10) {
                // as the string is prematurely cut, add the closing terminator to the json value
                line.write("\"}".getBytes(UTF_8));
                c = '\n';
            }

            if (c == '\n') {
                processLine(new String(line.toByteArray(), UTF_8));
                line.reset();
            }
        }

        if (!logsController.stopped.get()) {
            if (logsController.isPodRunning(podName)) {
                // odd, why did our stream end??  try to resume processing..
                LOG.info("End of Log stream for running pod: {}", podName);
            } else {
                // Seems like the normal case where stream ends because pod is stopped.
                LOG.info("End of Log stream for terminated pod: {}", podName);
            }
        }
    }

    private static class InflightData {
        Activity activity = new Activity();
        ArrayList<ActivityStep> doneSteps = new ArrayList<>();
        Map<String, ActivityStep> activeSteps = new LinkedHashMap<>();
        Map<String, Object> metadata = new HashMap<>();

        public ActivityStep getStep(String step, String id) throws IOException {
            ActivityStep rc = activeSteps.get(step);
            if (rc == null) {
                rc = new ActivityStep();
                rc.setId(step);
                rc.setAt(KeyGenerator.getKeyTimeMillis(id));
                activeSteps.put(step, rc);
            }
            return rc;
        }
    }

    InflightData getInflightData(String exchangeId, String logts) throws IOException {
        InflightData data = inflightActivities.get(exchangeId);
        if (data == null) {
            data = new InflightData();
            data.activity.setPod(podName);
            data.activity.setVer(deploymentVersion);
            data.activity.setId(exchangeId);
            data.activity.setAt(KeyGenerator.getKeyTimeMillis(exchangeId));
            data.activity.setLogts(logts);
            inflightActivities.put(exchangeId, data);
        }
        return data;
    }

    void processLine(String line) throws IOException {
        // Does it look like a data of json structured output?
        Matcher matcher = LOG_LINE_REGEX.matcher(line);
        if (!matcher.matches()) {
            return;
        }

        String time = matcher.group(1);
        String data = matcher.group(2);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> json = Json.reader().forType(HashMap.class).readValue(data); //NOPMD

            // are the required fields set?
            String exchange = validate((String) json.remove("exchange"));
            if (exchange == null) {
                // This log entry is not valid json format
                return;
            }
            long keyTimeMillis = KeyGenerator.getKeyTimeMillis(exchange);
            long until = now() - logsController.getRetentionTime().toMillis();
            if (keyTimeMillis < until) {
                // This log entry is too old.. don't process it..
                return;
            }
            InflightData inflightData = getInflightData(exchange, time);

            String id = validate((String) json.remove("id"));
            String step = (String) json.remove("step");
            if (step == null) {
                // Looks like an exchange level logging event.
                processLogLineExchange(json, inflightData, exchange, time);
            } else {
                // Looks like a step level logging event.
                processLogLineStep(json, inflightData, step, id);
            }

        } catch (JsonDBException | ClassCastException | IOException ignored) {
            /// log record not in the expected format.
        } catch (InterruptedException e) {
            final InterruptedIOException rethrow = new InterruptedIOException(e.getMessage());
            rethrow.initCause(e);
            throw rethrow;
        }
    }

    private void processLogLineStep(Map<String, Object> json, InflightData inflightData, String step, String id) throws IOException {
        ActivityStep as = inflightData.getStep(step, id);
        String message = (String) json.remove("message");
        if (message != null) {
            if (as.getMessages() == null) {
                as.setMessages(new ArrayList<>());
            }
            as.getMessages().add(message);
        }

        String failure = (String) json.remove("failure");
        if (failure != null) {
            as.setFailure(failure);
        }

        Number duration = (Number) json.remove("duration");
        if (duration != null) {
            as.setDuration(duration.longValue());
        }

        if (!json.isEmpty()) {
            if (as.getEvents() == null) {
                as.setEvents(new ArrayList<>());
            }
            as.getEvents().add(toJsonNode(json));
        }

        if (duration != null) {
            inflightData.activeSteps.remove(step);
            if (inflightData.doneSteps.size() == 50) {
                ActivityStep truncated = new ActivityStep();
                truncated.addMessage("Max activity tracking steps reached.  No further steps will be recorded.");
                inflightData.doneSteps.add(truncated);
            }
            if (inflightData.doneSteps.size() < 50) {
                inflightData.doneSteps.add(as);
            }
        }
    }

    private void processLogLineExchange(Map<String, Object> json, InflightData inflightData, String exchange, String time) throws IOException, InterruptedException {
        Boolean failed = (Boolean) json.remove("failed");
        if (failed != null) {
            inflightData.activity.setFailed(failed);
        }
        String status = (String) json.remove("status");
        inflightData.metadata.putAll(json);
        if (status != null) {
            inflightData.activity.setStatus(status);
            if ("done".equals(status)) {
                inflightData.activity.setSteps(inflightData.doneSteps);
                if (!inflightData.metadata.isEmpty()) {
                    inflightData.activity.setMetadata(toJsonNode(inflightData.metadata));
                }

                String activityAsString = Json.writer().writeValueAsString(inflightData.activity);
                String transactionPath = format("/exchanges/%s/%s", integrationId, exchange);
                inflightActivities.remove(exchange);

                logsController.eventQueue.put(batch -> {
                    // Do as little as possible in here, single thread processes the event queue.
                    batch.put(transactionPath, activityAsString);
                    trackState(time, batch);
                });

            }
        }
    }

    long now() {
        return System.currentTimeMillis();
    }

    private static JsonNode toJsonNode(Map<String, Object> json) throws IOException {
        return Json.reader().readTree(Json.writer().writeValueAsString(json));
    }

    private void trackState(String time, Map<String, Object> batch) {
        state.time = time;
        String podStatPath = "/pods/" + podName;
        batch.put(podStatPath, state);
        batch.put("/integrations/" + integrationId, Boolean.TRUE);
    }

    private static String validate(String value) {
        if (value == null) {
            return null;
        }
        return validateKey(value);
    }

    private static String message(final Throwable e) {
        final StringBuilder buffy = new StringBuilder()//
            .append(e.getClass().getName())//
            .append(": ")//
            .append(e.getMessage());

        if (e.getCause() != null && e.getCause() != e) {
            buffy.append(", caused by: ");
            buffy.append(message(e.getCause()));
        }

        return buffy.toString();
    }

}
