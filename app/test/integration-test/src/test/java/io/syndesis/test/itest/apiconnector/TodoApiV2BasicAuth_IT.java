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
@ContextConfiguration(classes = TodoApiV2BasicAuth_IT.EndpointConfig.class)
public class TodoApiV2BasicAuth_IT extends SyndesisIntegrationTestSupport {

    private static final int TODO_SERVER_PORT = SocketUtils.findAvailableTcpPort();
    static {
        Testcontainers.exposeHostPorts(TODO_SERVER_PORT);
    }

    @Autowired
    private HttpServer todoApiServer;

    /**
     * Integration uses api connector to send OpenAPI client requests to a REST endpoint. The client API connector was generated
     * from OpenAPI 2.x specification and uses basic authentication security scheme.
     *
     * The test verifies the correct basic authentication headers to be sent by the client.
     *  GET /api list all tasks
     */
    @ClassRule
    public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
                            .name("todo-api-client")
                            .fromExport(TodoApiV2BasicAuth_IT.class.getResource("TodoApiV2BasicAuth-export"))
                            .customize("$..configuredProperties.period", "5000")
                            .customize("$..configuredProperties.host",
                                String.format("http://%s:%s", GenericContainer.INTERNAL_HOST_HOSTNAME, TODO_SERVER_PORT))
                            .build()
                            .withExposedPorts(SyndesisTestEnvironment.getServerPort(),
                                              SyndesisTestEnvironment.getManagementPort());

    @Test
    @CitrusTest
    public void testApiConnectorUsingBasicAuth(@CitrusResource TestCaseRunner runner) {
        runner.variable("id", "citrus:randomNumber(4)");
        runner.variable("task", "Syndesis rocks!");

        runner.run(echo("List all tasks"));

        runner.when(http().server(todoApiServer)
            .receive()
            .get("/api")
            .message()
            .header("Authorization", "Basic citrus:encodeBase64('syndesis:secret')"));

        runner.then(http().server(todoApiServer)
            .send()
            .response(HttpStatus.OK)
            .message()
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .body("[{\"id\": ${id}, \"task\":\"${task}\", \"completed\": 0}]"));
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
