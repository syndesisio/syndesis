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

import java.util.List;

import static io.fabric8.funktion.model.FunktionAssertions.assertEndpointStep;
import static io.fabric8.funktion.model.FunktionAssertions.assertFlow;
import static io.fabric8.funktion.model.FunktionAssertions.assertFunctionStep;
import static org.assertj.core.api.Assertions.assertThat;

/**
 */
public class ModelMarshalTest {
    private static final transient Logger LOG = LoggerFactory.getLogger(ModelMarshalTest.class);

    @Test
    public void testMarshal() throws Exception {
        String expectedName = "cheese";
        String expectedTrigger = "activemq:foo";
        String expectedFunctionName = "io.fabric8.funktion.example.Main::cheese";
        String expectedEndpointUrl = "http://google.com/";

        Funktion expected = new Funktion();
        expected.createFlow().name(expectedName).endpoint(expectedTrigger).function(expectedFunctionName).endpoint(expectedEndpointUrl);

        String yaml = Funktions.toYaml(expected);

        System.out.println("Created YAML: " + yaml);


        Funktion actual = Funktions.loadFromString(yaml);

        Flow actualFlow = assertFlow(actual, 0);
        assertEndpointStep(actualFlow, 0, expectedTrigger);
        assertFunctionStep(actualFlow, 1, expectedFunctionName);
        assertEndpointStep(actualFlow, 2, expectedEndpointUrl);
    }
}
