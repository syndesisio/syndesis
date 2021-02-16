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
package io.syndesis.integration.runtime.tracing;

import java.util.ArrayList;
import java.util.Arrays;

import io.jaegertracing.internal.JaegerSpan;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.spi.Reporter;
import io.opentracing.Tracer;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.common.util.Resources;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Handler;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.syndesis.integration.runtime.IntegrationTestSupport.dumpRoutes;
import static io.syndesis.integration.runtime.IntegrationTestSupport.newIntegration;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ActivityTracingWithSplitTest {
    protected CamelContext context;
    protected ArrayList<JaegerSpan> activityEvents;

    @BeforeEach
    public void before() throws Exception {

        activityEvents = new ArrayList<>();

        Tracer tracer = new JaegerTracer.Builder(getClass().getName()).withReporter(new Reporter() {
            @Override
            public void report(JaegerSpan span) {
                activityEvents.add(span);
            }

            @Override
            public void close() {
                // no resource to dispose
            }
        }).withSampler(new ConstSampler(true)).build();

        final RouteBuilder routeBuilder = new IntegrationRouteBuilder("", Resources.loadServices(IntegrationStepHandler.class),
            Arrays.asList(new TracingActivityTrackingPolicyFactory(tracer))) {

            @Override
            protected Integration loadIntegration() {
                return newIntegration(
                    new Step.Builder()
                        .id("source")
                        .stepKind(StepKind.endpoint)
                        .action(new ConnectorAction.Builder()
                            .descriptor(new ConnectorDescriptor.Builder()
                                .componentScheme("direct")
                                .putConfiguredProperty("name", "start")
                                .build())
                            .build())
                        .build(),
                    new Step.Builder()
                        .stepKind(StepKind.split)
                        .build(),
                    new Step.Builder()
                        .id("step-1")
                        .stepKind(StepKind.log)
                        .putConfiguredProperty("contextLoggingEnabled", "true")
                        .putConfiguredProperty("customText", "Log me baby one more time").build(),
                    new Step.Builder()
                        .id("step-2")
                        .stepKind(StepKind.endpoint)
                        .action(new ConnectorAction.Builder()
                            .descriptor(new ConnectorDescriptor.Builder()
                                .componentScheme("class")
                                .putConfiguredProperty("beanName", TestBean.class.getName())
                                .build())
                            .build())
                        .build(),
                    new Step.Builder()
                        .id("step-3")
                        .stepKind(StepKind.log)
                        .putConfiguredProperty("contextLoggingEnabled", "true")
                        .putConfiguredProperty("customText", "Log me baby one more time").build(),
                    new Step.Builder()
                        .id("step-4")
                        .stepKind(StepKind.endpoint)
                        .action(new ConnectorAction.Builder()
                            .descriptor(new ConnectorDescriptor.Builder()
                                .componentScheme("mock")
                                .putConfiguredProperty("name", "end")
                                .build())
                            .build())
                        .build());
            }
        };

        context = new DefaultCamelContext();
        context.setUuidGenerator(KeyGenerator::createKey);
        context.addLogListener(new TracingLogListener(tracer));
        context.addInterceptStrategy(new TracingInterceptStrategy(tracer));
        context.addRoutes(routeBuilder);
        context.getShutdownStrategy().setTimeout(1);
        context.start();

        dumpRoutes(context, routeBuilder.getRouteCollection());
    }

    @AfterEach
    public void after() throws Exception {
        context.stop();
    }

    @Test
    public void testLoggingWithSuccessStep() throws Exception {
        final MockEndpoint result = context.getEndpoint("mock:end", MockEndpoint.class);
        result.expectedBodiesReceived("Hello World");
        context.createProducerTemplate().sendBody("direct:start", new String[] {"Hello"});
        result.assertIsSatisfied();

        assertEquals(6, activityEvents.size());
    }

    @Test
    public void testLoggingWithErrorStep() throws Exception {
        assertThatExceptionOfType(CamelExecutionException.class)
            .isThrownBy(() -> context.createProducerTemplate().sendBody("direct:start", new String[] {"error"}))
            .withMessageStartingWith("Exception occurred during execution on the exchange: Exchange")
            .withCause(new RuntimeException("Bean Error"));

        assertEquals(4, activityEvents.size());
    }

    public static class TestBean {
        @Handler
        public String process(String body) {
            if ("error".equals(body)) {
                throw new RuntimeException("Bean Error");
            }
            return body + " World";
        }
    }
}
