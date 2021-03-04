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
package io.syndesis.connector.soap.cxf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;

import io.syndesis.connector.soap.cxf.payload.RequestSoapPayloadConverter;
import io.syndesis.connector.soap.cxf.payload.ResponseSoapPayloadConverter;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.cxf.CxfPayload;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.staxutils.StaxUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.xmlunit.assertj.XmlAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class SoapPayloadConverterTest {

    private final RequestSoapPayloadConverter requestConverter = new RequestSoapPayloadConverter(Soap12.getInstance());
    private final ResponseSoapPayloadConverter responseConverter = new ResponseSoapPayloadConverter();
    private static final CamelContext CONTEXT = new DefaultCamelContext();

    @ParameterizedTest(name = "{index}: {0}")
    @ValueSource(strings = { "/sayHi.xml", "/getTradePrice.xml" })
    public void convertXmlToCxfToXml(final String fileName) throws IOException, XMLStreamException {
        final ByteArrayInputStream bis;
        try (InputStream inputStream = SoapPayloadConverterTest.class.getResourceAsStream(fileName)) {
            bis = new ByteArrayInputStream(IOUtils.readBytesFromStream(inputStream));
        }

        // read XML bytes as an XML Document
        final Document request = StaxUtils.read(bis);
        bis.reset();

        // convert XML to CxfPayload
        final Exchange exchange = createExchangeWithBody(bis);
        final Message in = exchange.getIn();
        requestConverter.process(exchange);

        Assertions.assertThat(in.getBody()).isInstanceOf(CxfPayload.class);

        // convert CxfPayload back to XML
        final SoapMessage soapMessage = new SoapMessage(Soap12.getInstance());
        in.setHeader("CamelCxfMessage", soapMessage);
        responseConverter.process(exchange);

        assertThat(in.getBody()).isInstanceOf(InputStream.class);
        Document response = StaxUtils.read((InputStream) in.getBody());

        XmlAssert.assertThat(response).and(request).ignoreWhitespace().areSimilar();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @ValueSource(strings = { "/sayHi.xml", "/getTradePrice.xml" })
    public void convertXmlToValidCxfPayload(final String fileName) throws IOException, XMLStreamException {
        final ByteArrayInputStream bis;
        try (InputStream inputStream = SoapPayloadConverterTest.class.getResourceAsStream(fileName)) {
            bis = new ByteArrayInputStream(IOUtils.readBytesFromStream(inputStream));
        }

        // read XML bytes as an XML Document
        StaxUtils.read(bis);
        bis.reset();

        // convert XML to CxfPayload
        final Exchange exchange = createExchangeWithBody(bis);
        final Message in = exchange.getIn();
        requestConverter.process(exchange);

        final Object body = in.getBody();
        Assertions.assertThat(body).isInstanceOf(CxfPayload.class);
        @SuppressWarnings("unchecked")
        final CxfPayload<Source> cxfPayload = (CxfPayload<Source>) body;

        // validate every header and body part XML
        for (Source headerPart : cxfPayload.getHeaders()) {
            validateXml(headerPart);
        }
        for (Source bodyPart : cxfPayload.getBodySources()) {
            validateXml(bodyPart);
        }
    }


    static Exchange createExchangeWithBody(ByteArrayInputStream bis) {
        final DefaultExchange exchange = new DefaultExchange(CONTEXT);
        final Message in = exchange.getIn();
        in.setBody(bis);

        return exchange;
    }

    static void validateXml(Source part) throws XMLStreamException, IOException {
        // try to write the part using an XML stream writer
        try (OutputStream nowhere = new NullOutputStream()){
            StaxUtils.copy(part, nowhere);
        }
    }
}
