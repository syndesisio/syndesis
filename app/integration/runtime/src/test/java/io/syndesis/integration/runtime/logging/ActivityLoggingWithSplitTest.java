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
package io.syndesis.integration.runtime.logging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import io.syndesis.common.util.Json;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.integration.runtime.IntegrationTestSupport;
import io.syndesis.integration.runtime.capture.OutMessageCaptureProcessor;
import io.syndesis.integration.runtime.util.JsonSupport;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.AvoidThrowingRawExceptionTypes"})
public class ActivityLoggingWithSplitTest extends IntegrationTestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityLoggingWithSplitTest.class);

    protected CamelContext context;
    protected ActivityTracker activityTracker;
    protected ArrayList<ActivityEvent> activityEvents;

    @Before
    public void before() throws Exception {
        activityEvents = new ArrayList<>();

        activityTracker = items -> {
            try {
                String json = JsonSupport.toJsonObject(items);
                ActivityEvent event = Json.reader().forType(ActivityEvent.class).readValue(json);

                activityEvents.add(event);
            } catch (IOException e) {
                LOGGER.warn("", e);
            }
        };

        context = new DefaultCamelContext();
        context.setUuidGenerator(KeyGenerator::createKey);
        context.addLogListener(new IntegrationLoggingListener(activityTracker));
        context.addInterceptStrategy(new ActivityTrackingInterceptStrategy(activityTracker));
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start")
                    .id("start")
                    .routePolicy(new ActivityTrackingPolicy(activityTracker))
                    .split()
                        .body()
                        .process(OutMessageCaptureProcessor.INSTANCE)
                        .pipeline()
                            .id("log")
                            .log(LoggingLevel.INFO, "log", "log", "hi")
                            .process(OutMessageCaptureProcessor.INSTANCE)
                        .end()
                        .pipeline()
                            .id("rnderr")
                            .process().body(String.class, body -> {
                                if ("error".equals(body)) {
                                    throw new RuntimeException("Bean Error");
                                }
                            })
                            .process(OutMessageCaptureProcessor.INSTANCE)
                        .end()
                        .pipeline()
                            .id("end")
                            .to("mock:end")
                            .process(OutMessageCaptureProcessor.INSTANCE)
                        .end()
                    .end();
            }
        });

        context.start();
    }

    @After
    public void after() throws Exception {
        context.stop();
    }

    @Test
    public void testLoggingWithSuccessStep() throws Exception {

        final MockEndpoint result = context.getEndpoint("mock:end", MockEndpoint.class);
        result.expectedBodiesReceived("Hello", "World");
        context.createProducerTemplate().sendBody("direct:start", new String[]{"Hello", "World"});
        result.assertIsSatisfied();

        // There should be 1 exchanges logged.
        assertEquals(1, activityEvents.stream().map(x -> x.exchange).collect(Collectors.toSet()).size());
        // There should be 1 "status":"begin"
        assertEquals(1, activityEvents.stream().map(x -> x.status).filter(x -> "begin".equals(x)).collect(Collectors.toList()).size());
        // There should be 1 "status":"done"
        assertEquals(1, activityEvents.stream().map(x -> x.status).filter(x -> "done".equals(x)).collect(Collectors.toList()).size());

    }

    @Test
    public void testLoggingWithErrorStep() throws Exception {

        try {
            context.createProducerTemplate().sendBody("direct:start", new String[]{"Hello", "error"});
            fail("Expected exception");
        } catch (CamelExecutionException e) {
            // expected.
        }

        // There should be 1 exchanges logged.
        assertEquals(1, activityEvents.stream().map(x -> x.exchange).collect(Collectors.toSet()).size());
        // There should be 1 "status":"begin"
        assertEquals(1, activityEvents.stream().map(x -> x.status).filter(x -> "begin".equals(x)).collect(Collectors.toList()).size());
        // There should be 1 "status":"done"
        assertEquals(1, activityEvents.stream().map(x -> x.status).filter(x -> "done".equals(x)).collect(Collectors.toList()).size());
    }

    // **************
    // Helpers
    // **************

    public static class ActivityEvent {
        public String exchange;
        public String status;
        public String step;
        public String duration;
        public String messages;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ActivityEvent that = (ActivityEvent) o;

            if (!exchange.equals(that.exchange)) return false;
            if (status != null ? !status.equals(that.status) : that.status != null) return false;
            if (step != null ? !step.equals(that.step) : that.step != null) return false;
            if (duration != null ? !duration.equals(that.duration) : that.duration != null) return false;
            return messages != null ? messages.equals(that.messages) : that.messages == null;
        }

        @Override
        public int hashCode() {
            int result = exchange.hashCode();
            result = 31 * result + (status != null ? status.hashCode() : 0);
            result = 31 * result + (step != null ? step.hashCode() : 0);
            result = 31 * result + (duration != null ? duration.hashCode() : 0);
            result = 31 * result + (messages != null ? messages.hashCode() : 0);
            return result;
        }
    }
}
