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

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;

import io.syndesis.common.util.json.JsonUtils;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.spi.HeadersMapFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HttpRequestWrapperProcessorTest {

    private static final String PARAMS_AND_BODY = "{\"parameters\":{\"param1\":\"param_value1\",\"param2\":[\"param_value2_1\",\"param_value2_2\"]},\"body\":{\"body1\":\"body_value1\",\"body2\":\"body_value2\"}}";

    private static final String ONLY_PARAMS = "{\"parameters\":{\"param1\":\"param_value1\",\"param2\":[\"param_value2_1\",\"param_value2_2\"]}}";

    final ObjectSchema schema = new ObjectSchema();

    public HttpRequestWrapperProcessorTest() {
        final ObjectSchema parameters = new ObjectSchema();
        parameters.putProperty("param1", JsonSchema.minimalForFormat(JsonFormatTypes.STRING));
        parameters.putProperty("param2", JsonSchema.minimalForFormat(JsonFormatTypes.STRING));
        schema.putProperty("parameters", parameters);

        final ObjectSchema body = new ObjectSchema();
        body.putProperty("body1", JsonSchema.minimalForFormat(JsonFormatTypes.STRING));
        body.putProperty("body2", JsonSchema.minimalForFormat(JsonFormatTypes.STRING));
        schema.putProperty("body", body);
    }

    public static Collection<Object[]> cases() {
        return Arrays.asList(
            new Object[] {null, ONLY_PARAMS},
            new Object[] {"", ONLY_PARAMS},
            new Object[] {"{\"body1\":\"body_value1\",\"body2\":\"body_value2\"}", PARAMS_AND_BODY},
            new Object[] {new ByteArrayInputStream(new byte[0]), ONLY_PARAMS}
        );

    }

    @ParameterizedTest
    @MethodSource("cases")
    public void shouldMapValuesFromMessageHeaders(final Object givenBody, final Object replacedBody) throws Exception {
        String schemaStr = JsonUtils.writer().forType(JsonSchema.class).writeValueAsString(schema);
        JsonNode schemaNode = JsonUtils.reader().forType(JsonNode.class).readTree(schemaStr);
        final HttpRequestWrapperProcessor processor = new HttpRequestWrapperProcessor(schemaNode);

        final Exchange exchange = mock(Exchange.class);
        final Message message = mock(Message.class);
        final CamelContext camelContext = mock(CamelContext.class);
        when(camelContext.getHeadersMapFactory()).thenReturn(mock(HeadersMapFactory.class));
        when(exchange.getIn()).thenReturn(message);
        when(exchange.getContext()).thenReturn(camelContext);
        when(message.getBody()).thenReturn(givenBody);
        when(message.getHeader("param1", String[].class)).thenReturn(new String[] {"param_value1"});
        when(message.getHeader("param2", String[].class)).thenReturn(new String[] {"param_value2_1", "param_value2_2"});

        processor.process(exchange);

        final ArgumentCaptor<Message> replacement = ArgumentCaptor.forClass(Message.class);
        verify(exchange).setIn(replacement.capture());
        assertThat(replacement.getValue().getBody()).isEqualTo(replacedBody);
    }

    @ParameterizedTest
    @MethodSource("cases")
    public void shouldMapValuesFromHttpRequest(final Object givenBody, final Object replacedBody) throws Exception {
        final String schemaStr = JsonUtils.writer().forType(JsonSchema.class).writeValueAsString(schema);
        final JsonNode schemaNode = JsonUtils.reader().forType(JsonNode.class).readTree(schemaStr);
        final HttpRequestWrapperProcessor processor = new HttpRequestWrapperProcessor(schemaNode);

        final Exchange exchange = mock(Exchange.class);
        final Message message = mock(Message.class);
        final CamelContext camelContext = mock(CamelContext.class);
        when(camelContext.getHeadersMapFactory()).thenReturn(mock(HeadersMapFactory.class));
        when(exchange.getIn()).thenReturn(message);
        when(exchange.getContext()).thenReturn(camelContext);
        final HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(message.getHeader(Exchange.HTTP_SERVLET_REQUEST, HttpServletRequest.class)).thenReturn(servletRequest);
        when(message.getBody()).thenReturn(givenBody);
        when(servletRequest.getParameterValues("param1")).thenReturn(new String[] {"param_value1"});
        when(servletRequest.getParameterValues("param2")).thenReturn(new String[] {"param_value2_1", "param_value2_2"});

        processor.process(exchange);

        final ArgumentCaptor<Message> replacement = ArgumentCaptor.forClass(Message.class);
        verify(exchange).setIn(replacement.capture());
        assertThat(replacement.getValue().getBody()).isEqualTo(replacedBody);
    }
}
