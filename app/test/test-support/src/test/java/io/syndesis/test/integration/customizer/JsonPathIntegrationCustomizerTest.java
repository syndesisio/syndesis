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

package io.syndesis.test.integration.customizer;

import java.util.Arrays;
import java.util.UUID;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Christoph Deppisch
 */
public class JsonPathIntegrationCustomizerTest {

    @Test
    public void shouldCustomizeProperties() {
        Integration toCustomize = new Integration.Builder()
                .name("test")
                .addConnection(new Connection.Builder()
                    .id(UUID.randomUUID().toString())
                    .name("test-connection")
                    .putConfiguredProperty("connection-property", "initial")
                    .build())
                .addFlow(new Flow.Builder()
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
                    .build())
                .build();

        Assert.assertEquals(toCustomize, new JsonPathIntegrationCustomizer(null, null, null).apply(toCustomize));
        Assert.assertEquals("customized", new JsonPathIntegrationCustomizer("$..connection-property", "customized").apply(toCustomize).getConnections().get(0).getConfiguredProperties().get("connection-property"));
        Assert.assertEquals("customized", new JsonPathIntegrationCustomizer("$..customText", "customized").apply(toCustomize).getFlows().get(0).getSteps().get(1).getConfiguredProperties().get("customText"));
        Assert.assertEquals("foo", new JsonPathIntegrationCustomizer("$..configuredProperties", "new_key", "foo").apply(toCustomize).getFlows().get(0).getSteps().get(1).getConfiguredProperties().get("new_key"));
    }
}
