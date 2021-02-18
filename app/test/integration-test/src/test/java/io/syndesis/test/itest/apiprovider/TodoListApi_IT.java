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
@ContextConfiguration(classes = TodoListApi_IT.EndpointConfig.class)
public class TodoListApi_IT extends SyndesisIntegrationTestSupport {

    private static final String VND_OAI_OPENAPI_JSON = "application/vnd.oai.openapi+json";

    @Autowired
    private HttpClient todoListApiClient;

    @Autowired
    private DataSource sampleDb;

    /**
     * Integration uses api provider to enable rest service operations for accessing tasks in the sample database.
     *
     * Available flows in api
     *  POST /todos stores given tasks (Json array) to the sample database (using inbound collection support and split/aggregate)
     *  GET /todos provides all available tasks as Json array (using collection support)
     */
    @ClassRule
    public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
                            .name("todo-list-api")
                            .fromExport(TodoListApi_IT.class.getResource("TodoListApi-export"))
                            .build()
                            .withNetwork(getSyndesisDb().getNetwork())
                            .withExposedPorts(SyndesisTestEnvironment.getServerPort(),
                                              SyndesisTestEnvironment.getManagementPort());

    @Test
    @CitrusTest
    public void testGetOpenApiSpec(@CitrusResource TestCaseRunner runner) {
        cleanupDatabase(runner);

        runner.given(waitFor().http()
                .method(HttpMethod.GET.name())
                .seconds(10L)
                .status(HttpStatus.OK.value())
                .url(String.format("http://localhost:%s/actuator/health", integrationContainer.getManagementPort())));

        runner.when(http().client(todoListApiClient)
                .send()
                .get("/openapi.json"));

        runner.then(http().client(todoListApiClient)
                .receive()
                .response(HttpStatus.OK)
                .message()
                .contentType(VND_OAI_OPENAPI_JSON)
                .body(new ClassPathResource("todolist-api.json", TodoApi_IT.class)));
    }

    @Test
    @CitrusTest
    public void testAddTodoList(@CitrusResource TestCaseRunner runner) {
        cleanupDatabase(runner);

        runner.given(http().client(todoListApiClient)
                .send()
                .post("/todos")
                .message()
                .body("[{\"name\":\"Wash the cat\",\"done\":0}," +
                            "{\"name\":\"Feed the cat\",\"done\":0}," +
                            "{\"name\":\"Play with the cat\",\"done\":0}]"));

        runner.when(http().client(todoListApiClient)
                .receive()
                .response(HttpStatus.CREATED));

        runner.then(query(sampleDb)
                .statement("select task, completed from todo")
                .validate("task", "Wash the cat", "Feed the cat", "Play with the cat")
                .validate("completed", "0", "0", "0"));
    }

    @Test
    @CitrusTest
    public void testGetTodoList(@CitrusResource TestCaseRunner runner) {
        cleanupDatabase(runner);

        runner.given(sql(sampleDb)
                .statements(Arrays.asList("insert into todo (task, completed) values ('Wash the dog', 0)",
                        "insert into todo (task, completed) values ('Feed the dog', 0)",
                        "insert into todo (task, completed) values ('Play with the dog', 0)")));

        runner.when(http().client(todoListApiClient)
                .send()
                .get("/todos"));

        runner.then(http().client(todoListApiClient)
                .receive()
                .response(HttpStatus.OK)
                .message()
                .body("[{\"id\":\"@ignore@\",\"name\":\"Wash the dog\",\"done\":0}," +
                            "{\"id\":\"@ignore@\",\"name\":\"Feed the dog\",\"done\":0}," +
                            "{\"id\":\"@ignore@\",\"name\":\"Play with the dog\",\"done\":0}]"));
    }

    @Test
    @CitrusTest
    public void testGetEmptyTodoList(@CitrusResource TestCaseRunner runner) {
        cleanupDatabase(runner);

        runner.when(http().client(todoListApiClient)
                .send()
                .get("/todos"));

        runner.then(http().client(todoListApiClient)
                .receive()
                .response(HttpStatus.OK)
                .message()
                .body("[]"));
    }

    @Configuration
    public static class EndpointConfig {
        @Bean
        public HttpClient todoListApiClient() {
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
