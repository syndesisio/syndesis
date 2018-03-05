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

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PayloadConverterTest {

    private static final CamelContext CONTEXT = new DefaultCamelContext();

    private final PayloadConverter converter = new PayloadConverter();

    @Test
    public void shouldBeRobust() {
        converter.process(createExhangeWithBody(null, null));
        converter.process(createExhangeWithBody(null, ""));
        converter.process(createExhangeWithBody(null, "<xml/>"));
        converter.process(createExhangeWithBody(null, "{}"));
        converter.process(createExhangeWithBody("application/xml", "<xml/>"));
        converter.process(createExhangeWithBody("application/json", "{}"));
    }

    @Test
    public void shouldConvertUnifiedJsonToHeadersAndBody() {
        final Exchange exchange = createExhangeWithBody("application/json",
            "{\"parameters\":{\"slug\":\"1\", \"tick\":\"tock\"},\"body\":{\"description\":\"hello\"}}");

        converter.process(exchange);

        assertThat(exchange.getIn().getHeader("slug")).isEqualTo("1");
        assertThat(exchange.getIn().getHeader("tick")).isEqualTo("tock");
        assertThat(exchange.getIn().getBody()).isEqualTo("{\"description\":\"hello\"}");
    }

    @Test
    public void shouldConvertUnifiedXmlToHeadersAndBody() {
        final Exchange exchange = createExhangeWithBody("application/xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<r:request xmlns:r=\"http://syndesis.io/v1/swagger-connector-template/request\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + //
                "  <p:parameters xmlns:p=\"http://syndesis.io/v1/swagger-connector-template/parameters\">" + //
                "    <p:slug>1</p:slug>" + //
                "    <p:tick>tock</p:tick>" + //
                "  </p:parameters>" + //
                "  <r:body>\n" + //
                "    <description>hello</description>\n" + //
                "  </r:body>\n" + //
                "</r:request>\n");

        converter.process(exchange);

        assertThat(exchange.getIn().getHeader("slug")).isEqualTo("1");
        assertThat(exchange.getIn().getHeader("tick")).isEqualTo("tock");
        assertThat((String) exchange.getIn().getBody())
            .isXmlEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<description>hello</description>");
    }

    private static Exchange createExhangeWithBody(final String contentType, final String payload) {
        final Exchange exchange = new DefaultExchange(CONTEXT);

        final DefaultMessage in = new DefaultMessage(CONTEXT);
        in.setHeader(Exchange.CONTENT_TYPE, contentType);
        exchange.setIn(in);

        in.setBody(payload);

        return exchange;
    }
}
