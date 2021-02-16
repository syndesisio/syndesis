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

import java.net.URI;
import java.util.function.Consumer;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.connector.support.test.ConnectorTestSupport;

import org.junit.jupiter.api.extension.ExtendWith;
import org.zapodot.junit5.jms.EmbeddedJmsBroker;
import org.zapodot.junit5.jms.annotations.EmbeddedJms;

@ExtendWith(EmbeddedJmsBroker.class)
public abstract class ActiveMQConnectorTestSupport extends ConnectorTestSupport {

    @EmbeddedJms
    private URI brokerUri;

    protected Step newActiveMQEndpointStep(String actionId, Consumer<Step.Builder> consumer) {
        final Connector connector = getResourceManager().mandatoryLoadConnector("activemq");
        final ConnectorAction action = getResourceManager().mandatoryLookupAction(connector, actionId);

        final Step.Builder builder = new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(action)
            .connection(new Connection.Builder()
                .connector(connector)
                .putConfiguredProperty("brokerUrl", brokerUri.toString())
                .build());

        consumer.accept(builder);

        return builder.build();
    }
}
