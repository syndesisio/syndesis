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
import java.util.Arrays;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.cxf.CxfPayload;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.staxutils.StaxUtils;
import org.assertj.core.api.Assertions;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;

import io.syndesis.connector.soap.cxf.payload.RequestSoapPayloadConverter;
import io.syndesis.connector.soap.cxf.payload.ResponseSoapPayloadConverter;

import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

@RunWith(Parameterized.class)
public class SoapPayloadConverterTest extends CamelTestSupport {

    private RequestSoapPayloadConverter requestConverter;
    private ResponseSoapPayloadConverter responseConverter;

    @Parameter
    public String fileName;

    private InputStream inputStream;

    @Parameters(name = "{index}: {0}")
    public static List<String> getPayloadFileNames() {
        return Arrays.asList("/sayHi.xml", "/getTradePrice.xml");
    }

    @Before
    public void setup() {
        requestConverter = new RequestSoapPayloadConverter(Soap12.getInstance());
        responseConverter = new ResponseSoapPayloadConverter();
        inputStream = SoapPayloadConverterTest.class.getResourceAsStream(fileName);
    }

    @Test
    public void convertXmlToCxfToXml() throws IOException, XMLStreamException {

        // read XML bytes as an XML Document
        final ByteArrayInputStream bis = new ByteArrayInputStream(IOUtils.readBytesFromStream(inputStream));
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

        assertIsInstanceOf(InputStream.class, in.getBody());
        Document response = StaxUtils.read((InputStream) in.getBody());

        XMLUnit.setIgnoreAttributeOrder(true);
        assertThat(response, isSimilarTo(request).ignoreWhitespace());
    }

    @Test
    public void convertXmlToValidCxfPayload() throws IOException, XMLStreamException {

        // read XML bytes as an XML Document
        ByteArrayInputStream bis = new ByteArrayInputStream(IOUtils.readBytesFromStream(inputStream));
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

    private static void validateXml(Source part) throws XMLStreamException {
        // try to write the part using an XML stream writer
        StaxUtils.copy(part, new ByteArrayOutputStream());
    }
}
