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
package io.syndesis.connector.swagger;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class PayloadConverter implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(PayloadConverter.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void process(final Exchange exchange) throws Exception {
        final Message in = exchange.getIn();

        convertAsJson(in);
    }

    private void convertAsJson(final Message in) throws IOException, JsonProcessingException {
        final String body = in.getBody(String.class);
        if (body == null) {
            return;
        }

        final JsonNode payload;
        try {
            payload = MAPPER.readTree(body);
        } catch (final JsonProcessingException e) {
            LOG.warn("Unable to parse payload, continuing without conversion", e);

            return;
        }

        payload.with("parameters").fields().forEachRemaining(e -> in.setHeader(e.getKey(), e.getValue().asText()));

        final JsonNode requestBody = payload.get("body");
        if (requestBody == null) {
            in.setBody(null);
        } else {
            in.setBody(MAPPER.writeValueAsString(requestBody));
        }
    }

}
