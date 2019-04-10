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

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.integration.runtime.IntegrationTestSupport;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;

@SuppressWarnings("PMD.ExcessiveImports")
public class HeadersStepHandlerTest extends IntegrationTestSupport {

    @Test
    public void testSetHeadersStepHandler() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(
                new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .id(KeyGenerator.createKey())
                        .descriptor(new ConnectorDescriptor.Builder()
                            .connectorId("new")
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "start")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.headers)
                    .putConfiguredProperty("action", "set")
                    .putConfiguredProperty("MyHeader1", "1")
                    .putConfiguredProperty("MyHeader2", "2")
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "result")
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
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);

            result.expectedMessageCount(1);
            result.expectedHeaderReceived("MyHeader1", "1");
            result.expectedHeaderReceived("MyHeader2", "2");

            template.sendBody("direct:start", "");

            result.assertIsSatisfied();
        } finally {
            context.stop();
        }
    }

    @Test
    public void testRemoveHeadersStepHandler() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(
                new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .id(KeyGenerator.createKey())
                        .descriptor(new ConnectorDescriptor.Builder()
                            .connectorId("new")
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "start")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.headers)
                    .putConfiguredProperty("action", "set")
                    .putConfiguredProperty("MyHeader1", "1")
                    .putConfiguredProperty("MyHeader2", "2")
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.headers)
                    .putConfiguredProperty("action", "remove")
                    .putConfiguredProperty("MyHeader1", "")
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("mock")
                            .putConfiguredProperty("name", "result")
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
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);

            result.expectedMessageCount(1);
            result.expectedMessagesMatches(e -> !e.getIn().getHeaders().containsKey("Myheader1"));
            result.expectedMessagesMatches(e -> e.getIn().getHeaders().containsKey("Myheader2"));

            template.sendBody("direct:start", "");

            result.assertIsSatisfied();
        } finally {
            context.stop();
        }
    }
}
