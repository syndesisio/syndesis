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

import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.http.server.HttpServerBuilder;
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import com.consol.citrus.jms.endpoint.JmsEndpointBuilder;
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

import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = AMQToHttp_IT.EndpointConfig.class)
public class AMQToHttp_IT extends SyndesisIntegrationTestSupport {

    private static final int TODO_SERVER_PORT = SocketUtils.findAvailableTcpPort();
    static {
        Testcontainers.exposeHostPorts(TODO_SERVER_PORT);
    }

    @Autowired
    private HttpServer todoApiServer;

    @Autowired
    private JmsEndpoint todoJms;

    @ClassRule
    public static JBossAMQBrokerContainer amqBrokerContainer = new JBossAMQBrokerContainer();

    /**
     * Integration waits for messages on AMQ queue and maps incoming tasks to Http service. Both AMQ and Http connections use
     * Json instance schema definitions. Data mapper maps from one specification to the other.
     */
    @ClassRule
    public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
            .name("amq-to-http")
            .fromExport(AMQToHttp_IT.class.getResource("AMQToHttp-export"))
            .customize("$..configuredProperties.schedulerExpression", "5000")
            .customize("$..configuredProperties.baseUrl",
                        String.format("http://%s:%s", GenericContainer.INTERNAL_HOST_HOSTNAME, TODO_SERVER_PORT))
            .build()
            .dependsOn(amqBrokerContainer)
            .withNetwork(amqBrokerContainer.getNetwork());

    @Test
    @CitrusTest
    public void testHttpToAMQ(@CitrusResource TestCaseRunner runner) {
        runner.given(send().endpoint(todoJms)
                .message()
                .body("{\"id\": \"1\", \"name\":\"Learn some #golang\", \"done\": 1}"));

        runner.when(http().server(todoApiServer)
                .receive()
                .post()
                .message()
                .body("{\"id\": \"1\", \"task\":\"Learn some #golang\", \"completed\": 1}"));

        runner.then(http().server(todoApiServer)
                .send()
                .response(HttpStatus.CREATED));

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
            return new JmsEndpointBuilder()
                    .connectionFactory(connectionFactory())
                    .destination("todos")
                    .build();
        }

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
