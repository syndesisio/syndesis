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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 */
public class ModelUnmarshalTest {
    private static final transient Logger LOG = LoggerFactory.getLogger(ModelUnmarshalTest.class);

    @Test
    public void testUnarshal() throws Exception {
        FunktionConfig config = FunktionConfigs.findFromFolder(getTestResources());
        List<FunktionRule> rules = config.getRules();
        assertThat(rules).isNotEmpty();

        for (FunktionRule rule : rules) {
            LOG.info("Loaded: " + rule);
        }


        List<FunktionRule> actualRules = config.getRules();
        assertThat(actualRules).hasSize(2);

        FunktionRule actualRule1 = actualRules.get(0);
        FunktionRule actualRule2 = actualRules.get(1);

        assertThat(actualRule1.getName()).describedAs("name").isEqualTo("thingy");
        assertThat(actualRule1.getTrigger()).describedAs("trigger").isEqualTo("http://0.0.0.0:8080");
        List<FunktionAction> actualRuleActions = actualRule1.getActions();
        assertThat(actualRuleActions).hasSize(1);

        FunktionAction actualAction1 = actualRuleActions.get(0);
        assertThat(actualAction1).isInstanceOf(InvokeFunction.class);
        InvokeFunction actualFunction = (InvokeFunction) actualAction1;
        assertThat(actualFunction.getName()).isEqualTo("io.fabric8.funktion.example.Main::cheese");
        assertThat(actualFunction.getKind()).isEqualTo("function");



        assertThat(actualRule2.getName()).describedAs("name").isEqualTo("another");
        assertThat(actualRule2.getTrigger()).describedAs("trigger").isEqualTo("activemq:foo.bar");
        List<FunktionAction> actualRuleActions2 = actualRule2.getActions();
        assertThat(actualRuleActions2).hasSize(1);

        FunktionAction actualAction2 = actualRuleActions2.get(0);
        assertThat(actualAction2).isInstanceOf(InvokeEndpoint.class);
        InvokeEndpoint actualEndpoint = (InvokeEndpoint) actualAction2;
        assertThat(actualEndpoint.getUrl()).isEqualTo("activemq:whatnot");
        assertThat(actualEndpoint.getKind()).isEqualTo("endpoint");
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
