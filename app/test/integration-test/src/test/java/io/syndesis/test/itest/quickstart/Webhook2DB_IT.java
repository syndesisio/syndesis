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

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.dsl.runner.TestRunnerBeforeTestSupport;
import com.consol.citrus.http.client.HttpClient;
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
    public void testWebhook2Db(@CitrusResource TestRunner runner) {
        runner.http(builder -> builder.client(webHookClient)
                .send()
                .post()
                .payload("{\"task\":\"My new task!\"}"));

        runner.http(builder -> builder.client(webHookClient)
                .receive()
                .response(HttpStatus.NO_CONTENT));

        runner.query(builder -> builder.dataSource(sampleDb)
                .statement("select count(*) as found_records from todo where task = 'My new task!'")
                .validate("found_records", String.valueOf(1)));
    }

    @Configuration
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class EndpointConfig {
        @Bean
        public HttpClient webHookClient() {
            return CitrusEndpoints.http().client()
                    .requestUrl(String.format("http://localhost:%s/webhook/quickstart", integrationContainer.getServerPort()))
                    .build();
        }

        @Bean
        public TestRunnerBeforeTestSupport beforeTest(DataSource sampleDb) {
            return new TestRunnerBeforeTestSupport() {
                @Override
                public void beforeTest(TestRunner runner) {
                    runner.sql(builder -> builder.dataSource(sampleDb)
                                                 .statement("delete from todo"));
                }
            };
        }
    }
}
