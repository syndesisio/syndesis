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
package io.syndesis.connector.support.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.http.common.HttpMessage;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.util.ExchangeHelper;

/**
 * Integrations may frequently overwrite message body content. HttpMessage is having issues with that
 * as it tries to read the request input stream cache multiple times in that case. This results to stream already closed
 * errors while processing the message body.
 *
 * Normal stream caching mechanisms on the http message implementation do not solve this issue as message body gets frequently
 * replaced while running the integration and this in particular is not covered.
 *
 * This replaces the message on the exchange so the type of the message is no longer HttpMessage and when copies of the message
 * are attempted the HTTP request stream is not read the second time. At which point the HTTP request stream might be closed.
 */
public class HttpMessageToDefaultMessageProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        final Message message = exchange.getIn();
        if (message instanceof HttpMessage) {
            final Message replacement = new DefaultMessage(exchange.getContext());
            replacement.copyFrom(message);
            ExchangeHelper.replaceMessage(exchange, replacement, false);
        }
    }
}
