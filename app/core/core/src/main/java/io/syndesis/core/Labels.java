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
package io.syndesis.core;

import java.util.Locale;

public final class Labels {

    private static final String VALID_REGEX = "(([A-Za-z0-9][-A-Za-z0-9_.]*)?[A-Za-z0-9])?";
    private static final String INVALID_CHARACTER_REGEX = "[^a-zA-Z0-9-]";
    private static final String SIDES_REGEX = "[A-Za-z0-9]";

    private static final String SPACE = " ";
    private static final String DASH = "-";
    private static final int MAXIMUM_NAME_LENGTH = 100;

    private Labels() {
        //Utility
    }

    /**
     * Sanitizes the specified name by applying the following rules:
     * 1. Keep the first 100 characters.
     * 2. Replace spaces with dashes.
     * 3. Remove invalid characters.
     * @param name  The specified name.
     * @return      The sanitized string.
     */
    public static String sanitize(String name) {
        return trim(name)
            .replaceAll(SPACE, DASH)
            .toLowerCase(Locale.US)
            .chars()
            .filter(i -> !String.valueOf(i).matches(INVALID_CHARACTER_REGEX))
            .collect(StringBuilder::new,
                (b, chr) -> {
                    int lastChar = b.length() > 0 ? b.charAt(b.length() - 1) : -1;

                    if (lastChar != '-' || chr != '-') {
                        b.appendCodePoint(chr);
                    }
             }, StringBuilder::append)
            .toString();
    }

    private static String trim(String name) {
        String trimmed = name.length() > MAXIMUM_NAME_LENGTH ? name.substring(0, MAXIMUM_NAME_LENGTH) : name;

        if (trimmed.matches(VALID_REGEX)) {
            return trimmed;
        }

        int length = trimmed.length();
        if (length <= 1) {
            throw new IllegalStateException("Specified string:" + name + " cannot be sanitized.");
        }

        String first = trimmed.substring(0, 1);
        if (!first.matches(SIDES_REGEX)) {
            return trim(trimmed.substring(1));
        }

        String last = trimmed.substring(length -1, length);
        if (!last.matches(SIDES_REGEX)) {
            return trim(trimmed.substring(0, length - 1));
        }
        return trimmed;
    }
}
