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

//
// Helpers
//
var migrateAction = function(action) {
    if (!action) {
        return false;
    }

    if (action.descriptor.camelConnectorGAV || action.descriptor.camelConnectorPrefi) {
        console.log("Migrating action: " + action.id);

        action.descriptor.componentScheme = "rest-swagger";

        delete action.descriptor.camelConnectorGAV;
        delete action.descriptor.camelConnectorPrefix;

        return true
    }

    return false
}

var migrateConnector = function(connector) {
    if (!connector) {
        return false;
    }

    if (connector.connectorGroupId === "swagger-connector-template") {
        console.log("Migrating connector: " + connector.id);

        connector.connectorFactory = "io.syndesis.connector.rest.swagger.ConnectorFactory";

        connector.connectorCustomizers = [
            "io.syndesis.connector.rest.swagger.SpecificationResourceCustomizer",
            "io.syndesis.connector.rest.swagger.AuthenticationCustomizer",
            "io.syndesis.connector.rest.swagger.RequestCustomizer",
            "io.syndesis.connector.rest.swagger.ResponseCustomizer"
        ];
        connector.dependencies = [
            {
                "id": "io.syndesis.connector:connector-rest-swagger",
                "type": "MAVEN"
            }
        ];

        if (connector.actions) {
            connector.actions.forEach(function (action) {
                migrateAction(action)
            });
        }

        return true
    }

    return false
}

var migrateConnection = function(connection) {
    if (!connection) {
        return false;
    }

    if (migrateConnector(connection.connector)) {
        return true
    }

    return false
}


var migrateStep = function(step) {
    if (!step) {
        return false;
    }

    var done = false
    done |=  migrateAction(step.action)
    done |=  migrateConnection(step.connection)

    return done
}

var migrate = function(type, path, consumer) {
    console.log("Start " + type + " migration")

    var migrated  = 0;
    var inspected = 0;
    var elements  = jsondb.get(path);

    if (elements) {
        Object.keys(elements).forEach(function(elementId) {
            inspected++;

            if (consumer(elements[elementId])) {
                migrated++;
            }
        });

        if (migrated > 0) {
            jsondb.update(path, elements);
        }
    }

    console.log(type + ": migrated " + migrated + " out of " + inspected);
}

//
// Migration
//

console.log("Migration to schema 29 ...");
console.log("This migration will update API Client to new connector model");

migrate("connectors", "/connectors", migrateConnector);
migrate("connections", "/connections", function(connection) {
    return migrateConnector(connection.connector);
});
migrate("integrations", "/integrations", function(integration) {
    var done = false

    if (integration.steps) {
        integration.steps.forEach(function (step) {
            console.log("")
            done |= migrateStep(step)
        });
    }

    if (integration.flows) {
        integration.flows.forEach(function (flow) {
            flow.steps.forEach(function (step) {
                done |= migrateStep(step)
            });
        });
    }

    return done;
});

console.log("Migrated to schema 29 completed");
