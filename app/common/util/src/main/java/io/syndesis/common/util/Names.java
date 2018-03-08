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
package io.syndesis.common.util;

import java.util.Locale;
import static io.syndesis.common.util.Strings.truncate;

public final class Names {

    private static final String VALID_REGEX = "[-A-Za-z0-9\\._]+";
    private static final String INVALID_CHARACTER_REGEX = "[^a-zA-Z0-9-\\._]";
    private static final String SPACE = " ";
    private static final String BLANK = "";
    private static final String DASH = "-";
    //The actual limit is 253,
    // but individual resources have other limitations
    // (e.g. service has 63: RFC 1035)
    private static final int MAXIMUM_NAME_LENGTH = 63;

    private Names() {
        //Utility
    }

    /**
     * Sanitizes the specified name by applying the following rules:
     * 1. Replace spaces with dashes.
     * 2. Remove invalid characters.
     * 3. Keep the first 64 characters.
     * @param name  The specified name.
     * @return      The sanitized string.
     */
    public static String sanitize(String name) {
        return truncate(name
            .replaceAll(SPACE, DASH)
            .replaceAll(INVALID_CHARACTER_REGEX, BLANK)
            .toLowerCase(Locale.US)
            .chars()
            .filter(i -> !String.valueOf(i).matches(INVALID_CHARACTER_REGEX))
             //Handle consecutive dashes
            .collect(StringBuilder::new,
                (b, chr) -> {
                    int lastChar = b.length() > 0 ? b.charAt(b.length() - 1) : -1;

                    if (lastChar != '-' || chr != '-') {
                        b.appendCodePoint(chr);
                    }
             }, StringBuilder::append)
            .toString(), MAXIMUM_NAME_LENGTH);
    }


    public static boolean isValid(String name) {
        return name.matches(VALID_REGEX) && name.length() <= MAXIMUM_NAME_LENGTH;
    }

    public static void validate(String name) {
        if (!isValid(name)) {
            throw new IllegalArgumentException("Invalid name: [" + name + "].");
        }
    }
}
