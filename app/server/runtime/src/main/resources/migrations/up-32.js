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

console.log("Migrating to schema 32 ...");
migrate("integrations", "/integrations", function(integration) {

    var changed = false;
    if (integration.continuousDeliveryState) {

        var cdPath = "/integrations/:" + integration.id + "/continuousDeliveryState/";

        var newStateMap = {};

        for (name in integration.continuousDeliveryState) {

            // get CD state
            var state = integration.continuousDeliveryState[name];

            // does environment exist?
            var envId = null;
            var ids = internal.jsondb.fetchIdsByPropertyValue("/environments", "name", name)

            if (ids.size > 0) {
                envId = ids.values[0];
            } else {
                // create environment object
                envId = jsondb.createKey();
                var env = {
                    "id": envId,
                    "name": name
                };
                jsondb.set("/environments/:" + envId, env);
            }

            // update this CD environment
            jsondb.delete(cdPath + name);
            delete state.name;
            state.environmentId = envId;
            newStateMap[envId] = state;

            changed = true;
        }

        if (changed) {
            integration.continuousDeliveryState = newStateMap;
        }
    }

    return changed;
});

console.log("Migration to schema 32 completed");
