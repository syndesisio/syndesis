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

import javax.jms.TextMessage;

import io.syndesis.common.model.integration.Step;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

@SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.JUnitTestsShouldIncludeAssert"})
public class ActiveMQPublishConnectorTest extends ActiveMQConnectorTestSupport {

    // **************************
    // Set up
    // **************************

    @Override
    protected List<Step> createSteps() {
        return Arrays.asList(
            newSimpleEndpointStep(
                "direct",
                builder -> builder.putConfiguredProperty("name", "start")),
            newActiveMQEndpointStep(
                "io.syndesis.connector:connector-activemq-publish",
                builder -> {
                    builder.putConfiguredProperty("destinationName", testName.getMethodName());
                    builder.putConfiguredProperty("destinationType", "queue");
                })
        );
    }

    // **************************
    // Tests
    // **************************

    @Test
    public void subscribeTest() {
        final String message = UUID.randomUUID().toString();

        template().sendBody("direct:start", message);

        JmsTemplate template = new JmsTemplate(broker.createConnectionFactory());
        Object answer = template.receive(testName.getMethodName());

        Assertions.assertThat(answer).isInstanceOf(TextMessage.class);
        Assertions.assertThat(answer).hasFieldOrPropertyWithValue("text", message);
    }
}
