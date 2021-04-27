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

package io.syndesis.test.itest.amq;

import javax.jms.ConnectionFactory;

import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.amq.JBossAMQBrokerContainer;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.util.SocketUtils;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.jms.endpoint.JmsEndpoint;

@org.testcontainers.junit.jupiter.Testcontainers
public class HttpToAMQ_IT extends SyndesisIntegrationTestSupport {

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

    private final JmsEndpoint todoJms = CitrusEndpoints.jms()
        .asynchronous()
        .connectionFactory(connectionFactory())
        .destination("todos")
        .build();

    @Container
    public static final JBossAMQBrokerContainer AMQ_BROKER = new JBossAMQBrokerContainer();

    /**
     * Integration periodically requests list of tasks (as Json array) from Http service and maps the results to AMQ queue.
     * AMQ queue is provided with all tasks (Json array) as message payload.
     */
    @Container
    public static final SyndesisIntegrationRuntimeContainer INTEGRATION_CONTAINER = new SyndesisIntegrationRuntimeContainer.Builder()
            .name("http-to-amq")
            .fromExport(HttpToAMQ_IT.class.getResource("HttpToAMQ-export"))
            .customize("$..configuredProperties.schedulerExpression", "5000")
            .customize("$..configuredProperties.baseUrl",
                        String.format("http://%s:%s", GenericContainer.INTERNAL_HOST_HOSTNAME, TODO_SERVER_PORT))
            .build()
            .withNetwork(AMQ_BROKER.getNetwork())
            .waitingFor(Wait.defaultWaitStrategy().withStartupTimeout(SyndesisTestEnvironment.getContainerStartupTimeout()));

    @Test
    @CitrusTest
    public void testHttpToAMQ(@CitrusResource TestRunner runner) {
        runner.http(builder -> builder.server(TODO_API_SERVER)
                .receive()
                .get());

        runner.http(builder -> builder.server(TODO_API_SERVER)
                .send()
                .response(HttpStatus.OK)
                .payload("[{\"id\": \"1\", \"task\":\"Learn to play drums\", \"completed\": 0}," +
                          "{\"id\": \"2\", \"task\":\"Learn to play guitar\", \"completed\": 1}]"));

        runner.receive(builder -> builder.endpoint(todoJms)
                        .payload("[{\"id\": \"1\", \"name\":\"Learn to play drums\", \"done\": 0}," +
                                  "{\"id\": \"2\", \"name\":\"Learn to play guitar\", \"done\": 1}]"));
    }

    @Test
    @CitrusTest
    public void testHttpToAMQEmptyList(@CitrusResource TestRunner runner) {
        runner.http(builder -> builder.server(TODO_API_SERVER)
                .receive()
                .get());

        runner.http(builder -> builder.server(TODO_API_SERVER)
                .send()
                .response(HttpStatus.OK)
                .payload("[]"));

        runner.receive(builder -> builder.endpoint(todoJms)
                        .payload("[]"));
    }

    private static ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory(AMQ_BROKER.getUsername(),
            AMQ_BROKER.getPassword(),
            String.format("tcp://localhost:%s", AMQ_BROKER.getOpenwirePort()));
    }
}
