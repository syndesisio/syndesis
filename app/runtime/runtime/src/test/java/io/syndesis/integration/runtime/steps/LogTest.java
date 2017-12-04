/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.integration.runtime.steps;

import io.syndesis.integration.runtime.SyndesisTestSupport;
import io.syndesis.integration.model.Flow;
import io.syndesis.integration.model.SyndesisModel;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.util.jndi.JndiContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import java.util.Date;


/**
 */
public class LogTest extends SyndesisTestSupport {
    public static final String START_URI = "direct:start";
    public static final String RESULTS_URI = "mock:results";
    private static final transient Logger LOG = LoggerFactory.getLogger(LogTest.class);
    private static final int MESSAGE_COUNT = 9;


    @EndpointInject(uri = RESULTS_URI)
    protected MockEndpoint resultEndpoint;

    @Test
    public void testStep() throws Exception {
        // TODO
        // resultEndpoint.expectedMessageCount(3);
        resultEndpoint.expectedMinimumMessageCount(3);
        resultEndpoint.setResultWaitTime(2000);

        for (int i = 0; i < MESSAGE_COUNT; i++) {
            template.sendBody(START_URI, "{ \"id\": " + i + " }");
        }

        // lets pause to give the requests time to be processed
        // to check that the throttle really does kick in
        resultEndpoint.assertIsSatisfied();

        logMessagesReceived(resultEndpoint);
    }

    @Override
    protected Context createJndiContext() throws Exception {
        JndiContext answer = new JndiContext();
        answer.bind("addTime", new TimerBean());
        return answer;
    }

    @Override
    protected void addSyndesisFlows(SyndesisModel syndesis) {
        Flow flow = syndesis.createFlow().endpoint(START_URI);
        flow.log("Hello ${body}", null, null, null);
        flow.endpoint(RESULTS_URI);
    }

    public static class TimerBean {
        public String time(String body) {
            return "{ \"payload\": " + body + ", \"time\": " + new Date() + " }";
        }
    }
}
