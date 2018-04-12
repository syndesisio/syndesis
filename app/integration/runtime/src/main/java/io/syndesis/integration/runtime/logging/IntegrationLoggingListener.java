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

import io.syndesis.common.util.KeyGenerator;
import org.apache.camel.Exchange;
import org.apache.camel.spi.LogListener;
import org.apache.camel.util.CamelLogger;
import org.slf4j.Marker;

import static io.syndesis.integration.runtime.util.JsonSupport.toJsonObject;

public class IntegrationLoggingListener implements LogListener {
    @SuppressWarnings("PMD.SystemPrintln")
    @Override
    public String onLog(Exchange exchange, CamelLogger camelLogger, String message) {
        final String activityId = exchange.getProperty(IntegrationLoggingConstants.ACTIVITY_ID, String.class);

        if (activityId != null) {
            final Marker marker = camelLogger.getMarker();
            final String step = marker != null ? marker.getName() : "null";

            System.out.println(toJsonObject(
                "exchange", activityId,
                "step", step,
                "id", KeyGenerator.createKey(),
                "message", message)
            );
        }

        return message;
    }
}
