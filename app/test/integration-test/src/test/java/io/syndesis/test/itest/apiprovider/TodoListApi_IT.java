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

package io.syndesis.test.itest.apiprovider;

import java.util.Arrays;

import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.http.client.HttpClient;

@Testcontainers
public class TodoListApi_IT extends SyndesisIntegrationTestSupport {

    private static final String VND_OAI_OPENAPI_JSON = "application/vnd.oai.openapi+json";

    private final HttpClient todoListApiClient = CitrusEndpoints.http().client()
        .requestUrl(String.format("http://localhost:%s", INTEGRATION_CONTAINER.getServerPort()))
        .build();

    /**
     * Integration uses api provider to enable rest service operations for accessing tasks in the sample database.
     * Available flows in api
     *  POST /todos stores given tasks (Json array) to the sample database (using inbound collection support and split/aggregate)
     *  GET /todos provides all available tasks as Json array (using collection support)
     */
    @Container
    public static final SyndesisIntegrationRuntimeContainer INTEGRATION_CONTAINER = new SyndesisIntegrationRuntimeContainer.Builder()
                            .name("todo-list-api")
                            .fromExport(TodoListApi_IT.class.getResource("TodoListApi-export"))
                            .build()
                            .withNetwork(getSyndesisDb().getNetwork())
                            .withExposedPorts(SyndesisTestEnvironment.getServerPort(),
                                              SyndesisTestEnvironment.getManagementPort());

    @Test
    @CitrusTest
    public void testGetOpenApiSpec(@CitrusResource TestRunner runner) {
        runner.waitFor().http()
                .method(HttpMethod.GET)
                .seconds(10L)
                .status(HttpStatus.OK)
                .url(String.format("http://localhost:%s/actuator/health", INTEGRATION_CONTAINER.getManagementPort()));

        runner.http(action -> action.client(todoListApiClient)
                .send()
                .get("/openapi.json"));

        runner.http(builder -> builder.client(todoListApiClient)
                .receive()
                .response(HttpStatus.OK)
                .contentType(VND_OAI_OPENAPI_JSON)
                .payload(new ClassPathResource("todolist-api.json", TodoApi_IT.class)));
    }

    @Test
    @CitrusTest
    public void testAddTodoList(@CitrusResource TestRunner runner) {
        runner.http(builder -> builder.client(todoListApiClient)
                .send()
                .post("/todos")
                .payload("[{\"name\":\"Wash the cat\",\"done\":0}," +
                            "{\"name\":\"Feed the cat\",\"done\":0}," +
                            "{\"name\":\"Play with the cat\",\"done\":0}]"));

        runner.http(builder -> builder.client(todoListApiClient)
                .receive()
                .response(HttpStatus.CREATED));

        runner.query(builder -> builder.dataSource(sampleDb())
                .statement("select task, completed from todo")
                .validate("task", "Wash the cat", "Feed the cat", "Play with the cat")
                .validate("completed", "0", "0", "0"));
    }

    @Test
    @CitrusTest
    public void testGetTodoList(@CitrusResource TestRunner runner) {
        runner.sql(builder -> builder.dataSource(sampleDb())
                .statements(Arrays.asList("insert into todo (task, completed) values ('Wash the dog', 0)",
                        "insert into todo (task, completed) values ('Feed the dog', 0)",
                        "insert into todo (task, completed) values ('Play with the dog', 0)")));

        runner.http(builder -> builder.client(todoListApiClient)
                .send()
                .get("/todos"));

        runner.http(builder -> builder.client(todoListApiClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("[{\"id\":\"@ignore@\",\"name\":\"Wash the dog\",\"done\":0}," +
                            "{\"id\":\"@ignore@\",\"name\":\"Feed the dog\",\"done\":0}," +
                            "{\"id\":\"@ignore@\",\"name\":\"Play with the dog\",\"done\":0}]"));
    }

    @Test
    @CitrusTest
    public void testGetEmptyTodoList(@CitrusResource TestRunner runner) {
        runner.http(builder -> builder.client(todoListApiClient)
                .send()
                .get("/todos"));

        runner.http(builder -> builder.client(todoListApiClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("[]"));
    }

}
