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

import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.model.PipelineDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.DelegateAsyncProcessor;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.StringHelper;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.syndesis.integration.runtime.logging.IntegrationLoggingConstants;

public class TracingInterceptStrategy implements InterceptStrategy {
    private final Tracer tracer;

    public TracingInterceptStrategy(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Processor wrapProcessorInInterceptors(CamelContext context, ProcessorDefinition<?> definition, Processor target, Processor nextTarget) throws Exception {
        if (definition instanceof PipelineDefinition) {
            final String id = definition.getId();
            if (ObjectHelper.isEmpty(id)) {
                return target;
            }

            final String stepId = StringHelper.after(id, "step:");
            if (ObjectHelper.isEmpty(stepId)) {
                return target;
            }

            return new EventProcessor(target, stepId);
        }
        return target;
    }

    private class EventProcessor extends DelegateAsyncProcessor {
        private final String stepId;

        public EventProcessor(Processor processor, String stepId) {
            super(processor);
            this.stepId = stepId;
        }

        @Override
        public boolean process(final Exchange exchange, final AsyncCallback callback) {
            final Message in = exchange.getIn();
            final Span activitySpan = exchange.getProperty(IntegrationLoggingConstants.ACTIVITY_SPAN, Span.class);
            Scope activityScope = tracer.scopeManager().activate(activitySpan, false);
            try {
                try (Scope scope = tracer.buildSpan(stepId).withTag("kind", "step").startActive(false)) {
                    final Span span = scope.span();
                    in.setHeader(IntegrationLoggingConstants.STEP_SPAN, span);
                    return super.process(exchange, doneSync -> {
                        span.finish();
                        callback.done(doneSync);
                    });
                }
            } finally {
                activityScope.close();
            }
        }
    }
}
