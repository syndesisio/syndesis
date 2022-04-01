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
package io.syndesis.server.api.generator.soap.parser;

import java.io.InputStream;
import java.net.URI;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class Resolver implements EntityResolver {
    @Override
    @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION")
    public InputSource resolveEntity(final String publicId, final String systemId) {
        final URI uri = parseUri(systemId);
        if (uri == null) {
            // default
            return new InputSource(systemId);
        }

        final String key = (uri.getHost() + uri.getPath()).replace('/', '_').replaceAll("_$", "");

        final String path = "/schema/" + key + ".xml";
        // The stream gets passed via the InputSource, and we can only assume
        // that the client will fetch it from there and make sure it is closed
        final InputStream resource = Resolver.class.getResourceAsStream(path);
        if (resource == null) {
            // default
            return new InputSource(systemId);
        }

        final InputSource inputSource = new InputSource();
        inputSource.setByteStream(resource);
        inputSource.setPublicId(publicId);
        inputSource.setSystemId(systemId);

        return inputSource;
    }

    static URI parseUri(final String given) {
        if (given == null) {
            return null;
        }

        try {
            return URI.create(given);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }
}
