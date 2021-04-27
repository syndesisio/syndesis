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

package io.syndesis.test.itest.quickstart;

import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.http.client.HttpClient;

@Testcontainers
public class Webhook2DB_IT extends SyndesisIntegrationTestSupport {

    private final HttpClient webHookClient = CitrusEndpoints.http().client()
        .requestUrl(String.format("http://localhost:%s/webhook/quickstart", INTEGRATION_CONTAINER.getServerPort()))
        .build();

    /**
     * Quickstart integration from https://github.com/syndesisio/syndesis-quickstarts/tree/master/webhook-2-db
     */
    @Container
    public static final SyndesisIntegrationRuntimeContainer INTEGRATION_CONTAINER = new SyndesisIntegrationRuntimeContainer.Builder()
                            .name("webhook-to-db")
                            .fromExport(Webhook2DB_IT.class.getResource("Webhook2Db-export"))
                            .customize("$..configuredProperties.contextPath", "quickstart")
                            .build()
                            .withNetwork(getSyndesisDb().getNetwork())
                            .withExposedPorts(SyndesisTestEnvironment.getServerPort());

    @Test
    @CitrusTest
    public void testWebhook2Db(@CitrusResource TestRunner runner) {
        runner.http(builder -> builder.client(webHookClient)
                .send()
                .post()
                .payload("{\"task\":\"My new task!\"}"));

        runner.http(builder -> builder.client(webHookClient)
                .receive()
                .response(HttpStatus.OK));

        runner.query(builder -> builder.dataSource(sampleDb())
                .statement("select count(*) as found_records from todo where task = 'My new task!'")
                .validate("found_records", String.valueOf(1)));
    }

}
