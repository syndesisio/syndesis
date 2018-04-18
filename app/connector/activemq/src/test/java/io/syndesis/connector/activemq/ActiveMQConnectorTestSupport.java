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
package io.syndesis.connector.activemq;

import java.util.function.Consumer;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.connector.support.test.ConnectorTestSupport;
import org.apache.activemq.junit.EmbeddedActiveMQBroker;
import org.junit.Rule;
import org.junit.rules.TestName;


public abstract class ActiveMQConnectorTestSupport extends ConnectorTestSupport {
    @Rule
    public TestName testName = new TestName();

    @Rule
    public EmbeddedActiveMQBroker broker = new EmbeddedActiveMQBroker() {
        @Override
        protected void configure() {
            disableJMX();
        }
    };

    // **************************
    // Helpers
    // **************************

    protected Step newActiveMQEndpointStep(String actionId, Consumer<Step.Builder> consumer) {
        final Connector connector = getResourceManager().mandatoryLoadConnector("activemq");
        final ConnectorAction action = getResourceManager().mandatoryLookupAction(connector, actionId);

        final Step.Builder builder = new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(action)
            .connection(new io.syndesis.common.model.connection.Connection.Builder()
                .connector(connector)
                .putConfiguredProperty("brokerUrl", broker.getVmURL())
                .build());

        consumer.accept(builder);

        return builder.build();
    }
}
