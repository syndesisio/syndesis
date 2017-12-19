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
package io.syndesis.integration.runtime.steps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.syndesis.integration.model.Flow;
import io.syndesis.integration.model.SyndesisModel;
import io.syndesis.integration.runtime.SyndesisTestSupport;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

public class FilterTest extends SyndesisTestSupport {
    public static final String START_URI = "direct:start";
    public static final String MATCHED_URI = "mock:matched";
    public static final String ALL_MESSAGES_URI = "mock:allMessages";

    @EndpointInject(uri = MATCHED_URI)
    protected MockEndpoint matchedEndpoint;

    @EndpointInject(uri = ALL_MESSAGES_URI)
    protected MockEndpoint allMessagesEndpoint;

    protected List<String> matchingMessages = Arrays.asList("{ \"name\": \"James\" }");
    protected List<String> notMatchingMessages = Arrays.asList("{ \"name\": \"Jimmi\" }");

    @Test
    public void testStep() throws Exception {
        List<String> allMessages = new ArrayList<>();
        allMessages.addAll(matchingMessages);
        allMessages.addAll(notMatchingMessages);

        matchedEndpoint.expectedBodiesReceived(matchingMessages);
        allMessagesEndpoint.expectedBodiesReceived(allMessages);

        for (Object body : allMessages) {
            template.sendBody(START_URI, body);
        }

        matchedEndpoint.assertIsSatisfied();
        allMessagesEndpoint.assertIsSatisfied();

        logMessagesReceived(matchedEndpoint, allMessagesEndpoint);
    }

    @Override
    protected void addSyndesisFlows(SyndesisModel syndesis) {
        Flow flow = syndesis.createFlow();
        flow.endpoint(START_URI);
        flow.filter("${body.name} == 'James'").endpoint(MATCHED_URI);
        flow.endpoint(ALL_MESSAGES_URI);
    }
}
