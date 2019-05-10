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
package io.syndesis.connector.rest.swagger.auth;

import io.syndesis.integration.runtime.util.SyndesisHeaderStrategy;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SetAuthorizationHeaderTest {

    @Test
    public void shouldSetHeaderAndWhitelist() throws Exception {
        final Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        new SetAuthorizationHeader("value").process(exchange);

        assertThat(exchange.getIn().getHeader("Authorization")).isEqualTo("value");
        assertThat(SyndesisHeaderStrategy.isWhitelisted(exchange, "Authorization")).isTrue();
    }

}
