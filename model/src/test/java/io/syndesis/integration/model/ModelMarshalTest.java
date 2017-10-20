/*
 * Copyright 2016 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package io.syndesis.integration.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import io.syndesis.integration.model.steps.Endpoint;
import io.syndesis.integration.model.steps.Step;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelMarshalTest {
    private static final transient Logger LOG = LoggerFactory.getLogger(ModelMarshalTest.class);

    @Test
    public void testMarshal() throws Exception {
        String expectedName = "cheese";
        String expectedTrigger = "activemq:foo";
        String expectedFunctionName = "io.syndesis.runtime.integration.example.Main::cheese";
        String expectedEndpointUrl = "http://google.com/";

        SyndesisModel expected = new SyndesisModel();
        expected.createFlow().name(expectedName).endpoint(expectedTrigger).function(expectedFunctionName).endpoint(expectedEndpointUrl);

        ObjectMapper mapper = SyndesisModelHelpers.createObjectMapper();
        String yaml = SyndesisModelHelpers.registerDefaultSteps(mapper).writeValueAsString(expected);

        LOG.info("Created YAML:\n{}", yaml);

        try(InputStream is = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))) {
            SyndesisModel actual = new SyndesisModelBuilder().build(is);

            Flow actualFlow = SyndesisAssertions.assertFlow(actual, 0);
            SyndesisAssertions.assertEndpointStep(actualFlow, 0, expectedTrigger);
            SyndesisAssertions.assertFunctionStep(actualFlow, 1, expectedFunctionName);
            SyndesisAssertions.assertEndpointStep(actualFlow, 2, expectedEndpointUrl);
        }
    }

    @Test
    public void testMarshalWithCustomSteps() throws Exception {
        Step step1 = new MasterEndpoint("http://google.com/1");
        Step step2 = new Endpoint("http://google.com/2");

        SyndesisModel model = new SyndesisModel();
        model.createFlow().addStep(step1).addStep(step2);

        ObjectMapper mapper = SyndesisModelHelpers.createObjectMapper();
        mapper.registerSubtypes(new NamedType(step1.getClass(), step1.getKind()));
        mapper.registerSubtypes(new NamedType(step2.getClass(), step2.getKind()));

        String yaml = mapper.writeValueAsString(model);

        LOG.info("Created YAML:\n{}", yaml);

        try(InputStream is = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))) {
            SyndesisModel actual = new SyndesisModelBuilder().addStep(step1).build(is);

            Flow actualFlow = SyndesisAssertions.assertFlow(actual, 0);
            SyndesisAssertions.assertFlowHasStep(actualFlow, 0, MasterEndpoint.class);
            SyndesisAssertions.assertFlowHasStep(actualFlow, 1, Endpoint.class);

        }
    }

    public static class MasterEndpoint extends Step {
        public static final String KIND = "master-endpoint";

        private String uri;

        public MasterEndpoint() {
            super(KIND);
        }

        public MasterEndpoint(String uri) {
            super(KIND);
            setUri(uri);
        }

        @Override
        public String toString() {
            return "MasterEndpoint: " + uri;
        }

        public final String getUri() {
            return uri;
        }

        public final void setUri(String uri) {
            this.uri = uri;

            if (!uri.startsWith("master:")) {
                this.uri = "master:" + uri;
            } else {
                this.uri = uri;
            }
        }
    }
}
