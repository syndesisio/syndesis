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

import static io.syndesis.common.util.IOStreams.readBytes;
import static io.syndesis.common.util.IOStreams.readText;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utilities for working with class loader resources.
 */
public final class Resources {

    private Resources() {
    }

    public static byte[] getResourceAsBytes(String file) throws IOException {
        return getResourceAsBytes(file, Resources.class.getClassLoader());
    }

    public static byte[] getResourceAsBytes(String file, ClassLoader cl) throws IOException {
        InputStream i = cl.getResourceAsStream(file);
        if (i == null) {
            return null;
        }
        try (InputStream is = i) {
            return readBytes(is);
        }
    }

    public static String getResourceAsText(String file) throws IOException {
        return getResourceAsText(file, Resources.class.getClassLoader());
    }

    public static String getResourceAsText(String file, ClassLoader cl) throws IOException {
        InputStream i = cl.getResourceAsStream(file);
        if (i == null) {
            return null;
        }
        try (InputStream is = i) {
            return readText(is);
        }
    }

}
