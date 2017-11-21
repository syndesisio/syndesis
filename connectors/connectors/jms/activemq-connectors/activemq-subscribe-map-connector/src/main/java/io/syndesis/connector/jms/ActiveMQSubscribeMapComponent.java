/**
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.jms;

import java.util.Map;

import org.apache.camel.Message;

/**
 * Camel activemq-subscribe-map connector
 */
public class ActiveMQSubscribeMapComponent extends AbstractActiveMQConnector {

    public ActiveMQSubscribeMapComponent() {
        super("activemq-subscribe-map", ActiveMQSubscribeMapComponent.class.getName());

        // create JmsMessage from Camel message
        setBeforeConsumer(exchange -> {
            final Message in = exchange.getIn();
            final JmsMapMessage jmsMapMessage = new JmsMapMessage(in.getBody(Map.class));
            jmsMapMessage.setHeaders(in.getHeaders());
            in.setBody(jmsMapMessage);
        });
    }

}
