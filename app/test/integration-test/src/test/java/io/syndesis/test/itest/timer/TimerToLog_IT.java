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

package io.syndesis.test.itest.timer;

import java.util.Arrays;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.integration.source.JsonIntegrationSource;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;
import org.junit.Test;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * @author Christoph Deppisch
 */
public class TimerToLog_IT extends SyndesisIntegrationTestSupport {

    @Test
    public void timeToLogTest() {
        SyndesisIntegrationRuntimeContainer.Builder integrationContainerBuilder = new SyndesisIntegrationRuntimeContainer.Builder()
                .name("timer-to-log")
                .fromFlow(new Flow.Builder()
                    .steps(Arrays.asList(new Step.Builder()
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
                                    .putConfiguredProperty("timer-name", "syndesis-timer")
                                    .build())
                            .build())
                        .build(),
                    new Step.Builder()
                        .stepKind(StepKind.log)
                        .putConfiguredProperty("bodyLoggingEnabled", "false")
                        .putConfiguredProperty("contextLoggingEnabled", "false")
                        .putConfiguredProperty("customText", "Hello Syndesis!")
                        .build()))
                    .build());

        try (SyndesisIntegrationRuntimeContainer integrationContainer = integrationContainerBuilder.build()
                        .waitingFor(Wait.forLogMessage(".*\"message\":\"Hello Syndesis!\".*\\s", 1))) {
            integrationContainer.start();
        }
    }

    @Test
    public void timeToLogExportTest() {
        SyndesisIntegrationRuntimeContainer.Builder integrationContainerBuilder = new SyndesisIntegrationRuntimeContainer.Builder()
                .name("timer-to-log-export")
                .fromExport(TimerToLog_IT.class.getResourceAsStream("TimerToLog-export.zip"));

        try (SyndesisIntegrationRuntimeContainer integrationContainer = integrationContainerBuilder.build()
                .waitingFor(Wait.forLogMessage(".*\"message\":\"Hello Syndesis!\".*\\s", 2))) {
            integrationContainer.start();
        }
    }

    @Test
    public void timeToLogJsonTest() {
        SyndesisIntegrationRuntimeContainer.Builder integrationContainerBuilder = new SyndesisIntegrationRuntimeContainer.Builder()
                .name("timer-to-log-json")
                .fromSource(new JsonIntegrationSource(TimerToLog_IT.class.getResourceAsStream("TimerToLog.json")));

        try (SyndesisIntegrationRuntimeContainer integrationContainer = integrationContainerBuilder.build()
                .waitingFor(Wait.forLogMessage(".*\"message\":\"Hello Syndesis!\".*\\s", 1))) {
            integrationContainer.start();
        }
    }
}
