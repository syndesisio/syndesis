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

migrate("integrations", "/integrations", function(integration) {
    var changed = false;

    var ch = function() {
        changed = true;
    }

    if (Array.isArray(integration.tags)) {
        integration.tags = integration.tags.map(change("http4", "http", ch))
            .map(change("https4", "https", ch));
    }

    if (Array.isArray(integration.flows)) {
        var migrateAction = function(action) {
            if (action) {
                action.id = change("io.syndesis.connector:connector-http:ttps4-periodic-invoke-url", "io.syndesis.connector:connector-http:https-periodic-invoke-url", ch)(action.id);
                action.id = change("io.syndesis.connector:connector-http:https4-invoke-url", "io.syndesis.connector:connector-http:https-invoke-url", ch)(action.id);
                action.id = change("io.syndesis.connector:connector-http:http4-periodic-invoke-url", "io.syndesis.connector:connector-http:http-periodic-invoke-url", ch)(action.id);
                action.id = change("io.syndesis.connector:connector-http:http4-invoke-url", "io.syndesis.connector:connector-http:http-invoke-url", ch)(action.id);

                if (action.descriptor && action.descriptor.connectorFactory) {
                    action.descriptor.connectorFactory = change("io.syndesis.connector.http.HttpConnectorFactories$Http4", "io.syndesis.connector.http.HttpConnectorFactories$Http", ch)(action.descriptor.connectorFactory);
                    action.descriptor.connectorFactory = change("io.syndesis.connector.http.HttpConnectorFactories$Https4", "io.syndesis.connector.http.HttpConnectorFactories$Https", ch)(action.descriptor.connectorFactory);
                }
            }
        }

        integration.flows.forEach(function(flow) {
            if (Array.isArray(flow.steps)) {
                flow.steps.forEach(function(step) {
                    if (step.action) {
                        migrateAction(step.action);
                    }

                    if (step.connection) {
                        step.connection.icon = change("assets:http4.svg", "assets:http.svg", ch)(step.connection.icon);
                        step.connection.icon = change("assets:https4.svg", "assets:https.svg", ch)(step.connection.icon);

                        if (step.connection.connectorId) {
                            step.connection.connectorId = change("http4", "http", ch)(step.connection.connectorId);
                            step.connection.connectorId = change("https4", "https", ch)(step.connection.connectorId);
                        }

                        if (step.connection.connector) {
                            step.connection.connector.id = change("http4", "http", ch)(step.connection.connector.id);
                            step.connection.connector.id = change("https4", "https", ch)(step.connection.connector.id);
                            step.connection.connector.componentScheme = change("http4", "http", ch)(step.connection.connector.componentScheme);
                            step.connection.connector.componentScheme = change("https4", "https", ch)(step.connection.connector.componentScheme);
                            step.connection.connector.icon = change("assets:http4.svg", "assets:http.svg", ch)(step.connection.connector.icon);
                            step.connection.connector.icon = change("assets:https4.svg", "assets:https.svg", ch)(step.connection.connector.icon);

                            if (Array.isArray(step.connection.connector.actions)) {
                                step.connection.connector.actions.forEach(migrateAction);
                            }
                        }
                    }
                });
            }
        });
    }

    return changed;
});
