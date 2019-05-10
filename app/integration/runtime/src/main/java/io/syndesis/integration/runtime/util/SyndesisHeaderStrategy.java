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

    public static final String WHITELISTED_HEADERS = "syndesis-whitelisted-headers";

    /**
     * Filters headers passed on from Camel message to (possibly) external
     * service by configured filters and the whitelist. The whitelist needs to
     * be a {@link Collection} of {@link String}s under the
     * {@link SyndesisHeaderStrategy#WHITELISTED_HEADERS} name.
     */
    @Override
    public boolean applyFilterToCamelHeaders(final String headerName, final Object headerValue, final Exchange exchange) {
        if (isWhitelisted(exchange, headerName)) {
            return false; // allow the header
        }

        return super.applyFilterToCamelHeaders(headerName, headerValue, exchange);
    }

    public static boolean isWhitelisted(final Exchange exchange, String headerName) {
        final Collection<String> whitelisted = whitelisted(exchange);

        return whitelisted != null && whitelisted.contains(headerName);
    }

    @Override
    protected void initialize() {
        super.initialize();

        // just remove everything
        setOutFilterPattern(".*");

        // we need to preserve the Content-Type header
        setInFilterPattern("^(?!Content-Type).*$");
    }

    public static void whitelist(final Exchange exchange, final Collection<String> headerNames) {
        for (final String headerName : headerNames) {
            whitelist(exchange, headerName);
        }
    }

    public static void whitelist(final Exchange exchange, final String headerName) {
        final Collection<String> existing = whitelisted(exchange);

        Collection<String> whitelisted = existing;
        if (existing == null) {
            whitelisted = new HashSet<>();
        }

        try {
            whitelisted.add(headerName);
        } catch (final UnsupportedOperationException ignored) {
            // handle unmodifiable collections
            whitelisted = new HashSet<>(existing);
            whitelisted.add(headerName);
        }

        exchange.setProperty(WHITELISTED_HEADERS, whitelisted);
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> whitelisted(final Exchange exchange) {
        return exchange.getProperty(WHITELISTED_HEADERS, Collection.class);
    }
}
