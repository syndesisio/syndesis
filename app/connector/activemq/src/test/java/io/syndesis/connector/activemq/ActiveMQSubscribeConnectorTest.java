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

import javax.jms.ConnectionFactory;

import io.syndesis.common.model.integration.Step;

import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;
import org.springframework.jms.core.JmsTemplate;
import org.zapodot.junit5.jms.annotations.EmbeddedJms;

public class ActiveMQSubscribeConnectorTest extends ActiveMQConnectorTestSupport {

    @EmbeddedJms 
    private ConnectionFactory connectionFactory;

    @Override
    protected List<Step> createSteps() {
        return Arrays.asList(
            newActiveMQEndpointStep(
                "io.syndesis.connector:connector-activemq-subscribe",
                builder -> {
                    builder.putConfiguredProperty("destinationName", "subscribeTest");
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
    public void subscribeTest() throws InterruptedException {
        final String message = UUID.randomUUID().toString();

        MockEndpoint mock = context().getEndpoint("mock:result", MockEndpoint.class);
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived(message);

        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.send("subscribeTest", session -> session.createTextMessage(message));

        mock.assertIsSatisfied();
    }
}
