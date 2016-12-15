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

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A helper class for creating Camel based assertions inside other test cases
 */
public abstract class TestRouteBuilder extends RouteBuilder {
    private static final transient Logger LOG = LoggerFactory.getLogger(TestRouteBuilder.class);

    protected MockEndpoint results;
    protected MockEndpoint errors;

    public MockEndpoint getResults() {
        return results;
    }

    public MockEndpoint getErrors() {
        return errors;
    }

    @Override
    public void configure() throws Exception {
        this.results = endpoint("mock:results", MockEndpoint.class);
        this.errors = endpoint("mock:errors", MockEndpoint.class);

        configureTest();
    }

    protected abstract void configureTest() throws Exception;

    /**
     * Asserts that all the expectations on the <code>results</code> and <code>errors</code> endpoints are met
     *
     * @return the results exchanges
     * @throws Exception if the assertion fails
     */
    public List<Exchange> assertIsSatisfied() throws Exception {
        results.assertIsSatisfied();
        errors.assertIsSatisfied();
        List<Exchange> results = this.results.getExchanges();
        logResults(results);
        return results;
    }

    protected void logResults(List<Exchange> results) {
        int idx = 0;
        for (Exchange result : results) {
            Message in = result.getIn();
            LOG.info("=> Result exchange: " + idx++ + " " + in.getBody() + " headers: " + in.getHeaders());
        }
    }
}
