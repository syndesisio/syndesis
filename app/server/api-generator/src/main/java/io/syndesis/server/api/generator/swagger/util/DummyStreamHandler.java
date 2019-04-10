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
package io.syndesis.server.api.generator.swagger.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

final class DummyStreamHandler extends URLStreamHandler {

    static final URL DUMMY_URL;
    static {
        try {
            DUMMY_URL = new URL("dummy", null, 0, "part", new DummyStreamHandler());
        } catch (final MalformedURLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DummyStreamHandler() {
        // only needed for `DUMMY_URL`
    }

    @Override
    protected URLConnection openConnection(final URL url) throws IOException {
        return null;
    }
}
