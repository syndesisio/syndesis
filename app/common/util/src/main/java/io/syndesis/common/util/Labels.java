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
    private static final int MAXIMUM_KEY_LENGTH = 253;
    private static final String VALID_KEY_REGEX = "(?:(?:[A-Za-z0-9][-A-Za-z0-9_./]*)?[A-Za-z0-9])?";
    private static final int MAXIMUM_VALUE_LENGTH = 63;
    private static final String VALID_VALUE_REGEX = "(?:(?:[A-Za-z0-9][-A-Za-z0-9_.]*)?[A-Za-z0-9])?";

    private static final String SPACE = " ";
    private static final String DASH = "-";

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
        return trim(name
            .replaceAll(SPACE, DASH)
            .chars()
            .filter(Labels::isValidChar)
            //Handle consecutive dashes
            .collect(StringBuilder::new,
                (b, chr) -> {
                    int lastChar = b.length() > 0 ? b.charAt(b.length() - 1) : -1;

                    if (lastChar != '-' || chr != '-') {
                        b.appendCodePoint(chr);
                    }
             }, StringBuilder::append)
            .toString());
    }


    public static boolean isValid(String value) {
        return value.matches(VALID_VALUE_REGEX) && value.length() <= MAXIMUM_VALUE_LENGTH;
    }

    public static String validate(String value) {
        if (!isValid(value)) {
            throw new IllegalArgumentException("Invalid label value: [" + value + "].");
        }
        return value;
    }

    public static boolean isValidKey(String key) {
        return key.matches(VALID_KEY_REGEX)
            && key.length() <= MAXIMUM_KEY_LENGTH
            && !key.startsWith("syndesis.io/")
            && !key.startsWith("kubernetes.io/")
            && !key.startsWith("k8s.io/");
    }

    public static String validateKey(String key) {
        if (!isValidKey(key)) {
            throw new IllegalArgumentException("Invalid label key: [" + key + "].");
        }
        return key;
    }

    private static String trim(String name) {
        if (isValid(name)) {
            return name;
        }

        int length = name.length();
        if (length <= 1) {
            throw new IllegalStateException("Specified string:" + name + " cannot be sanitized.");
        }

        // Recursively trim
        if (!isAlphaNumeric(name.charAt(0))) {
            return trim(name.substring(1));
        }
        if (!isAlphaNumeric(name.charAt(length - 1))) {
            return trim(name.substring(0, length - 1));
        }

        return name;
    }

    private static boolean isValidChar(int i) {
        return isAlphaNumeric(i) || i == '.' || i == '-' || i == '_';
    }

    private static boolean isAlphaNumeric(int i) {
        return (i >= 'a' && i <= 'z') ||
               (i >= '0' && i <= '9') ||
               (i >= 'A' && i <= 'Z');
    }
}
