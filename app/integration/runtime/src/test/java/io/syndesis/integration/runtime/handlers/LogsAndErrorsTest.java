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

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.extension.api.Step;
import io.syndesis.integration.runtime.IntegrationTestSupport;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Handy class to test logging of log messages and errors
 */
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class LogsAndErrorsTest extends IntegrationTestSupport {

    @Test
    public void testRoute() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(
                new io.syndesis.common.model.integration.Step.Builder()
                    .id("s1")
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "expression")
                            .build())
                        .build())
                    .build(),
                new io.syndesis.common.model.integration.Step.Builder()
                    .id("s2")
                    .stepKind(StepKind.extension)
                    .action(new StepAction.Builder()
                        .descriptor(new StepDescriptor.Builder()
                            .kind(StepAction.Kind.STEP)
                            .entrypoint(LogExtension.class.getName())
                            .build())
                        .build())
                    .build(),
                new io.syndesis.common.model.integration.Step.Builder()
                    .id("s3")
                    .stepKind(StepKind.extension)
                    .action(new StepAction.Builder()
                        .descriptor(new StepDescriptor.Builder()
                            .kind(StepAction.Kind.STEP)
                            .entrypoint(ErrorExtension.class.getName())
                            .build())
                        .build())
                    .build(),
                new io.syndesis.common.model.integration.Step.Builder()
                    .id("s4")
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "expression")
                            .build())
                        .build())
                    .build()
            );

            // Set up the camel context
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);

            result.expectedBodiesReceived("1","3");

            Exchange e1 = template.request("direct:expression", e -> e.getIn().setBody("1"));
            assertThat(e1.isFailed()).isFalse();

            Exchange e2 = template.request("direct:expression", e -> e.getIn().setBody("2"));
            assertThat(e2.isFailed()).isTrue();

            Exchange e3 = template.request("direct:expression", e -> e.getIn().setBody("3"));
            assertThat(e3.isFailed()).isFalse();

            result.assertIsSatisfied();

        } finally {
            context.stop();
        }
    }

    public static class LogExtension implements Step {
        @Override
        public Optional<ProcessorDefinition<?>> configure(CamelContext context, ProcessorDefinition<?> definition, Map<String, Object> parameters) {
            return Optional.of(definition.log(LoggingLevel.INFO, definition.getId(), definition.getId(), "Got ${body}"));
        }
    }

    public static class ErrorExtension implements Step {
        private static int count;

        @Override
        public Optional<ProcessorDefinition<?>> configure(CamelContext context, ProcessorDefinition<?> definition, Map<String, Object> parameters) {
            ProcessorDefinition<?> processor = definition.process(exchange -> {
                count++;
                if( count == 2 ) {
                    throw new IOException("Bean Error");
                }
            });
            return Optional.of(processor);
        }
    }
}
