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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import io.syndesis.integration.runtime.sb.IntegrationRuntimeAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        IntegrationRuntimeAutoConfiguration.class,
        ComponentProxyCustomizerTest.TestConfiguration.class,
        ComponentProxyCustomizerTest.MyBean.class
    },
    properties = {
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG",
        "syndesis.integration.runtime.capture.enabled = false",
        "syndesis.integration.runtime.enabled = true",
        "syndesis.integration.runtime.configuration-location = classpath:/syndesis/integration/component/proxy/ComponentProxyCustomizerTest.json"
    }
)
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.UseLocaleWithCaseConversions"})
public class ComponentProxyCustomizerTest {

    @Autowired
    private ApplicationContext context;
    @Autowired
    private CamelContext camelContext;

    // ***************************
    // Test
    // ***************************

    @Test
    public void testRequest() {
        assertThat(context.getBean("my-bean", MyBean.class)).isNotNull();

        final ProducerTemplate template = camelContext.createProducerTemplate();
        final String body = "hello";
        final String result = template.requestBody("direct:start", body, String.class);
        final String expected = Base64.getEncoder().encodeToString("HELLO WORLD!".toUpperCase().getBytes(StandardCharsets.US_ASCII));

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testSend() throws Exception {
        assertThat(context.getBean("my-bean", MyBean.class)).isNotNull();

        final ProducerTemplate template = camelContext.createProducerTemplate();
        final MockEndpoint mock = camelContext.getEndpoint("mock:result", MockEndpoint.class);
        final String body = "hello";
        final String expected = Base64.getEncoder().encodeToString("HELLO WORLD!".toUpperCase().getBytes(StandardCharsets.US_ASCII));

        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived(expected);

        template.sendBody("direct:start", body);

        mock.assertIsSatisfied();
    }

    // ***************************
    //
    // ***************************

    @Configuration
    public static class TestConfiguration {
    }

    @Component("my-bean")
    public static class MyBean {
        public String process(@Body String body) {
            return body.toUpperCase();
        }
    }

    public static class MyCustomizer implements ComponentProxyCustomizer {
        @Override
        public void customize(ComponentProxyComponent connector, Map<String, Object> options) {
            connector.setBeforeProducer(
                e -> e.getIn().setBody(e.getIn().getBody(String.class) + " WORLD!")
            );
            connector.setAfterProducer(
                e -> e.getIn().setBody(Base64.getEncoder().encodeToString(e.getIn().getBody(byte[].class)))
            );
        }
    }
}
