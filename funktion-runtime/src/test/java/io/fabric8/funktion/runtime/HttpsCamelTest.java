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
package io.fabric8.funktion.runtime;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 */
public class HttpsCamelTest extends CamelTestSupport {
    private static final transient Logger LOG = LoggerFactory.getLogger(HttpsCamelTest.class);

    @EndpointInject(uri = "mock:results")
    protected MockEndpoint results;

    @Override
    public boolean isDumpRouteCoverage() {
        return true;
    }

    // TODO
    @Ignore
    public void testInvokeHTTPS() throws Exception {
        results.expectedMessageCount(1);
        results.assertIsSatisfied();

        List<Exchange> exchanges = results.getExchanges();
        Assertions.assertThat(exchanges.size()).isGreaterThan(0);
        Exchange exchange = exchanges.get(0);
        Message in = exchange.getIn();
        String json = in.getBody(String.class);
        LOG.info("Received: " + json);
    }


    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("timer://foo?period=5000").to("https4://jsonplaceholder.typicode.com/posts").to("mock:results");
            }
        };
    }

}