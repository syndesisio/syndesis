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
package io.syndesis.integration.runtime.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.syndesis.common.util.IOStreams;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.syndesis.integration.runtime.util.JsonSimplePredicate.convertSimpleToOGNLForMaps;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class JsonSimplePredicateTest {

    public static class Bean {
        final int prop = 1;

        public int getProp() {
            return prop;
        }
    }

    private static final DefaultCamelContext CONTEXT = new DefaultCamelContext();

    @Test
    @Parameters({"2 == 1, 2 == 1", //
        "${body.prop} == 1, ${body[prop]} == 1", //
        "${body.prop} == 1 OR ${body.fr_op.gl$op.ml0op[3]} == '2.4', ${body[prop]} == 1 OR ${body[fr_op][gl$op][ml0op][3]} == '2.4'"})
    public void shouldConvertSimpleExpressionsToOgnl(final String simple, final String ognl) {
        assertThat(convertSimpleToOGNLForMaps(simple)).isEqualTo(ognl);
    }

    @Test
    @Parameters({"body.prop, body[prop]", //
        "body.size(), body.size()", //
        "body[3], body[3]", //
        "body[3].prop, body[3][prop]", //
        "body.fr_op.gl$op.ml0op[3], body[fr_op][gl\\$op][ml0op][3]"})
    public void shouldConvertSimpleToOgnl(final String simple, final String ognl) {
        final Matcher matcher = Pattern.compile("(.*)").matcher(simple);
        matcher.find();

        assertThat(JsonSimplePredicate.toOgnl(matcher)).isEqualTo(ognl);
    }

    @Test
    public void shouldFilterByDefaultForJavaBeanInput() {
        final JsonSimplePredicate predicate = new JsonSimplePredicate("${body.prop} == 1", CONTEXT);
        assertThat(predicate.matches(exchangeWith(new Bean()))).isEqualTo(true);
    }

    @Test
    public void shouldFilterByDefaultForNonJsonInput() {
        final JsonSimplePredicate predicate = new JsonSimplePredicate("${body} == 1", CONTEXT);
        assertThat(predicate.matches(exchangeWith("1"))).isEqualTo(true);
        assertThat(predicate.matches(exchangeWith("wat"))).isEqualTo(false);
    }

    @Test
    @Parameters({"{}, false", "{\"prop\": 1}, true", "{\"prop\": 2}, false"})
    public void shouldFilterInputStreams(final String payload, final boolean expected) throws IOException {
        SingleReadInputStream inputStream = new SingleReadInputStream(payload);
        Exchange exchange = exchangeWith(inputStream);
        final JsonSimplePredicate predicate = new JsonSimplePredicate("${body.prop} == 1", CONTEXT);
        assertThat(predicate.matches(exchange)).isEqualTo(expected);
        assertThat(inputStream.isClosed()).isTrue();
        assertThat(IOStreams.readText(exchange.getIn().getBody(InputStream.class))).isEqualTo(payload);
    }

    @Test
    @Parameters({"{}, false", "{\"prop\": 1}, true", "{\"prop\": 2}, false"})
    public void shouldFilterOnJsonStrings(final String payload, final boolean expected) {
        final JsonSimplePredicate predicate = new JsonSimplePredicate("${body.prop} == 1", CONTEXT);
        assertThat(predicate.matches(exchangeWith(payload))).isEqualTo(expected);
    }

    @Test
    @Parameters({"[], false", "[{\"prop\": 1}], true", "[{\"prop\": 2}], false"})
    public void shouldFilterOnJsonArrayStrings(final String payload, final boolean expected) {
        final JsonSimplePredicate predicate = new JsonSimplePredicate("${body.size()} == 1 && ${body[0].prop} == 1", CONTEXT);
        assertThat(predicate.matches(exchangeWith(payload))).isEqualTo(expected);
    }

    @Test
    public void shouldFilterOnListOfJsonStrings() {
        final JsonSimplePredicate predicate = new JsonSimplePredicate("${body[0].prop} == 1", CONTEXT);
        assertThat(predicate.matches(exchangeWith(Collections.emptyList()))).isEqualTo(false);
        assertThat(predicate.matches(exchangeWith(Arrays.asList("{\"prop\": 1}", "{\"prop\": 2}")))).isEqualTo(true);
        assertThat(predicate.matches(exchangeWith(Arrays.asList("{\"prop\": 3}", "{\"prop\": 4}")))).isEqualTo(false);
    }

    private static Exchange exchangeWith(final Object body) {
        final DefaultExchange exchange = new DefaultExchange(CONTEXT);
        final DefaultMessage payload = new DefaultMessage(CONTEXT);
        payload.setBody(body);
        exchange.setIn(payload);

        return exchange;
    }

    private static class SingleReadInputStream extends InputStream {
        private boolean closed = false;
        private final InputStream delegate;

        SingleReadInputStream(String data) {
            this.delegate = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void close() throws IOException {
            super.close();
            closed = true;
        }

        @Override
        public int read() throws IOException {
            if (closed) {
                throw new IllegalStateException("Read after close");
            }

            return delegate.read();
        }

        public boolean isClosed() {
            return closed;
        }
    }
}
