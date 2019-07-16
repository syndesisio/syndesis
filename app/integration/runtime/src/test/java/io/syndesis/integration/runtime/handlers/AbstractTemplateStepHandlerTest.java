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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;
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
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.model.integration.step.template.TemplateStepLanguage;
import io.syndesis.common.model.integration.step.template.TemplateStepLanguage.SymbolSyntax;
import io.syndesis.common.util.StringConstants;
import io.syndesis.integration.runtime.IntegrationTestSupport;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public abstract class AbstractTemplateStepHandlerTest extends IntegrationTestSupport {

    private DirectEndpoint directEndpoint = new DirectEndpoint();

    private static ObjectMapper mapper = new ObjectMapper();

    protected CamelContext context;

    @Before
    public void setup() throws Exception {
        context = new DefaultCamelContext();
    }

    @After
    public void tearDown() throws Exception {
        context.stop();
        context = null;
    }

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

    protected abstract TemplateStepLanguage getLanguage();

    protected SymbolSyntax getSymbolSyntax() {
        return getLanguage().getDefaultSymbolSyntax();
    }

    protected Dependency getDependency() {
        return Dependency.maven(getLanguage().mavenDependency());
    }

    protected IntegrationWithRouteBuilder generateRoute(String template, Collection<Symbol> symbols) throws JsonProcessingException {
        String inSpec = createSpec(symbols);

        Symbol symbol = new Symbol("message", "string", getSymbolSyntax());
        String outSpec = createSpec(Collections.singletonList(symbol));

        Integration integration = newIntegration(
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
                .putConfiguredProperty("language", getLanguage().toString())
                .addDependency(getDependency())
                .build(),
            new Step.Builder()
                .id("mock-endpoint")
                .stepKind(StepKind.endpoint)
                .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                                .componentScheme("mock")
                                .putConfiguredProperty("name", "result")
                                .build())
                        .build())
                .build()
            );

            RouteBuilder routes = newIntegrationRouteBuilder(integration);
            return new IntegrationWithRouteBuilder(integration, routes);
        }

    protected void sendData(CamelContext context, List<String> messages) {
        ProducerTemplate template = context.createProducerTemplate();

        for (String message : messages) {
            Exchange exchange = context.getEndpoint(directEndpoint.toString()).createExchange();
            Message msg = exchange.getIn();
            msg.setBody(message);
            template.send(directEndpoint.toString(), exchange);
        }
    }

    protected String toJson(String message) throws Exception {
        ObjectNode node = mapper.createObjectNode();
        node.put("message", message);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }

    protected void checkDynamicDependency(Integration integration) {
        List<Dependency> dependencies = new ArrayList<>();

        integration.getFlows().stream()
            .flatMap(flow -> flow.getSteps().stream())
            .forEach(step -> {
                dependencies.addAll(step.getDependencies());
            });

        assertTrue(dependencies.contains(getDependency()));
    }

    protected void testTemplate(Symbol[] symbols, String template, List<String> testMessages,
                              List<String> expectedMessages) throws JsonProcessingException, Exception, InterruptedException {
        IntegrationWithRouteBuilder irb = generateRoute(template, Arrays.asList(symbols));
        Integration integration = irb.integration();
        checkDynamicDependency(integration);

        RouteBuilder routes = irb.routeBuilder();

        // Set up the camel context
        context.addRoutes(routes);
        dumpRoutes(context);

        context.start();

        // Dump routes as XML for troubleshooting
        dumpRoutes(context);

        MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
        result.setExpectedMessageCount(1);
        result.expectedBodiesReceived(expectedMessages);

        sendData(context, testMessages);

        result.assertIsSatisfied();
    }

    protected void testTemplateStepBasic(Symbol[] symbols) throws Exception {
        List<String> testMessages = new ArrayList<>();
        testMessages.add(
            data(
                dataPair(symbols[0].id, "10:00"),
                dataPair(symbols[1].id, "Bob"),
                dataPair(symbols[2].id, "Greetings, How are you?")
            )
        );
        testMessages.add(
            data(
                 dataPair(symbols[0].id, "10:15"),
                 dataPair(symbols[1].id, "Susan"),
                 dataPair(symbols[2].id, "Hello, Pleased to meet you!")
            )
        );

        String template = EMPTY_STRING +
                    "At " + symbols[0] + ", " + symbols[1] + NEW_LINE +
                    "stated submitted the following message:" + NEW_LINE +
                    symbols[2];

        List<String> expectedMessages = new ArrayList<>();
        String expectedMessage = template.replace(symbols[0].toString(), "10:00")
                                                            .replace(symbols[1].toString(), "Bob")
                                                            .replace(symbols[2].toString(), "Greetings, How are you?");
        expectedMessages.add(toJson(expectedMessage));

        expectedMessage = template.replace(symbols[0].toString(), "10:15")
                                                         .replace(symbols[1].toString(), "Susan")
                                                         .replace(symbols[2].toString(), "Hello, Pleased to meet you!");
        expectedMessages.add(toJson(expectedMessage));

        testTemplate(symbols, template, testMessages, expectedMessages);
     }

    protected void testTemplateStepNoSpacesInSymbolAllowed(Symbol[] symbols) throws Exception {
        String template = EMPTY_STRING +
            "At " + symbols[0] + ", " + symbols[1] + NEW_LINE +
            "stated submitted the following message:" + NEW_LINE +
            symbols[2];

        assertThatThrownBy(() -> {
            IntegrationWithRouteBuilder irb = generateRoute(template, Arrays.asList(symbols));
            final RouteBuilder routes = irb.routeBuilder();

            // Set up the camel context
            context.addRoutes(routes);
        })
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not valid syntactically");
     }

    @Test
    public void testTemplateStepBrackets() throws Exception {
        Symbol[] symbols = {
            new Symbol("time", "string"),
            new Symbol("name", "string"),
            new Symbol("text", "string")
        };

        String template = EMPTY_STRING +
            "At " + OPEN_BRACKET + symbols[0] + CLOSE_BRACKET + ", " +
            OPEN_BRACKET + symbols[1] + CLOSE_BRACKET + NEW_LINE +
            "submitted the following message:" + NEW_LINE +
            symbols[2];

        List<String> testMessages = new ArrayList<>();
        testMessages.add(
            data(
                dataPair(symbols[0].id, "10:00"),
                dataPair(symbols[1].id, "Bob"),
                dataPair(symbols[2].id, "Greetings, How are you?")
            )
        );

        List<String> expectedMessages = new ArrayList<>();
        String expectedMessage = template.replace(symbols[0].toString(), "10:00")
                                                            .replace(symbols[1].toString(), "Bob")
                                                            .replace(symbols[2].toString(), "Greetings, How are you?");
        expectedMessages.add(toJson(expectedMessage));

        testTemplate(symbols, template, testMessages, expectedMessages);
    }

    @Test
    public void testTemplateStepContiguousSymbols() throws Exception {
        Symbol[] symbols = {
            new Symbol("time", "string"),
            new Symbol("name", "string"),
            new Symbol("text", "string")
        };

        String template = EMPTY_STRING +
            "Time:" + symbols[0] + "/ Name:" + symbols[1] + "/ Text:" + symbols[2];

        List<String> testMessages = new ArrayList<>();
        testMessages.add(
            data(
                dataPair(symbols[0].id, "10:00"),
                dataPair(symbols[1].id, "Bob"),
                dataPair(symbols[2].id, "Greetings, How are you?")
            )
        );

        List<String> expectedMessages = new ArrayList<>();
        String expectedMessage = template.replace(symbols[0].toString(), "10:00")
                                                            .replace(symbols[1].toString(), "Bob")
                                                            .replace(symbols[2].toString(), "Greetings, How are you?");
        expectedMessages.add(toJson(expectedMessage));

        testTemplate(symbols, template, testMessages, expectedMessages);
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

    protected class Symbol {
        public String id;
        public String type;
        private String openSymbol;
        private String closeSymbol;

        public Symbol(String id, String type, String openSymbol, String closeSymbol) {
            this.id = id;
            this.type = type;
            this.openSymbol = openSymbol;
            this.closeSymbol = closeSymbol;
        }

        public Symbol(String id, String type, SymbolSyntax syntax) {
            this(id, type, syntax.open(), syntax.close());
        }

        public Symbol(String id, String type) {
            this(id, type, getSymbolSyntax());
        }

        @Override
        public String toString() {
            return openSymbol + this.id + closeSymbol;
        }
    }

    protected static class IntegrationWithRouteBuilder {
        private final Integration integration;
        private final RouteBuilder routeBuilder;

        public IntegrationWithRouteBuilder(Integration integration, RouteBuilder routeBuilder) {
            this.integration = integration;
            this.routeBuilder = routeBuilder;
        }

        public Integration integration() {
            return this.integration;
        }

        public RouteBuilder routeBuilder() {
            return this.routeBuilder;
        }
    }
}
