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
package io.syndesis.integration.runtime;

import java.util.Arrays;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    properties = {
        "spring.main.banner-mode=off",
        "syndesis.configuration=classpath:io/syndesis/integration/runtime/flow-with-default-split-inline.yml"
    },
    classes = {
        CamelAutoConfiguration.class,
        SyndesisAutoConfiguration.class
    }
)
public class FlowWithDefaultSplitInlineTest {
    @Produce(uri = "direct:start")
    private ProducerTemplate template;
    @EndpointInject(uri = "mock:results")
    private MockEndpoint mock;

    @Test
    public void test() throws Exception {
        mock.expectedMessageCount(3);
        mock.expectedBodiesReceived("1","2","3");

        template.sendBody(Arrays.asList("1","2","3"));

        mock.assertIsSatisfied();
    }
}
