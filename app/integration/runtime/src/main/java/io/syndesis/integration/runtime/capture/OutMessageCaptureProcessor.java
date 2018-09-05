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
package io.syndesis.integration.runtime.capture;

import java.util.HashMap;
import java.util.Map;

import io.syndesis.integration.runtime.logging.IntegrationLoggingConstants;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.MessageSupport;

/**
 * Used to capture the out messages of processors with configured ids.  The messages are placed into
 * a map stored in the camel exchange property using the processor id as the map key.
 */
public class OutMessageCaptureProcessor implements Processor {
    public static final Processor INSTANCE = new OutMessageCaptureProcessor();
    public static final String CAPTURED_OUT_MESSAGES_MAP = "Syndesis.CAPTURED_OUT_MESSAGES_MAP";

    @Override
    public void process(Exchange exchange) throws Exception {
        final Message message = exchange.hasOut() ? exchange.getOut() : exchange.getIn();
        final String id = message.getHeader(IntegrationLoggingConstants.STEP_ID, String.class);

        if (id != null) {
            Message copy = message.copy();
            Map<String, Message> outMessagesMap = getCapturedMessageMap(exchange);
            if (copy instanceof MessageSupport && copy.getExchange() == null) {
                ((MessageSupport) copy).setExchange(message.getExchange());
            }

            outMessagesMap.put(id, copy);
        }
    }

    public static Map<String, Message> getCapturedMessageMap(Exchange exchange) {
        @SuppressWarnings("unchecked")
        Map<String, Message> outMessagesMap = exchange.getProperty(CAPTURED_OUT_MESSAGES_MAP, Map.class);
        if( outMessagesMap == null ) {
            outMessagesMap = new HashMap<>();
            exchange.setProperty(CAPTURED_OUT_MESSAGES_MAP, outMessagesMap);
        }
        return outMessagesMap;
    }
}
