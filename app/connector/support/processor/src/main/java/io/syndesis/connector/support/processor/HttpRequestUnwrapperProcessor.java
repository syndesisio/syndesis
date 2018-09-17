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
import io.syndesis.common.util.Json;
import io.syndesis.connector.support.processor.util.SimpleJsonSchemaInspector;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.util.ObjectHelper;

import java.io.InputStream;
import java.util.Set;

public class HttpRequestUnwrapperProcessor implements Processor {
    private final Set<String> parameters;

    private static final ObjectReader READER = Json.reader().forType(JsonNode.class);

    public HttpRequestUnwrapperProcessor(JsonNode schema) {
        this.parameters = SimpleJsonSchemaInspector.getProperties(schema, "parameters");
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        final Message message = exchange.getIn();
        final Object body = message.getBody();

        JsonNode data = null;
        if (body instanceof String) {
            final String string = (String) body;
            if (ObjectHelper.isNotEmpty(string)) {
                data = READER.readTree(string);
            }
        } else if (body instanceof InputStream) {
            try (InputStream stream = (InputStream) body) {
                if (stream.available() > 0) {
                    data = READER.readTree(stream);
                }
            }
        }

        if (data != null) {
            JsonNode paramMap = data.get("parameters");
            JsonNode bodyData = data.get("body");

            if (paramMap != null || bodyData != null) {
                if (paramMap != null) {
                    for (String key : this.parameters) {
                        String val = paramMap.asText(key);
                        exchange.getIn().setHeader(key, val);
                    }
                }

                message.setBody(bodyData != null ? Json.toString(bodyData) : null);
            }
        }
    }

    public Set<String> getParameters() {
        return parameters;
    }
}
