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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static io.syndesis.common.util.Strings.utf8;

/**
 * Utilities for working with InputStream and OutputStreams
 */
public final class IOStreams {

    private IOStreams(){
    }

    public static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buff = new byte[1024];
        int len;
        while( (len=is.read(buff))>=0 ) { // NOPMD
            os.write(buff, 0, len);
        }
    }

    public static byte[] readBytes(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        copy(is, os);
        return os.toByteArray();
    }

    public static String readText(InputStream is) throws IOException {
        return utf8(readBytes(is));
    }

}
