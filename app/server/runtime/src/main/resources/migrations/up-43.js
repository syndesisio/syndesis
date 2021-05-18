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

var migrateAction = function(action) {
    if (!action) {
        return false;
    }

    if (action.id !== 'io.syndesis:api-provider-end') {
        return false;
    }

    var changed = false;
    var descriptor = action.descriptor;
    if (descriptor.exceptionHandler !== 'io.syndesis.connector.apiprovider.ApiProviderOnExceptionHandler') {
        descriptor.exceptionHandler = 'io.syndesis.connector.apiprovider.ApiProviderOnExceptionHandler';
        changed = true;
    }

    var propertyDefinitionSteps = descriptor.propertyDefinitionSteps;
    var configuration = propertyDefinitionSteps[0].properties;
    var errorResponseCodes = configuration['errorResponseCodes'];
    if (errorResponseCodes.defaultValue !== '{"SERVER_ERROR":"500"}') {
        errorResponseCodes.defaultValue = '{"SERVER_ERROR":"500"}';
        changed = true;
    }

    var returnBody = configuration['returnBody'];
    if (returnBody.defaultValue !== true) {
        returnBody.defaultValue = true;
        changed = true;
    }

    return changed;
}

var migrateConnector = function(connector) {
    if (!connector) {
        return false;
    }

    if (connector.id !== 'api-provider') {
        return false;
    }

    if (connector.actions) {
        var changed = false;
        connector.actions.forEach(function(action) {
            changed |= migrateAction(action)
        });

        return changed;
    }

    return false;
}

var migrateConnection = function(connection) {
    if (!connection) {
        return false;
    }

    if (migrateConnector(connection.connector)) {
        return true;
    }

    return false;
}


var migrateStep = function(step) {
    if (!step) {
        return false;
    }

    var changed = migrateAction(step.action)
    changed |= migrateConnection(step.connection)

    return changed
}

var migrate = function(path, migrateFn) {
    var elements = jsondb.get(path);

    if (!elements) {
        return;
    }

    var migrated = 0;

    Object.keys(elements).forEach(function(elementId) {
        if (migrateFn(elements[elementId])) {
            migrated++;
        }
    });

    if (migrated > 0) {
        jsondb.update(path, elements);
    }
}

console.log('Migration to schema 43 ...');

migrate('/connectors', migrateConnector);
migrate('/connections', function(connection) {
    return migrateConnector(connection.connector);
});
migrate('/integrations', function(integration) {
    var changed = false

    if (integration.steps) {
        integration.steps.forEach(function(step) {
            changed |= migrateStep(step)
        });
    }

    if (integration.flows) {
        integration.flows.forEach(function(flow) {
            flow.steps.forEach(function(step) {
                changed |= migrateStep(step)
            });
        });
    }

    return changed;
});

console.log('Migrated to schema 43 completed');
