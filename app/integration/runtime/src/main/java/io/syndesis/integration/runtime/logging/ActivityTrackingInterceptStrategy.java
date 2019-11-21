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
import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.NamedNode;
import org.apache.camel.Processor;
import org.apache.camel.model.PipelineDefinition;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.camel.support.processor.DefaultExchangeFormatter;
import org.apache.camel.support.processor.DelegateAsyncProcessor;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.StringHelper;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ActivityTrackingInterceptStrategy implements InterceptStrategy {
    private static final DefaultExchangeFormatter FORMATTER = new DefaultExchangeFormatter();

    private final ActivityTracker tracker;

    public ActivityTrackingInterceptStrategy(ActivityTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public Processor wrapProcessorInInterceptors(CamelContext context, NamedNode definition, Processor target, Processor nextTarget) throws Exception {
        if (this.tracker == null) {
            return target;
        }

        if (definition instanceof PipelineDefinition) {
            final String id = definition.getId();
            if (ObjectHelper.isEmpty(id)) {
                return target;
            }

            final String stepId = StringHelper.after(id, "step:");
            if (ObjectHelper.isEmpty(stepId)) {
                return target;
            }

            return new EventProcessor(target);
        }

        return target;
    }

    private class EventProcessor extends DelegateAsyncProcessor {
        EventProcessor(Processor processor) {
            super(processor);
        }

        @Override
        public boolean process(final Exchange exchange, final AsyncCallback callback) {
            final String trackerId = KeyGenerator.createKey();
            final long createdAt = System.nanoTime();
            final Message in = exchange.getIn();
            in.setHeader(IntegrationLoggingConstants.STEP_TRACKER_ID, trackerId);

            return super.process(exchange, doneSync -> {
                final String activityId =  exchange.getProperty(IntegrationLoggingConstants.ACTIVITY_ID, String.class);
                final String stepId = in.getHeader(IntegrationLoggingConstants.STEP_ID, String.class);

                if (activityId != null) {
                    tracker.track(
                        "exchange", activityId,
                        "step", stepId != null ? stepId : "none",
                        "id", trackerId,
                        "duration", System.nanoTime() - createdAt,
                        "failure", failure(exchange)
                    );
                }

                callback.done(doneSync);
            });
        }
    }

    // ******************
    // Helpers
    // ******************


    private static String failure(Exchange exchange) {
        if (exchange.isFailed()) {
            if (exchange.getException() != null) {
                return getStackTrace(exchange.getException());
            }
            return FORMATTER.format(exchange);
        }
        return null;
    }

    private static String getStackTrace(Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
