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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.cxf.CxfPayload;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.staxutils.W3CDOMStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static io.syndesis.connector.soap.cxf.payload.AbstractSoapPayloadConverter.XML_OUTPUT_FACTORY;

final class SoapPayloadReaderFilter implements EventFilter {

    private final SoapVersion soapVersion;
    private boolean inEnvelope;
    private boolean inHeader;
    private boolean inBody;

    private W3CDOMStreamWriter headersWriter;
    private final List<Source> bodyParts;

    // current body part
    private QName bodyPartName;
    private boolean inBodyPart;

    // current body part writer
    private XMLStreamWriter bodyPartWriter;
    private ByteArrayOutputStream bodyPartBytes;

    SoapPayloadReaderFilter(SoapVersion soapVersion) {
        this.bodyParts = new ArrayList<>();
        this.soapVersion = soapVersion;
    }

    @Override
    public boolean accept(XMLEvent event) {
        final int eventType = event.getEventType();

        switch (eventType) {
        case XMLStreamConstants.START_ELEMENT:
            processStartElement(event);
            break;
        case XMLStreamConstants.END_ELEMENT:
            processEndElement(event);
            break;
        default:
            break;
        }

        if (inHeader) {
            // copy headers to DOM
            processHeaderEvent(event);
        }

        if (inBodyPart) {
            // copy body part to byte array
            processBodyPartEvent(event);
        }

        // ignore all events in output, since this filter consumes them all
        return false;
    }

    private void processBodyPartEvent(XMLEvent event) {
        try {
            StaxUtils.writeEvent(event, bodyPartWriter);
        } catch (XMLStreamException e) {
            throw new RuntimeCamelException("Error reading SOAP Body: " + e.getMessage(), e);
        }
    }

    // copy events to headersWriter
    private void processHeaderEvent(XMLEvent event) {
        try {
            StaxUtils.writeEvent(event, headersWriter);
        } catch (XMLStreamException e) {
            throw new RuntimeCamelException("Error reading SOAP Header: " + e.getMessage(), e);
        }
    }

    private void processEndElement(final XMLEvent event) {
        final QName name = event.asEndElement().getName();

        if (soapVersion.getEnvelope().equals(name)) {
            inEnvelope = false;
        } else if (inEnvelope && soapVersion.getHeader().equals(name)) {
            inHeader = false;
            // copy header end element
            processHeaderEvent(event);
        } else if (inEnvelope && soapVersion.getBody().equals(name)) {
            inBody = false;
        }

        if (inBody && bodyPartName != null && bodyPartName.equals(name)) {
            // end of body part
            inBodyPart = false;
            closeBodyPart(event);
        }
    }

    private void closeBodyPart(XMLEvent event) {
        try {
            // copy the end element
            processBodyPartEvent(event);

            // close the writer and stream
            bodyPartWriter.close();
            bodyPartBytes.close();

            // add body part byte stream
            bodyParts.add(new StreamSource(bodyPartBytes.toInputStream()));

        } catch (XMLStreamException | IOException e) {
            throw new RuntimeCamelException("Error closing Body part: " + e.getMessage(), e);
        }
    }

    private void processStartElement(final XMLEvent event) {
        final QName name = event.asStartElement().getName();

        if (soapVersion.getEnvelope().equals(name)) {
            inEnvelope = true;
        } else if (inEnvelope && soapVersion.getHeader().equals(name)) {
            inHeader = true;
            // create a writer for headers
            this.headersWriter = new W3CDOMStreamWriter();
        } else if (inEnvelope && soapVersion.getBody().equals(name)) {
            inBody = true;
        }

        if (inBody && !inBodyPart) {
            inBodyPart = !soapVersion.getBody().equals(name);
            bodyPartName = name;
            bodyPartBytes = new ByteArrayOutputStream();
            try {
                bodyPartWriter = XML_OUTPUT_FACTORY.createXMLStreamWriter(new StreamResult(bodyPartBytes));
            } catch (XMLStreamException e) {
                throw new RuntimeCamelException(String.format("Error parsing body part %s: %s", bodyPartName,
                    e.getMessage()), e);
            }
        }
    }

    public CxfPayload<Source> getCxfPayload() {
        return new CxfPayload<>(getHeaders(), bodyParts, new HashMap<>());
    }

    private List<Source> getHeaders() {
        if (headersWriter == null) {
            return new ArrayList<>();
        }

        // get part elements from headers DOM
        final Document headers = headersWriter.getDocument();
        final NodeList soapHeader = headers.getElementsByTagNameNS(soapVersion.getNamespace(),
            soapVersion.getHeader().getLocalPart());
        if (soapHeader.getLength() != 1) {
            throw new RuntimeCamelException("Uexpected error, missing SOAP Header element");
        }
        final NodeList headerParts = soapHeader.item(0).getChildNodes();

        // get all child elements of soap header element as header parts
        final List<Source> headerElements = new ArrayList<>();
        for (int i = 0; i < headerParts.getLength(); i++) {
            final Node item = headerParts.item(i);
            if (item instanceof Element) {
                headerElements.add(new DOMSource(item));
            }
        }

        return headerElements;
    }
}
