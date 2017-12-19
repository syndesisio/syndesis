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
package io.syndesis.camel.component.proxy.runtime;

import java.util.Arrays;

import io.syndesis.integration.runtime.SyndesisRouteBuilder;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class ComponentProxyRuntimeSplitTest extends CamelTestSupport {

    // ***************************
    // Set up camel context
    // ***************************

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        final String path = getClass().getSimpleName() + "-" + getTestMethodName() + ".yaml";
        final RoutesBuilder builder = new SyndesisRouteBuilder("classpath:" + path);

        return builder;
    }

    // ***************************
    // Test
    // ***************************

    @Test
    public void testDefaultSplitWithCollection() throws Exception {
        final MockEndpoint mock = getMockEndpoint("mock:result");
        final String[] values = { "a","b","c" };

        mock.expectedMessageCount(values.length);
        mock.expectedBodiesReceived(values);

        template().sendBody("direct:start", Arrays.asList(values));

        mock.assertIsSatisfied();
    }

    @Test
    public void testDefaultSplitWithPojo() throws Exception {
        final MockEndpoint mock = getMockEndpoint("mock:result");
        final String value = "a";

        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived(value);

        template().sendBody("direct:start", value);

        mock.assertIsSatisfied();
    }

    @Test
    public void testTokenizerSplit() throws Exception {
        final MockEndpoint mock = getMockEndpoint("mock:result");
        final String values = "a,b,c";
        final String[] result = values.split(",");

        mock.expectedMessageCount(result.length);
        mock.expectedBodiesReceived(result);

        template().sendBody("direct:start", values);

        mock.assertIsSatisfied();
    }
}
