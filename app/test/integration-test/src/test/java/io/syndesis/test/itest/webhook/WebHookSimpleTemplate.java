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

package io.syndesis.test.itest.webhook;

import static com.consol.citrus.http.actions.HttpActionBuilder.http;

import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.http.actions.HttpClientActionBuilder;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.client.HttpClientBuilder;
import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author María Arias de Reyna
 */
@ContextConfiguration(classes = WebHookSimpleTemplate.EndpointConfig.class)
public class WebHookSimpleTemplate extends SyndesisIntegrationTestSupport {

    @Autowired
    private HttpClient webHookClient;

    /**
     * This integration provides a webhook that expects a POST request and apply a couple of templates with
     * data mappings. Used to check basic data mapping with atlasmap.
     */
    @ClassRule
    public static SyndesisIntegrationRuntimeContainer integrationContainer =
        new SyndesisIntegrationRuntimeContainer.Builder()
            .name("webhook-simple-template")
            .fromExport(WebHookSimpleTemplate.class.getResource("WebhookSimpleTemplate"))
            .enableDebug()
            .build()
            .withExposedPorts(SyndesisTestEnvironment.getServerPort(),
                SyndesisTestEnvironment.getManagementPort());


    @Test
    @CitrusTest
    public void templateWebhook(@CitrusResource TestCaseRunner runner) {
        runner.when(http().client(webHookClient)
                        .send()
                        .post()
                        .payload(payload("A",
                            "B")));

        final HttpClientActionBuilder.HttpClientReceiveActionBuilder receive =
            http().client(webHookClient).receive();

        runner.then(receive.response(HttpStatus.NO_CONTENT));

    }

    private static String payload(String astring, String bstring) {
        return String.format("{\n \"astring\": \"" + astring + "\",\n \"anotherstring\": \"" + bstring + "\"\n }",
            astring,
            bstring);
    }

    @Configuration
    public static class EndpointConfig {
        @Bean
        public HttpClient webHookClient() {
            return new HttpClientBuilder()
                       .requestUrl(String.format("http://localhost:%s/webhook/why-do-you-fail",
                           integrationContainer.getServerPort()))
                       .build();
        }
    }
}
