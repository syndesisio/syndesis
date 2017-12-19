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
package io.syndesis.connector.swagger;

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
    public void shouldBeRobust() throws Exception {
        converter.process(createExhangeWithBody(""));
        converter.process(createExhangeWithBody("<xml/>"));
        converter.process(createExhangeWithBody(null));
    }

    @Test
    public void shouldConvertUnifiedToHeadersAndBody() throws Exception {
        final Exchange exchange = createExhangeWithBody(
            "{\"parameters\":{\"slug\":\"1\", \"tick\":\"tock\"},\"body\":{\"description\":\"hello\"}}");

        converter.process(exchange);

        assertThat(exchange.getIn().getHeader("slug")).isEqualTo("1");
        assertThat(exchange.getIn().getHeader("tick")).isEqualTo("tock");
        assertThat(exchange.getIn().getBody()).isEqualTo("{\"description\":\"hello\"}");
    }

    private static Exchange createExhangeWithBody(final String payload) {
        final Exchange exchange = new DefaultExchange(CONTEXT);

        final DefaultMessage in = new DefaultMessage(CONTEXT);
        exchange.setIn(in);

        in.setBody(payload);

        return exchange;
    }
}
