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
package io.syndesis.integration.runtime.logging;

import java.util.Objects;

import io.syndesis.integration.runtime.util.DefaultRoutePolicy;
import org.apache.camel.Exchange;
import org.apache.camel.Route;

public final class ActivityTrackingPolicy extends DefaultRoutePolicy {
    private final ActivityTracker tracker;

    public ActivityTrackingPolicy(ActivityTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void onExchangeBegin(Route route, Exchange exchange) {
        if (tracker == null) {
            return;
        }

        exchange.setProperty(IntegrationLoggingConstants.ACTIVITY_ID, exchange.getExchangeId());

        tracker.track(
            "exchange", exchange.getExchangeId(),
            "status", "begin"
        );
    }

    @Override
    public void onExchangeDone(Route route, Exchange exchange) {
        if (tracker == null) {
            return;
        }

        final String activityId = exchange.getProperty(IntegrationLoggingConstants.ACTIVITY_ID, String.class);
        final String exchangeId = exchange.getExchangeId();

        if (Objects.nonNull(activityId) && Objects.equals(activityId, exchangeId)) {
            tracker.track(
                "exchange", activityId,
                "status", "done",
                "failed", exchange.isFailed()
            );
        }
    }
}
