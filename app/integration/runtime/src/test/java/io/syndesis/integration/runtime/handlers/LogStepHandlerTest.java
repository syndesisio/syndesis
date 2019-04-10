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
package io.syndesis.integration.runtime.handlers;

import io.syndesis.common.model.integration.Step;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;

import org.apache.camel.LoggingLevel;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class LogStepHandlerTest {

    final LogStepHandler handler = new LogStepHandler();

    final IntegrationRouteBuilder NOT_USED = null;

    final ProcessorDefinition<?> route = spy(new RouteDefinition());

    @Test
    public void shouldAddLogProcessorWithCustomMessage() {
        final Step step = new Step.Builder().putConfiguredProperty("customText", "Log me baby one more time").build();

        assertThat(handler.handle(step, route, NOT_USED, "1", "2")).contains(route);

        verify(route).log(LoggingLevel.INFO, "Log me baby one more time");
    }

    @Test
    public void shouldAddLogProcessorWithCustomMessageAndStepId() {
        final Step step = new Step.Builder().id("step-id")
            .putConfiguredProperty("customText", "Log me baby one more time").build();

        assertThat(handler.handle(step, route, NOT_USED, "1", "2")).contains(route);

        verify(route).log(LoggingLevel.INFO, (String) null, "step-id", "Log me baby one more time");
    }

    @Test
    public void shouldGenerateMessages() {
        final Step step = new Step.Builder().putConfiguredProperty("customText", "Log me baby one more time").build();

        assertThat(LogStepHandler.createMessage(step)).isEqualTo("Log me baby one more time");

        final Step withContext = new Step.Builder().createFrom(step)
            .putConfiguredProperty("contextLoggingEnabled", "true").build();
        assertThat(LogStepHandler.createMessage(withContext))
            .isEqualTo("Message Context: [${in.headers}] Log me baby one more time");

        final Step withBody = new Step.Builder().createFrom(step).putConfiguredProperty("bodyLoggingEnabled", "true")
            .build();
        assertThat(LogStepHandler.createMessage(withBody)).isEqualTo("Body: [${bean:bodyLogger}] Log me baby one more time");

        final Step withContextAndBody = new Step.Builder().createFrom(step)
            .putConfiguredProperty("contextLoggingEnabled", "true").putConfiguredProperty("bodyLoggingEnabled", "true")
            .build();
        assertThat(LogStepHandler.createMessage(withContextAndBody))
            .isEqualTo("Message Context: [${in.headers}] Body: [${bean:bodyLogger}] Log me baby one more time");
    }

    @Test
    public void shouldNotAddLogProcessorWhenNotingIsSpecifiedToLog() {
        final Step step = new Step.Builder().build();

        assertThat(handler.handle(step, route, NOT_USED, "1", "2")).isEmpty();

        verifyZeroInteractions(route);
    }
}
