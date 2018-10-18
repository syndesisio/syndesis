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

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.xml.stream.XMLInputFactory;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.util.MessageHelper;

import com.fasterxml.jackson.databind.ObjectMapper;

abstract class PayloadConverterBase implements Processor {
    static final ObjectMapper MAPPER = new ObjectMapper();

    static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();

    private static final MimeType JSON;

    private static final MimeType NONE;

    private static final MimeType XML;

    static {
        try {
            JSON = createMimeType("application/json");
            XML = createMimeType("application/xml");
            NONE = createMimeType("application/octet-stream");
        } catch (final MimeTypeParseException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public void process(final Exchange exchange) {
        final Message in = exchange.getIn();

        final String contentType = MessageHelper.getContentType(in);

        try {
            final MimeType mimeType = createMimeType(contentType);

            if (mimeType.match(JSON)) {
                convertAsJson(in);
            } else if (mimeType.match(XML)) {
                convertAsXml(in);
            }
        } catch (final MimeTypeParseException ignored) {
            // we can't parse the MIME type
        }

    }

    abstract void convertAsJson(Message in);

    abstract void convertAsXml(Message in);

    static String bodyAsString(final Message in) {
        final String body = in.getBody(String.class);
        if (body == null) {
            return null;
        }
        return body;
    }

    private static MimeType createMimeType(final String raw) throws MimeTypeParseException {
        if (raw == null) {
            return NONE;
        }
        return new MimeType(raw);
    }
}
