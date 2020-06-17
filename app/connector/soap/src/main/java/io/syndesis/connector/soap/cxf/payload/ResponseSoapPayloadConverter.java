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
package io.syndesis.connector.soap.cxf.payload;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.camel.InvalidPayloadException;
import org.apache.camel.Message;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.cxf.CxfPayload;
import org.apache.camel.component.cxf.converter.CachedCxfPayload;
import org.apache.camel.converter.stream.CachedOutputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.staxutils.StaxSource;
import org.apache.cxf.staxutils.StaxUtils;
import org.w3c.dom.Node;

public class ResponseSoapPayloadConverter extends AbstractSoapPayloadConverter {

    protected static final String SOAP_PREFIX = "soap";

    @Override
    protected void convertMessage(Message in) {
        try {
            // get SOAP QNames from CxfPayload headers
            final SoapMessage soapMessage = in.getHeader("CamelCxfMessage", SoapMessage.class);
            final SoapVersion soapVersion = soapMessage.getVersion();

            // get CxfPayload body
            final CxfPayload<?> cxfPayload = in.getMandatoryBody(CxfPayload.class);
            final List<?> headers = cxfPayload.getHeaders();
            final List<Source> body = cxfPayload.getBodySources();

            final OutputStream outputStream = newOutputStream(in, cxfPayload);
            final XMLStreamWriter writer = newXmlStreamWriter(outputStream);

            // serialize headers and body into an envelope
            writeStartEnvelopeAndHeaders(soapVersion, headers, writer);
            if (body != null && !body.isEmpty()) {
                writeBody(writer, body, soapVersion);
            }
            writer.writeEndDocument();

            final InputStream inputStream = getInputStream(outputStream, writer);

            // set the input stream as the Camel message body
            in.setBody(inputStream);

        } catch (InvalidPayloadException | XMLStreamException | IOException e) {
            throw new RuntimeCamelException("Error parsing CXF Payload: " + e.getMessage(), e);
        }
    }

    protected static void writeStartEnvelopeAndHeaders(SoapVersion soapVersion, List<?> headers, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartDocument();
        writer.writeStartElement(SOAP_PREFIX,  soapVersion.getEnvelope().getLocalPart(), soapVersion.getNamespace());
        if (headers != null && !headers.isEmpty()) {
            writeHeaders(writer, headers, soapVersion);
        }
    }

    protected static InputStream getInputStream(OutputStream outputStream, XMLStreamWriter writer) throws XMLStreamException, IOException {
        writer.close();
        outputStream.close();

        final InputStream inputStream;
        if (outputStream instanceof CachedOutputStream) {
            inputStream = ((CachedOutputStream) outputStream).getInputStream();
        } else {
            inputStream = ((ByteArrayOutputStream) outputStream).toInputStream();
        }
        return inputStream;
    }

    protected static XMLStreamWriter newXmlStreamWriter(OutputStream outputStream) throws XMLStreamException {
        // use XML stream to write arbitrarily large SOAP messages
        return XML_OUTPUT_FACTORY.createXMLStreamWriter(new StreamResult(outputStream));
    }

    protected static OutputStream newOutputStream(Message in, CxfPayload<?> cxfPayload) {
        // create byte stream for output
        final OutputStream outputStream;
        if (cxfPayload instanceof CachedCxfPayload) {
            // use Camel CachedOutputStream for large messages
            outputStream = new CachedOutputStream(in.getExchange());
        } else {
            outputStream = new ByteArrayOutputStream();
        }
        return outputStream;
    }

    protected static void writeHeaders(XMLStreamWriter writer, List<?> headers, SoapVersion soapVersion) throws XMLStreamException {
        writer.writeStartElement(SOAP_PREFIX,  soapVersion.getHeader().getLocalPart(), soapVersion.getNamespace());
        writePartSources(writer, headers.stream()
            .map(h -> h instanceof Source ? (Source) h : new DOMSource((Node)h))
            .collect(Collectors.toList()));
        writer.writeEndElement();
    }

    private static void writeBody(XMLStreamWriter writer, List<Source> body, SoapVersion soapVersion) throws XMLStreamException {
        writer.writeStartElement(SOAP_PREFIX,  soapVersion.getBody().getLocalPart(), soapVersion.getNamespace());
        writePartSources(writer, body);
        writer.writeEndElement();
    }

    private static void writePartSources(XMLStreamWriter writer, List<Source> body) throws XMLStreamException {
        for (Source bodyPart : body) {
            XMLEventReader partReader = getPartReader(bodyPart);
            while(partReader.hasNext()) {
                StaxUtils.writeEvent(partReader.nextEvent(), writer);
            }
        }
    }

    private static XMLEventReader getPartReader(Source part) throws XMLStreamException {
        final XMLEventReader eventReader;
        if (part instanceof StaxSource) {
            // CXF StaxSource needs special handling
            eventReader = XML_INPUT_FACTORY.createXMLEventReader(((StaxSource) part).getXMLStreamReader());
        } else {
            eventReader = XML_INPUT_FACTORY.createXMLEventReader(part);
        }
        return XML_INPUT_FACTORY.createFilteredReader(eventReader, new DocumentContentFilter());
    }

    private static class DocumentContentFilter implements EventFilter {

        @Override
        public boolean accept(XMLEvent event) {
            final boolean shouldAccept;

            switch (event.getEventType()) {
            case XMLStreamConstants.START_DOCUMENT:
            case XMLStreamConstants.END_DOCUMENT:
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                shouldAccept = false;
                break;
            default:
                shouldAccept = true;
                break;
            }
            return shouldAccept;
        }
    }
}
