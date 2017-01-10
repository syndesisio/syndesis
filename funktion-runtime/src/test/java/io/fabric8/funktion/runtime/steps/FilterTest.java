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
import io.fabric8.funktion.model.Flow;
import io.fabric8.funktion.model.Funktion;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 */
public class FilterTest extends FunktionTestSupport {
    public static final String START_URI = "direct:start";
    public static final String MATCHED_URI = "mock:matched";
    public static final String ALL_MESSAGES_URI = "mock:allMessages";
    private static final transient Logger LOG = LoggerFactory.getLogger(FilterTest.class);
    @EndpointInject(uri = MATCHED_URI)
    protected MockEndpoint matchedEndpoint;

    @EndpointInject(uri = ALL_MESSAGES_URI)
    protected MockEndpoint allMessagesEndpoint;

    protected List<String> matchingMessages = Arrays.asList(
            "{ \"name\": \"James\" }"
    );
    protected List<String> notMatchingMessages = Arrays.asList(
            "{ \"name\": \"Jimmi\" }"
    );

    @Test
    public void testStep() throws Exception {
        List<String> allMessages = new ArrayList<>(matchingMessages);
        allMessages.addAll(notMatchingMessages);

        matchedEndpoint.expectedBodiesReceived(matchingMessages);
        allMessagesEndpoint.expectedBodiesReceived(allMessages);

        for (Object body : allMessages) {
            template.sendBody(START_URI, body);
        }

        MockEndpoint[] mockEndpoints = {
                matchedEndpoint, allMessagesEndpoint
        };
        MockEndpoint.assertIsSatisfied(mockEndpoints);
        logMessagesReceived(mockEndpoints);
    }

    @Override
    protected void addFunktionFlows(Funktion funktion) {
        Flow flow = funktion.createFlow().endpoint(START_URI);
        flow.filter("$.[?(@.name == 'James')]").endpoint(MATCHED_URI);
        flow.endpoint(ALL_MESSAGES_URI);
    }
}
