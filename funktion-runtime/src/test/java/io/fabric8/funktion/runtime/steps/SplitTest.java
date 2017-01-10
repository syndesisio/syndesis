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
package io.fabric8.funktion.runtime.steps;

import io.fabric8.funktion.FunktionTestSupport;
import io.fabric8.funktion.model.Funktion;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;


/**
 */
public class SplitTest extends FunktionTestSupport {
    public static final String START_URI = "direct:start";
    public static final String RESULTS_URI = "mock:results";
    private static final transient Logger LOG = LoggerFactory.getLogger(SplitTest.class);
    @EndpointInject(uri = RESULTS_URI)
    protected MockEndpoint resultsEndpoint;

    protected List<String> messages = Arrays.asList(
            "{ \"orderId\": \"ABC\", \"lineItems\": [{\"id\":123,\"name\":\"beer\"},{\"id\":456,\"name\":\"wine\"}] }"
    );
    protected List<String> expectedMessages = Arrays.asList(
            "{\"id\":123,\"name\":\"beer\"}",
            "{\"id\":456,\"name\":\"wine\"}"
    );

    @Test
    public void testStep() throws Exception {
        resultsEndpoint.expectedBodiesReceived(expectedMessages);

        for (Object body : messages) {
            template.sendBody(START_URI, body);
        }

        MockEndpoint.assertIsSatisfied(resultsEndpoint);
        logMessagesReceived(resultsEndpoint);
    }

    @Override
    protected void addFunktionFlows(Funktion funktion) {
        funktion.createFlow().endpoint(START_URI).split("$.lineItems").endpoint(RESULTS_URI);
    }
}
