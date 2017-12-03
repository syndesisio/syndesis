/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.integration.runtime.components.json;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;

/**
 */
public class JsonEndpointTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @Override
    public boolean isDumpRouteCoverage() {
        return true;
    }

    @Test
    public void testSendBeanWhichShouldMarshalToJSON() throws Exception {
        assertSendBodyReceivesCorrectPayload(new SampleBean("James", 10), "{\"name\":\"James\",\"size\":10}");
    }

    @Test
    public void testSendJSONStringWhichIsAlreadyJSON() throws Exception {
        String expectedBody = "{\"name\":\"James\",\"size\":10}";
        assertSendBodyReceivesCorrectPayload(expectedBody, expectedBody);
    }

    @Test
    public void testSendJSONBytesWhichIsAlreadyJSON() throws Exception {
        String expectedBody = "{\"name\":\"James\",\"size\":10}";
        assertSendBodyReceivesCorrectPayload(expectedBody.getBytes(), expectedBody);
    }

    @Test
    public void testSendJSONReaderWhichIsAlreadyJSON() throws Exception {
        String expectedBody = "{\"name\":\"James\",\"size\":10}";
        assertSendBodyReceivesCorrectPayload(new StringReader(expectedBody), expectedBody);
    }

    @Test
    public void testSendJSONInputStreamWhichIsAlreadyJSON() throws Exception {
        String expectedBody = "{\"name\":\"James\",\"size\":10}";
        byte[] data = expectedBody.getBytes();
        assertSendBodyReceivesCorrectPayload(new ByteArrayInputStream(data), expectedBody);
    }

    @Test
    public void testSendXMLWhichShouldPassThroughUnchanged() throws Exception {
        String xmlContentType = "text/xml";
        String expectedBody = "<foo>bar</foo>";

        resultEndpoint.expectedBodiesReceived(expectedBody);
        resultEndpoint.allMessages().header(Exchange.CONTENT_TYPE).isEqualTo(xmlContentType);

        template.sendBodyAndHeader(expectedBody, Exchange.CONTENT_TYPE, xmlContentType);

        resultEndpoint.assertIsSatisfied();
    }

    protected void assertSendBodyReceivesCorrectPayload(Object inputBody, String expectedBody) throws InterruptedException {
        resultEndpoint.expectedBodiesReceived(expectedBody);
        resultEndpoint.allMessages().header(Exchange.CONTENT_TYPE).isEqualTo(JsonEndpoint.JSON_CONTENT_TYPE);

        template.sendBody(inputBody);

        resultEndpoint.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start").to("json:marshal").convertBodyTo(String.class).to("mock:result");
            }
        };
    }

}