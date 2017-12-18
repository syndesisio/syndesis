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
package io.syndesis.connector.salesforce;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.connector.DataType;
import org.apache.camel.component.salesforce.api.utils.JsonUtils;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

/* default */ abstract class UnmarshallProcessor implements Processor {

    private static final ObjectMapper MAPPER = JsonUtils.createObjectMapper();

    private final Class<?> type;

    public UnmarshallProcessor(final DataType dataType) {
        if (dataType.getType() == DataType.Type.java) {
            try {
                final Class<?> tmp = Class.forName(dataType.getSubType());
                type = tmp;
            } catch (final ClassNotFoundException e) {
                throw new IllegalArgumentException("The specified class for type `" + dataType + "` cannot be found", e);
            }

        } else {
            type = null;
        }
    }

    @Override
    public final void process(final Exchange exchange) throws Exception {
        if (exchange.isFailed()) {
            return;
        }

        if (type == null) {
            return;
        }

        final Message message = message(exchange);
        final String bodyAsString = message.getBody(String.class);

        if (bodyAsString == null) {
            return;
        }

        try {
            final Object output = MAPPER.readValue(bodyAsString, type);
            message.setBody(output);
        } catch (final IOException e) {
            exchange.setException(e);
        }
    }

    /* default */ abstract Message message(final Exchange exchange);

}
