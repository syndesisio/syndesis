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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
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

        String yaml = SyndesisHelpers.toYaml(expected);

        System.out.println("Created YAML: " + yaml);


        SyndesisModel actual = SyndesisHelpers.loadFromString(yaml);

        Flow actualFlow = SyndesisAssertions.assertFlow(actual, 0);
        SyndesisAssertions.assertEndpointStep(actualFlow, 0, expectedTrigger);
        SyndesisAssertions.assertFunctionStep(actualFlow, 1, expectedFunctionName);
        SyndesisAssertions.assertEndpointStep(actualFlow, 2, expectedEndpointUrl);
    }
}
