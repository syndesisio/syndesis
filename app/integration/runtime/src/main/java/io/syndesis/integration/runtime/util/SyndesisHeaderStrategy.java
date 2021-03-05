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
package io.syndesis.integration.runtime.util;

import java.util.Collection;
import java.util.HashSet;

import org.apache.camel.Exchange;
import org.apache.camel.http.common.HttpHeaderFilterStrategy;

public final class SyndesisHeaderStrategy extends HttpHeaderFilterStrategy {

    public static final SyndesisHeaderStrategy INSTANCE = new SyndesisHeaderStrategy();

    public static final String ALLOWED_HEADERS = "syndesis-allowed-headers";

    /**
     * Filters headers passed on from Camel message to (possibly) external
     * service by configured filters and the allow list. The allow list needs to
     * be a {@link Collection} of {@link String}s under the
     * {@link SyndesisHeaderStrategy#ALLOWED_HEADERS} name.
     */
    @Override
    public boolean applyFilterToCamelHeaders(final String headerName, final Object headerValue, final Exchange exchange) {
        if (isAllowed(exchange, headerName)) {
            return false; // allow the header
        }

        return super.applyFilterToCamelHeaders(headerName, headerValue, exchange);
    }

    public static boolean isAllowed(final Exchange exchange, String headerName) {
        final Collection<String> allowed = allowList(exchange);

        return allowed != null && allowed.contains(headerName);
    }

    @Override
    protected void initialize() {
        super.initialize();

        // just remove everything
        setOutFilterPattern(".*");

        // we need to preserve the Content-Type header
        setInFilterPattern("^(?!Content-Type).*$");
    }

    public static void allow(final Exchange exchange, final Collection<String> headerNames) {
        for (final String headerName : headerNames) {
            allow(exchange, headerName);
        }
    }

    public static void allow(final Exchange exchange, final String headerName) {
        final Collection<String> existing = allowList(exchange);

        Collection<String> allowed = existing;
        if (existing == null) {
            allowed = new HashSet<>();
        }

        try {
            allowed.add(headerName);
        } catch (final UnsupportedOperationException ignored) {
            // handle unmodifiable collections
            allowed = new HashSet<>(existing);
            allowed.add(headerName);
        }

        exchange.setProperty(ALLOWED_HEADERS, allowed);
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> allowList(final Exchange exchange) {
        return exchange.getProperty(ALLOWED_HEADERS, Collection.class);
    }
}
