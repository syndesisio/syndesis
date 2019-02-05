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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationTestSupport;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class SplitStepHandlerJsonTest extends IntegrationTestSupport {

    @Test
    public void testTokenizeBodyStep() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = new IntegrationRouteBuilder(
                "classpath:/syndesis/integration/SplitStepHandlerJsonTest.json",
                Collections.emptyList()
            );

            // Set up the camel context
            context.addRoutes(routes);
            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);
            final String[] expected = { "a","b","c" };
            final List<String> body = Arrays.asList(expected);

            result.expectedMessageCount(3);
            result.expectedBodiesReceived((Object[])expected);

            template.sendBody("direct:expression", body);

            result.assertIsSatisfied();
        } finally {
            context.stop();
        }
    }
}
