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
package io.syndesis.connector.slack;

import java.util.Map;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;

public class SlackChannelCustomizer implements ComponentProxyCustomizer {

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        component.setBeforeProducer(this::beforeProducer);
        sanitizeUserOrChannel(options);
    }

    private void sanitizeUserOrChannel(Map<String, Object> options) {
        String username = ConnectorOptions.extractOption(options, "receiver");
        String channel = ConnectorOptions.popOption(options, "channel");

        if (channel != null) {
            if (channel.trim().charAt(0) == '#') {
                options.put("channel", channel.trim());
            } else {
                options.put("channel", "#" + channel.trim());
            }
        } else if (username != null) {
            if (username.trim().charAt(0) == '@') {
                options.put("channel", username.trim());
            } else {
                options.put("channel", "@" + username.trim());
            }
        }
    }

    private void beforeProducer(Exchange exchange) {

        final Message in = exchange.getIn();
        final SlackPlainMessage message = in.getBody(SlackPlainMessage.class);

        if (message != null) {
            in.setBody(message.getMessage());
        }

    }

}
