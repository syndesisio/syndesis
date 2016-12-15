/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.funktion.agent.support;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Performs tests using a {@link TestRouteBuilder} which is useful for adding camel related tests inside other test cases
 */
public class CamelTester {
    private static final transient Logger LOG = LoggerFactory.getLogger(CamelTester.class);

    protected final TestRouteBuilder routeBuilder;
    protected final CamelContext camelContext;

    public CamelTester(TestRouteBuilder routeBuilder) {
        this(routeBuilder, new DefaultCamelContext());
    }

    public CamelTester(TestRouteBuilder routeBuilder, CamelContext camelContext) {
        this.routeBuilder = routeBuilder;
        this.camelContext = camelContext;
    }

    public static void assertIsSatisfied(TestRouteBuilder routeBuilder) throws Exception {
        CamelTester tester = new CamelTester(routeBuilder);
        tester.assertIsSatisfied();
    }

    public static void assertIsSatisfied(TestRouteBuilder routeBuilder, CamelContext camelContext) throws Exception {
        CamelTester tester = new CamelTester(routeBuilder, camelContext);
        tester.assertIsSatisfied();
    }

    /**
     * Asserts that the expectations in the <code>routeBuilder</code> are satisfied
     *
     * @return the result exchanges
     * @throws Exception if the assertions could not be satisfied
     */
    public List<Exchange> assertIsSatisfied() throws Exception {
        camelContext.addRoutes(routeBuilder);

        try {
            camelContext.start();
            return routeBuilder.assertIsSatisfied();
        } catch (Throwable e) {
            LOG.error("Failed: " + e, e);
            throw e;
        } finally {
            camelContext.stop();
        }
    }
}
