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
package io.syndesis.rest.v1.util;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

public final class Urls {

    private Urls() {
        // Utility class, only static methods
    }

    public static URI apiBase(final HttpServletRequest httpRequest) {
        final URI current = URI.create(httpRequest.getRequestURL().toString());

        try {
            return new URI(current.getScheme(), null, current.getHost(), current.getPort(), basePath(current.getPath()),
                null, null);
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Unable to generate base URI based on the current (`" + current + "`)",
                e);
        }
    }

    /* default */ static String basePath(final String path) {
        final String[] parts = path.split("/");

        return "/" + parts[1] + "/" + parts[2] + "/";
    }

}
