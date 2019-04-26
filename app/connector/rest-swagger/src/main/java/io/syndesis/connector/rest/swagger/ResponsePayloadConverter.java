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

import java.io.IOException;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class ResponsePayloadConverter extends PayloadConverterBase {

    private static final Logger LOG = LoggerFactory.getLogger(ResponsePayloadConverter.class);

    public ResponsePayloadConverter(final DataShape dataShape) {
        super(dataShape.getKind());
    }

    public ResponsePayloadConverter(final DataShapeKinds kind) {
        super(kind);
    }

    @Override
    void convertAsJson(final Message in) {
        final ObjectNode replacement = MAPPER.createObjectNode();
        final ObjectNode parameters = replacement.putObject("parameters");
        final Integer status = in.getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);

        if (status != null) {
            parameters.put("Status", status);
        }

        final String contentType = in.getHeader(Exchange.CONTENT_TYPE, String.class);
        if (contentType != null) {
            parameters.put("Content-Type", contentType);
        }

        final String body = bodyAsString(in);
        if (body != null) {
            final JsonNode payload;
            try {
                payload = MAPPER.readTree(body);
            } catch (final IOException e) {
                LOG.warn("Unable to parse response payload, continuing without conversion: ", e.getMessage());
                LOG.debug("Unable to parse `{}` to JSON", body, e);

                return;
            }

            replacement.set("body", payload);
        }

        replaceBodyWith(in, replacement);
    }

    @Override
    void convertAsXml(final Message in) {
        // we don't modify XML responses
    }

    static void replaceBodyWith(final Message in, final ObjectNode replacement) {
        try {
            in.setBody(MAPPER.writeValueAsString(replacement));
        } catch (final JsonProcessingException e) {
            LOG.warn("Unable to replace payload, continuing without conversion: ", e.getMessage());
            LOG.debug("Unable to serialize `{}` to JSON", replacement, e);
        }
    }

}
