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

public final class Labels {

    private static final String VALID_REGEX = "(([A-Za-z0-9][-A-Za-z0-9_]*)?[A-Za-z0-9])?";
    private static final String INVALID_CHARACTER_REGEX = "[^a-zA-Z0-9-]";
    private static final String SIDES_REGEX = "[A-Za-z0-9]";

    private static final String SPACE = " ";
    private static final String DASH = "-";
    private static final int MAXIMUM_NAME_LENGTH = 63;

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
            .toString();
    }


    public static boolean isValid(String name) {
        return name.matches(VALID_REGEX) && name.length() <= MAXIMUM_NAME_LENGTH;
    }

    public static String validate(String name) {
        if (!isValid(name)) {
            throw new IllegalArgumentException("Invalid label: [" + name + "].");
        }
        return name;
    }

    private static String trim(String name) {
        if (isValid(name)) {
            return name;
        }

        int length = name.length();
        if (length <= 1) {
            throw new IllegalStateException("Specified string:" + name + " cannot be sanitized.");
        }

        String first = name.substring(0, 1);
        if (!first.matches(SIDES_REGEX)) {
            return trim(name.substring(1));
        }

        String last = name.substring(length -1, length);
        if (!last.matches(SIDES_REGEX)) {
            return trim(name.substring(0, length - 1));
        }
        return name;
    }
}
