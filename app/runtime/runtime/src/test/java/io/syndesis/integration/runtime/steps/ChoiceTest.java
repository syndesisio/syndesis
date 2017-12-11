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

import io.syndesis.integration.runtime.SyndesisTestSupport;
import io.syndesis.integration.model.Flow;
import io.syndesis.integration.model.SyndesisModel;
import io.syndesis.integration.model.steps.Choice;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 */
public class ChoiceTest extends SyndesisTestSupport {
    private static final transient Logger LOG = LoggerFactory.getLogger(ChoiceTest.class);

    public static final String START_URI = "direct:start";
    public static final String MATCHED_JAMES_URI = "mock:matchedJames";
    public static final String MATCHED_JIMMI_URI = "mock:matchedJimmi";
    public static final String OTHERWISE_URI = "mock:matchedOtherwise";
    public static final String ALL_MESSAGES_URI = "mock:allMessages";

    @EndpointInject(uri = MATCHED_JAMES_URI)
    protected MockEndpoint matchedEndpoint;

    @EndpointInject(uri = MATCHED_JIMMI_URI)
    protected MockEndpoint matchedJimmiEndpoint;

    @EndpointInject(uri = OTHERWISE_URI)
    protected MockEndpoint otherwiseEndpoint;

    @EndpointInject(uri = ALL_MESSAGES_URI)
    protected MockEndpoint allMessagesEndpoint;


    protected List<String> matchedJames = Arrays.asList(
            "{ \"name\": \"James\" }"
    );
    protected List<String> matchedJimmi = Arrays.asList(
            "{ \"name\": \"Jimmi\" }"
    );
    protected List<String> otherwiseMessages = Arrays.asList(
            "{ \"name\": \"Rob\" }"
    );

    @Test
    public void testStep() throws Exception {
        List<String> allMessages = new ArrayList<>(matchedJames);
        allMessages.addAll(matchedJimmi);
        allMessages.addAll(otherwiseMessages);

        matchedEndpoint.expectedBodiesReceived(matchedJames);
        matchedJimmiEndpoint.expectedBodiesReceived(matchedJimmi);
        otherwiseEndpoint.expectedBodiesReceived(otherwiseMessages);
        allMessagesEndpoint.expectedBodiesReceived(allMessages);

        for (Object body : allMessages) {
            template.sendBody(START_URI, body);
        }

        MockEndpoint[] mockEndpoints = {
                matchedEndpoint, matchedJimmiEndpoint, otherwiseEndpoint, allMessagesEndpoint
        };
        MockEndpoint.assertIsSatisfied(mockEndpoints);
        logMessagesReceived(mockEndpoints);
    }

    @Override
    protected void addSyndesisFlows(SyndesisModel syndesis) {
        Flow flow = syndesis.createFlow().endpoint(START_URI);
        Choice choice = flow.choice();
        choice.when("${body[name]} == 'James'").endpoint(MATCHED_JAMES_URI);
        choice.when("${body[name]} == 'Jimmi'").endpoint(MATCHED_JIMMI_URI);
        choice.otherwise().endpoint(OTHERWISE_URI);
        flow.endpoint(ALL_MESSAGES_URI);
    }
}
