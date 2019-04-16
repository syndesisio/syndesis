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

import io.syndesis.common.model.DataShapeKinds;

import org.apache.camel.Exchange;
import org.junit.Test;

import static io.syndesis.connector.rest.swagger.PayloadConverterHelper.createExhangeWithBody;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestPayloadConverterTest {

    @Test
    public void shouldBeRobust() {
        final RequestPayloadConverter converter = new RequestPayloadConverter((DataShapeKinds) null);

        converter.process(createExhangeWithBody(null, null));
        converter.process(createExhangeWithBody(null, ""));
        converter.process(createExhangeWithBody(null, "<xml/>"));
        converter.process(createExhangeWithBody(null, "{}"));
        converter.process(createExhangeWithBody("application/xml", "<xml/>"));
        converter.process(createExhangeWithBody("application/json", "{}"));
    }

    @Test
    public void shouldConvertUnifiedJsonToHeadersAndBody() {
        final RequestPayloadConverter converter = new RequestPayloadConverter(DataShapeKinds.JSON_SCHEMA);

        final Exchange exchange = createExhangeWithBody("application/json",
            "{\"parameters\":{\"slug\":\"1\", \"tick\":\"tock\"},\"body\":{\"description\":\"hello\"}}");

        converter.process(exchange);

        assertThat(exchange.getIn().getHeader("slug")).isEqualTo("1");
        assertThat(exchange.getIn().getHeader("tick")).isEqualTo("tock");
        assertThat(exchange.getIn().getBody()).isEqualTo("{\"description\":\"hello\"}");
    }

    @Test
    public void shouldConvertUnifiedXmlToHeadersAndBody() {
        final RequestPayloadConverter converter = new RequestPayloadConverter(DataShapeKinds.XML_SCHEMA);

        final Exchange exchange = createExhangeWithBody("application/xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<r:request xmlns:r=\"http://syndesis.io/v1/swagger-connector-template/request\" "
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
        assertThat((String) exchange.getIn().getBody()).isXmlEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<description>hello</description>");
    }

}
