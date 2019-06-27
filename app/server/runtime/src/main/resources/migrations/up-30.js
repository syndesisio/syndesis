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

var KeyGenerator = Java.type('io.syndesis.common.util.KeyGenerator')

console.log("Migration to schema 30 ...");
migrate("integrations", "/integrations", function(integration) {
    var changed = false;
    if (integration.flows) {
        integration.flows.forEach(function (flow) {
            var parts = /.+:flows:(.+)/.exec(flow.id);
            if (parts && parts.length > 1) {
                var operationId = parts[1];
                flow.id = KeyGenerator.createKey()
                flow.metadata['openapi-operationid'] = operationId;

                changed = true;
            }
        });
    }

    return changed;
});

console.log("Migrated to schema 30 completed");
