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

import java.util.regex.Pattern;

public final class Names {

    private static final String DEFAULT_NAME = "default";

    // The actual limit is 253,
    // but individual resources have other limitations
    // (e.g. service has 63: RFC 1035)
    private static final int MAXIMUM_NAME_LENGTH = 63;

    private static final Pattern VALID_REGEX = Pattern.compile("^[a-z0-9](?:[-A-Za-z0-9\\\\]{0,61}[a-z0-9])?$");

    private Names() {
        // Utility
    }

    public static boolean isValid(final String name) {
        return VALID_REGEX.matcher(name).matches() && name.length() <= MAXIMUM_NAME_LENGTH;
    }

    /**
     * Sanitizes the specified name.
     *
     * @param name The specified name.
     * @return The sanitized string.
     */
    public static String sanitize(final String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Specified name is empty or null");
        }

        final StringBuilder sanitized = new StringBuilder();
        char lastChar = 0;
        final char[] nameAry = name.toCharArray();
        for (int i = 0; i < nameAry.length && i < MAXIMUM_NAME_LENGTH; i++) {
            final char ch = nameAry[i];
            final char nextChar = determineNextCharacter(lastChar, ch);
            if (nextChar == 0) {
                continue;
            }

            lastChar = nextChar;
            sanitized.append(nextChar);
        }

        if (sanitized.length() == 0) {
            // if we didn't manage to find any characters to append as sanitized
            // use the default name
            return DEFAULT_NAME;
        }

        // make sure that the last character iz lowercase letter or digit
        final int lastIdx = sanitized.length() - 1;
        final char ch = sanitized.charAt(lastIdx);
        if (ch == '-') {
            if (lastIdx + 1 < MAXIMUM_NAME_LENGTH) {
                sanitized.append('0');
            } else {
                sanitized.setCharAt(lastIdx, '0');
            }
        }

        return sanitized.toString();
    }

    public static void validate(final String name) {
        if (!isValid(name)) {
            throw new IllegalArgumentException("Invalid name: [" + name + "].");
        }
    }

    private static char determineNextCharacter(final char lastChar, final char ch) {
        if (isDigit(ch)) {
            return ch;
        } else if (shouldConvertToDash(ch) && lastChar != '-' && lastChar != 0) {
            // don't add consecutive '-'
            return '-';
        } else {
            final char[] chars = Character.toChars(ch);

            if (isLetter(chars[0])) {
                return Character.toLowerCase(chars[0]);
            }
        }

        return 0;
    }

    private static boolean isDigit(final char ch) {
        return ch >= '0' && ch <= '9';
    }

    private static boolean isLetter(final char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    private static boolean shouldConvertToDash(final char ch) {
        return ch == ' ' || ch == '_' || ch == '-' || ch == '.';
    }
}
