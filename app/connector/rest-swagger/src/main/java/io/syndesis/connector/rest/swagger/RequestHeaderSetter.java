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

import java.util.HashMap;
import java.util.Map;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.integration.runtime.util.SyndesisHeaderStrategy;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

final class RequestHeaderSetter implements Processor {

    private static final String DEFAULT_CONTENT_TYPE = "application/json";

    private final Map<String, String> httpHeaders = new HashMap<>();

    RequestHeaderSetter(final DataShape inputDataShape, final DataShape outputDataShape) {
        httpHeaders.put("Content-Type", determineContentTypeOf(inputDataShape));
        httpHeaders.put("Accept", determineContentTypeOf(outputDataShape));
    }

    @Override
    public void process(final Exchange exchange) throws Exception {
        final Message in = exchange.getIn();
        final Map<String, Object> headers = in.getHeaders();
        headers.putAll(httpHeaders);

        SyndesisHeaderStrategy.whitelist(exchange, httpHeaders.keySet());
    }

    private static String determineContentTypeOf(final DataShape outputDataShape) {
        if (outputDataShape == null) {
            return DEFAULT_CONTENT_TYPE;
        }

        final DataShapeKinds kind = outputDataShape.getKind();
        switch (kind) {
        case JSON_INSTANCE:
        case JSON_SCHEMA:
            return "application/json";
        case XML_INSTANCE:
        case XML_SCHEMA:
        case XML_SCHEMA_INSPECTED:
            return "text/xml";
        default:
            return DEFAULT_CONTENT_TYPE;
        }
    }
}
