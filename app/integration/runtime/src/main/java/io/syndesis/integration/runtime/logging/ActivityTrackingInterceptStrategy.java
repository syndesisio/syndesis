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

import io.syndesis.common.util.Exceptions;
import io.syndesis.common.util.KeyGenerator;
import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.NamedNode;
import org.apache.camel.Processor;
import org.apache.camel.model.NoOutputDefinition;
import org.apache.camel.model.OutputDefinition;
import org.apache.camel.model.OutputExpressionNode;
import org.apache.camel.model.PipelineDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.support.processor.DefaultExchangeFormatter;
import org.apache.camel.support.processor.DelegateAsyncProcessor;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.StringHelper;

import java.util.List;

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

        if (shouldTrack(definition)) {
            final String id = definition.getId();
            if (ObjectHelper.isEmpty(id)) {
                return target;
            }

            final String stepId = StringHelper.after(id, "step:");
            if (ObjectHelper.isEmpty(stepId)) {
                return target;
            }

            if (shouldTrackDoneEvent(definition)) {
                return new TrackDoneEventProcessor(target, stepId);
            }

            return new TrackStartEventProcessor(target, stepId);
        }

        return target;
    }

    /**
     * Processor invokes activity tracker to track activity start event only. This is used for steps that hold other nested steps
     * such as filter or choice.
     */
    private class TrackStartEventProcessor extends DelegateAsyncProcessor {
        private final String stepId;

        TrackStartEventProcessor(Processor processor, String stepId) {
            super(processor);
            this.stepId = stepId;
        }

        @Override
        public boolean process(Exchange exchange, final AsyncCallback callback) {
            final String activityId =  ActivityTracker.getActivityId(exchange);
            if (activityId != null) {
                tracker.track(
                    "exchange", activityId,
                    "step", stepId,
                    "id", KeyGenerator.createKey(),
                    "duration", 0L,
                    "failure", null
                );
            }

            return super.process(exchange, callback::done);
        }
    }

    /**
     * Processor invokes activity tracker to track activity done event in async callback. Activity event is provided with duration time.
     */
    private class TrackDoneEventProcessor extends DelegateAsyncProcessor {
        private final String stepId;

        TrackDoneEventProcessor(Processor processor, String stepId) {
            super(processor);
            this.stepId = stepId;
        }

        @Override
        public boolean process(final Exchange exchange, final AsyncCallback callback) {
            final String trackerId = KeyGenerator.createKey();
            final long createdAt = System.nanoTime();
            final Message in = exchange.getIn();
            in.setHeader(IntegrationLoggingConstants.STEP_TRACKER_ID, trackerId);

            return super.process(exchange, doneSync -> {
                final String activityId =  ActivityTracker.getActivityId(exchange);

                if (activityId != null) {
                    tracker.track(
                        "exchange", activityId,
                        "step", stepId,
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

    /**
     * Activity tracking is only active for pipelines.
     * @param definition
     * @return
     */
    private static boolean shouldTrack(NamedNode definition) {
        return definition instanceof PipelineDefinition &&
                ObjectHelper.isNotEmpty(((PipelineDefinition) definition).getOutputs());
    }

    /**
     * Activities that do hold nested activities (such as {@link org.apache.camel.model.FilterDefinition}, {@link org.apache.camel.model.ChoiceDefinition})
     * should not track the done event because this leads to reversed order of log events.
     *
     * Only log done events with duration measurement for no output definitions like {@link org.apache.camel.model.ToDefinition}.
     * @param definition
     * @return
     */
    private static boolean shouldTrackDoneEvent(NamedNode definition) {
        if (!(definition instanceof OutputDefinition)) {
          return false;
        }

        final OutputDefinition<?> outputDefinition = (OutputDefinition) definition;
        final List<ProcessorDefinition<?>> outputs = outputDefinition.getOutputs();

        if (ObjectHelper.isEmpty(outputs)) {
            return false;
        }

        int stepIndexInPipeline = 0;
        if (outputs.size() > 1) {
            // 1st output in the pipeline should be the set header processor for the step id
            // 2nd output in the pipeline should be the actual step processor
            stepIndexInPipeline = 1;
        }

        return outputs.get(stepIndexInPipeline) instanceof NoOutputDefinition ||
               outputs.get(stepIndexInPipeline) instanceof OutputExpressionNode;
    }

    private static String failure(Exchange exchange) {
        if (exchange.isFailed()) {
            if (exchange.getException() != null) {
                return Exceptions.toString(exchange.getException());
            }
            return FORMATTER.format(exchange);
        }
        return null;
    }
}
