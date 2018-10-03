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
package io.syndesis.common.model.integration;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationBaseTest {

    @Test
    public void shouldCollectConnectorIdsFromConnectorAction() {
        assertThat(
            new Integration.Builder().addFlow(new Flow.Builder().addStep(stepWithConnectorInAction("ctr1")).build())
                .build().getUsedConnectorIds()).containsOnly("ctr1");
    }

    @Test
    public void shouldCollectConnectorIdsFromHollowIntegrations() {
        assertThat(new Integration.Builder().build().getUsedConnectorIds()).isEmpty();
    }

    @Test
    public void shouldCollectConnectorIdsFromSteps() {
        assertThat(new Integration.Builder().addFlow(new Flow.Builder().addStep(stepWithConnector("ctr1")).build())
            .build().getUsedConnectorIds()).containsOnly("ctr1");
    }

    private static Step stepWithConnector(String connectorId) {
        return new Step.Builder().connection(connectionWithConnector(connectorId)).build();
    }

    @Test
    public void shouldCollectConnectorIdsFromStepsAndConnectorActions() {
        assertThat(new Integration.Builder()
            .addFlow(
                new Flow.Builder().addStep(stepWithConnectorInAction("ctr1")).addStep(stepWithConnectorInAction("ctr2"))
                    .addStep(stepWithConnector("ctr1")).addStep(stepWithConnector("ctr3")).build())
            .build().getUsedConnectorIds()).containsOnly("ctr1", "ctr2", "ctr3");
    }

    private static ConnectorAction actionWithConnector(final String connectorId) {
        return new ConnectorAction.Builder().descriptor(connectorDescriptor(connectorId)).build();
    }

    private static Connection connectionWithConnector(final String connectorId) {
        return new Connection.Builder().connectorId(connectorId).build();
    }

    private static ConnectorDescriptor connectorDescriptor(final String connectorId) {
        return new ConnectorDescriptor.Builder().connectorId(connectorId).build();
    }

    private static Step stepWithConnectorInAction(final String connectorId) {
        return new Step.Builder()
            .action(new ConnectorAction.Builder().descriptor(connectorDescriptor(connectorId)).build()).build();
    }
}
