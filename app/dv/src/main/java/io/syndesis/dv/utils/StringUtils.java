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
package io.syndesis.dv.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This is a common place to put String utility methods.
 *
 *
 */
public final class StringUtils {

    /**
     * @param text the text being checked (may be <code>null</code>)
     * @return <code>true</code> if the specified text is <code>null</code>, contains only spaces, or is empty
     */
    public static boolean isBlank(final String text) {
        return ((text == null) || (text.trim().length() == 0));
    }

    /**
     * @return The stack trace of the given throwable as a string
     */
    public static String exceptionToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Don't allow construction outside of this class.
     */
    private StringUtils() {
        // nothing to do
    }

}
