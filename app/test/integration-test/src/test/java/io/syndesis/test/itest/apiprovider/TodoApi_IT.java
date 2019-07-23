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
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = TodoApi_IT.EndpointConfig.class)
public class TodoApi_IT extends SyndesisIntegrationTestSupport {

    private static final String VND_OAI_OPENAPI_JSON = "application/vnd.oai.openapi+json";

    @Autowired
    private HttpClient todoListApiClient;

    @Autowired
    private DataSource sampleDb;

    /**
     * Integration uses api provider to enable rest service operations for accessing tasks in the sample database.
     *
     * Available flows in api
     *  GET /todo/{id} provides the task with the given id as Json object
     *  GET /todos provides all available tasks as Json array (using collection support)
     *  GET /todos/open provides all uncompleted tasks as Json array (using basic filter)
     *  GET /todos/done provides all completed tasks as Json array (using basic filter)
     */
    @ClassRule
    public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
                            .name("todo-api")
                            .fromExport(TodoApi_IT.class.getResource("TodoApi-export"))
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
                .url(String.format("http://localhost:%s/health", integrationContainer.getManagementPort()));

        runner.http(action -> action.client(todoListApiClient)
                    .send()
                    .get("/openapi.json"));

        runner.http(builder -> builder.client(todoListApiClient)
                .receive()
                .response(HttpStatus.OK)
                .contentType(VND_OAI_OPENAPI_JSON)
                .payload(new ClassPathResource("todo-api.json", TodoApi_IT.class)));
    }

    @Test
    @CitrusTest
    public void testGetById(@CitrusResource TestRunner runner) {
        runner.sql(builder -> builder.dataSource(sampleDb)
                .statement("insert into todo (id, task, completed) values (9999, 'Walk the dog', 0)"));

        runner.http(builder -> builder.client(todoListApiClient)
                .send()
                .get("/todo/9999"));

        runner.http(builder -> builder.client(todoListApiClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("{\"name\":\"Walk the dog\",\"done\":0}"));
    }

    @Test
    @CitrusTest
    public void testGetAll(@CitrusResource TestRunner runner) {
        runner.sql(builder -> builder.dataSource(sampleDb)
                .statements(Arrays.asList("insert into todo (task, completed) values ('Wash the dog', 0)",
                        "insert into todo (task, completed) values ('Feed the dog', 0)",
                        "insert into todo (task, completed) values ('Play with the dog', 0)")));

        runner.http(builder -> builder.client(todoListApiClient)
                .send()
                .get("/todos"));

        runner.http(builder -> builder.client(todoListApiClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("[{\"name\":\"Wash the dog\",\"done\":0}," +
                            "{\"name\":\"Feed the dog\",\"done\":0}," +
                            "{\"name\":\"Play with the dog\",\"done\":0}]"));
    }

    @Test
    @CitrusTest
    public void testGetOpen(@CitrusResource TestRunner runner) {
        runner.sql(builder -> builder.dataSource(sampleDb)
                .statements(Arrays.asList("insert into todo (task, completed) values ('Wash the dog', 0)",
                        "insert into todo (task, completed) values ('Feed the dog', 0)",
                        "insert into todo (task, completed) values ('Play piano', 1)",
                        "insert into todo (task, completed) values ('Play guitar', 1)",
                        "insert into todo (task, completed) values ('Play with the dog', 0)")));

        runner.http(builder -> builder.client(todoListApiClient)
                .send()
                .get("/todos/open"));

        runner.http(builder -> builder.client(todoListApiClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("[{\"name\":\"Wash the dog\",\"done\":0}," +
                            "{\"name\":\"Feed the dog\",\"done\":0}," +
                            "{\"name\":\"Play with the dog\",\"done\":0}]"));
    }

    @Test
    @CitrusTest
    public void testGetDone(@CitrusResource TestRunner runner) {
        runner.sql(builder -> builder.dataSource(sampleDb)
                .statements(Arrays.asList("insert into todo (task, completed) values ('Wash the dog', 0)",
                        "insert into todo (task, completed) values ('Feed the dog', 0)",
                        "insert into todo (task, completed) values ('Play piano', 1)",
                        "insert into todo (task, completed) values ('Play guitar', 1)",
                        "insert into todo (task, completed) values ('Play with the dog', 0)")));

        runner.http(builder -> builder.client(todoListApiClient)
                .send()
                .get("/todos/done"));

        runner.http(builder -> builder.client(todoListApiClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("[{\"name\":\"Play piano\",\"done\":1}," +
                            "{\"name\":\"Play guitar\",\"done\":1}]"));
    }

    @Configuration
    public static class EndpointConfig {
        @Bean
        public HttpClient todoListApiClient() {
            return CitrusEndpoints.http().client()
                    .requestUrl(String.format("http://localhost:%s", integrationContainer.getServerPort()))
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
