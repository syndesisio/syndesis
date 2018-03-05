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

import java.util.function.Consumer;

import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.integration.Step;
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
}
