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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.xml.soap.Node;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.camel.InvalidPayloadException;
import org.apache.camel.Message;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.cxf.CxfPayload;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.headers.Header;

public final class RequestSoapPayloadConverter extends AbstractSoapPayloadConverter {

    private final SoapVersion soapVersion;

    public RequestSoapPayloadConverter(SoapVersion soapVersion) {
        this.soapVersion = soapVersion;
    }

    @Override
    protected void convertMessage(final Message in) {
        try {
            final Source body = bodyAsSource(in);

            // extract body as stream, and headers as elements in single DOM
            final XMLEventReader bodyReader = XML_INPUT_FACTORY.createXMLEventReader(body);
            final SoapPayloadReaderFilter payloadFilter = new SoapPayloadReaderFilter(soapVersion);
            final XMLEventReader eventReader = XML_INPUT_FACTORY.createFilteredReader(bodyReader, payloadFilter);

            // all the work is done in the filter, so we ignore the event writer output
            try (OutputStream bos = new ByteArrayOutputStream()) {
                XMLEventWriter target = XML_OUTPUT_FACTORY.createXMLEventWriter(bos);
                target.add(eventReader);
            }

            // convert filtered parts to CxfPayload
            final CxfPayload<Source> cxfPayload = payloadFilter.getCxfPayload();

            // add existing SOAP headers
            final List<?> existingHeaders = (List<?>) in.getHeader(Header.HEADER_LIST);
            if (existingHeaders != null) {
                final List<Source> headers = cxfPayload.getHeaders();
                for (Object header : existingHeaders) {
                    if (header instanceof Source) {
                        headers.add((Source) header);
                    } else {
                        // wrap dom node
                        headers.add(new DOMSource((Node)header));
                    }
                }
            }

            in.setBody(cxfPayload);

        } catch (XMLStreamException | InvalidPayloadException | IOException e) {
            throw new RuntimeCamelException("Error creating SOAP message from request message: " + e.getMessage(), e);
        }
    }

    static Source bodyAsSource(final Message in) throws InvalidPayloadException {
        return new StreamSource(in.getMandatoryBody(InputStream.class));
    }

}
