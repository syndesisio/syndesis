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

import javax.xml.stream.XMLInputFactory;

import io.syndesis.common.model.DataShapeKinds;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.fasterxml.jackson.databind.ObjectMapper;

abstract class PayloadConverterBase implements Processor {
    static final ObjectMapper MAPPER = new ObjectMapper();

    static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();

    private final DataShapeKinds kind;

    PayloadConverterBase(final DataShapeKinds kind) {
        this.kind = kind;
    }

    @Override
    public void process(final Exchange exchange) {
        if (kind == null) {
            return;
        }

        final Message in = exchange.getIn();

        switch (kind) {
        case JSON_INSTANCE:
        case JSON_SCHEMA:
            convertAsJson(in);
            break;
        case XML_INSTANCE:
        case XML_SCHEMA:
        case XML_SCHEMA_INSPECTED:
            convertAsXml(in);
            break;
        default:
            // perform no conversion
            break;
        }

    }

    abstract void convertAsJson(Message in);

    abstract void convertAsXml(Message in);

    static String bodyAsString(final Message in) {
        final String body = in.getBody(String.class);
        if (body == null) {
            return null;
        }
        return body;
    }

}
