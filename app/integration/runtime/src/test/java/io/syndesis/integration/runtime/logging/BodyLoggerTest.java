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

package io.syndesis.integration.runtime.logging;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.converter.stream.InputStreamCache;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class BodyLoggerTest {

    public static Stream<Arguments> data() {
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of("SimpleBody", "SimpleBody"),
            Arguments.of(new String[] {"a", "b", "c"}, "[a, b, c]"),
            Arguments.of(Arrays.asList("a", "b", "c"), "[a, b, c]"),
            Arguments.of(
                Arrays.asList(new InputStreamCache("Hello".getBytes(StandardCharsets.UTF_8)), new InputStreamCache("World".getBytes(StandardCharsets.UTF_8))),
                "[Hello, World]"),
            Arguments.of(new InputStreamCache[] {new InputStreamCache("Hello".getBytes(StandardCharsets.UTF_8)),
                new InputStreamCache("World".getBytes(StandardCharsets.UTF_8))}, "[Hello, World]"),
            Arguments.of(new InputStreamCache("Hello World".getBytes(StandardCharsets.UTF_8)), "Hello World"),
            Arguments.of(new GroupedExchangeList("a", "b", "c"), "[a, b, c]"));
    }

    @ParameterizedTest(name = "{2}")
    @MethodSource("data")
    public void testDefaultLogger(Object body, String logResult) {
        CamelContext context = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody(body);

        Assertions.assertEquals(logResult, new BodyLogger.Default().log(exchange));
    }

    private static class GroupedExchangeList extends ArrayList<String> {
        public GroupedExchangeList(String ... items) {
            super(Arrays.asList(items));
        }

        @Override
        public String toString() {
            return "overwrite";
        }
    }
}
