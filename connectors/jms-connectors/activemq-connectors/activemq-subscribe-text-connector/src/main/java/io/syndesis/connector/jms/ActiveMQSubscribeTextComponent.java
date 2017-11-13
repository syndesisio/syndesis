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

import org.apache.camel.Message;

/**
 * Camel activemq-subscribe-text connector
 */
public class ActiveMQSubscribeTextComponent extends AbstractActiveMQConnector {

    public ActiveMQSubscribeTextComponent() {
        super("activemq-subscribe-text", ActiveMQSubscribeTextComponent.class.getName());

        // create JmsMessage from Camel message
        setBeforeConsumer(exchange -> {
            final Message in = exchange.getIn();
            final JmsTextMessage jmsTextMessage = new JmsTextMessage(in.getBody(String.class));
            jmsTextMessage.setHeaders(in.getHeaders());
            in.setBody(jmsTextMessage);
        });
    }

}
