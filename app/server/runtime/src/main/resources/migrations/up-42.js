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

console.log("Migrating to schema 42 ...");
console.log("This migration will update standardizedError keys and names");

var migrateConnector = function(connector) {
    if (!connector) {
        return false;
    }
    var changed = false;
    if (connector.id === "sql") {
        console.log("Migrating connector: " + connector.id);
        connector.actions.forEach(function (action) {
            migrateSqlAction(action);
            changed = true;
        });
    }
    return changed;
}

var migrateSqlAction = function(action) {
    if (!action || (! (action.id === 'sql-connector' || action.id === 'sql-start-connector'
    	|| action.id === 'sql-stored-connector' || action.id === 'sql-stored-start-connector'))) {
        return false;
    }
    console.log("Migrating action: " + action.id);

    action.descriptor.standardizedErrors = [
        {
          "name": "DATA_ACCESS_ERROR",
          "displayName": "DataAccessError"
        },
        {
          "name": "ENTITY_NOT_FOUND_ERROR",
          "displayName": "EntityNotFoundError"
        },
        {
          "name": "DUPLICATE_KEY_ERROR",
          "displayName": "DuplicateKeyError"
        },
        {
          "name": "CONNECTOR_ERROR",
          "displayName": "ConnectorError"
        }
    ];
    return true
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

    var done = false;
    done |=  migrateSqlAction(step.action);
    done |=  migrateConnection(step.connection);

    if (step.configuredProperties) {
        for (property in step.configuredProperties) {
            if (property === 'errorResponseCodes') {
                json = step.configuredProperties[property];
                json = json.replace(/SQL_/g, '');
                json = json.replace(/org.springframework.dao.DuplicateKeyException/g, 'DUPLICATE_KEY_ERROR');
                step.configuredProperties[property] = json;
                done |= true;
            }
        }
    }
    return done;
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


migrate("connectors", "/connectors", migrateConnector);
migrate("connections", "/connections", function(connection) {
    return migrateConnector(connection.connector);
});

migrate("integrations", "/integrations", function(integration) {
    var done = true;

    if (integration.steps) {
        integration.steps.forEach(function (step) {
            console.log("")
            done |= migrateStep(step)
        });
    }

    if (integration.flows) {
        integration.flows.forEach(function (flow) {
            
            flow.steps.forEach(function (step) {
                console.log("flow " + flow.id + " : step " + step.id);
                done |= migrateStep(step)
            });
        });
    }

    return done;
});

console.log("Migration to schema 42 completed");
