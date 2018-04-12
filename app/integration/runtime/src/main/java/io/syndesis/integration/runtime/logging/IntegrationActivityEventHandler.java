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

import java.util.EventObject;
import java.util.Objects;

import org.apache.camel.Exchange;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.management.event.ExchangeFailedEvent;
import org.apache.camel.support.EventNotifierSupport;

import static io.syndesis.integration.runtime.util.JsonSupport.toJsonObject;

public class IntegrationActivityEventHandler extends EventNotifierSupport {
    @Override
    public void notify(EventObject event) throws Exception {
        if (event instanceof ExchangeCompletedEvent) {
            onExchangeCompletedEvent((ExchangeCompletedEvent)event);
        } else if (event instanceof ExchangeFailedEvent) {
            onExchangeFailedEvent((ExchangeFailedEvent)event);
        }
    }

    @Override
    public boolean isEnabled(EventObject event) {
        return event instanceof ExchangeCompletedEvent || event instanceof ExchangeFailedEvent;
    }

    @SuppressWarnings("PMD.SystemPrintln")
    private void onExchangeCompletedEvent(ExchangeCompletedEvent event) {
        StepDoneTracker.done(event.getExchange());

        final Exchange exchange = event.getExchange();
        final String activityId = exchange.getProperty(IntegrationLoggingConstants.ACTIVITY_ID, String.class);

        if (Objects.equals(activityId, exchange.getExchangeId())) {
            System.out.println(toJsonObject(
                "exchange", activityId,
                "status", "done",
                "failed", exchange.isFailed()));
        }
    }

    @SuppressWarnings("PMD.SystemPrintln")
    private void onExchangeFailedEvent(ExchangeFailedEvent event) {
        StepDoneTracker.done(event.getExchange());

        final Exchange exchange = event.getExchange();
        final String activityId = exchange.getProperty(IntegrationLoggingConstants.ACTIVITY_ID, String.class);

        if (Objects.equals(activityId, exchange.getExchangeId())) {
            System.out.println(toJsonObject(
                "exchange", activityId,
                "status", "done",
                "failed", exchange.isFailed()));
        }
    }
}
