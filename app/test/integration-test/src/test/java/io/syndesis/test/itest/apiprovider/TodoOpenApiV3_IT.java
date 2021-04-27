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
public class TodoOpenApiV3_IT extends SyndesisIntegrationTestSupport {

    private static final String VND_OAI_OPENAPI_JSON = "application/vnd.oai.openapi+json";

    private final HttpClient todoApiClient = CitrusEndpoints.http().client()
        .requestUrl(String.format("http://localhost:%s", INTEGRATION_CONTAINER.getServerPort()))
        .build();

    /**
     * Integration uses api provider to enable rest service operations for accessing tasks in the sample database.
     * Available flows in api
     *  GET /api/{id} provides the task with the given id as Json object
     *  GET /api provides all available tasks as Json array (using collection support)
     *  GET /api/open provides all uncompleted tasks as Json array (using basic filter)
     *  GET /api/done provides all completed tasks as Json array (using basic filter)
     */
    @Container
    public static final SyndesisIntegrationRuntimeContainer INTEGRATION_CONTAINER = new SyndesisIntegrationRuntimeContainer.Builder()
                            .name("todo-api")
                            .fromExport(TodoOpenApiV3_IT.class.getResource("TodoOpenApiV3-export"))
                            .build()
                            .withNetwork(getSyndesisDb().getNetwork())
                            .withExposedPorts(SyndesisTestEnvironment.getServerPort(),
                                              SyndesisTestEnvironment.getManagementPort());

    @Test
    @CitrusTest
    public void testHealthCheck(@CitrusResource TestRunner runner) {
        runner.waitFor().http()
            .method(HttpMethod.GET)
            .seconds(10L)
            .status(HttpStatus.OK)
            .url(String.format("http://localhost:%s/actuator/health", INTEGRATION_CONTAINER.getManagementPort()));
    }

    @Test
    @CitrusTest
    public void testGetOpenApiSpec(@CitrusResource TestRunner runner) {
        runner.http(action -> action.client(todoApiClient)
                    .send()
                    .get("/openapi.json"));

        runner.http(builder -> builder.client(todoApiClient)
                .receive()
                .response(HttpStatus.OK)
                .contentType(VND_OAI_OPENAPI_JSON)
                .payload(new ClassPathResource("todo-openapi-v3.json", TodoOpenApiV3_IT.class)));
    }

    @Test
    @CitrusTest
    public void testGetTask(@CitrusResource TestRunner runner) {
        runner.variable("id", "citrus:randomNumber(4)");

        runner.sql(builder -> builder.dataSource(sampleDb())
                .statement("insert into todo (id, task, completed) values (${id}, 'Walk the dog', 0)"));

        runner.http(builder -> builder.client(todoApiClient)
                .send()
                .get("/api/${id}"));

        runner.http(builder -> builder.client(todoApiClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("{\"id\":${id},\"task\":\"Walk the dog\",\"completed\":0}"));
    }

    @Test
    @CitrusTest
    public void testTaskNotFound(@CitrusResource TestRunner runner) {
        runner.variable("id", "citrus:randomNumber(4)");

        runner.http(builder -> builder.client(todoApiClient)
                .send()
                .get("/api/${id}"));

        runner.http(builder -> builder.client(todoApiClient)
                .receive()
                .response(HttpStatus.NOT_FOUND));
    }

    @Test
    @CitrusTest
    public void testUpdateTask(@CitrusResource TestRunner runner) {
        runner.variable("id", "citrus:randomNumber(4)");

        runner.sql(builder -> builder.dataSource(sampleDb())
            .statement("insert into todo (id, task, completed) values (${id}, 'Walk the dog', 0)"));

        runner.http(builder -> builder.client(todoApiClient)
            .send()
            .put("/api/${id}")
            .payload("{\"id\":${id},\"task\":\"WALK THE DOG\",\"completed\":1}"));

        runner.http(builder -> builder.client(todoApiClient)
            .receive()
            .response(HttpStatus.OK)
            .payload("{\"id\":${id},\"task\":\"WALK THE DOG\",\"completed\":1}"));

        runner.query(builder -> builder.dataSource(sampleDb())
            .statement("select task, completed from todo where id=${id}")
            .validate("TASK", "WALK THE DOG")
            .validate("COMPLETED", "1"));
    }

    @Test
    @CitrusTest
    public void testDeleteTask(@CitrusResource TestRunner runner) {
        runner.variable("id", "citrus:randomNumber(4)");

        runner.sql(builder -> builder.dataSource(sampleDb())
            .statement("insert into todo (id, task, completed) values (${id}, 'Walk the dog', 0)"));

        runner.http(builder -> builder.client(todoApiClient)
            .send()
            .delete("/api/${id}"));

        runner.http(builder -> builder.client(todoApiClient)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        runner.query(builder -> builder.dataSource(sampleDb())
            .statement("select count(task) as TASKS_FOUND from todo where id=${id}")
            .validate("TASKS_FOUND", "0"));
    }

    @Test
    @CitrusTest
    public void testListTasks(@CitrusResource TestRunner runner) {
        runner.sql(builder -> builder.dataSource(sampleDb())
                .statements(Arrays.asList("insert into todo (task, completed) values ('Wash the dog', 0)",
                        "insert into todo (task, completed) values ('Feed the dog', 0)",
                        "insert into todo (task, completed) values ('Play with the dog', 0)")));

        runner.http(builder -> builder.client(todoApiClient)
                .send()
                .get("/api"));

        runner.http(builder -> builder.client(todoApiClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("[" +
                        "{\"id\":\"@ignore@\",\"task\":\"Wash the dog\",\"completed\":0}," +
                        "{\"id\":\"@ignore@\",\"task\":\"Feed the dog\",\"completed\":0}," +
                        "{\"id\":\"@ignore@\",\"task\":\"Play with the dog\",\"completed\":0}" +
                    "]"));
    }

}
