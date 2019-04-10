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
package io.syndesis.server.jsondb.impl;

import java.util.regex.Pattern;

/**
 * Helper methods to work with String objects
 */
public final class Strings {

    private Strings() {
        // utility class
    }

    public static String prefix(String base, String prefix) {
        if( !base.startsWith(prefix) ) {
            return prefix + base;
        }
        return base;
    }

    public static String suffix(String base, String suffix) {
        if( !base.endsWith(suffix) ) {
            return base+suffix;
        }
        return base;
    }

    public static String trimPrefix(String value, String prefix) {
        if( value.startsWith(prefix) ) {
            return value.substring(prefix.length());
        }
        return value;
    }

    public static String trimSuffix(String value, String suffix) {
        if( value.endsWith(suffix) ) {
            return value.substring(0, value.length()-suffix.length());
        }
        return value;
    }

    public static String splitPart(String delimiter, String value, int idx) {
        int i = idx-1;
        String[] split = value.split(Pattern.quote(delimiter), -1);
        if( i < split.length ) {
            return split[i];
        } else {
            return value;
        }
    }

}
