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

import io.syndesis.common.util.KeyGenerator;
import io.syndesis.integration.runtime.capture.OutMessageCaptureProcessor;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.LoggingLevel;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.util.ObjectHelper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ActivityLoggingTest extends AbstractActivityLoggingTest {

    @Override
    protected RoutesBuilder createTestRoutes() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start")
                    .id("start")
                    .routePolicy(new IntegrationActivityTrackingPolicy(activityTracker))
                    .setHeader(IntegrationLoggingConstants.STEP_ID, KeyGenerator::createKey)
                    .pipeline()
                        .id("step:log")
                        .setHeader(IntegrationLoggingConstants.STEP_ID, KeyGenerator::createKey)
                        .log(LoggingLevel.INFO, "log", "log", "hi")
                        .process(OutMessageCaptureProcessor.INSTANCE)
                    .end()
                    .pipeline()
                        .id("step:rnderr")
                        .setHeader(IntegrationLoggingConstants.STEP_ID, KeyGenerator::createKey)
                        .process().body(String.class, body -> {
                            if ("Hello Error".equals(body)) {
                                throw new RuntimeException("Bean Error");
                            }
                        })
                        .process(OutMessageCaptureProcessor.INSTANCE)
                    .end()
                    .pipeline()
                        .id("step:end")
                        .setHeader(IntegrationLoggingConstants.STEP_ID, KeyGenerator::createKey)
                        .to("mock:end")
                        .process(OutMessageCaptureProcessor.INSTANCE)
                    .end();
            }
        };
    }

    @Test
    public void testLoggingWithSuccessStep() throws Exception {
        final MockEndpoint result = context.getEndpoint("mock:end", MockEndpoint.class);
        result.expectedBodiesReceived("Hello World");
        context.createProducerTemplate().sendBody("direct:start", "Hello World");
        result.assertIsSatisfied();

        // There should be 1 exchanges logged
        assertEquals(1, findExchangesLogged().size());
        // There should be 1 "status":"begin"
        assertEquals(1, findActivityEvents(x -> "begin".equals(x.status)).size());
        // There should be 1 "status":"done"
        assertEquals(1, findActivityEvents(x -> "done".equals(x.status)).size());
        // There should be no failed flag on activity with "status":"done"
        assertEquals("false", findActivityEvent(x -> "done".equals(x.status)).failed);

        // There should be 1 log activity
        assertEquals(1, findActivityEvents(x -> "log".equals(x.step) && ObjectHelper.isEmpty(x.duration)).size());
        assertEquals(1, findActivityEvents(x -> "log".equals(x.step) && ObjectHelper.isNotEmpty(x.duration)).size());
        assertEquals("hi", findActivityEvent(x -> "log".equals(x.step)).message);

        // There should be step activity tracking events
        assertEquals(3, findActivityEvents(x -> ObjectHelper.isNotEmpty(x.duration)).size());
    }

    @Test
    public void testLoggingWithErrorStep() {
        try {
            context.createProducerTemplate().sendBody("direct:start", "Hello Error");
            fail("Expected exception");
        } catch (CamelExecutionException e) {
            // expected.
        }

        // There should be 1 exchanges logged
        assertEquals(1, findExchangesLogged().size());
        // There should be 1 "status":"begin"
        assertEquals(1, findActivityEvents(x -> "begin".equals(x.status)).size());
        // There should be 1 "status":"done"
        assertEquals(1, findActivityEvents(x -> "done".equals(x.status)).size());
        // There should be a failed flag on activity with "status":"done"
        assertEquals("true", findActivityEvent(x -> "done".equals(x.status)).failed);

        // There should be 1 log activity
        assertEquals(1, findActivityEvents(x -> "log".equals(x.step) && ObjectHelper.isEmpty(x.duration)).size());
        assertEquals(1, findActivityEvents(x -> "log".equals(x.step) && ObjectHelper.isNotEmpty(x.duration)).size());
        assertEquals("hi", findActivityEvent(x -> "log".equals(x.step)).message);

        // There should be step activity tracking events
        assertEquals(2, findActivityEvents(x -> ObjectHelper.isNotEmpty(x.duration)).size());

        // There should be a failure report activity event
        assertEquals(1, findActivityEvents(x -> ObjectHelper.isNotEmpty(x.failure)).size());
        assertTrue(findActivityEvent(x -> ObjectHelper.isNotEmpty(x.failure)).failure.startsWith("java.lang.RuntimeException: Bean Error"));
    }
}
