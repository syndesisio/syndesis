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
package io.syndesis.server.logging.jaeger.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.syndesis.server.endpoint.v1.handler.activity.Activity;
import io.syndesis.server.endpoint.v1.handler.activity.ActivityStep;
import io.syndesis.server.endpoint.v1.handler.activity.ActivityTrackingService;

/**
 * Implements a dblogging service for the Activity JAXRS service.
 */
@Component
@ConditionalOnProperty(value = "endpoints.dblogging.enabled", havingValue = "true", matchIfMissing = true)
public class JaegerActivityTrackingService implements ActivityTrackingService {
    private static final Logger LOG = LoggerFactory.getLogger(JaegerActivityTrackingService.class);

    private final JaegerQueryAPI jaegerQueryApi;

    public JaegerActivityTrackingService(@Value("jaeger.query.api.url") String jaegerQueryAPIURL) {
        this(new JaegerQueryAPI(jaegerQueryAPIURL));
    }

    public JaegerActivityTrackingService(JaegerQueryAPI jaegerQueryApi) {
        this.jaegerQueryApi = jaegerQueryApi;
    }

    // NOPMD
    @Override
    public List<Activity> getActivities(String integrationId, String from, Integer requestedLimit) throws IOException {

        int lookbackDays = 30;
        int limit = 10;
        if (requestedLimit != null) {
            limit = requestedLimit;
        }
        if (limit > 1000) {
            limit = 1000; // max out to 1000 per request.
        }

        // http://localhost:16686/api/traces?end=1548280423588000&limit=20&lookback=1h&maxDuration&minDuration&service=io.syndesis.integration.runtime.tracing.ActivityTracingWithSplitTest&start=1548276823588000
        ArrayList<JaegerQueryAPI.Trace> traces = jaegerQueryApi.tracesForService(integrationId, lookbackDays, limit);


        LOG.debug("traces: {}", traces);

        ArrayList<Activity> rc = new ArrayList<>();
        for (JaegerQueryAPI.Trace trace : traces) {
            Activity activity = null;
            ArrayList<ActivityStep> steps = new ArrayList<>();

            if (trace.data != null && trace.data.size() >= 1) {


                for (JaegerQueryAPI.Span span : trace.data) {
                    switch (span.findTag("kind", String.class)) {
                        case "activity": {
                            activity = new Activity();
                            activity.setId(trace.traceID);
                            // activity.setVer();
                            JaegerQueryAPI.JaegerProcess process = trace.processes.get(span.processID);
                            String version = process.findTag("version", String.class);
                            activity.setVer(version);
                            String hostname = process.findTag("hostname", String.class);
                            activity.setPod(hostname);
                            activity.setStatus("done");
                            activity.setAt(span.startTime);
                            Boolean failed = span.findTag("failed", Boolean.class);
                            if (failed != null) {
                                activity.setFailed(failed);
                            }
                        }
                        break;
                        case "step": {
                            ActivityStep step = new ActivityStep();
                            step.setId(span.operationName);
                            step.setAt(span.startTime);
                            step.setDuration(span.duration);

                            List<String> messages = span.findLogs("event");
                            step.setMessages(messages);

                            // TODO:
                            // step.setFailure();

                            steps.add(step);
                        }
                        break;
                        default:
                            LOG.debug("Unknown span: " + span);
                            break;
                    }

                }
            }

            if (activity != null) {
                activity.setSteps(steps);
                rc.add(activity);
            }

        }

        LOG.debug("rc: {}", rc);
        return rc;
    }


    public static void main(String[] args) throws IOException {
        JaegerActivityTrackingService service = new JaegerActivityTrackingService("http://localhost:16686/api");
        List<Activity> activities = service.getActivities("io.syndesis.integration.runtime.tracing.ActivityTracingWithSplitTest", null, 10);
        LOG.debug("activities: {}", activities);

    }
}
