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
package io.syndesis.integration.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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

        String yaml = YamlHelpers.createObjectMapper().writeValueAsString(expected);

        LOG.info("Created YAML:\n{}", yaml);

        try(InputStream is = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))) {
            SyndesisModel actual = YamlHelpers.load(is);

            Flow actualFlow = SyndesisAssertions.assertFlow(actual, 0);
            SyndesisAssertions.assertEndpointStep(actualFlow, 0, expectedTrigger);
            SyndesisAssertions.assertFunctionStep(actualFlow, 1, expectedFunctionName);
            SyndesisAssertions.assertEndpointStep(actualFlow, 2, expectedEndpointUrl);
        }
    }
}
