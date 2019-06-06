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
package io.syndesis.connector.support.processor;

import java.util.HashMap;

import io.syndesis.common.util.Json;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.spi.HeadersMapFactory;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpRequestUnwrapperProcessorTest {

    CamelContext camelContext = mock(CamelContext.class);

    Exchange exchange = new DefaultExchange(camelContext);

    HttpRequestUnwrapperProcessor processor = new HttpRequestUnwrapperProcessor(schema());

    @Before
    public void setupMocks() {
        final HeadersMapFactory factory = mock(HeadersMapFactory.class);
        when(camelContext.getHeadersMapFactory()).thenReturn(factory);
        when(factory.newMap()).thenReturn(new HashMap<>());
    }

    @Test
    public void shouldUnwrapArrayResponses() throws Exception {
        final Message in = new DefaultMessage(camelContext);
        exchange.setIn(in);
        in.setBody("{\"body\":[{\"b1\":\"c1\"},{\"b2\":\"c2\"}]}");

        processor.process(exchange);

        assertThat(in.getBody()).isEqualTo("[{\"b1\":\"c1\"},{\"b2\":\"c2\"}]");
    }

    @Test
    public void shouldUnwrapResponses() throws Exception {
        final Message in = new DefaultMessage(camelContext);
        exchange.setIn(in);
        in.setBody("{\"parameters\":{\"h1\":\"v1\",\"h3\":\"v3\"},\"body\":{\"b1\":\"c1\",\"b2\":\"c2\"}}");

        processor.process(exchange);

        assertThat(in.getHeaders()).containsOnly(entry("h1", "v1"), entry("h3", "v3"));
        assertThat(in.getBody()).isEqualTo("{\"b1\":\"c1\",\"b2\":\"c2\"}");
    }

    @Test
    public void simpleNonStringValuesShouldBeUnwrappedVerbatim() throws Exception {
        final Message in = new DefaultMessage(camelContext);
        exchange.setIn(in);
        in.setBody("{\"body\":123}");

        processor.process(exchange);

        assertThat(in.getHeaders()).containsOnly(entry(Exchange.CONTENT_TYPE, "text/plain"));
        assertThat(in.getBody()).isEqualTo("123");
    }

    @Test
    public void simpleValuesShouldBeUnwrappedVerbatim() throws Exception {
        final Message in = new DefaultMessage(camelContext);
        exchange.setIn(in);
        in.setBody("{\"body\":\"simple\"}");

        processor.process(exchange);

        assertThat(in.getHeaders()).containsOnly(entry(Exchange.CONTENT_TYPE, "text/plain"));
        assertThat(in.getBody()).isEqualTo("simple");
    }

    private static JsonNode schema() {
        final ObjectNode schema = Json.copyObjectMapperConfiguration().createObjectNode();
        final ObjectNode parameters = schema.putObject("properties").putObject("parameters").putObject("properties");
        parameters.putObject("h1");
        parameters.putObject("h2");
        parameters.putObject("h3");

        return schema;
    }
}
