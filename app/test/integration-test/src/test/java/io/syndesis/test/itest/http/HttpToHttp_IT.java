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

import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.SocketUtils;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.http.server.HttpServer;

@org.testcontainers.junit.jupiter.Testcontainers
public class HttpToHttp_IT extends SyndesisIntegrationTestSupport {

    private static final int TODO_SERVER_PORT = SocketUtils.findAvailableTcpPort();
    static {
        Testcontainers.exposeHostPorts(TODO_SERVER_PORT);
    }

    private static final HttpServer TODO_API_SERVER = startup(CitrusEndpoints.http()
        .server()
        .port(TODO_SERVER_PORT)
        .autoStart(true)
        .timeout(60000L)
        .build());

    /**
     * Integration periodically requests list of tasks (as Json array) from Http service and maps the results to an update call on the same Http service.
     * Incoming tasks are split and each entry is filtered on the task name to not start with "Important:" and status to be uncompleted.
     * When filter criteria matches the task name is updated with "Important:" prefix.
     * Other tasks are ignored.
     */
    @Container
    public static final SyndesisIntegrationRuntimeContainer INTEGRATION_CONTAINER = new SyndesisIntegrationRuntimeContainer.Builder()
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
    public void testGetHealth(@CitrusResource TestRunner runner) {
        runner.waitFor().http()
                .method(HttpMethod.GET)
                .seconds(10L)
                .status(HttpStatus.OK)
                .url(String.format("http://localhost:%s/actuator/health", INTEGRATION_CONTAINER.getManagementPort()));
    }

    @Test
    @CitrusTest
    public void testHttpToHttp(@CitrusResource TestRunner runner) {
        runner.http(builder -> builder.server(TODO_API_SERVER)
                .receive()
                .get());

        runner.http(builder -> builder.server(TODO_API_SERVER)
                .send()
                .response(HttpStatus.OK)
                .payload("[{\"id\": \"1\", \"task\":\"Learn to play drums\", \"completed\": 0}," +
                          "{\"id\": \"2\", \"task\":\"Learn to play guitar\", \"completed\": 0}," +
                          "{\"id\": \"3\", \"task\":\"Important: Learn to play piano\", \"completed\": 0}]"));

        runner.http(builder -> builder.server(TODO_API_SERVER)
                .receive()
                .put()
                .payload("{\"id\": \"1\", \"task\":\"Important: Learn to play drums\", \"completed\": 0}"));

        runner.http(builder -> builder.server(TODO_API_SERVER)
                .send()
                .response(HttpStatus.ACCEPTED));

        runner.http(builder -> builder.server(TODO_API_SERVER)
                .receive()
                .put()
                .payload("{\"id\": \"2\", \"task\":\"Important: Learn to play guitar\", \"completed\": 0}"));

        runner.http(builder -> builder.server(TODO_API_SERVER)
                .send()
                .response(HttpStatus.ACCEPTED));

        runner.receiveTimeout(builder -> builder.endpoint(TODO_API_SERVER)
                .timeout(1000L));
    }

    @Test
    @CitrusTest
    public void testHttpToHttpEmptyBody(@CitrusResource TestRunner runner) {
        runner.http(builder -> builder.server(TODO_API_SERVER)
                .receive()
                .get());

        runner.http(builder -> builder.server(TODO_API_SERVER)
                .send()
                .response(HttpStatus.OK)
                .payload("[]"));

        runner.receiveTimeout(builder -> builder.endpoint(TODO_API_SERVER)
                .timeout(1000L));
    }

}
