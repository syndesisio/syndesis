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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.integration.runtime.util.SyndesisHeaderStrategy;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestHeaderSetterTest {

    @ParameterizedTest
    @EnumSource(DataShapeKinds.class)
    void shouldDetermineContentType(final DataShapeKinds kind) {
        final DataShape shape = new DataShape.Builder()
            .kind(kind)
            .build();

        assertThat(RequestHeaderSetter.determineContentTypeOf(shape)).satisfiesAnyOf(
            c -> assertThat(c).isEqualTo("application/json"),
            c -> assertThat(c).isEqualTo("text/xml"));
    }

    @ParameterizedTest
    @CsvSource({
        "JSON_INSTANCE,        XML_INSTANCE,  application/json, text/xml",
        "JSON_SCHEMA,          JSON_SCHEMA,   application/json, application/json",
        "XML_INSTANCE,         XML_SCHEMA,    text/xml,         text/xml",
        "XML_SCHEMA,           NONE,          text/xml,         application/json",
        "XML_SCHEMA_INSPECTED, JSON_INSTANCE, text/xml,         application/json"
    })
    void shouldPassStandardHeaders(final DataShapeKinds requestContentType, final DataShapeKinds responseContentType, final String expectedRequestContentType,
        final String expectedResponseContenType) throws Exception {
        final DataShape inShape = new DataShape.Builder()
            .kind(requestContentType)
            .build();

        final DataShape outShape = new DataShape.Builder()
            .kind(responseContentType)
            .build();

        final RequestHeaderSetter requestHeaderSetter = new RequestHeaderSetter(inShape, outShape);

        final Exchange exchange = mock(Exchange.class);
        final HashSet<Object> allowed = new HashSet<>();
        when(exchange.getProperty(SyndesisHeaderStrategy.ALLOWED_HEADERS, Collection.class)).thenReturn(allowed);

        final Message message = mock(Message.class);
        when(exchange.getIn()).thenReturn(message);
        final HashMap<String, Object> headers = new HashMap<>();
        when(message.getHeaders()).thenReturn(headers);

        requestHeaderSetter.process(exchange);

        assertThat(headers).contains(
            entry("Content-Type", expectedRequestContentType),
            entry("Accept", expectedResponseContenType));

        assertThat(allowed).contains("Content-Type", "Accept");
    }

}
