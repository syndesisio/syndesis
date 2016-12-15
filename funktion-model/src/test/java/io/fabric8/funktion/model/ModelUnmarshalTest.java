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
package io.fabric8.funktion.model;

import io.fabric8.funktion.model.steps.Step;
import io.fabric8.funktion.model.steps.Endpoint;
import io.fabric8.funktion.model.steps.Function;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static io.fabric8.funktion.model.FunktionAssertions.assertEndpointStep;
import static io.fabric8.funktion.model.FunktionAssertions.assertFlow;
import static io.fabric8.funktion.model.FunktionAssertions.assertFunctionStep;
import static org.assertj.core.api.Assertions.assertThat;

/**
 */
public class ModelUnmarshalTest {
    private static final transient Logger LOG = LoggerFactory.getLogger(ModelUnmarshalTest.class);

    @Test
    public void testUnarshal() throws Exception {
        Funktion config = Funktions.findFromFolder(getTestResources());
        List<Flow> rules = config.getFlows();
        assertThat(rules).isNotEmpty();

        for (Flow rule : rules) {
            LOG.info("Loaded: " + rule);
        }


        Flow actualFlow1 = assertFlow(config, 0);
        Flow actualFlow2 =  assertFlow(config, 1);


        assertThat(actualFlow1.getName()).describedAs("name").isEqualTo("thingy");
        assertEndpointStep(actualFlow1, 0, "http://0.0.0.0:8080");
        assertFunctionStep(actualFlow1, 1, "io.fabric8.funktion.example.Main::cheese");

        assertThat(actualFlow2.getName()).describedAs("name").isEqualTo("another");
        assertEndpointStep(actualFlow2, 0, "http://0.0.0.0:8080");
        assertEndpointStep(actualFlow2, 1, "activemq:whatnot");
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
