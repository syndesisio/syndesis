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

package io.syndesis.test.itest.http;

import java.time.Duration;

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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.SocketUtils;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;

import static com.consol.citrus.actions.ReceiveTimeoutAction.Builder.receiveTimeout;
import static com.consol.citrus.container.Wait.Builder.waitFor;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = HttpToHttp_IT.EndpointConfig.class)
public class HttpToHttp_IT extends SyndesisIntegrationTestSupport {

    private static final int TODO_SERVER_PORT = SocketUtils.findAvailableTcpPort();
    static {
        Testcontainers.exposeHostPorts(TODO_SERVER_PORT);
    }

    @Autowired
    private HttpServer todoApiServer;

    /**
     * Integration periodically requests list of tasks (as Json array) from Http service and maps the results to an update call on the same Http service.
     * Incoming tasks are split and each entry is filtered on the task name to not start with "Important:" and status to be uncompleted.
     * When filter criteria matches the task name is updated with "Important:" prefix.
     * Other tasks are ignored.
     */
    @ClassRule
    public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
            .name("http-to-http")
            .fromExport(HttpToHttp_IT.class.getResource("HttpToHttp-export"))
            .customize("$..configuredProperties.schedulerExpression", "5000")
            .customize("$..configuredProperties.baseUrl",
                        String.format("http://%s:%s", GenericContainer.INTERNAL_HOST_HOSTNAME, TODO_SERVER_PORT))
            .build()
            .withExposedPorts(SyndesisTestEnvironment.getServerPort(),
                              SyndesisTestEnvironment.getManagementPort());

    @Test
    @CitrusTest
    public void testGetHealth(@CitrusResource TestCaseRunner runner) {
        runner.run(waitFor().http()
                .method(HttpMethod.GET.name())
                .seconds(10L)
                .status(HttpStatus.OK.value())
                .url(String.format("http://localhost:%s/actuator/health", integrationContainer.getManagementPort())));
    }

    @Test
    @CitrusTest
    public void testHttpToHttp(@CitrusResource TestCaseRunner runner) {
        runner.given(http().server(todoApiServer)
                .receive()
                .get());

        runner.when(http().server(todoApiServer)
                .send()
                .response(HttpStatus.OK)
                .message()
                .body("[{\"id\": \"1\", \"task\":\"Learn to play drums\", \"completed\": 0}," +
                          "{\"id\": \"2\", \"task\":\"Learn to play guitar\", \"completed\": 0}," +
                          "{\"id\": \"3\", \"task\":\"Important: Learn to play piano\", \"completed\": 0}]"));

        runner.then(http().server(todoApiServer)
                .receive()
                .put()
                .message()
                .body("{\"id\": \"1\", \"task\":\"Important: Learn to play drums\", \"completed\": 0}"));

        runner.when(http().server(todoApiServer)
                .send()
                .response(HttpStatus.ACCEPTED));

        runner.then(http().server(todoApiServer)
                .receive()
                .put()
                .message()
                .body("{\"id\": \"2\", \"task\":\"Important: Learn to play guitar\", \"completed\": 0}"));

        runner.when(http().server(todoApiServer)
                .send()
                .response(HttpStatus.ACCEPTED));

        runner.then(receiveTimeout(todoApiServer)
                .timeout(1000L));
    }

    @Test
    @CitrusTest
    public void testHttpToHttpEmptyBody(@CitrusResource TestCaseRunner runner) {
        runner.given(http().server(todoApiServer)
                .receive()
                .get());

        runner.when(http().server(todoApiServer)
                .send()
                .response(HttpStatus.OK)
                .message()
                .body("[]"));

        runner.then(receiveTimeout(todoApiServer)
                .timeout(1000L));
    }

    @Configuration
    public static class EndpointConfig {
        @Bean
        public HttpServer todoApiServer() {
            return new HttpServerBuilder()
                    .port(TODO_SERVER_PORT)
                    .autoStart(true)
                    .timeout(Duration.ofSeconds(SyndesisTestEnvironment.getDefaultTimeout()).toMillis())
                    .build();
        }
    }
}
