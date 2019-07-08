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
package io.syndesis.connector.twitter;

import java.util.Map;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

public class RetrieveDirectMessageCustomizer implements ComponentProxyCustomizer {

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        component.setAfterConsumer(exchange -> {
            final twitter4j.DirectMessage twitterMessage = exchange.getIn(twitter4j.DirectMessage.class);
            // wrapper for twitter4j.DirectMessage to suppress deprecated fields on atlas map step,
            // that can lead to mapping errors
            DirectMessage message = new DirectMessage();
            message.setCreatedAt(twitterMessage.getCreatedAt());
            message.setId(twitterMessage.getId());
            message.setRecipientId(twitterMessage.getRecipientId());
            message.setSenderId(twitterMessage.getSenderId());
            message.setText(twitterMessage.getText());
            exchange.getIn().setBody(message);
        });
    }
}
