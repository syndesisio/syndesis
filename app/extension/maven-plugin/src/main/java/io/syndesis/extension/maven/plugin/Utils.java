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
package io.syndesis.extension.maven.plugin;

final class Utils {

    private Utils() {
        // utility class
    }

    static boolean isEmpty(final String value) {
        return value == null || value.isEmpty();
    }

    static boolean isNotEmpty(final String value) {
        return value != null && !value.isEmpty();
    }

    static String nvl(final String... values) {
        for (final String value : values) {
            if (isNotEmpty(value)) {
                return value;
            }
        }

        // all values are either null or empty
        return null;
    }
}
