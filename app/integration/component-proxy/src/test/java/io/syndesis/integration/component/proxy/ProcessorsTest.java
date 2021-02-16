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

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.processor.Pipeline;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ProcessorsTest {

    private final Processor processor1 = mock(Processor.class);

    private final Processor processor2 = mock(Processor.class);

    private final Processor processor3 = mock(Processor.class);

    @ParameterizedTest
    @MethodSource("mutators")
    public void shouldAddProcessors(Function<ComponentProxyComponent, Processor> getter, BiConsumer<ComponentProxyComponent, Processor> adder) {
        final ComponentProxyComponent component = createComponent();

        adder.accept(component, processor1);

        assertThat(getter.apply(component)).isEqualTo(processor1);
    }

    @ParameterizedTest
    @MethodSource("mutators")
    public void shouldCombineMultipleBeforeProducersIntoPipeline(Function<ComponentProxyComponent, Processor> getter,
        BiConsumer<ComponentProxyComponent, Processor> adder) {
        final ComponentProxyComponent component = createComponent();

        adder.accept(component, processor1);
        adder.accept(component, processor2);
        adder.accept(component, processor3);

        final Processor got = getter.apply(component);
        assertThat(got).isInstanceOf(Pipeline.class);
        final Pipeline pipeline = (Pipeline) got;
        assertThat(pipeline.getProcessors()).containsExactly(processor1, processor2, processor3);
    }

    @ParameterizedTest
    @MethodSource("mutators")
    public void shouldCombineTwoProcessorsIntoPipeline(Function<ComponentProxyComponent, Processor> getter,
        BiConsumer<ComponentProxyComponent, Processor> adder) {
        final ComponentProxyComponent component = createComponent();

        adder.accept(component, processor1);
        adder.accept(component, processor2);

        final Processor got = getter.apply(component);
        assertThat(got).isInstanceOf(Pipeline.class);
        final Pipeline pipeline = (Pipeline) got;
        assertThat(pipeline.getProcessors()).containsExactly(processor1, processor2);
    }

    public static Stream<Arguments> mutators() {
        return Stream.of(
            createCase(c -> c.getBeforeProducer(), (c, p) -> Processors.addBeforeProducer(c, p)),
            createCase(c -> c.getAfterProducer(), (c, p) -> Processors.addAfterProducer(c, p)),
            createCase(c -> c.getBeforeConsumer(), (c, p) -> Processors.addBeforeConsumer(c, p)),
            createCase(c -> c.getAfterConsumer(), (c, p) -> Processors.addAfterConsumer(c, p)));
    }

    static Arguments createCase(final Function<ComponentProxyComponent, Processor> getter,
        final BiConsumer<ComponentProxyComponent, Processor> adder) {

        return Arguments.of(getter, adder);
    }

    private static ComponentProxyComponent createComponent() {
        final ComponentProxyComponent component = new ComponentProxyComponent("test", "test");
        component.setCamelContext(mock(CamelContext.class));

        return component;
    }
}
