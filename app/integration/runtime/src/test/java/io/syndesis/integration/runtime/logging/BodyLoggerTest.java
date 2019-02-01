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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.converter.stream.InputStreamCache;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * @author Christoph Deppisch
 */
@RunWith(Parameterized.class)
public class BodyLoggerTest {

    private final Object body;
    private final String logResult;

    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    public BodyLoggerTest(Object body, String logResult) {
        this.body = body;
        this.logResult = logResult;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { null, null },
                { "SimpleBody", "SimpleBody" },
                { new String[] {"a", "b", "c"}, "[a, b, c]" },
                { Arrays.asList("a", "b", "c"), "[a, b, c]" },
                { Arrays.asList(new InputStreamCache("Hello".getBytes(CHARSET_UTF8)), new InputStreamCache("World".getBytes(CHARSET_UTF8))), "[Hello, World]" },
                { new InputStreamCache[] {new InputStreamCache("Hello".getBytes(CHARSET_UTF8)), new InputStreamCache("World".getBytes(CHARSET_UTF8))}, "[Hello, World]" },
                { new InputStreamCache("Hello World".getBytes(CHARSET_UTF8)), "Hello World" },
                { new GroupedExchangeList("a", "b", "c"), "[a, b, c]" }
        });
    }

    @Test
    public void testDefaultLogger() {
        CamelContext context = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody(body);

        Assert.assertEquals(logResult, new BodyLogger.Default().log(exchange));
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
