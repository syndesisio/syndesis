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
package io.syndesis.connector.rest.swagger;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;

final class PayloadConverterHelper {

    private static final CamelContext CONTEXT = new DefaultCamelContext();

    static Exchange createExhangeWithBody(final String contentType, final String payload) {
        final Exchange exchange = new DefaultExchange(CONTEXT);

        final DefaultMessage in = new DefaultMessage(CONTEXT);
        in.setHeader(Exchange.CONTENT_TYPE, contentType);
        exchange.setIn(in);

        in.setBody(payload);

        return exchange;
    }

}
