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

package io.syndesis.test.container.server;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.db.SyndesisDbContainer;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

/**
 * @author Christoph Deppisch
 */
public class SyndesisServerContainer extends GenericContainer<SyndesisServerContainer> {

    private static final int SERVER_PORT = 8080;
    private static final int JOLOKIA_PORT = 8778;

    protected SyndesisServerContainer(String imageTag, String javaOptions) {
        super(String.format("syndesis/syndesis-server:%s", imageTag));
        withEnv("JAVA_OPTIONS", javaOptions);
    }

    protected SyndesisServerContainer(String serverJarPath, String javaOptions, boolean deleteOnExit) {
        super(new ImageFromDockerfile("syndesis-server", deleteOnExit)
                .withDockerfileFromBuilder(builder -> builder.from("fabric8/s2i-java:3.0-java8")
                             .env("JAVA_OPTIONS", javaOptions)
                             .expose(SERVER_PORT, JOLOKIA_PORT, SyndesisTestEnvironment.getDebugPort())
                             .build()));

        withClasspathResourceMapping(serverJarPath,"/deployments/server.jar", BindMode.READ_ONLY);
    }

    public static class Builder {
        private String imageTag = SyndesisTestEnvironment.getSyndesisImageTag();
        private boolean deleteOnExit = true;
        private boolean enableLogging = SyndesisTestEnvironment.isLoggingEnabled();
        private boolean enableDebug = SyndesisTestEnvironment.isDebugEnabled();

        private String serverJarPath;

        private Map<String, String> javaOptions = new HashMap<>();

        public Builder() {
            javaOptions.put("encrypt.key", "supersecret");
            javaOptions.put("controllers.dblogging.enabled", "false");
            javaOptions.put("openshift.enabled", "false");
            javaOptions.put("metrics.kind", "noop");
            javaOptions.put("features.monitoring.enabled", "false");
            javaOptions.put("spring.datasource.url", String.format("jdbc:postgresql://syndesis-db:%s/syndesis?sslmode=disable", SyndesisDbContainer.DB_PORT));
            javaOptions.put("spring.datasource.username", "syndesis");
            javaOptions.put("spring.datasource.password", "secret");
            javaOptions.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
            javaOptions.put("dao.kind", "jsondb");
        }

        public SyndesisServerContainer build() {
            SyndesisServerContainer container;
            if (ObjectHelper.isEmpty(serverJarPath)) {
                container = new SyndesisServerContainer(serverJarPath, getJavaOptionString(), deleteOnExit);
            } else {
                container = new SyndesisServerContainer(imageTag, getJavaOptionString());
            }

            if (enableLogging) {
                container.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("SERVER_CONTAINER")));
            }

            container.withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withName("syndesis-server"));

            if (enableDebug) {
                container.withExposedPorts(SERVER_PORT, JOLOKIA_PORT, SyndesisTestEnvironment.getDebugPort());
                container.withCreateContainerCmdModifier(cmd -> cmd.withPortBindings(new PortBinding(Ports.Binding.bindPort(SyndesisTestEnvironment.getDebugPort()), new ExposedPort(SyndesisTestEnvironment.getDebugPort()))));
            } else {
                container.withExposedPorts(SERVER_PORT, JOLOKIA_PORT);
            }

            container.waitingFor(new HttpWaitStrategy().forPort(SERVER_PORT)
                                                       .withStartupTimeout(Duration.ofMinutes(3)));

            return container;
        }

        public SyndesisServerContainer.Builder imageTag(String tag) {
            this.imageTag = tag;
            return this;
        }

        public SyndesisServerContainer.Builder withClasspathServerJar(String serverJarPath) {
            this.serverJarPath = serverJarPath;
            return this;
        }

        public SyndesisServerContainer.Builder withJavaOption(String name, String value) {
            this.javaOptions.put(name, value);
            return this;
        }

        public SyndesisServerContainer.Builder deleteOnExit(boolean deleteOnExit) {
            this.deleteOnExit = deleteOnExit;
            return this;
        }

        public SyndesisServerContainer.Builder enableLogging() {
            this.enableLogging = true;
            return this;
        }

        public SyndesisServerContainer.Builder enableDebug() {
            this.enableDebug = true;
            return this;
        }

        private String getJavaOptionString() {
            StringJoiner stringJoiner = new StringJoiner(" -D", "-D", "");
            stringJoiner.setEmptyValue("");
            javaOptions.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).forEach(stringJoiner::add);

            String optionString = stringJoiner.toString();
            if (enableDebug) {
                return optionString + String.format(" -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=%s", SyndesisTestEnvironment.getDebugPort());
            }

            return optionString;
        }
    }

    public int getServerPort() {
        return getMappedPort(SERVER_PORT);
    }

    public int getJolokiaPort() {
        return getMappedPort(JOLOKIA_PORT);
    }
}
