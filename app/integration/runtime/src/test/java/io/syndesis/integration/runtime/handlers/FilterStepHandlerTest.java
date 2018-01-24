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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.syndesis.integration.runtime.handlers.support.StepHandlerTestSupport;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.action.ConnectorDescriptor;
import io.syndesis.model.filter.ExpressionFilterStep;
import io.syndesis.model.filter.RuleFilterStep;
import io.syndesis.model.integration.SimpleStep;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.SpringCamelContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        FilterStepHandlerTest.TestConfiguration.class
    },
    properties = {
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG"
    }
)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class FilterStepHandlerTest extends StepHandlerTestSupport {
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testExpressionFilterStep() throws Exception {
        final CamelContext context = new SpringCamelContext(applicationContext);

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(
                new SimpleStep.Builder()
                    .stepKind("endpoint")
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "start")
                            .build())
                        .build())
                    .build(),
                new ExpressionFilterStep.Builder()
                    .putConfiguredProperty("filter", "${body.name} == 'James'")
                    .build(),
                new SimpleStep.Builder()
                    .stepKind("endpoint")
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

            final List<String> matchingMessages = Collections.singletonList("{ \"name\": \"James\" }");
            final List<String> notMatchingMessages = Collections.singletonList("{ \"name\": \"Jimmi\" }");
            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);

            List<String> allMessages = new ArrayList<>();
            allMessages.addAll(matchingMessages);
            allMessages.addAll(notMatchingMessages);

            result.expectedBodiesReceived(matchingMessages);

            for (Object body : allMessages) {
                template.sendBody("direct:start", body);
            }

            result.assertIsSatisfied();
        } finally {
            context.stop();
        }
    }

    @Test
    public void testRuleFilterStepWithJsonSimplePath() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(
                new SimpleStep.Builder()
                    .stepKind("endpoint")
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "start")
                            .build())
                        .build())
                    .build(),
                new RuleFilterStep.Builder()
                    .putConfiguredProperty("type", "rule")
                    .putConfiguredProperty("predicate", "OR")
                    .putConfiguredProperty("rules", "[{\"path\":\"name\",\"op\":\"==\",\"value\":\"James\"}, {\"path\":\"name\",\"op\":\"==\",\"value\":\"Roland\"}]")
                    .build(),
                new SimpleStep.Builder()
                    .stepKind("endpoint")
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

            final List<String> matchingMessages = Arrays.asList("{ \"name\": \"James\" }", "{ \"name\": \"Roland\" }");
            final List<String> notMatchingMessages = Collections.singletonList("{ \"name\": \"Jimmi\" }");
            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);

            List<String> allMessages = new ArrayList<>();
            allMessages.addAll(matchingMessages);
            allMessages.addAll(notMatchingMessages);

            result.expectedBodiesReceived(matchingMessages);

            for (Object body : allMessages) {
                template.sendBody("direct:start", body);
            }

            result.assertIsSatisfied();
        } finally {
            context.stop();
        }
    }

    @Test
    public void testRuleFilterStepWithJsonComplexPath() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(
                new SimpleStep.Builder()
                    .stepKind("endpoint")
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "start")
                            .build())
                        .build())
                    .build(),
                new RuleFilterStep.Builder()
                    .putConfiguredProperty("type", "rule")
                    .putConfiguredProperty("predicate", "OR")
                    .putConfiguredProperty("rules", "[{\"path\":\"user.name\",\"op\":\"==\",\"value\":\"James\"}, {\"path\":\"user.name\",\"op\":\"==\",\"value\":\"Roland\"}]")
                    .build(),
                new SimpleStep.Builder()
                    .stepKind("endpoint")
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

            final List<String> matchingMessages = Arrays.asList("{ \"user\": { \"name\": \"James\" } }", "{  \"user\": { \"name\": \"Roland\" } }");
            final List<String> notMatchingMessages = Collections.singletonList("{ \"user\": { \"name\": \"Jimmi\" } }");
            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);

            List<String> allMessages = new ArrayList<>();
            allMessages.addAll(matchingMessages);
            allMessages.addAll(notMatchingMessages);

            result.expectedBodiesReceived(matchingMessages);

            for (Object body : allMessages) {
                template.sendBody("direct:start", body);
            }

            result.assertIsSatisfied();
        } finally {
            context.stop();
        }
    }

    @Test
    public void testRuleFilterStepWithPOJO() throws Exception {
        final CamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(
                new SimpleStep.Builder()
                    .stepKind("endpoint")
                    .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("direct")
                            .putConfiguredProperty("name", "start")
                            .build())
                        .build())
                    .build(),
                new RuleFilterStep.Builder()
                    .putConfiguredProperty("type", "rule")
                    .putConfiguredProperty("predicate", "OR")
                    .putConfiguredProperty("rules", "[{\"path\":\"name\",\"op\":\"==\",\"value\":\"James\"}, {\"path\":\"name\",\"op\":\"==\",\"value\":\"Roland\"}]")
                    .build(),
                new SimpleStep.Builder()
                    .stepKind("endpoint")
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

            final List<User> matchingMessages = Arrays.asList(new User("James"), new User("Roland"));
            final List<User> notMatchingMessages = Collections.singletonList(new User("Jimmy"));
            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);

            List<User> allMessages = new ArrayList<>();
            allMessages.addAll(matchingMessages);
            allMessages.addAll(notMatchingMessages);

            result.expectedBodiesReceived(matchingMessages);

            for (Object body : allMessages) {
                template.sendBody("direct:start", body);
            }

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
}
