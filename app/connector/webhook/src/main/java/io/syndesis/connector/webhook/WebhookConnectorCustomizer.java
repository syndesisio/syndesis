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
package io.syndesis.connector.webhook;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;

import java.util.Map;

public class WebhookConnectorCustomizer implements ComponentProxyCustomizer {

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        // Unconditionally we remove output in 7.1 release
        component.setAfterConsumer(this::removeOutput);
    }

    public void removeOutput(final Exchange exchange) {
        exchange.getOut().setBody("");
        exchange.getOut().removeHeaders("*");

        if (exchange.getException() == null) {
            // In case of exception, we leave the error code as is
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 204);
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_TEXT, "No Content");
        }
    }
}
