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

package io.syndesis.test.itest.conditional;

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
public class ConditionalFlows_IT extends SyndesisIntegrationTestSupport {

    private final HttpClient webHookClient = CitrusEndpoints.http().client()
        .requestUrl(String.format("http://localhost:%s/webhook/test-webhook", INTEGRATION_CONTAINER.getServerPort()))
        .build();

    /**
     * This integration provides a webhook that expects a POST request to store task entries to the sample database. The
     * webhook defines a Json instance schema that represents a contact with first_name and company properties.
     * Inbound requests are routed to different conditional flows based on condition evaluation on the message body.
     *  When body.fist_name == 'John' save 'Drink beer with John' as task to the DB
     *  When body.company == 'Red Hat' save 'Meet {{first_name}} from Red Hat' as task to the DB
     *  Otherwise store 'Drink coffee with {{first_name}} from {{company}}' as task
     */
    @Container
    public static final SyndesisIntegrationRuntimeContainer INTEGRATION_CONTAINER = new SyndesisIntegrationRuntimeContainer.Builder()
                            .name("conditional-flows")
                            .fromExport(ConditionalFlows_IT.class.getResource("ConditionalFlows-export"))
                            .build()
                            .withNetwork(getSyndesisDb().getNetwork())
                            .withExposedPorts(SyndesisTestEnvironment.getServerPort());

    @Test
    @CitrusTest
    public void testWebHookToDb(@CitrusResource TestRunner runner) {
        runner.http(builder -> builder.client(webHookClient)
                .send()
                .post()
                .payload(contact("John", "Red Hat")));

        runner.http(builder -> builder.client(webHookClient)
                .receive()
                .response(HttpStatus.OK));

        verifyRecordInDb(runner, "Drink beer with John");
    }

    @Test
    @CitrusTest
    public void testWebHookToDbBasicFilter(@CitrusResource TestRunner runner) {
        runner.http(builder -> builder.client(webHookClient)
                .send()
                .post()
                .payload(contact("Bill", "Microsoft")));

        runner.http(builder -> builder.client(webHookClient)
                .receive()
                .response(HttpStatus.OK));

        verifyRecordInDb(runner, "Drink coffee with Bill from Microsoft");
    }

    @Test
    @CitrusTest
    public void testWebHookToDbAdvancedFilter(@CitrusResource TestRunner runner) {
        runner.http(builder -> builder.client(webHookClient)
                .send()
                .post()
                .payload(contact("Joanna", "Red Hat")));

        runner.http(builder -> builder.client(webHookClient)
                .receive()
                .response(HttpStatus.OK));

        verifyRecordInDb(runner, "Meet Joanna from Red Hat");
    }

    private static String contact(String firstName, String company) {
        return String.format("{\"first_name\":\"%s\",\"company\":\"%s\"}", firstName, company);
    }

    private static void verifyRecordInDb(TestRunner runner, String task) {
        runner.query(builder -> builder.dataSource(sampleDb())
                .statement("select count(*) as found_records from todo where task='" + task + "'")
                .validate("found_records", String.valueOf(1)));
    }

}
