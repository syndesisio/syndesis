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

import javax.sql.DataSource;
import java.util.Arrays;

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
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

import static com.consol.citrus.actions.ExecuteSQLAction.Builder.sql;
import static com.consol.citrus.actions.ExecuteSQLQueryAction.Builder.query;
import static com.consol.citrus.container.Wait.Builder.waitFor;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = TodoOpenApiV3_IT.EndpointConfig.class)
public class TodoOpenApiV3_IT extends SyndesisIntegrationTestSupport {

    private static final String VND_OAI_OPENAPI_JSON = "application/vnd.oai.openapi+json";

    @Autowired
    private HttpClient todoApiClient;

    @Autowired
    private DataSource sampleDb;

    /**
     * Integration uses api provider to enable rest service operations for accessing tasks in the sample database.
     *
     * Available flows in api
     *  GET /api/{id} provides the task with the given id as Json object
     *  GET /api provides all available tasks as Json array (using collection support)
     *  GET /api/open provides all uncompleted tasks as Json array (using basic filter)
     *  GET /api/done provides all completed tasks as Json array (using basic filter)
     */
    @ClassRule
    public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
                            .name("todo-api")
                            .fromExport(TodoOpenApiV3_IT.class.getResource("TodoOpenApiV3-export"))
                            .build()
                            .withNetwork(getSyndesisDb().getNetwork())
                            .withExposedPorts(SyndesisTestEnvironment.getServerPort(),
                                              SyndesisTestEnvironment.getManagementPort());

    @Test
    @CitrusTest
    public void testHealthCheck(@CitrusResource TestCaseRunner runner) {
        runner.run(waitFor().http()
            .method(HttpMethod.GET.name())
            .seconds(10L)
            .status(HttpStatus.OK.value())
            .url(String.format("http://localhost:%s/actuator/health", integrationContainer.getManagementPort())));
    }

    @Test
    @CitrusTest
    public void testGetOpenApiSpec(@CitrusResource TestCaseRunner runner) {
        cleanupDatabase(runner);

        runner.when(http().client(todoApiClient)
                    .send()
                    .get("/openapi.json"));

        runner.then(http().client(todoApiClient)
                .receive()
                .response(HttpStatus.OK)
                .message()
                .contentType(VND_OAI_OPENAPI_JSON)
                .body(new ClassPathResource("todo-openapi-v3.json", TodoOpenApiV3_IT.class)));
    }

    @Test
    @CitrusTest
    public void testGetTask(@CitrusResource TestCaseRunner runner) {
        cleanupDatabase(runner);

        runner.variable("id", "citrus:randomNumber(4)");

        runner.given(sql(sampleDb)
                .statement("insert into todo (id, task, completed) values (${id}, 'Walk the dog', 0)"));

        runner.when(http().client(todoApiClient)
                .send()
                .get("/api/${id}"));

        runner.then(http().client(todoApiClient)
                .receive()
                .response(HttpStatus.OK)
                .message()
                .body("{\"id\":${id},\"task\":\"Walk the dog\",\"completed\":0}"));
    }

    @Test
    @CitrusTest
    public void testTaskNotFound(@CitrusResource TestCaseRunner runner) {
        cleanupDatabase(runner);

        runner.variable("id", "citrus:randomNumber(4)");

        runner.when(http().client(todoApiClient)
                .send()
                .get("/api/${id}"));

        runner.then(http().client(todoApiClient)
                .receive()
                .response(HttpStatus.NOT_FOUND));
    }

    @Test
    @CitrusTest
    public void testUpdateTask(@CitrusResource TestCaseRunner runner) {
        cleanupDatabase(runner);

        runner.variable("id", "citrus:randomNumber(4)");

        runner.given(sql(sampleDb)
            .statement("insert into todo (id, task, completed) values (${id}, 'Walk the dog', 0)"));

        runner.when(http().client(todoApiClient)
            .send()
            .put("/api/${id}")
            .message()
            .body("{\"id\":${id},\"task\":\"WALK THE DOG\",\"completed\":1}"));

        runner.then(http().client(todoApiClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .body("{\"id\":${id},\"task\":\"WALK THE DOG\",\"completed\":1}"));

        runner.then(query(sampleDb)
            .statement("select task, completed from todo where id=${id}")
            .validate("TASK", "WALK THE DOG")
            .validate("COMPLETED", "1"));
    }

    @Test
    @CitrusTest
    public void testDeleteTask(@CitrusResource TestCaseRunner runner) {
        cleanupDatabase(runner);

        runner.variable("id", "citrus:randomNumber(4)");

        runner.given(sql(sampleDb)
            .statement("insert into todo (id, task, completed) values (${id}, 'Walk the dog', 0)"));

        runner.when(http().client(todoApiClient)
            .send()
            .delete("/api/${id}"));

        runner.then(http().client(todoApiClient)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        runner.then(query(sampleDb)
            .statement("select count(task) as TASKS_FOUND from todo where id=${id}")
            .validate("TASKS_FOUND", "0"));
    }

    @Test
    @CitrusTest
    public void testListTasks(@CitrusResource TestCaseRunner runner) {
        cleanupDatabase(runner);

        runner.given(sql(sampleDb)
                .statements(Arrays.asList("insert into todo (task, completed) values ('Wash the dog', 0)",
                        "insert into todo (task, completed) values ('Feed the dog', 0)",
                        "insert into todo (task, completed) values ('Play with the dog', 0)")));

        runner.when(http().client(todoApiClient)
                .send()
                .get("/api"));

        runner.then(http().client(todoApiClient)
                .receive()
                .response(HttpStatus.OK)
                .message()
                .body("[" +
                        "{\"id\":\"@ignore@\",\"task\":\"Wash the dog\",\"completed\":0}," +
                        "{\"id\":\"@ignore@\",\"task\":\"Feed the dog\",\"completed\":0}," +
                        "{\"id\":\"@ignore@\",\"task\":\"Play with the dog\",\"completed\":0}" +
                    "]"));
    }

    @Configuration
    public static class EndpointConfig {
        @Bean
        public HttpClient todoApiClient() {
            return new HttpClientBuilder()
                    .requestUrl(String.format("http://localhost:%s", integrationContainer.getServerPort()))
                    .build();
        }
    }

    private void cleanupDatabase(TestCaseRunner runner) {
        runner.given(sql(sampleDb)
            .statement("delete from todo"));
    }
}
