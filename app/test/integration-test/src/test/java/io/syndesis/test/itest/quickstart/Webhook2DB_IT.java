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

import javax.sql.DataSource;

import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
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

import static com.consol.citrus.actions.ExecuteSQLAction.Builder.sql;
import static com.consol.citrus.actions.ExecuteSQLQueryAction.Builder.query;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = Webhook2DB_IT.EndpointConfig.class)
public class Webhook2DB_IT extends SyndesisIntegrationTestSupport {

    @Autowired
    private HttpClient webHookClient;

    @Autowired
    private DataSource sampleDb;

    /**
     * Quickstart integration from https://github.com/syndesisio/syndesis-quickstarts/tree/master/webhook-2-db
     */
    @ClassRule
    public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
                            .name("webhook-to-db")
                            .fromExport(Webhook2DB_IT.class.getResource("Webhook2Db-export"))
                            .customize("$..configuredProperties.contextPath", "quickstart")
                            .build()
                            .withNetwork(getSyndesisDb().getNetwork())
                            .withExposedPorts(SyndesisTestEnvironment.getServerPort());

    @Test
    @CitrusTest
    public void testWebhook2Db(@CitrusResource TestCaseRunner runner) {
        cleanupDatabase(runner);

        runner.given(http().client(webHookClient)
                .send()
                .post()
                .message()
                .body("{\"task\":\"My new task!\"}"));

        runner.when(http().client(webHookClient)
                .receive()
                .response(HttpStatus.OK));

        runner.then(query(sampleDb)
                .statement("select count(*) as found_records from todo where task = 'My new task!'")
                .validate("found_records", String.valueOf(1)));
    }

    @Configuration
    public static class EndpointConfig {
        @Bean
        public HttpClient webHookClient() {
            return new HttpClientBuilder()
                    .requestUrl(String.format("http://localhost:%s/webhook/quickstart", integrationContainer.getServerPort()))
                    .build();
        }
    }

    private void cleanupDatabase(TestCaseRunner runner) {
        runner.given(sql(sampleDb)
            .statement("delete from todo"));
    }
}
