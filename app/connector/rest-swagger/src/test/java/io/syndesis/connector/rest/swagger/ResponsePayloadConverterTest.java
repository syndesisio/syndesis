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

public class ResponsePayloadConverterTest {

    @Test
    public void shouldBeRobust() {
        final ResponsePayloadConverter converter = new ResponsePayloadConverter((DataShapeKinds) null);

        converter.process(createExhangeWithBody(null, null));
        converter.process(createExhangeWithBody(null, ""));
        converter.process(createExhangeWithBody(null, "<xml/>"));
        converter.process(createExhangeWithBody(null, "{}"));
        converter.process(createExhangeWithBody("application/xml", "<xml/>"));
        converter.process(createExhangeWithBody("application/json", "{}"));
    }

    @Test
    public void shouldConvert() {
        final ResponsePayloadConverter converter = new ResponsePayloadConverter(DataShapeKinds.JSON_SCHEMA);

        final Exchange exchange = createExhangeWithBody("application/json", "{\"hello\":\"world\"}");

        converter.process(exchange);

        assertThat(exchange.getIn().getBody()).isEqualTo("{\"parameters\":{\"Content-Type\":\"application/json\"},\"body\":{\"hello\":\"world\"}}");
    }
}
