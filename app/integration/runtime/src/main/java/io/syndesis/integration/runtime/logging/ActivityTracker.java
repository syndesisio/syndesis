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

import io.syndesis.integration.runtime.util.JsonSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.util.ObjectHelper;

@FunctionalInterface
public interface ActivityTracker {
    void track(Object... items);

    /**
     * Default implementation that log activity on STOUT.
     */
    class SysOut implements ActivityTracker {
        @SuppressWarnings("PMD.SystemPrintln")
        @Override
        public void track(Object... fields) {
            System.out.println(JsonSupport.toJsonObject(fields));
        }
    }

    static String getActivityId(Exchange exchange) {
        return exchange.getProperty(IntegrationLoggingConstants.ACTIVITY_ID, String.class);
    }

    static void initializeTracking(Exchange exchange) {
        if (ObjectHelper.isEmpty(getActivityId(exchange))) {
            Message in = exchange.getIn();

            if (in != null && in.getHeader(IntegrationLoggingConstants.ACTIVITY_ID) != null) {
                exchange.setProperty(IntegrationLoggingConstants.ACTIVITY_ID, in.removeHeader(IntegrationLoggingConstants.ACTIVITY_ID));
            } else {
                exchange.setProperty(IntegrationLoggingConstants.ACTIVITY_ID, exchange.getExchangeId());
            }
        }
    }

    default void startTracking(Exchange exchange) {
        initializeTracking(exchange);
        track("exchange", getActivityId(exchange), "status", "begin");
    }

    default void finishTracking(Exchange exchange) {
        final String activityId = getActivityId(exchange);
        if (Objects.nonNull(activityId)) {
            track("exchange", activityId, "status", "done", "failed", exchange.isFailed());
        }
    }
}
