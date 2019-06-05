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

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import io.syndesis.common.util.Json;
import io.syndesis.connector.support.processor.util.SimpleJsonSchemaInspector;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public final class HttpRequestUnwrapperProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestUnwrapperProcessor.class);

    private final Set<String> parameters;

    public HttpRequestUnwrapperProcessor(final JsonNode schema) {
        parameters = SimpleJsonSchemaInspector.getProperties(schema, "parameters");
    }

    @Override
    public void process(final Exchange exchange) throws Exception {
        final Message message = exchange.getIn();
        final Object body = message.getBody();

        final JsonNode data = parseBody(body);

        if (data != null) {
            final JsonNode paramMap = data.get("parameters");
            final JsonNode bodyData = data.get("body");

            if (paramMap != null || bodyData != null) {
                if (paramMap != null) {
                    for (final String key : parameters) {
                        final JsonNode valueNode = paramMap.get(key);
                        if (valueNode != null) {
                            final String val = valueNode.asText();
                            message.setHeader(key, val);
                        }
                    }
                }

                if (bodyData == null) {
                    message.setBody(null);
                    return;
                }

                if (bodyData.isContainerNode()) {
                    message.setBody(Json.toString(bodyData));
                    return;
                }

                message.setHeader(Exchange.CONTENT_TYPE, "text/plain");
                message.setBody(bodyData.asText());
            }
        }
    }

    static JsonNode parseBody(final Object body) throws IOException, JsonProcessingException {
        if (body == null) {
            return null;
        }

        if (body instanceof String) {
            final String string = (String) body;
            if (ObjectHelper.isNotEmpty(string)) {
                return Json.reader().readTree(string);
            }
        } else if (body instanceof InputStream) {
            try (InputStream stream = (InputStream) body) {
                if (stream.available() > 0) {
                    return Json.reader().readTree(stream);
                }
            }
        }

        LOG.warn("Unable to parse given body as JSON, unsupported body given: {}", body.getClass());

        return null;
    }
}
