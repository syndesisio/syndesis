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
package io.syndesis.integration.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.model.FromDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.camel.util.AsyncProcessorConverterHelper;

/**
 * Used to capture the out messages of processors with configured ids.  The messages are placed into
 * a map stored in the camel exchange property using the processor id as the map key.
 */
public class OutMessageCaptureInterceptStrategy implements InterceptStrategy {

    public static final String CAPTURED_OUT_MESSAGES_MAP = "Syndesis.CAPTURED_OUT_MESSAGES_MAP";

    @Override
    public Processor wrapProcessorInInterceptors(CamelContext context, ProcessorDefinition<?> definition, Processor target, Processor nextTarget) throws Exception {

        // First processor needs to handle the in message as the out message from the consumer since
        // we can't intercept consumers
        FromDefinition from = getFromDefinition(definition);
        boolean captureIn = from!=null;
        boolean captureOut = definition.hasCustomIdAssigned();

        if (captureIn || captureOut) {
            return AsyncProcessorConverterHelper.convert(exchange -> {
                if( captureIn ) {
                    addToMap(exchange, from.getId(), exchange.getIn());
                }
                target.process(exchange);
                if( captureOut ) {
                    Message message = exchange.hasOut() ? exchange.getOut() : exchange.getIn();
                    addToMap(exchange, definition.getId(), message);
                }
            });
        } else {
            // skip over processors with a generated id
            return target;
        }
    }

    private void addToMap(Exchange exchange, String key, Message msg) {
        if( msg != null ) {
            Message copy = msg.copy();
            Map<String, Message> outMessagesMap = getCapturedMessageMap(exchange);
            outMessagesMap.put(key, copy);
        }
    }

    private static FromDefinition getFromDefinition(ProcessorDefinition<?> definition) {
        ProcessorDefinition<?> parent = definition.getParent();
        if( parent instanceof RouteDefinition ) {
            RouteDefinition route = (RouteDefinition) parent;
            List<ProcessorDefinition<?>> outputs = route.getOutputs();
            if( outputs!=null && outputs.get(0) == definition) {
                return route.getInputs().get(0);
            }
        }
        return null;
    }

    public static Map<String, Message> getCapturedMessageMap(Exchange exchange) {
        Map<String, Message> outMessagesMap = exchange.getProperty(CAPTURED_OUT_MESSAGES_MAP, Map.class);
        if( outMessagesMap == null ) {
            outMessagesMap = new HashMap<>();
            exchange.setProperty(CAPTURED_OUT_MESSAGES_MAP, outMessagesMap);
        }
        return outMessagesMap;
    }
}
