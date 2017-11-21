/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.generator.swagger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;

/* default */ final class InMemoryUrlStreamHandler extends URLStreamHandler {
    private final String specification;

    /* default */ InMemoryUrlStreamHandler(final String specification) {
        this.specification = specification;
    }

    @Override
    protected URLConnection openConnection(final URL u) throws IOException {
        return new URLConnection(u) {
            @Override
            public void connect() throws IOException {
                // NOP
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(specification.getBytes(StandardCharsets.UTF_8));
            }
        };
    }
}

