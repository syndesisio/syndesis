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
package io.syndesis.integration.runtime.template;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import io.syndesis.common.model.integration.step.template.TemplateStepConstants;
import io.syndesis.common.model.integration.step.template.TemplateStepLanguage;
import io.syndesis.common.model.integration.step.template.TemplateStepLanguage.SymbolSyntax;
import io.syndesis.integration.runtime.handlers.AbstractTemplateStepHandlerTest;

public class MustacheTemplateStepHandlerTest extends AbstractTemplateStepHandlerTest implements TemplateStepConstants {

    @Override
    protected TemplateStepLanguage getLanguage() {
        return TemplateStepLanguage.MUSTACHE;
    }

    @Test
    public void testTemplateStepBasicWithPrefix() throws Exception {
        Symbol[] symbols = {
            new Symbol("body.time", "string"),
            new Symbol("body.name", "string"),
            new Symbol("body.text", "string")
        };

        testTemplateStepBasic(symbols);
     }

    @Test
    public void testTemplateStepBasicWithoutPrefix() throws Exception {
        Symbol[] symbols = {
            new Symbol("time", "string"),
            new Symbol("name", "string"),
            new Symbol("text", "string")
        };

        testTemplateStepBasic(symbols);
     }

    @Test
    public void testTemplateStepDanglingSection() throws Exception {
        Symbol[] symbols = {
            new Symbol("id", "string"),
            new Symbol("course", "array", "{{#", getSymbolSyntax().close()),
            new Symbol("text", "string")
        };

        String template = EMPTY_STRING +
            symbols[0] + " passed the following courses:" + NEW_LINE +
            symbols[1] + NEW_LINE +
            "\t* {{name}}" + NEW_LINE +
            symbols[2];

        assertThatThrownBy(() -> {
            IntegrationWithRouteBuilder irb = generateRoute(template, Arrays.asList(symbols));
            RouteBuilder routes = irb.routeBuilder();

            // Set up the camel context
            context.addRoutes(routes);
        })
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("invalid");
     }

    @Test
    public void testTemplateStepIterate() throws Exception {
        List<String> testMessages = new ArrayList<>();
        testMessages.add(
            data(
                dataPair("name", "Bob"),
                dataPair("course",
                    dataPair("name", "Mathematics"),
                        dataPair("name", "English"),
                        dataPair("name", "Chemistry")
               ),
               dataPair("text", "Will be going to the University of Southampton.")
          )
        );

        testMessages.add(
            data(
                dataPair("name", "Susan"),
                dataPair("course",
                    dataPair("name", "Physics"),
                    dataPair("name", "German"),
                    dataPair("name", "Art")
                ),
                dataPair("text", "Will be joining Evans, Richards and Dean.")
            )
        );

        Symbol[] symbols = {
            new Symbol("name", "string"),
            new Symbol("course", "array", "{{#", getSymbolSyntax().close()),
            new Symbol("body.text", "string")
        };

        String mustacheTemplate = EMPTY_STRING +
                symbols[0] + " passed the following courses:" + NEW_LINE +
                symbols[1] + NEW_LINE +
                "\t* {{name}}" + NEW_LINE +
                "{{/" + symbols[1].id + getSymbolSyntax().close() +
                symbols[2];

        try {
            IntegrationWithRouteBuilder irb = generateRoute(mustacheTemplate, Arrays.asList(symbols));
            final RouteBuilder routes = irb.routeBuilder();

            // Set up the camel context
            context.addRoutes(routes);
            dumpRoutes(context);

            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            result.setExpectedCount(2);

            List<String> expectedMessages = new ArrayList<>();
            expectedMessages.add(toJson(
                                 "Bob passed the following courses:" + NEW_LINE +
                                 "\t* Mathematics" + NEW_LINE +
                                 "\t* English" + NEW_LINE +
                                 "\t* Chemistry" + NEW_LINE +
                                 "Will be going to the University of Southampton."));
            expectedMessages.add(toJson(
                                 "Susan passed the following courses:" + NEW_LINE +
                                 "\t* Physics" + NEW_LINE +
                                 "\t* German" + NEW_LINE +
                                 "\t* Art" + NEW_LINE +
                                 "Will be joining Evans, Richards and Dean."));
            result.expectedBodiesReceived(expectedMessages);

            sendData(context, testMessages);

            result.assertIsSatisfied();

        } finally {
            context.stop();
        }
    }

    @Test
    public void testTemplateStepSpacesInSymbolAllowed() throws Exception {
        Symbol[] symbols = {
            new Symbol("the time", "string"),
            new Symbol("the name", "string"),
            new Symbol("text content", "string")
        };

        testTemplateStepBasic(symbols);
     }

    @Test
    public void testTemplateStepSpacesInSymbolAllowedWithPrefix() throws Exception {
        Symbol[] symbols = {
            new Symbol("body.the time", "string"),
            new Symbol("body.the name", "string"),
            new Symbol("body.text content", "string")
        };

        testTemplateStepBasic(symbols);
     }

    @Test
    public void testInvalidTemplate() throws Exception {
        //
        // Uses velocity symbols to create invalid template
        //
        SymbolSyntax velocitySyntax = TemplateStepLanguage.VELOCITY.getDefaultSymbolSyntax();
        Symbol[] symbols = {
            new Symbol("time", "string", velocitySyntax),
            new Symbol("name", "string", velocitySyntax),
            new Symbol("text", "string", velocitySyntax)
        };

        assertThatThrownBy(() -> {
            testTemplateStepBasic(symbols);
        })
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("invalid");
    }
}
