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
package io.syndesis.integration.runtime.template.velocity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.Arrays;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;

/**
 * Tests for velocity formal template step handler, ie. using the formal
 * syntax for velocity symbols, eg. ${xyz}
 *
 */
public class VelocityFormalSyntaxTest extends AbstractVelocityTemplateStepHandlerTest {

    @Test
    public void testTemplateStepNoSpacesInSymbolAllowed() throws Exception {
        Symbol[] symbols = {
            new Symbol("the time", "string"),
            new Symbol("the name", "string"),
            new Symbol("text content", "string")
        };

        testTemplateStepNoSpacesInSymbolAllowed(symbols);
     }

    @Test
    public void testTemplateStepNoClosingTag() throws Exception {
        Symbol[] symbols = {
            new Symbol("name", "string")
        };

        String template = EMPTY_STRING +
            "Hello ${name, how are you?";

        assertThatThrownBy(() -> {
            IntegrationWithRouteBuilder irb = generateRoute(template, Arrays.asList(symbols));
            RouteBuilder routes = irb.routeBuilder();

            // Set up the camel context
            context.addRoutes(routes);
        })
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("due to an incomplete symbol");
    }

    @Test
    public void testTemplateStepNoClosingEndTag() throws Exception {
        Symbol[] symbols = {
            new Symbol("name", "string")
        };

        String template = EMPTY_STRING +
            "Hello this is your name: ${name";

        assertThatThrownBy(() -> {
            IntegrationWithRouteBuilder irb = generateRoute(template, Arrays.asList(symbols));
            final RouteBuilder routes = irb.routeBuilder();

            // Set up the camel context
            context.addRoutes(routes);
        })
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("invalid due to an incomplete symbol");
    }
}
