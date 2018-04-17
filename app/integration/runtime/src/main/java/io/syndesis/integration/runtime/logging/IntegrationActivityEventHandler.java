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
import org.apache.camel.management.event.ExchangeCreatedEvent;
import org.apache.camel.management.event.ExchangeFailedEvent;
import org.apache.camel.support.EventNotifierSupport;

import static io.syndesis.integration.runtime.util.JsonSupport.toJsonObject;

public class IntegrationActivityEventHandler extends EventNotifierSupport {

    @Override
    public boolean isEnabled(EventObject event) {
        return event instanceof ExchangeCompletedEvent
            || event instanceof ExchangeFailedEvent
            || event instanceof ExchangeCreatedEvent;
    }

    @Override
    public void notify(EventObject event) throws Exception {
        if (event instanceof ExchangeCompletedEvent) {
            onExchangeCompletedEvent((ExchangeCompletedEvent)event);
        } else if (event instanceof ExchangeFailedEvent) {
            onExchangeFailedEvent((ExchangeFailedEvent)event);
        } else if (event instanceof ExchangeCreatedEvent) {
            onExchangeCreatedEvent((ExchangeCreatedEvent)event);
        }
    }

    @SuppressWarnings("PMD.SystemPrintln")
    private void onExchangeCreatedEvent(ExchangeCreatedEvent event) {
        Exchange exchange = event.getExchange();
        if (activityId(exchange) ==null) {
            exchange.setProperty(IntegrationLoggingConstants.ACTIVITY_ID, exchange.getExchangeId());
            System.out.println(toJsonObject(
                "exchange", exchange.getExchangeId(),
                "status", "begin"));
        }
    }

    private void onExchangeCompletedEvent(ExchangeCompletedEvent event) {
        done(event.getExchange());
    }

    private void onExchangeFailedEvent(ExchangeFailedEvent event) {
        done(event.getExchange());
    }

    @SuppressWarnings("PMD.SystemPrintln")
    private static void done(Exchange exchange) {
        StepDoneTracker.done(exchange);

        final String activityId = activityId(exchange);

        if (Objects.equals(activityId, exchange.getExchangeId())) {
            System.out.println(toJsonObject(
                "exchange", activityId,
                "status", "done",
                "failed", exchange.isFailed()));
        }
    }

    private static String activityId(Exchange exchange) {
        return exchange.getProperty(IntegrationLoggingConstants.ACTIVITY_ID, String.class);
    }

}
