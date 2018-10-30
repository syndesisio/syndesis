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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.SpringCamelContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.action.StepAction.Kind;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.StringConstants;
import io.syndesis.integration.runtime.IntegrationTestSupport;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        TemplateStepHandlerTest.TestConfiguration.class
    },
    properties = {
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG"
    }
)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class TemplateStepHandlerTest extends IntegrationTestSupport {
    @Autowired
    private ApplicationContext applicationContext;

    private DirectEndpoint directEndpoint = new DirectEndpoint();

    private static ObjectMapper mapper = new ObjectMapper();

    private String createSpec(Collection<Symbol> symbols) throws JsonProcessingException {
        final ObjectNode schema = JsonNodeFactory.instance.objectNode();
        schema.put("$schema", "http://json-schema.org/schema#");
        schema.put("type", "object");
        schema.put("title", "Template JSON Schema");

        ObjectNode properties = schema.objectNode();
        for (Symbol symbol : symbols) {
            ObjectNode property = schema.objectNode();
            property.put("type", symbol.type);
            properties.set(symbol.id, property);
        }
        schema.set("properties", properties);
        String inSpec = mapper.writeValueAsString(schema);
        return inSpec;
    }

    private RouteBuilder generateRoute(String template, Collection<Symbol> symbols) throws JsonProcessingException {
        String inSpec = createSpec(symbols);

        Symbol symbol = new Symbol("message", "string");
        String outSpec = createSpec(Collections.singletonList(symbol));

        RouteBuilder routes = newIntegrationRouteBuilder(
            new Step.Builder()
                .id(directEndpoint.id())
                .stepKind(StepKind.endpoint)
                .action(new ConnectorAction.Builder()
                    .descriptor(new ConnectorDescriptor.Builder()
                         .componentScheme(directEndpoint.schema)
                         .putConfiguredProperty("name", directEndpoint.name)
                         .build())
                    .build())
                .build(),
            new Step.Builder()
                .id("templating")
                .stepKind(StepKind.template)
                .action(new StepAction.Builder()
                        .descriptor(new StepDescriptor.Builder()
                                .kind(Kind.STEP)
                                .inputDataShape(new DataShape.Builder()
                                            .kind(DataShapeKinds.JSON_SCHEMA)
                                            .specification(inSpec)
                                            .build())
                                .outputDataShape(new DataShape.Builder()
                                            .kind(DataShapeKinds.JSON_SCHEMA)
                                            .specification(outSpec)
                                            .build())
                                .build())
                        .build())
                .putConfiguredProperty("template", template)
                .addDependency(Dependency.maven("org.apache.camel:camel-mustache:2.21.0.fuse-720007"))
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

            return routes;
        }

    private void sendData(CamelContext context, List<String> messages) {
        ProducerTemplate template = context.createProducerTemplate();

        for (String message : messages) {
            Exchange exchange = context.getEndpoint(directEndpoint.toString()).createExchange();
            Message msg = exchange.getIn();
            msg.setBody(message);
            template.send(directEndpoint.toString(), exchange);
        }
    }

    private String toJson(String message) throws Exception {
        ObjectNode node = mapper.createObjectNode();
        node.put("message", message);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }

    @Test
    public void testTemplateStepBasicWithPrefix() throws Exception {
        CamelContext context = new SpringCamelContext(applicationContext);

        List<String> testMessages = new ArrayList<>();
        testMessages.add(
            data(
                dataPair("time", "10:00"),
                dataPair("name", "Bob"),
                dataPair("text", "Greetings, How are you?")
            )
        );
        testMessages.add(
            data(
                 dataPair("time", "10:15"),
                 dataPair("name", "Susan"),
                 dataPair("text", "Hello, Pleased to meet you!")
            )
        );

        Symbol[] symbols = {
            new Symbol("time", "string"),
            new Symbol("name", "string"),
            new Symbol("text", "string")
        };

        String template = EMPTY_STRING +
                "At {{" + symbols[0].id + "}}, {{" + symbols[1].id + "}}" + NEW_LINE +
                "stated submitted the following message:" + NEW_LINE +
                "{{" + symbols[2].id + "}}";


        List<String> expectedMessages = new ArrayList<>();
        String expectedMessage = template.replaceAll("\\{\\{|\\}\\}", EMPTY_STRING)
                                                        .replace(symbols[0].id, "10:00")
                                                        .replace(symbols[1].id, "Bob")
                                                        .replace(symbols[2].id, "Greetings, How are you?");
        expectedMessages.add(toJson(expectedMessage));

        expectedMessage = template.replaceAll("\\{\\{|\\}\\}", EMPTY_STRING)
                                                         .replace(symbols[0].id, "10:15")
                                                         .replace(symbols[1].id, "Susan")
                                                         .replace(symbols[2].id, "Hello, Pleased to meet you!");
        expectedMessages.add(toJson(expectedMessage));

        try {
            final RouteBuilder routes = generateRoute(template, Arrays.asList(symbols));

            // Set up the camel context
            context.addRoutes(routes);
            dumpRoutes(context);

            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            result.setExpectedMessageCount(2);
            result.expectedBodiesReceived(expectedMessages);

            sendData(context, testMessages);

            result.assertIsSatisfied();

        } finally {
            context.stop();
        }
     }

    @Test
    public void testTemplateStepBasicWithoutPrefix() throws Exception {
        CamelContext context = new SpringCamelContext(applicationContext);

        List<String> testMessages = new ArrayList<>();
        testMessages.add(
            data(
                dataPair("time", "10:00"),
                dataPair("name", "Bob"),
                dataPair("text", "Greetings, How are you?")
            )
        );
        testMessages.add(
            data(
                 dataPair("time", "10:15"),
                 dataPair("name", "Susan"),
                 dataPair("text", "Hello, Pleased to meet you!")
            )
        );

        Symbol[] symbols = {
            new Symbol("body.time", "string"),
            new Symbol("body.name", "string"),
            new Symbol("body.text", "string")
        };

        String template = EMPTY_STRING +
                "At {{" + symbols[0].id + "}}, {{" + symbols[1].id + "}}" + NEW_LINE +
                "stated submitted the following message:" + NEW_LINE +
                "{{" + symbols[2].id + "}}";


        List<String> expectedMessages = new ArrayList<>();
        String expectedMessage = template.replaceAll("\\{\\{|\\}\\}", EMPTY_STRING)
                                                        .replace(symbols[0].id, "10:00")
                                                        .replace(symbols[1].id, "Bob")
                                                        .replace(symbols[2].id, "Greetings, How are you?");
        expectedMessages.add(toJson(expectedMessage));

        expectedMessage = template.replaceAll("\\{\\{|\\}\\}", EMPTY_STRING)
                                                         .replace(symbols[0].id, "10:15")
                                                         .replace(symbols[1].id, "Susan")
                                                         .replace(symbols[2].id, "Hello, Pleased to meet you!");
        expectedMessages.add(toJson(expectedMessage));

        try {
            final RouteBuilder routes = generateRoute(template, Arrays.asList(symbols));

            // Set up the camel context
            context.addRoutes(routes);
            dumpRoutes(context);

            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            result.setExpectedMessageCount(2);
            result.expectedBodiesReceived(expectedMessages);

            sendData(context, testMessages);

            result.assertIsSatisfied();

        } finally {
            context.stop();
        }
     }

    @Test
    public void testTemplateStepNoSpacesInSymbolAllowed() throws Exception {
        CamelContext context = new SpringCamelContext(applicationContext);

        Symbol[] symbols = {
            new Symbol("the time", "string"),
            new Symbol("the name", "string"),
            new Symbol("text content", "string")
        };

        String template = EMPTY_STRING +
            "At {{" + symbols[0].id + "}}, {{" + symbols[1].id + "}}" + NEW_LINE +
            "stated submitted the following message:" + NEW_LINE +
            "{{" + symbols[2].id + "}}";

        try {
            final RouteBuilder routes = generateRoute(template, Arrays.asList(symbols));

            // Set up the camel context
            context.addRoutes(routes);
            fail("Should not allow spaces in symbols");
        } catch (Exception ex) {
            assertTrue(ex.getMessage().contains("invalid"));
        } finally {
            context.stop();
        }
     }

    @Test
    public void testTemplateStepDanglingSection() throws Exception {
        CamelContext context = new SpringCamelContext(applicationContext);

        Symbol[] symbols = {
            new Symbol("id", "string"),
            new Symbol("course", "array"),
            new Symbol("text", "string")
        };

        String template = EMPTY_STRING +
            "{{id}} passed the following courses:" + NEW_LINE +
            "{{#course}}" + NEW_LINE +
            "\t* {{name}}" + NEW_LINE +
            "{{text}}";

        try {
            final RouteBuilder routes = generateRoute(template, Arrays.asList(symbols));

            // Set up the camel context
            context.addRoutes(routes);
            fail("Should not allow dangling section");
        } catch (Exception ex) {
            assertTrue(ex.getMessage().contains("invalid"));
        } finally {
            context.stop();
        }
     }

    @Test
    public void testTemplateStepIterate() throws Exception {
        CamelContext context = new SpringCamelContext(applicationContext);

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
            new Symbol("course", "array"),
            new Symbol("body.text", "string")
        };

        String mustacheTemplate = EMPTY_STRING +
                "{{" + symbols[0].id + "}} passed the following courses:" + NEW_LINE +
                "{{#" + symbols[1].id + "}}" + NEW_LINE +
                "\t* {{name}}" + NEW_LINE +
                "{{/" + symbols[1].id + "}}" +
                "{{" + symbols[2].id + "}}";

        try {
            final RouteBuilder routes = generateRoute(mustacheTemplate, Arrays.asList(symbols));

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

    // ***************************
    //
    // ***************************

    public static final class User {
        private String name;

        public User(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            User user = (User) o;
            return Objects.equals(name, user.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    // ***************************
    //
    // ***************************

    @Configuration
    public static class TestConfiguration {
    }

    private static class DirectEndpoint implements StringConstants {
        private String schema = "direct";
        private String name = "start";

        public String id() {
            return schema + HYPHEN + name;
        }

        @Override
        public String toString() {
            return schema + COLON + name;
        }
    }

    private static class Symbol {
        public String id;
        public String type;

        public Symbol(String id, String type) {
            this.id = id;
            this.type = type;
        }
    }
}
