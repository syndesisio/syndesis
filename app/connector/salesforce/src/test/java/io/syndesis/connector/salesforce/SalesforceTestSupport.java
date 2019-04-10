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
package io.syndesis.connector.salesforce;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.util.Json;
import io.syndesis.connector.support.test.ConnectorTestSupport;
import org.apache.camel.CamelContext;

public abstract class SalesforceTestSupport extends ConnectorTestSupport {

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();
        context.setAutoStartup(false);

        return context;
    }

    protected final Step newSalesforceEndpointStep(String actionId, Consumer<Connection.Builder> connectionConsumer, Consumer<Step.Builder> stepConsumer) {
        return newEndpointStep("salesforce", "io.syndesis.connector:connector-salesforce:" + actionId, connectionConsumer, stepConsumer);
    }



    public static Connector mandatoryLookupConnector() {
        Connector connector;

        try (InputStream is = SalesforceTestSupport.class.getResourceAsStream("/META-INF/syndesis/connector/salesforce.json")) {
            connector = Json.reader().forType(Connector.class).readValue(is);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        if (connector == null) {
            throw new IllegalStateException("Unable to lod salesforce connector");
        }

        return connector;
    }

    public static final ConnectorAction mandatoryLookupAction(Connector connector, String actionId) {
        final String fullActionId = "io.syndesis.connector:connector-salesforce:" + actionId;

        for (ConnectorAction action : connector.getActions()) {
            if (action.getId().isPresent() && action.getId().get().equals(fullActionId)) {
                return action;
            }
        }

        throw new IllegalArgumentException("Unable to find action: " + actionId);
    }
}
