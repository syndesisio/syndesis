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

import java.util.StringJoiner;
import java.util.stream.Stream;

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
public class WebHookSplitToDB_IT extends SyndesisIntegrationTestSupport {

    private final HttpClient webHookClient = CitrusEndpoints.http().client()
        .requestUrl(String.format("http://localhost:%s/webhook/test-webhook", INTEGRATION_CONTAINER.getServerPort()))
        .build();

    /**
     * This integration provides a webhook that expects a POST request to store contacts to the sample database. The
     * webhook defines a Json instance schema as Json array and saves incoming contacts to the DB.
     * Inbound requests are split first and then filtered in two ways
     *  Basic filter applies to the company name property to be "Red Hat"
     *  Advanced filter applies to the first_name property to not be like "Unknown"
     */
    @Container
    public static final SyndesisIntegrationRuntimeContainer INTEGRATION_CONTAINER = new SyndesisIntegrationRuntimeContainer.Builder()
                            .name("webhook-split-to-db")
                            .fromExport(WebHookSplitToDB_IT.class.getResource("WebhookSplitToDB-export"))
                            .build()
                            .withNetwork(getSyndesisDb().getNetwork())
                            .withExposedPorts(SyndesisTestEnvironment.getServerPort());

    @Test
    @CitrusTest
    public void testWebHookToDb(@CitrusResource TestRunner runner) {
        runner.http(builder -> builder.client(webHookClient)
                .send()
                .post()
                .payload(contacts( "Red Hat", "John", "Johnny")));

        runner.http(builder -> builder.client(webHookClient)
                .receive()
                .response(HttpStatus.OK));

        verifyRecordsInDb(runner, 2);
    }

    @Test
    @CitrusTest
    public void testWebHookToDbBasicFilter(@CitrusResource TestRunner runner) {
        runner.http(builder -> builder.client(webHookClient)
                .send()
                .post()
                .payload(contacts("Microsoft", "Bill", "Johnny")));

        runner.http(builder -> builder.client(webHookClient)
                .receive()
                .response(HttpStatus.OK));

        verifyRecordsInDb(runner, 0);
    }

    @Test
    @CitrusTest
    public void testWebHookToDbAdvancedFilter(@CitrusResource TestRunner runner) {
        runner.http(builder -> builder.client(webHookClient)
                .send()
                .post()
                .payload(contacts( "Red Hat", "Unknown")));

        runner.http(builder -> builder.client(webHookClient)
                .receive()
                .response(HttpStatus.OK));

        verifyRecordsInDb(runner, 0);
    }

    private static String contacts(String company, String ... firstNames) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");

        Stream.of(firstNames)
                .map(firstName -> String.format("{\"first_name\":\"%s\",\"company\":\"%s\"}", firstName, company))
                .forEach(joiner::add);

        return joiner.toString();
    }

    private static void verifyRecordsInDb(TestRunner runner, int numberOfRecords) {
        runner.query(builder -> builder.dataSource(sampleDb())
                .statement("select count(*) as found_records from contact where lead_source='webhook'")
                .validate("found_records", String.valueOf(numberOfRecords)));
    }

}
