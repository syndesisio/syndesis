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

package io.syndesis.test.integration.project;

import java.nio.file.Path;

import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.test.SyndesisTestEnvironment;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Christoph Deppisch
 */
public class SpringBootProjectBuilderTest {

    Integration integration = new Integration.Builder()
            .name("test-integration")
            .addConnection(new Connection.Builder()
                    .name("test-connection")
                    .connector(new Connector.Builder()
                            .name("test-connector")
                            .addAction(new ConnectorAction.Builder()
                                    .name("test-action")
                                    .descriptor(new ConnectorDescriptor.Builder()
                                            .componentScheme("test")
                                            .build())
                                    .build())
                            .putConfiguredProperty("password", "»ENC:7dceb0f85800ce4fb9e8ac9b263f477c99d6eeffb3e2418c8461f756e69bd288")
                            .build())
                    .build())
            .addFlow(new Flow.Builder()
                    .name("test-flow")
                    .addStep(new Step.Builder()
                            .stepKind(StepKind.log)
                            .putConfiguredProperty("customText", "Hello from Syndesis")
                            .build())
                    .addStep(new Step.Builder()
                            .stepKind(StepKind.endpoint)
                            .action(new ConnectorAction.Builder()
                                    .descriptor(new ConnectorDescriptor.Builder()
                                            .build())
                                    .build())
                                    .addDependency(Dependency.maven("org.apache.camel:camel-test:latest"))
                            .connection(new Connection.Builder()
                                    .name("test-connection")
                                    .connector(new Connector.Builder()
                                            .name("test-connector")
                                            .componentScheme("mock")
                                            .addAction(new ConnectorAction.Builder()
                                                    .name("test-action")
                                                    .descriptor(new ConnectorDescriptor.Builder()
                                                            .componentScheme("test")
                                                            .build())
                                                    .build())
                                            .build())
                                    .putConfiguredProperty("password", "»ENC:7dceb0f85800ce4fb9e8ac9b263f477c99d6eeffb3e2418c8461f756e69bd288")
                                    .build())
                            .build())
                    .build())
            .build();

    @Test
    public void shouldBuildProject() {
        SpringBootProjectBuilder projectBuilder = new SpringBootProjectBuilder("test-project", SyndesisTestEnvironment.getSyndesisVersion())
                                                        .withOutputDirectory(SyndesisTestEnvironment.getOutputDirectory());

        Path projectDir = projectBuilder.build(() -> integration);
        Assert.assertNotNull(projectDir.resolve("pom.xml"));
        Assert.assertNotNull(projectDir.resolve("src").resolve("main").resolve("resources").resolve("application.properties"));
        Assert.assertNotNull(projectDir.resolve("src").resolve("main").resolve("resources").resolve("syndesis").resolve("integration").resolve("integration.json"));
    }

}
