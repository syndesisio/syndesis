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
