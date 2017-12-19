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

import io.syndesis.integration.model.SyndesisModel;
import io.syndesis.integration.runtime.SyndesisTestSupport;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

public class SplitInlineTest extends SyndesisTestSupport {
    public static final String START_URI = "direct:start";
    public static final String RESULTS_URI = "mock:results";

    @EndpointInject(uri = RESULTS_URI)
    protected MockEndpoint resultsEndpoint;

    @Test
    public void testStep() throws Exception {
        resultsEndpoint.expectedBodiesReceived("a","b","c");
        template.sendBody(START_URI, "a,b,c");

        MockEndpoint.assertIsSatisfied(resultsEndpoint);

        logMessagesReceived(resultsEndpoint);
    }

    @Override
    protected void addSyndesisFlows(SyndesisModel syndesis) {
        syndesis.createFlow()
            .endpoint(START_URI)
            .splitInline("tokenize", ",")
            .endpoint(RESULTS_URI);
    }
}
