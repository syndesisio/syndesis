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

package io.syndesis.test.itest.extensions;

import java.net.URISyntaxException;

import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.wait.strategy.Wait;

@ContextConfiguration(classes = IntegrationDependencies_IT.EndpointConfig.class)
public class IntegrationDependencies_IT extends SyndesisIntegrationTestSupport {

    @Test
    public void shouldVerifyDependecyClasspath() throws URISyntaxException {

        Dependency integrationLibraryExtension = Dependency.libraryTag(
            this.getClass().getResource("syndesis-library-test-driver-1.0.0.jar").getFile()
        );

        SyndesisIntegrationRuntimeContainer.Builder integrationContainerBuilder = new SyndesisIntegrationRuntimeContainer.Builder()
            .name("bean-to-log")
            .disableDebug()
            // We need an S2I to load extensions in classpath
            .enableS2IBuild()
            .fromIntegration(
                new Integration.Builder()
                    .id("test-integration")
                    .name("Test Integration")
                    .description("This is a test integration!")
                    .addDependency(integrationLibraryExtension)
                    .addFlow(new Flow.Builder()
                        .id("flow")
                        .addSteps(
                            new Step.Builder()
                                .id("timer")
                                .stepKind(StepKind.endpoint)
                                .connection(new Connection.Builder()
                                    .id("timer-connection")
                                    .connector(new Connector.Builder()
                                        .id("timer")
                                        .putProperty("period",
                                            new ConfigurationProperty.Builder()
                                                .kind("property")
                                                .secret(false)
                                                .componentProperty(false)
                                                .build())
                                        .build())
                                    .build())
                                .putConfiguredProperty("period", "1000")
                                .action(new ConnectorAction.Builder()
                                    .id("periodic-timer-action")
                                    .descriptor(new ConnectorDescriptor.Builder()
                                        .connectorId("timer")
                                        .componentScheme("timer")
                                        .putConfiguredProperty("timerName", "syndesis-timer")
                                        .build())
                                    .build())
                                .build(),
                            new Step.Builder()
                                .id("bean")
                                .stepKind(StepKind.endpoint)
                                .action(new ConnectorAction.Builder()
                                    .descriptor(new ConnectorDescriptor.Builder()
                                        .componentScheme("bean")
                                        .putConfiguredProperty("beanName", "io.syndesis.extensions.test.A")
                                        .putConfiguredProperty("method", "a")
                                        .build())
                                    .build())
                                .build(),
                            new Step.Builder()
                                .id("log")
                                .stepKind(StepKind.log)
                                .putConfiguredProperty("bodyLoggingEnabled", "true")
                                .build()
                        )
                        .build()
                    )
                    .build()
            );

        try (SyndesisIntegrationRuntimeContainer integrationContainer = integrationContainerBuilder.build()
                .waitingFor(Wait.forLogMessage(".*\"message\":\"Body: \\[\\[A\\]\\] \".*\\s", 1))
            ) {
            integrationContainer.start();
        }
    }

}
