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
package io.syndesis.connector.activemq;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.syndesis.common.model.integration.Step;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

@SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.JUnitTestsShouldIncludeAssert"})
public class ActiveMQSubscribeConnectorTest extends ActiveMQConnectorTestSupport {

    // **************************
    // Set up
    // **************************

    @Override
    protected List<Step> createSteps() {
        return Arrays.asList(
            newActiveMQEndpointStep(
                "io.syndesis.connector:connector-activemq-subscribe",
                builder -> {
                    builder.putConfiguredProperty("destinationName", testName.getMethodName());
                    builder.putConfiguredProperty("destinationType", "queue");
                }),
            newSimpleEndpointStep(
                "mock",
                builder -> builder.putConfiguredProperty("name", "result"))
        );
    }

    // **************************
    // Tests
    // **************************

    @Test
    public void subscribeTest() throws Exception {
        final String message = UUID.randomUUID().toString();

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived(message);

        JmsTemplate template = new JmsTemplate(broker.createConnectionFactory());
        template.send(testName.getMethodName(), session -> session.createTextMessage(message));

        mock.assertIsSatisfied();
    }
}
