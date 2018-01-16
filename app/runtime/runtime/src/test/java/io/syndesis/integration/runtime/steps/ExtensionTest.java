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
package io.syndesis.integration.runtime.steps;

import java.util.Map;
import java.util.Optional;

import io.syndesis.integration.model.SyndesisModel;
import io.syndesis.integration.model.steps.Endpoint;
import io.syndesis.integration.model.steps.Extension;
import io.syndesis.integration.runtime.SyndesisRouteBuilder;
import io.syndesis.extension.api.SyndesisStepExtension;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class ExtensionTest extends CamelTestSupport {
    @EndpointInject(uri = "mock:result")
    private MockEndpoint result;

    @Produce(uri = "direct:extension")
    private ProducerTemplate template;

    @Test
    public void testExtension() throws Exception {
        result.expectedMessageCount(1);
        result.expectedBodiesReceived("the_body-the_header-hello");

        template.sendBodyAndHeader("the_body", "ExtensionHeader", "the_header");

        result.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder[] createRouteBuilders() {
        return new RouteBuilder[] { createRouteBuilder(), createSyndesisRouteBuilder() };
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:a/b")
                    .to("mock:result");
            }
        };
    }

    protected RouteBuilder createSyndesisRouteBuilder() {
        return new SyndesisRouteBuilder("") {
            @Override
            protected SyndesisModel loadModel() throws Exception {
                SyndesisModel syndesis = new SyndesisModel();
                syndesis.createFlow()
                    .addStep(new Endpoint("direct:extension"))
                    .addStep(new Extension()
                        .name(MyExtension.class.getName())
                        .property("message", "hello"))
                    .addStep(new Endpoint("direct:a/b"));

                return syndesis;
            }
        };
    }

    public static class MyExtension implements SyndesisStepExtension {
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
                exchange.getIn().setBody(
                    String.join(
                        "-",
                        exchange.getIn().getBody(String.class),
                        exchange.getIn().getHeader("ExtensionHeader", String.class),
                        message)
                );
            });

            return Optional.of(processor);
        }
    }
}
