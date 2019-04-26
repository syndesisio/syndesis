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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;

import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public final class RequestPayloadConverter extends PayloadConverterBase {

    private static final Logger LOG = LoggerFactory.getLogger(RequestPayloadConverter.class);

    public RequestPayloadConverter(final DataShape dataShape) {
        super(dataShape.getKind());
    }

    public RequestPayloadConverter(final DataShapeKinds kind) {
        super(kind);
    }

    @Override
    void convertAsJson(final Message in) {
        final String body = bodyAsString(in);
        if (body == null) {
            return;
        }

        final JsonNode payload;
        try {
            payload = MAPPER.readTree(body);
        } catch (final IOException e) {
            LOG.warn("Unable to parse payload, continuing without conversion", e);

            return;
        }

        payload.with("parameters").fields().forEachRemaining(e -> in.setHeader(e.getKey(), e.getValue().asText()));

        final JsonNode requestBody = payload.get("body");
        if (requestBody == null) {
            in.setBody(null);
        } else {
            try {
                in.setBody(MAPPER.writeValueAsString(requestBody));
            } catch (final JsonProcessingException e) {
                LOG.warn("Unable to parse payload, continuing without conversion", e);

                return;
            }
        }
    }

    @Override
    void convertAsXml(final Message in) {
        final String body = bodyAsString(in);
        if (body == null) {
            return;
        }

        try {
            final XMLStreamReader bodyReader = XML_INPUT_FACTORY.createXMLStreamReader(new StringReader(body));

            final XMLStreamReader eventReader = XML_INPUT_FACTORY.createFilteredReader(bodyReader, new XmlPayloadProcessor(in.getHeaders()));

            final Source source = new StAXSource(eventReader);
            final ByteArrayOutputStream out = new ByteArrayOutputStream(body.length());
            final Result result = new StreamResult(out);

            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(source, result);

            in.setBody(new String(out.toByteArray(), StandardCharsets.UTF_8));
        } catch (XMLStreamException | TransformerFactoryConfigurationError | TransformerException e) {
            LOG.warn("Unable to parse payload, continuing without conversion", e);

            return;
        }

    }

}
