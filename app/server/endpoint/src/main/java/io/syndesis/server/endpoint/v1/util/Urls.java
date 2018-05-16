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
package io.syndesis.server.endpoint.v1.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public final class Urls {

    static final String ORIGIN_HEADER = "X-Forwarded-Origin";

    private static final Pattern HOST_AND_PORT_PATTERN = Pattern.compile("(?<host>[^:]+)(?::(?<port>\\d+))?");

    private Urls() {
        // Utility class, only static methods
    }

    public static URI absoluteTo(final HttpServletRequest httpRequest, final URI url) {
        final URI current = currentUri(httpRequest);

        try {
            return new URI(current.getScheme(), null, current.getHost(), current.getPort(), url.getPath(),
                url.getQuery(), url.getFragment());
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException(
                "Unable to generate URI based on the current (`" + current + "`) and the return (`" + url + "`) URLs",
                e);
        }
    }

    public static URI appHome(final HttpServletRequest httpRequest) {
        final URI current = currentUri(httpRequest);

        try {
            return new URI(current.getScheme(), null, current.getHost(), current.getPort(), null, null, null);
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException(
                "Unable to generate app home URI based on the current (`" + current + "`)", e);
        }
    }

    public static URI apiBase(final HttpServletRequest httpRequest) {
        final URI current = currentUri(httpRequest);

        try {
            return new URI(current.getScheme(), null, current.getHost(), current.getPort(), basePath(current.getPath()),
                null, null);
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Unable to generate base URI based on the current (`" + current + "`)",
                e);
        }
    }

    static String basePath(final String path) {
        final String[] parts = path.split("/", -1);

        return "/" + parts[1] + "/" + parts[2] + "/";
    }

    static URI currentUri(final HttpServletRequest httpRequest) {
        // in development we have Angular CLI proxying to HA proxy (openshift
        // router) that proxies to the syndesis-server; we need the origin
        // hostname/port/scheme/
        final String origin = httpRequest.getHeader(ORIGIN_HEADER);
        final Optional<URI> maybeOriginUri = parseOrigin(origin);

        final URI currentUri = URI.create(httpRequest.getRequestURL().toString());

        if (maybeOriginUri.isPresent()) {

            try {
                final URI originUri = maybeOriginUri.get();
                return new URI(originUri.getScheme(), null, originUri.getHost(), originUri.getPort(),
                    currentUri.getPath(), currentUri.getQuery(), currentUri.getFragment());
            } catch (final URISyntaxException e) {
                throw new IllegalArgumentException(
                    "Could not parse and recreate the current URI with origin of: " + origin, e);
            }
        }

        return currentUri;
    }

    static Map<String, String> parseKeyValueParts(final String origin) {
        final String[] parts = origin.split(";", 3);
        if (parts.length == 0) {
            return Collections.emptyMap();
        }

        final Map<String, String> keyValues = new HashMap<>(3);
        for (final String part : parts) {
            final String[] keyValue = part.split("=", 2);

            if (keyValue.length != 2) {
                continue;
            }

            final String key = keyValue[0].trim();
            final String value = keyValue[1].trim();
            if (!key.isEmpty() && !value.isEmpty()) {
                keyValues.put(key, value);
            }
        }
        return keyValues;
    }

    static Optional<URI> parseOrigin(final String origin) {
        if (origin == null || origin.isEmpty()) {
            return Optional.empty();
        }

        @SuppressWarnings("PMD.UseConcurrentHashMap")
        final Map<String, String> keyValues = parseKeyValueParts(origin);

        final String scheme = keyValues.get("proto");
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            return Optional.empty();
        }

        final String hostAndPort = keyValues.get("host");
        final Matcher matcher = HOST_AND_PORT_PATTERN.matcher(hostAndPort);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        final String host = matcher.group("host");
        final String portStr = matcher.group("port");

        try {
            if (portStr == null || portStr.isEmpty()) {
                return Optional.of(new URI(scheme, host, null, null));
            }

            final int port = Integer.parseUnsignedInt(portStr);
            return Optional.of(new URI(scheme, null, host, port, null, null, null));
        } catch (final URISyntaxException ignored) {
            return Optional.empty();
        }
    }

}
