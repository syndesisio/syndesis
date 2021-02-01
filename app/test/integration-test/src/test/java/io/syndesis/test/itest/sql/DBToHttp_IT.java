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

package io.syndesis.test.itest.sql;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.http.server.HttpServerBuilder;
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
import org.springframework.util.SocketUtils;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;

import static com.consol.citrus.actions.ExecuteSQLAction.Builder.sql;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = DBToHttp_IT.EndpointConfig.class)
public class DBToHttp_IT extends SyndesisIntegrationTestSupport {

    private static final int HTTP_TEST_SERVER_PORT = SocketUtils.findAvailableTcpPort();
    static {
        Testcontainers.exposeHostPorts(HTTP_TEST_SERVER_PORT);
    }

    @Autowired
    private DataSource sampleDb;

    @Autowired
    private HttpServer httpTestServer;

    /**
     * Integration periodically retrieves all contacts (ordered by first_name) from the database and maps the
     * entries (first_name, last_name, company) to a Http endpoint.
     * The integration uses a split step to pass entries one by one to the Http endpoint.
     */
    @ClassRule
    public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
            .name("db-to-http")
            .fromExport(DBToHttp_IT.class.getResource("DBToHttp-export"))
            .customize("$..configuredProperties.schedulerExpression", "5000")
            .customize("$..configuredProperties.baseUrl",
                    String.format("http://%s:%s", GenericContainer.INTERNAL_HOST_HOSTNAME, HTTP_TEST_SERVER_PORT))
            .build()
            .withNetwork(getSyndesisDb().getNetwork());

    @Test
    @CitrusTest
    public void testDBToHttp(@CitrusResource TestCaseRunner runner) {
        cleanupDatabase(runner);

        runner.given(sql(sampleDb)
                .statements(Arrays.asList("insert into contact (first_name, last_name, company) values ('Joe','Jackson','Red Hat')",
                                          "insert into contact (first_name, last_name, company) values ('Joanne','Jackson','Red Hat')")));

        runner.when(http().server(httpTestServer)
                .receive()
                .put()
                .message()
                .selector(Collections.singletonMap("jsonPath:$.contact", "@startsWith(Joanne)@"))
                .body("{\"contact\":\"Joanne Jackson Red Hat\"}"));

        runner.then(http().server(httpTestServer)
                .send()
                .response(HttpStatus.OK));

        runner.then(http().server(httpTestServer)
                .receive()
                .put()
                .message()
                .selector(Collections.singletonMap("jsonPath:$.contact", "@startsWith(Joe)@"))
                .body("{\"contact\":\"Joe Jackson Red Hat\"}"));

        runner.then(http().server(httpTestServer)
                .send()
                .response(HttpStatus.OK));
    }

    @Configuration
    public static class EndpointConfig {

        @Bean
        public HttpServer httpTestServer() {
            return new HttpServerBuilder()
                    .port(HTTP_TEST_SERVER_PORT)
                    .autoStart(true)
                    .timeout(Duration.ofSeconds(SyndesisTestEnvironment.getDefaultTimeout()).toMillis())
                    .build();
        }
    }

    private void cleanupDatabase(TestCaseRunner runner) {
        runner.given(sql(sampleDb)
            .statement("delete from contact"));
    }
}
