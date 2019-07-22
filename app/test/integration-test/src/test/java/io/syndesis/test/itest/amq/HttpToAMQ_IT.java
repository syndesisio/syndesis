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
import java.time.Duration;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.amq.JBossAMQBrokerContainer;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.SocketUtils;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = HttpToAMQ_IT.EndpointConfig.class)
public class HttpToAMQ_IT extends SyndesisIntegrationTestSupport {

    private static int todoServerPort = SocketUtils.findAvailableTcpPort();
    static {
        Testcontainers.exposeHostPorts(todoServerPort);
    }

    @Autowired
    private HttpServer todoApiServer;

    @Autowired
    private JmsEndpoint todoJms;

    @ClassRule
    public static JBossAMQBrokerContainer amqBrokerContainer = new JBossAMQBrokerContainer();

    /**
     * Integration periodically requests list of tasks (as Json array) from Http service and maps the results to AMQ queue.
     * AMQ queue is provided with all tasks (Json array) as message payload.
     */
    @ClassRule
    public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
            .name("http-to-amq")
            .fromExport(HttpToAMQ_IT.class.getResource("HttpToAMQ-export"))
            .customize("$..configuredProperties.schedulerExpression", "5000")
            .customize("$..configuredProperties.baseUrl",
                        String.format("http://%s:%s", GenericContainer.INTERNAL_HOST_HOSTNAME, todoServerPort))
            .build()
            .withNetwork(amqBrokerContainer.getNetwork())
            .waitingFor(Wait.defaultWaitStrategy().withStartupTimeout(Duration.ofSeconds(SyndesisTestEnvironment.getContainerStartupTimeout())));

    @Test
    @CitrusTest
    public void testHttpToAMQ(@CitrusResource TestRunner runner) {
        runner.http(builder -> builder.server(todoApiServer)
                .receive()
                .get());

        runner.http(builder -> builder.server(todoApiServer)
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
        runner.http(builder -> builder.server(todoApiServer)
                .receive()
                .get());

        runner.http(builder -> builder.server(todoApiServer)
                .send()
                .response(HttpStatus.OK)
                .payload("[]"));

        runner.receive(builder -> builder.endpoint(todoJms)
                        .payload("[]"));
    }

    @Configuration
    public static class EndpointConfig {
        @Bean
        public ConnectionFactory connectionFactory() {
            return new ActiveMQConnectionFactory(amqBrokerContainer.getUsername(),
                                                 amqBrokerContainer.getPassword(),
                                                 String.format("tcp://localhost:%s", amqBrokerContainer.getOpenwirePort()));
        }

        @Bean
        public JmsEndpoint todoJms() {
            return CitrusEndpoints.jms()
                    .asynchronous()
                    .connectionFactory(connectionFactory())
                    .destination("todos")
                    .build();
        }

        @Bean
        public HttpServer todoApiServer() {
            return CitrusEndpoints.http()
                    .server()
                    .port(todoServerPort)
                    .autoStart(true)
                    .timeout(60000L)
                    .build();
        }
    }
}
