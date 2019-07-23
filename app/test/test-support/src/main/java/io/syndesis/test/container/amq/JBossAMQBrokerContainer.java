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

package io.syndesis.test.container.amq;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * @author Christoph Deppisch
 */
public class JBossAMQBrokerContainer extends GenericContainer<JBossAMQBrokerContainer> {

    private static final int OPENWIRE_PORT = 61616;
    private static final int STOMP_PORT = 61613;
    private static final int AMQP_PORT = 5672;
    private static final int MQTT_PORT = 1883;
    private static final int JOLOKIA_PORT = 8778;

    private static final String USERNAME = "amq";
    private static final String PASSWORD = "secret";

    private static final String IMAGE_VERSION = "1.3";

    public JBossAMQBrokerContainer() {
        super(String.format("registry.access.redhat.com/jboss-amq-6/amq63-openshift:%s", IMAGE_VERSION));

        withEnv("AMQ_USER", USERNAME);
        withEnv("AMQ_PASSWORD", PASSWORD);
        withEnv("AMQ_TRANSPORTS", "openwire,stomp,amqp,mqtt");

        withExposedPorts(OPENWIRE_PORT);
        withExposedPorts(STOMP_PORT);
        withExposedPorts(AMQP_PORT);
        withExposedPorts(MQTT_PORT);
        withExposedPorts(JOLOKIA_PORT);

        withNetwork(Network.newNetwork());
        withNetworkAliases("broker-amq-tcp");

        withCreateContainerCmdModifier(cmd -> cmd.withName("broker-amq"));

        waitingFor(Wait.forLogMessage(".*Apache ActiveMQ.*started.*\\s", 1));
    }

    public int getOpenwirePort() {
        return getMappedPort(OPENWIRE_PORT);
    }

    public int getStompPort() {
        return getMappedPort(STOMP_PORT);
    }

    public int getAmqpPort() {
        return getMappedPort(AMQP_PORT);
    }

    public int getMqttPort() {
        return getMappedPort(MQTT_PORT);
    }

    public int getJolokiaPort() {
        return getMappedPort(JOLOKIA_PORT);
    }

    public String getUsername() {
        return USERNAME;
    }

    public String getPassword() {
        return PASSWORD;
    }
}
