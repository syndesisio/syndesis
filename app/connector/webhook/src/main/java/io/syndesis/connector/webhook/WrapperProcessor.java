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

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;

import io.syndesis.common.util.Json;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.util.ObjectHelper;

class WrapperProcessor implements Processor {
    final Set<String> parameters;

    static final ObjectReader READER = Json.reader().forType(JsonNode.class);

    WrapperProcessor(ObjectSchema schema) {
        final Map<String, JsonSchema> properties = schema.getProperties();

        final JsonSchema parametersSchema = properties.get("parameters");
        if (!(parametersSchema instanceof ObjectSchema)) {
            parameters = Collections.emptySet();
        } else {
            parameters = ((ObjectSchema) parametersSchema).getProperties().keySet();
        }
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        final Message message = exchange.getIn();
        final Object body = message.getBody();

        final ObjectNode rootNode = Json.copyObjectMapperConfiguration().createObjectNode();

        if (!parameters.isEmpty()) {
            final ObjectNode parametersNode = rootNode.putObject("parameters");

            for (String parameter : parameters) {
                parametersNode.put(parameter, message.getHeader(parameter, String.class));
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
        } else if (body != null){
            rootNode.putPOJO("body", body);
        }

        message.setBody(Json.toString(rootNode));
    }
}
