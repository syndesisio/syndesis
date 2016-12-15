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
import io.fabric8.funktion.model.steps.InvokeEndpoint;
import io.fabric8.funktion.model.steps.InvokeFunction;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
        Flow expectedRule = expected.createFlow();
        expectedRule.setName(expectedName);
        expectedRule.setTrigger(expectedTrigger);
        expectedRule.addStep(new InvokeFunction(expectedFunctionName));
        expectedRule.addStep(new InvokeEndpoint(expectedEndpointUrl));

        String yaml = Funktions.toYaml(expected);

        System.out.println("Created YAML: " + yaml);


        Funktion actual = Funktions.loadFromString(yaml);
        List<Flow> actualRules = actual.getFlows();
        assertThat(actualRules).hasSize(1);

        Flow actualRule = actualRules.get(0);
        assertThat(actualRule.getName()).describedAs("name").isEqualTo(expectedName);
        assertThat(actualRule.getTrigger()).describedAs("trigger").isEqualTo(expectedTrigger);

        List<Step> actualRuleActions = actualRule.getSteps();
        assertThat(actualRuleActions).hasSize(2);

        Step actualAction1 = actualRuleActions.get(0);
        assertThat(actualAction1).isInstanceOf(InvokeFunction.class);

        Step actualAction2 = actualRuleActions.get(1);
        assertThat(actualAction2).isInstanceOf(InvokeEndpoint.class);

        InvokeFunction actualFunction = (InvokeFunction) actualAction1;
        assertThat(actualFunction.getName()).isEqualTo(expectedFunctionName);
        assertThat(actualFunction.getKind()).isEqualTo("function");

        InvokeEndpoint actualEndpoint = (InvokeEndpoint) actualAction2;
        assertThat(actualEndpoint.getUrl()).isEqualTo(expectedEndpointUrl);
        assertThat(actualEndpoint.getKind()).isEqualTo("endpoint");
    }
}
