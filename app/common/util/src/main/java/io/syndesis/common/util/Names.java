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
import java.util.regex.Pattern;
import static io.syndesis.common.util.Strings.truncate;

public final class Names {

    private static final Pattern VALID_REGEX = Pattern.compile("^[a-z0-9][-A-Za-z0-9\\\\]{0,61}[a-z0-9]$");
    private static final Pattern INVALID_START_REGEX = Pattern.compile("^[^a-z0-9]+");
    private static final Pattern INVALID_CHARACTER_REGEX = Pattern.compile("[^a-zA-Z0-9-\\._]");
    private static final String SPACE = " ";
    private static final String BLANK = "";
    private static final String DASH = "-";
    private static final String DOT = "\\.";
    private static final String UNDERSCORE = "_";
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
     * 2. Replace underscores with dashes.
     * 3. Replace dots with dashes
     * 4. Ensures that the first character is a alphanumeric
     * 5. Remove invalid characters.
     * 6. Keep the first 64 characters.
     * @param name  The specified name.
     * @return      The sanitized string.
     */
    public static String sanitize(String name) {
        final String firstPass = name
            .replaceAll(SPACE, DASH)
            .replaceAll(UNDERSCORE, DASH)
            .replaceAll(DOT, DASH)
            .toLowerCase(Locale.US);

        final String secondPass = INVALID_START_REGEX.matcher(firstPass).replaceAll(BLANK);

        final String thirdPass = INVALID_CHARACTER_REGEX.matcher(secondPass).replaceAll(BLANK);

        final String fourthPass = truncate(thirdPass.chars()
             //Handle consecutive dashes
            .collect(StringBuilder::new,
                (b, chr) -> {
                    int lastChar = b.length() > 0 ? b.charAt(b.length() - 1) : -1;

                    if (lastChar != '-' || chr != '-') {
                        b.appendCodePoint(chr);
                    }
             }, StringBuilder::append)
            .toString(), MAXIMUM_NAME_LENGTH);

        final int fourthPassLength = fourthPass.length();
        if (Character.isLetterOrDigit(fourthPass.charAt(fourthPassLength - 1))) {
            // this includes some letters and numbers out of ASCII range,
            // but those should have been filtered out prior
            return fourthPass;
        }

        if (fourthPassLength < MAXIMUM_NAME_LENGTH) {
            return fourthPass + "0";
        }

        return fourthPass.substring(0, MAXIMUM_NAME_LENGTH - 1) + "0";
    }


    public static boolean isValid(String name) {
        return VALID_REGEX.matcher(name).matches() && name.length() <= MAXIMUM_NAME_LENGTH;
    }

    public static void validate(String name) {
        if (!isValid(name)) {
            throw new IllegalArgumentException("Invalid name: [" + name + "].");
        }
    }
}
