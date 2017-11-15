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

import java.net.URISyntaxException;
import java.util.Map;

import org.apache.camel.Message;

/**
 * Camel activemq-respond-map connector
 */
public class ActiveMQRespondMapComponent extends AbstractActiveMQConnector {

    public ActiveMQRespondMapComponent() {
        super("activemq-respond-map", ActiveMQRespondMapComponent.class.getName());

        // create JmsMessage from Camel message
        setBeforeConsumer(exchange -> {
            final Message in = exchange.getIn();
            final JmsMapMessage jmsMapMessage = new JmsMapMessage(in.getBody(Map.class));
            jmsMapMessage.setHeaders(in.getHeaders());
            in.setBody(jmsMapMessage);
        });

        setAfterConsumer(exchange -> {

            if (!exchange.isFailed()) {
                final Message in = exchange.getIn();
                final JmsMapMessage jmsMapMessage = in.getBody(JmsMapMessage.class);
                in.setBody(jmsMapMessage.getBody());
                if (jmsMapMessage.getHeaders() != null) {
                    in.setHeaders(jmsMapMessage.getHeaders());
                }
            }
        });
    }

    @Override
    public String createEndpointUri(String scheme, Map<String, String> options) throws URISyntaxException {
        // set exchange pattern to InOut explicitly
        options.put("exchangePattern", "InOut");
        return super.createEndpointUri(scheme, options);
    }
}
