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

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
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

import java.io.InputStream;
import java.util.EnumSet;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class WebhookCustomizerTest {
    @Test
    public void testOpenApiCustomizer() throws Exception {
        TestResourceManager manager = new TestResourceManager();

        Connector connector;
        ConnectorAction webhookIncomingAction;

        try (InputStream is = WebhookCustomizerTest.class.getResourceAsStream("/META-INF/syndesis/connector/webhook.json")) {
            connector = Json.readFromStream(is, Connector.class);
            webhookIncomingAction = connector.getActions(ConnectorAction.class).stream()
                .filter(a -> a.getId().get().equals("io.syndesis:webhook-incoming"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException());
        }

        webhookIncomingAction = webhookIncomingAction.builder().descriptor(new ConnectorDescriptor.Builder().connectorId("webhook").build()).build();

        Integration integration = manager.newIntegration(
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .putConfiguredProperty("contextPath", "token")
                .action(webhookIncomingAction)
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

        CamelKIntegrationCustomizer customizer = new WebhookCustomizer();

        io.syndesis.server.controller.integration.camelk.crd.Integration i = customizer.customize(
            deployment,
            new io.syndesis.server.controller.integration.camelk.crd.Integration(),
            EnumSet.of(Exposure.SERVICE)
        );

        assertThat(i.getSpec().getConfiguration()).hasSize(2);
        assertThat(i.getSpec().getConfiguration()).anyMatch(
            c -> Objects.equals("customizer.servletregistration.enabled=true", c.getValue())
                    && Objects.equals("property", c.getType())
        );
        assertThat(i.getSpec().getConfiguration()).anyMatch(
            c -> Objects.equals("customizer.servletregistration.path=/webhook/*", c.getValue())
                && Objects.equals("property", c.getType())
        );
    }
}
