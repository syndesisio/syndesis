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
package io.syndesis.server.controller.integration.camelk.customizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.Json;
import io.syndesis.server.controller.integration.camelk.TestResourceManager;
import io.syndesis.server.openshift.Exposure;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class KnativeCustomizerTest {

    @Test
    public void testKnativeCustomizerChannels() throws IOException {
        Connector connector;
        ConnectorAction sinkAction;
        ConnectorAction sourceAction;

        try (InputStream is = KnativeCustomizerTest.class.getResourceAsStream("/META-INF/syndesis/connector/knative.json")) {
            connector = Json.readFromStream(is, Connector.class);
            sinkAction = connector.getActions(ConnectorAction.class).stream()
                .filter(a -> a.getId().get().equals("io.syndesis:knative-channel-send-connector"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException());
            sourceAction = connector.getActions(ConnectorAction.class).stream()
                .filter(a -> a.getId().get().equals("io.syndesis:knative-channel-receive-connector"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException());
        }

        TestResourceManager manager = new TestResourceManager();
        Integration integration = manager.newIntegration(
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .putConfiguredProperty("name", "my-channel-sink")
                .action(sinkAction)
                .connection(new Connection.Builder()
                    .connector(connector)
                    .build())
                .build(),
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .putConfiguredProperty("name", "my-channel-source")
                .action(sourceAction)
                .connection(new Connection.Builder()
                    .connector(connector)
                    .build())
                .build()
        );

        IntegrationDeployment deployment = new IntegrationDeployment.Builder()
            .userId("user")
            .id("idId")
            .spec(integration)
            .build();

        CamelKIntegrationCustomizer customizer = new KnativeCustomizer();

        io.syndesis.server.controller.integration.camelk.crd.Integration i = customizer.customize(
            deployment,
            new io.syndesis.server.controller.integration.camelk.crd.Integration(),
            EnumSet.of(Exposure.SERVICE)
        );

        assertThat(i.getSpec().getTraits()).containsKey("knative");
        assertThat(i.getSpec().getTraits().get("knative").getConfiguration()).containsOnly(
            entry("enabled", "true"),
            entry("channel-sources", "my-channel-source"),
            entry("channel-sinks", "my-channel-sink"),
            entry("endpoint-sources", "default"),
            entry("endpoint-sinks", "")
        );
    }

    @Test
    public void testKnativeCustomizerEndpoints() throws IOException {
        Connector connector;
        ConnectorAction sinkAction;
        ConnectorAction sourceAction;

        try (InputStream is = KnativeCustomizerTest.class.getResourceAsStream("/META-INF/syndesis/connector/knative.json")) {
            connector = Json.readFromStream(is, Connector.class);
            sinkAction = connector.getActions(ConnectorAction.class).stream()
                .filter(a -> a.getId().get().equals("io.syndesis:knative-endpoint-call-connector"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException());
            sourceAction = connector.getActions(ConnectorAction.class).stream()
                .filter(a -> a.getId().get().equals("io.syndesis:knative-endpoint-expose-connector"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException());
        }

        TestResourceManager manager = new TestResourceManager();
        Integration integration = manager.newIntegration(
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .putConfiguredProperty("name", "my-endpoint-sink")
                .action(sinkAction)
                .connection(new Connection.Builder()
                    .connector(connector)
                    .build())
                .build(),
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .putConfiguredProperty("name", "my-endpoint-source")
                .action(sourceAction)
                .connection(new Connection.Builder()
                    .connector(connector)
                    .build())
                .build()
        );

        IntegrationDeployment deployment = new IntegrationDeployment.Builder()
            .userId("user")
            .id("idId")
            .spec(integration)
            .build();

        CamelKIntegrationCustomizer customizer = new KnativeCustomizer();

        io.syndesis.server.controller.integration.camelk.crd.Integration i = customizer.customize(
            deployment,
            new io.syndesis.server.controller.integration.camelk.crd.Integration(),
            EnumSet.of(Exposure.SERVICE)
        );

        assertThat(i.getSpec().getTraits()).containsKey("knative");
        assertThat(i.getSpec().getTraits().get("knative").getConfiguration()).containsOnly(
            entry("enabled", "true"),
            entry("channel-sources", ""),
            entry("channel-sinks", ""),
            entry("endpoint-sources", "default"),
            entry("endpoint-sinks", "my-endpoint-sink")
        );
    }
}
