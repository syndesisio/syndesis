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
package io.syndesis.integration.component.proxy;

import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.processor.Pipeline;
import org.apache.camel.processor.PollEnricher;

public final class Processors {

    private Processors() {
        // utility class
    }

    public static void addAfterConsumer(final ComponentProxyComponent component, final Processor processor) {
        final Processor existing = component.getAfterConsumer();

        if (existing == null) {
            component.setAfterConsumer(processor);
        } else {
            final CamelContext camelContext = component.getCamelContext();

            final Processor replacement = replacementPipelineFrom(camelContext, existing, processor);

            component.setAfterConsumer(replacement);
        }
    }

    public static void addAfterProducer(final ComponentProxyComponent component, final Processor processor) {
        final Processor existing = component.getAfterProducer();

        if (existing == null) {
            component.setAfterProducer(processor);
        } else {
            final CamelContext camelContext = component.getCamelContext();

            final Processor replacement = replacementPipelineFrom(camelContext, existing, processor);

            component.setAfterProducer(replacement);
        }
    }

    public static void addBeforeConsumer(final ComponentProxyComponent component, final Processor processor) {
        final Processor existing = component.getBeforeConsumer();

        if (existing == null) {
            component.setBeforeConsumer(processor);
        } else {
            final CamelContext camelContext = component.getCamelContext();

            final Processor replacement = replacementPipelineFrom(camelContext, existing, processor);

            component.setBeforeConsumer(replacement);
        }
    }

    public static void addBeforeProducer(final ComponentProxyComponent component, final Processor processor) {
        final Processor existing = component.getBeforeProducer();

        if (existing == null) {
            component.setBeforeProducer(processor);
        } else {
            final CamelContext camelContext = component.getCamelContext();

            final Processor replacement = replacementPipelineFrom(camelContext, existing, processor);

            component.setBeforeProducer(replacement);
        }
    }

    static Processor replacementPipelineFrom(final CamelContext camelContext, final Processor existing, final Processor additional) {
        final Processor replacement;
        if (existing instanceof Pipeline) {
            final List<Processor> processors = ((Pipeline) existing).next();
            processors.add(additional);

            replacement = Pipeline.newInstance(camelContext, processors);
        } else {
            replacement = Pipeline.newInstance(camelContext, existing, additional);
        }
        return replacement;
    }

    public static Processor pollEnricher(final String endpointUri, ComponentProxyComponent proxyComponent) {
        final PollEnricher pollEnricher = new PollEnricher(endpointUri, -1);
        pollEnricher.setDefaultAggregationStrategy();

        return Pipeline.newInstance(proxyComponent.getCamelContext(), proxyComponent.getBeforeConsumer(), pollEnricher, proxyComponent.getAfterConsumer());
    }
}
