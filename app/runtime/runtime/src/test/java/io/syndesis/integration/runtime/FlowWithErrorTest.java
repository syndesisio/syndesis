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

import io.syndesis.extension.api.SyndesisStepExtension;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.fail;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    properties = {
        "spring.main.banner-mode=off",
        "syndesis.configuration=classpath:io/syndesis/integration/runtime/flow-with-error.yml"
    },
    classes = {
        CamelAutoConfiguration.class,
        SyndesisAutoConfiguration.class
    }
)
public class FlowWithErrorTest {
    @Produce(uri = "direct:start")
    private ProducerTemplate template;
    @EndpointInject(uri = "mock:results")
    private MockEndpoint mock;

    @Test
    public void test() throws Exception {
        mock.expectedMessageCount(3);
        mock.expectedBodiesReceived("1","3");
        template.sendBody("1");
        try {
            template.sendBody("2");
            fail("Expected exception");
        } catch (CamelExecutionException e) {
        }
        template.sendBody("3");
        mock.assertIsSatisfied();
    }

    public static class MyExtension implements SyndesisStepExtension {
        static int count = 0;

        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public Optional<ProcessorDefinition> configure(CamelContext context, ProcessorDefinition definition, Map<String, Object> parameters) {
            ProcessorDefinition processor = definition.process(exchange -> {
                count++;
                if( count == 2 ) {
                    throw new IOException("Bean Error");
                }
            });

            return Optional.of(processor);
        }
    }

}
