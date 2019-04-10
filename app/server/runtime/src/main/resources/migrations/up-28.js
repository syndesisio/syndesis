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
var migrated = 0;
var path = "/integrations";

var integrations = jsondb.get(path);

var migrationHelper = {
    implicitSplitSteps: [
        "sql-start-connector",
        "io.syndesis:servicenow-action-retrieve-record",
        "io.syndesis:aws-s3-polling-bucket-connector"
    ],
    shouldMigrate: function (step) {
        if (!step) {
            return false;
        }

        if (!step.action) {
            return false;
        }

        return this.implicitSplitSteps.indexOf(step.action.id) !== -1;
    },
    getInputShape: function (step) {
        if (step &&
            step.action &&
            step.action.descriptor &&
            step.action.descriptor.inputDataShape) {
            return step.action.descriptor.inputDataShape;
        } else {
            return this.noShape();
        }
    },
    getOutputShape: function (step) {
        if (step &&
            step.action &&
            step.action.descriptor &&
            step.action.descriptor.outputDataShape) {
            return step.action.descriptor.outputDataShape;
        } else {
            return this.anyShape();
        }
    },
    anyShape: function () {
        return {
            "kind": "any"
        };
    },
    noShape: function () {
        return {
            "kind": "none"
        };
    },
    createKey: function () {
        return Packages.io.syndesis.common.util.KeyGenerator.createKey();
    }
};

if (integrations) {
    console.log("Migration to schema 28 ...");
    console.log("This migration will auto add split steps to integrations with implicit split configured");

    Object.keys(integrations).forEach(function(integrationId, index) {
        var integration = integrations[integrationId];
        var flow = integration["flows"][0];

        if (flow) {
            var steps = flow["steps"];
            if (steps) {
                var step = steps[0];

                if (migrationHelper.shouldMigrate(step)) {
                    var nextStep = steps[1];
                    if (nextStep && nextStep.stepKind !== 'split') {
                        console.log("Migrating integration '" + integrationId + "' - adding explicit split step");

                        steps.splice(1, 0, {
                            "id": step.id ? step.id : migrationHelper.createKey(),
                            "stepKind": "split",
                            "name": "Split",
                            "metadata": {
                                "configured": "true"
                            },
                            "action": {
                                "actionType": "step",
                                "descriptor": {
                                    "inputDataShape": migrationHelper.getInputShape(step),
                                    "outputDataShape": migrationHelper.getOutputShape(step),
                                }
                            },
                            "configuredProperties": {
                                "expression": "${body}",
                                "aggregationStrategy": "original"
                            },
                        });

                        if (step.id) {
                            step.id = migrationHelper.createKey();
                        }

                        migrated++;
                    }
                }
            }
        } else {
            console.log("Integration flow element is missing - skipping integration '" + integrationId + "'");
        }
    });

    if (migrated > 0) {
        jsondb.update(path, integrations);
    }

    console.log("Number of integrations migrated: " + migrated);
}

console.log("Migrated to schema 28 completed");
