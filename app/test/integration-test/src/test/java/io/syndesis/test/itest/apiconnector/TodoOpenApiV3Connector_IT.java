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

package io.syndesis.test.itest.apiconnector;

import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.SocketUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.http.server.HttpServer;

@Testcontainers
public class TodoOpenApiV3Connector_IT extends SyndesisIntegrationTestSupport {

    private static final int TODO_SERVER_PORT = SocketUtils.findAvailableTcpPort();
    static {
        org.testcontainers.Testcontainers.exposeHostPorts(TODO_SERVER_PORT);
    }

    private static final HttpServer TODO_API_SERVER = startup(CitrusEndpoints.http()
        .server()
        .port(TODO_SERVER_PORT)
        .autoStart(true)
        .timeout(600000L)
        .build());

    /**
     * Integration uses api connector to send OpenAPI client requests to a REST endpoint. The client API connector was generated
     * from OpenAPI 3.x specification.
     * The integration invokes following sequence of client requests on the test server
     *  POST /api adds a new task
     *  PUT /api update task
     *  GET /api/{id} fetch task by id
     *  GET /api list all tasks
     *  DELETE /api/{id} delete task
     */
    @Container
    public static final SyndesisIntegrationRuntimeContainer INTEGRATION_CONTAINER = new SyndesisIntegrationRuntimeContainer.Builder()
                            .name("todo-api-client")
                            .fromExport(TodoOpenApiV3Connector_IT.class.getResource("TodoOpenApiV3Connector-export"))
                            .customize("$..configuredProperties.period", "5000")
                            .customize("$..configuredProperties.host",
                                String.format("http://%s:%s", GenericContainer.INTERNAL_HOST_HOSTNAME, TODO_SERVER_PORT))
                            .build()
                            .withExposedPorts(SyndesisTestEnvironment.getServerPort(),
                                              SyndesisTestEnvironment.getManagementPort());

    @Test
    @CitrusTest
    public void testAddAndList(@CitrusResource TestRunner runner) {
        runner.variable("id", "citrus:randomNumber(4)");
        runner.variable("task", "Hello from todo client!");

        runner.echo("Add new task");

        runner.http(builder -> builder.server(TODO_API_SERVER)
            .receive()
            .post("/api")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .payload("{\"task\":\"${task}\", \"completed\": 0}"));

        runner.http(builder -> builder.server(TODO_API_SERVER)
            .send()
            .response(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .payload("{\"id\": ${id}, \"task\":\"${task}\", \"completed\": 0}"));

        runner.echo("Update task");

        runner.http(builder -> builder.server(TODO_API_SERVER)
            .receive()
            .put("/api/${id}")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .payload("{\"id\": ${id}, \"task\":\"citrus:upperCase(${task})\", \"completed\": 0}"));

        runner.http(builder -> builder.server(TODO_API_SERVER)
            .send()
            .response(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .payload("{\"id\": ${id}, \"task\":\"${task}\", \"completed\": 0}"));

        runner.echo("Fetch task by id");

        runner.http(builder -> builder.server(TODO_API_SERVER)
            .receive()
            .get("/api/${id}"));

        runner.http(builder -> builder.server(TODO_API_SERVER)
            .send()
            .response(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .payload("{\"id\": ${id}, \"task\":\"${task}\", \"completed\": 0}"));

        runner.echo("List all tasks");

        runner.http(builder -> builder.server(TODO_API_SERVER)
            .receive()
            .get("/api"));

        runner.http(builder -> builder.server(TODO_API_SERVER)
            .send()
            .response(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .payload("[{\"id\": ${id}, \"task\":\"${task}\", \"completed\": 0}]"));

        runner.echo("Delete task by id");

        runner.http(builder -> builder.server(TODO_API_SERVER)
            .receive()
            .delete("/api/${id}"));

        runner.http(builder -> builder.server(TODO_API_SERVER)
            .send()
            .response(HttpStatus.NO_CONTENT));
    }
}
