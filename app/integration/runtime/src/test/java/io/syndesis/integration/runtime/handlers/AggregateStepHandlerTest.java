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
import java.util.List;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.DataShapeMetaData;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.integration.runtime.logging.ActivityTracker;
import io.syndesis.integration.runtime.logging.ActivityTrackingInterceptStrategy;
import io.syndesis.integration.runtime.logging.BodyLogger;
import io.syndesis.integration.runtime.logging.IntegrationLoggingListener;
import io.syndesis.integration.runtime.util.JsonSupport;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.SimpleRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.syndesis.integration.runtime.IntegrationTestSupport.dumpRoutes;
import static io.syndesis.integration.runtime.IntegrationTestSupport.newIntegrationRouteBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AggregateStepHandlerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AggregateStepHandlerTest.class);

    private static final String START_STEP = "start-step";
    private static final String SPLIT_STEP = "split-step";
    private static final String AGGREGATE_STEP = "aggregate-step";
    private static final String LOG_STEP = "log-step";
    private static final String MOCK_STEP = "mock-step";

    private final ActivityTracker activityTracker = Mockito.mock(ActivityTracker.class);

    @Before
    public void setupMocks() {
        reset(activityTracker);

        doAnswer(invocation -> {
            ActivityTracker.initializeTracking(invocation.getArgument(0));
            return null;
        }).when(activityTracker).startTracking(any(Exchange.class));

        doAnswer(invocation -> {
            LOGGER.debug(JsonSupport.toJsonObject(invocation.getArguments()));
            return null;
        }).when(activityTracker).track(any());
    }

    @Test
    public void testAggregateUnifiedJsonStep() throws Exception {
        final DefaultCamelContext context = new DefaultCamelContext();

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(activityTracker,
                    new Step.Builder()
                            .id(START_STEP)
                            .stepKind(StepKind.endpoint)
                            .action(new ConnectorAction.Builder()
                                    .descriptor(new ConnectorDescriptor.Builder()
                                            .componentScheme("direct")
                                            .putConfiguredProperty("name", "expression")
                                            .build())
                                    .build())
                            .build(),
                    new Step.Builder()
                            .id(SPLIT_STEP)
                            .stepKind(StepKind.split)
                            .build(),
                    new Step.Builder()
                            .id(LOG_STEP)
                            .stepKind(StepKind.log)
                            .putConfiguredProperty("bodyLoggingEnabled", "true")
                            .putConfiguredProperty("customText", "Log me baby one more time")
                            .build(),
                    new Step.Builder()
                            .id(AGGREGATE_STEP)
                            .stepKind(StepKind.aggregate)
                            .action(new StepAction.Builder()
                                    .descriptor(new StepDescriptor.Builder()
                                            .outputDataShape(new DataShape.Builder()
                                                    .kind(DataShapeKinds.JSON_SCHEMA)
                                                    .putMetadata(DataShapeMetaData.UNIFIED, "true")
                                                    .specification("{" +
                                                            "\"$schema\": \"http://json-schema.org/schema#\"," +
                                                            "\"id\": \"io:syndesis:webhook\"," +
                                                            "\"type\": \"object\"," +
                                                            "\"properties\": {" +
                                                                "\"body\": {" +
                                                                    "\"type\": \"array\"," +
                                                                    "\"items\": {" +
                                                                        "\"type\": \"object\"," +
                                                                        "\"properties\": {" +
                                                                            "\"id\":{\"type\":\"string\",\"required\":true}" +
                                                                            "\"name\":{\"type\":\"string\",\"required\":true}" +
                                                                        "}" +
                                                                    "}" +
                                                                "}" +
                                                            "}" +
                                                        "}")
                                                    .build())
                                            .build())
                                    .build())
                            .build(),
                    new Step.Builder()
                            .id(MOCK_STEP)
                            .stepKind(StepKind.endpoint)
                            .action(new ConnectorAction.Builder()
                                    .descriptor(new ConnectorDescriptor.Builder()
                                            .componentScheme("mock")
                                            .putConfiguredProperty("name", "expression")
                                            .build())
                                    .build())
                            .build()
            );

            // Set up the camel context
            context.setUuidGenerator(KeyGenerator::createKey);
            context.addLogListener(new IntegrationLoggingListener(activityTracker));
            context.addInterceptStrategy(new ActivityTrackingInterceptStrategy(activityTracker));
            context.addRoutes(routes);

            SimpleRegistry beanRegistry = new SimpleRegistry();
            beanRegistry.bind("bodyLogger", new BodyLogger.Default());
            context.setRegistry(beanRegistry);

            context.start();

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);

            final ProducerTemplate template = context.createProducerTemplate();
            final MockEndpoint result = context.getEndpoint("mock:expression", MockEndpoint.class);
            final List<String> body = Arrays.asList("{\"body\": {\"id\":1,\"name\":\"a\"}}",
                                                    "{\"body\": {\"id\":2,\"name\":\"b\"}}",
                                                    "{\"body\": {\"id\":3,\"name\":\"c\"}}");

            result.expectedMessageCount(1);
            result.expectedBodiesReceived("{" +
                        "\"body\":[" +
                            "{\"id\":1,\"name\":\"a\"}," +
                            "{\"id\":2,\"name\":\"b\"}," +
                            "{\"id\":3,\"name\":\"c\"}" +
                        "]" +
                    "}");

            template.sendBody("direct:expression", body);

            result.assertIsSatisfied();

            verify(activityTracker).startTracking(any(Exchange.class));
            verifyActivityStepTracking(SPLIT_STEP, 0);
            verifyActivityStepTracking(LOG_STEP, body.size());
            verifyActivityStepTracking(AGGREGATE_STEP, 0);
            verifyActivityStepTracking(MOCK_STEP, 1);
            verify(activityTracker).finishTracking(any(Exchange.class));
        } finally {
            context.stop();
        }
    }

    private void verifyActivityStepTracking(String stepId, int times) {
        verify(activityTracker, times(times)).track(eq("exchange"), anyString(), eq("step"), eq(stepId), eq("id"), anyString(), eq("duration"), anyLong(), eq("failure"), isNull());
    }
}
