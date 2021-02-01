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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.SocketUtils;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;

import static com.consol.citrus.actions.EchoAction.Builder.echo;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = TodoOpenApiV2Connector_IT.EndpointConfig.class)
public class TodoOpenApiV2Connector_IT extends SyndesisIntegrationTestSupport {

    private static final int TODO_SERVER_PORT = SocketUtils.findAvailableTcpPort();
    static {
        Testcontainers.exposeHostPorts(TODO_SERVER_PORT);
    }

    @Autowired
    private HttpServer todoApiServer;

    /**
     * Integration uses api connector to send OpenAPI client requests to a REST endpoint. The client API connector was generated
     * from OpenAPI 2.x specification.
     *
     * The integration invokes following sequence of client requests on the test server
     *  POST /api adds a new task
     *  PUT /api update task
     *  GET /api/{id} fetch task by id
     *  GET /api list all tasks
     *  DELETE /api/{id} delete task
     */
    @ClassRule
    public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
                            .name("todo-api-client")
                            .fromExport(TodoOpenApiV2Connector_IT.class.getResource("TodoOpenApiV2Connector-export"))
                            .customize("$..configuredProperties.period", "5000")
                            .customize("$..configuredProperties.host",
                                String.format("http://%s:%s", GenericContainer.INTERNAL_HOST_HOSTNAME, TODO_SERVER_PORT))
                            .build()
                            .withExposedPorts(SyndesisTestEnvironment.getServerPort(),
                                              SyndesisTestEnvironment.getManagementPort());

    @Test
    @CitrusTest
    public void testAddAndList(@CitrusResource TestCaseRunner runner) {
        runner.variable("id", "citrus:randomNumber(4)");
        runner.variable("task", "Hello from todo client!");

        runner.run(echo("Add new task"));

        runner.when(http().server(todoApiServer)
            .receive()
            .post("/api")
            .message()
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .body("{\"task\":\"${task}\", \"completed\": 0}"));

        runner.then(http().server(todoApiServer)
            .send()
            .response(HttpStatus.CREATED)
            .message()
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .body("{\"id\": ${id}, \"task\":\"${task}\", \"completed\": 0}"));

        runner.run(echo("Update task"));

        runner.when(http().server(todoApiServer)
            .receive()
            .put("/api/${id}")
            .message()
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .body("{\"id\": ${id}, \"task\":\"citrus:upperCase(${task})\", \"completed\": 0}"));

        runner.then(http().server(todoApiServer)
            .send()
            .response(HttpStatus.OK)
            .message()
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .body("{\"id\": ${id}, \"task\":\"${task}\", \"completed\": 0}"));

        runner.run(echo("Fetch task by id"));

        runner.when(http().server(todoApiServer)
            .receive()
            .get("/api/${id}"));

        runner.then(http().server(todoApiServer)
            .send()
            .response(HttpStatus.OK)
            .message()
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .body("{\"id\": ${id}, \"task\":\"${task}\", \"completed\": 0}"));

        runner.run(echo("List all tasks"));

        runner.when(http().server(todoApiServer)
            .receive()
            .get("/api"));

        runner.then(http().server(todoApiServer)
            .send()
            .response(HttpStatus.OK)
            .message()
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .body("[{\"id\": ${id}, \"task\":\"${task}\", \"completed\": 0}]"));

        runner.run(echo("Delete task by id"));

        runner.when(http().server(todoApiServer)
            .receive()
            .delete("/api/${id}"));

        runner.then(http().server(todoApiServer)
            .send()
            .response(HttpStatus.NO_CONTENT));
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
