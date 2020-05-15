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

/*
 * console.log(..) is a super common browser api folks expect.
 */
var console = {
    log: print
};

/*
 * Provide a nicer js interface to jsondb
 */
var jsondb = {
    get: function (path) {
        var rc = internal.jsondb.getAsString(path);
        if (rc === null) {
            return rc;
        }
        return JSON.parse(rc);
    },
    set: function (path, value) {
        internal.jsondb.set(path, JSON.stringify(value));
    },
    update: function (path, value) {
        internal.jsondb.update(path, JSON.stringify(value));
    },
    push: function (path, value) {
        internal.jsondb.push(path, JSON.stringify(value));
    },
    createKey: function () {
        return internal.jsondb.createKey();
    },
    delete: function (path) {
        return internal.jsondb.delete(path);
    },
    exists: function (path) {
        return internal.jsondb.exists(path);
    }
};

//
//Helpers
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

var change = function(previous, next, changeCallback) {
    return function(value) {
        if (value === previous) {
            if (changeCallback) {
                changeCallback();
            }
            return next;
        }

        return value;
    }
}