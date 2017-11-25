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
package io.syndesis.integration.runtime.steps;

import io.syndesis.integration.model.SyndesisModel;
import io.syndesis.integration.model.steps.Endpoint;
import io.syndesis.integration.model.steps.Function;
import io.syndesis.integration.runtime.SyndesisRouteBuilder;
import org.apache.camel.Body;
import org.apache.camel.EndpointInject;
import org.apache.camel.Handler;
import org.apache.camel.Header;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class FunctionTest extends CamelTestSupport {
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
                    .addStep(new Function()
                        .name(MyExtension.class.getName())
                        .property("message", "hello"))
                    .addStep(new Endpoint("direct:a/b"));

                return syndesis;
            }
        };
    }

    public static class MyExtension {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Handler
        public String configure(@Body String body, @Header("ExtensionHeader") String header) {
            return String.join("-", body, header, message);
        }
    }
}
