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

import java.util.Collection;

import org.apache.camel.Processor;
import org.apache.camel.processor.Pipeline;

public final class Processors {

    private Processors() {
        // utility class
    }

    public static void addAfterConsumer(final ComponentProxyComponent component, final Processor processor) {
        final Processor existing = component.getAfterConsumer();

        if (existing == null) {
            component.setAfterConsumer(processor);
        } else if (existing instanceof Pipeline) {
            final Pipeline pipeline = (Pipeline) existing;
            final Collection<Processor> processors = pipeline.getProcessors();
            processors.add(processor);
        } else {
            final Processor pipeline = Pipeline.newInstance(component.getCamelContext(), existing, processor);
            component.setAfterConsumer(pipeline);
        }
    }

    public static void addAfterProducer(final ComponentProxyComponent component, final Processor processor) {
        final Processor existing = component.getAfterProducer();

        if (existing == null) {
            component.setAfterProducer(processor);
        } else if (existing instanceof Pipeline) {
            final Pipeline pipeline = (Pipeline) existing;
            final Collection<Processor> processors = pipeline.getProcessors();
            processors.add(processor);
        } else {
            final Processor pipeline = Pipeline.newInstance(component.getCamelContext(), existing, processor);
            component.setAfterProducer(pipeline);
        }
    }

    public static void addBeforeConsumer(final ComponentProxyComponent component, final Processor processor) {
        final Processor existing = component.getBeforeConsumer();

        if (existing == null) {
            component.setBeforeConsumer(processor);
        } else if (existing instanceof Pipeline) {
            final Pipeline pipeline = (Pipeline) existing;
            final Collection<Processor> processors = pipeline.getProcessors();
            processors.add(processor);
        } else {
            final Processor pipeline = Pipeline.newInstance(component.getCamelContext(), existing, processor);
            component.setBeforeConsumer(pipeline);
        }
    }

    public static void addBeforeProducer(final ComponentProxyComponent component, final Processor processor) {
        final Processor existing = component.getBeforeProducer();

        if (existing == null) {
            component.setBeforeProducer(processor);
        } else if (existing instanceof Pipeline) {
            final Pipeline pipeline = (Pipeline) existing;
            final Collection<Processor> processors = pipeline.getProcessors();
            processors.add(processor);
        } else {
            final Processor pipeline = Pipeline.newInstance(component.getCamelContext(), existing, processor);
            component.setBeforeProducer(pipeline);
        }
    }

}
