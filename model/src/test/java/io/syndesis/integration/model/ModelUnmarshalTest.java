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

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

/**
 */
public class ModelUnmarshalTest {
    private static final transient Logger LOG = LoggerFactory.getLogger(ModelUnmarshalTest.class);

    @Test
    public void testUnarshal() throws Exception {
        Optional<SyndesisModel> config = SyndesisHelpers.tryFindFromFolder(getTestResources());
        assertTrue(config.isPresent());
        List<Flow> rules = config.get().getFlows();
        assertThat(rules).isNotEmpty();

        for (Flow rule : rules) {
            LOG.info("Loaded: " + rule);
        }


        Flow actualFlow1 = SyndesisAssertions.assertFlow(config.get(), 0);
        Flow actualFlow2 =  SyndesisAssertions.assertFlow(config.get(), 1);


        assertThat(actualFlow1.getName()).describedAs("name").isEqualTo("thingy");
        SyndesisAssertions.assertEndpointStep(actualFlow1, 0, "http://0.0.0.0:8080");
        SyndesisAssertions.assertFunctionStep(actualFlow1, 1, "io.syndesis.integration.runtime.example.Main::cheese");

        assertThat(actualFlow2.getName()).describedAs("name").isEqualTo("another");
        SyndesisAssertions.assertEndpointStep(actualFlow2, 0, "http://0.0.0.0:8080");
        SyndesisAssertions.assertEndpointStep(actualFlow2, 1, "activemq:whatnot");
    }


    public static File getTestResources() {
        File answer = new File(getBaseDir(), "src/test/resources");
        assertThat(answer).isDirectory().exists();
        return answer;
    }

    public static File getBaseDir() {
        String basedir = System.getProperty("basedir", System.getProperty("user.dir", "."));
        File answer = new File(basedir);
        assertThat(answer).isDirectory().exists();
        return answer;
    }
}
