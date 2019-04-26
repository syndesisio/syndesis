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

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.spi.LogListener;
import org.apache.camel.util.CamelLogger;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.syndesis.integration.runtime.logging.IntegrationLoggingConstants;


public class TracingLogListener implements LogListener {

    private final Tracer tracer;

    public TracingLogListener(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public String onLog(Exchange exchange, CamelLogger camelLogger, String message) {
        final Message in = exchange.getIn();
        Span span = in.getHeader(IntegrationLoggingConstants.STEP_SPAN, Span.class);
        if (span == null) {
            span = tracer.activeSpan();
        }
        if (span != null) {
            span.log(message);
        }
        return message;
    }
}
