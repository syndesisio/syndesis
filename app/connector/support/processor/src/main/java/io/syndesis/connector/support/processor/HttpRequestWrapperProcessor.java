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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.syndesis.common.util.Json;
import io.syndesis.connector.support.processor.util.SimpleJsonSchemaInspector;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.ObjectHelper;

import java.io.InputStream;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public class HttpRequestWrapperProcessor implements Processor {
    private final Set<String> parameters;

    private static final ObjectReader READER = Json.reader().forType(JsonNode.class);

    public HttpRequestWrapperProcessor(JsonNode schema) {
        this.parameters = SimpleJsonSchemaInspector.getProperties(schema, "parameters");
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        final Message message = exchange.getIn();
        final Object body = message.getBody();

        final ObjectNode rootNode = Json.copyObjectMapperConfiguration().createObjectNode();

        if (!parameters.isEmpty()) {
            final ObjectNode parametersNode = rootNode.putObject("parameters");

            final HttpServletRequest servletRequest = message.getHeader(Exchange.HTTP_SERVLET_REQUEST, HttpServletRequest.class);
            for (String parameter : parameters) {
                final String[] values;
                final String[] headerValues = message.getHeader(parameter, String[].class);
                if (servletRequest != null && headerValues == null) {
                    values = servletRequest.getParameterValues(parameter);
                } else {
                    values = headerValues;
                }

                putPrameterValueTo(parametersNode, parameter, values);
            }
        }

        if (body instanceof String) {
            final String string = (String) body;
            if (ObjectHelper.isNotEmpty(string)) {
                rootNode.set("body", READER.readValue(string));
            }
        } else if (body instanceof InputStream) {
            try (InputStream stream = (InputStream) body) {
                if (stream.available() > 0) {
                    rootNode.set("body", READER.readValue(stream));
                }
            }
        } else if (body != null) {
            rootNode.putPOJO("body", body);
        }

        final String newBody = Json.toString(rootNode);
        final Message replacement = new DefaultMessage(exchange.getContext());
        replacement.copyFromWithNewBody(message, newBody);

        // we rely on having the Content-Type match the body when we're
        // extracting parameters from the JSON body, otherwise we don't
        // know if the content is JSON or XML (or any future supported
        // content type)
        replacement.setHeader(Exchange.CONTENT_TYPE, "application/json");

        ExchangeHelper.replaceMessage(exchange, replacement, false);
    }

    private static void putPrameterValueTo(final ObjectNode node, String parameter, final String... values) {
        if (values == null || values.length == 0) {
            return;
        }

        if (values.length > 1) {
            final ArrayNode ary = node.putArray(parameter);
            for (String value : values) {
                ary.add(value);
            }
        } else {
            node.put(parameter, values[0]);
        }
    }

    public Set<String> getParameters() {
        return parameters;
    }
}
