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
package io.syndesis.integration.component.proxy;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import io.syndesis.integration.runtime.sb.IntegrationRuntimeAutoConfiguration;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        IntegrationRuntimeAutoConfiguration.class,
        ComponentProxySplitTokenizeTest.TestConfiguration.class
    },
    properties = {
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG",
        "syndesis.integration.runtime.capture.enabled = false",
        "syndesis.integration.runtime.enabled = true",
        "syndesis.integration.runtime.configuration-location = classpath:/syndesis/integration/component/proxy/ComponentProxySplitTokenizeTest.json"
    }
)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class ComponentProxySplitTokenizeTest {
    @Autowired
    private CamelContext camelContext;

    // ***************************
    // Test
    // ***************************

    @Test
    public void testSplit() throws InterruptedException {
        final ProducerTemplate template = camelContext.createProducerTemplate();
        final MockEndpoint mock = camelContext.getEndpoint("mock:result", MockEndpoint.class);
        final String values = "a,b,c";
        final String[] result = values.split(",");

        mock.expectedMessageCount(result.length);
        mock.expectedBodiesReceived((Object[])result);

        template.sendBody("direct:start", values);

        mock.assertIsSatisfied();
    }

    // ***************************
    //
    // ***************************

    @Configuration
    public static class TestConfiguration {
    }
}
