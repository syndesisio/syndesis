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
package io.syndesis.integration.model;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelUnmarshalTest {
    private static final transient Logger LOG = LoggerFactory.getLogger(ModelUnmarshalTest.class);

    @Test
    public void testUnmarshal() throws Exception {
        try (InputStream is = ModelUnmarshalTest.class.getResourceAsStream("/syndesis.yml")) {
            SyndesisModel model = YamlHelpers.load(is);

            List<Flow> rules = model.getFlows();
            assertThat(rules).isNotEmpty();

            for (Flow rule : rules) {
                LOG.info("Loaded: " + rule);
            }
            Flow actualFlow1 = SyndesisAssertions.assertFlow(model, 0);
            Flow actualFlow2 = SyndesisAssertions.assertFlow(model, 1);

            assertThat(actualFlow1.getName()).describedAs("name").isEqualTo("thingy");
            SyndesisAssertions.assertEndpointStep(actualFlow1, 0, "http://0.0.0.0:8080");
            SyndesisAssertions.assertFunctionStep(actualFlow1, 1, "io.syndesis.integration.runtime.example.Main::cheese");

            assertThat(actualFlow2.getName()).describedAs("name").isEqualTo("another");
            SyndesisAssertions.assertEndpointStep(actualFlow2, 0, "http://0.0.0.0:8080");
            SyndesisAssertions.assertEndpointStep(actualFlow2, 1, "activemq:whatnot");
        }
    }
}
