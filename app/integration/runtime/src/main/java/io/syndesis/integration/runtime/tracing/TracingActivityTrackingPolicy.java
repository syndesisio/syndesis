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
package io.syndesis.integration.runtime.tracing;

import java.util.Objects;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.syndesis.integration.runtime.logging.ActivityTracker;
import io.syndesis.integration.runtime.logging.IntegrationLoggingConstants;
import io.syndesis.integration.runtime.util.DefaultRoutePolicy;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.util.ObjectHelper;

public final class TracingActivityTrackingPolicy extends DefaultRoutePolicy {
    private final Tracer tracer;
    private final String flowId;

    public TracingActivityTrackingPolicy(Tracer tracer, String flowId) {
        this.tracer = tracer;
        this.flowId = flowId;
    }

    @Override
    public void onExchangeBegin(Route route, Exchange exchange) {
        if (tracer == null) {
            return;
        }
        String activityId = ActivityTracker.getActivityId(exchange);
        if (ObjectHelper.isEmpty(activityId)) {
            ActivityTracker.initializeTracking(exchange);
            activityId = ActivityTracker.getActivityId(exchange);
        }

        Span span = tracer
            .buildSpan(flowId)
            .withTag("kind", "activity")
            .withTag("exchange", activityId)
            .start();
        exchange.setProperty(IntegrationLoggingConstants.ACTIVITY_SPAN, span);
    }

    @Override
    public void onExchangeDone(Route route, Exchange exchange) {
        if (tracer == null) {
            return;
        }
        final String activityId = ActivityTracker.getActivityId(exchange);

        if (Objects.nonNull(activityId)) {
            final Span span = exchange.getProperty(IntegrationLoggingConstants.ACTIVITY_SPAN, Span.class);
            if (span != null) {
                span.setTag("failed", exchange.isFailed());
                span.finish();
            }
        }
    }
}
