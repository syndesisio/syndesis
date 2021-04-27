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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.jms.endpoint.JmsEndpoint;

@Testcontainers
public class AMQToHttp_IT extends SyndesisIntegrationTestSupport {

    private static final int TODO_SERVER_PORT = SocketUtils.findAvailableTcpPort();
    static {
        org.testcontainers.Testcontainers.exposeHostPorts(TODO_SERVER_PORT);
    }

    private static final HttpServer TODO_API_SERVER = startup(CitrusEndpoints.http()
            .server()
            .port(TODO_SERVER_PORT)
            .autoStart(true)
            .timeout(60000L)
            .build());

    private final JmsEndpoint todoJms =  CitrusEndpoints.jms()
        .asynchronous()
        .connectionFactory(connectionFactory())
        .destination("todos")
        .build();

    @Container
    public static final JBossAMQBrokerContainer amqBrokerContainer = new JBossAMQBrokerContainer();

    /**
     * Integration waits for messages on AMQ queue and maps incoming tasks to Http service. Both AMQ and Http connections use
     * Json instance schema definitions. Data mapper maps from one specification to the other.
     */
    @Container
    public static final SyndesisIntegrationRuntimeContainer INTEGRATION_CONTAINER = new SyndesisIntegrationRuntimeContainer.Builder()
            .name("amq-to-http")
            .fromExport(AMQToHttp_IT.class.getResource("AMQToHttp-export"))
            .customize("$..configuredProperties.schedulerExpression", "5000")
            .customize("$..configuredProperties.baseUrl",
                        String.format("http://%s:%s", GenericContainer.INTERNAL_HOST_HOSTNAME, TODO_SERVER_PORT))
            .build()
            .withNetwork(amqBrokerContainer.getNetwork())
            .waitingFor(Wait.defaultWaitStrategy().withStartupTimeout(SyndesisTestEnvironment.getContainerStartupTimeout()));

    @Test
    @CitrusTest
    public void testHttpToAMQ(@CitrusResource TestRunner runner) {
        runner.send(builder -> builder.endpoint(todoJms)
                .payload("{\"id\": \"1\", \"name\":\"Learn some #golang\", \"done\": 1}"));

        runner.http(builder -> builder.server(TODO_API_SERVER)
                .receive()
                .post()
                .payload("{\"id\": \"1\", \"task\":\"Learn some #golang\", \"completed\": 1}"));

        runner.http(builder -> builder.server(TODO_API_SERVER)
                .send()
                .response(HttpStatus.CREATED));

    }

    private static ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory(amqBrokerContainer.getUsername(),
                                             amqBrokerContainer.getPassword(),
                                             String.format("tcp://localhost:%s", amqBrokerContainer.getOpenwirePort()));
    }

}
