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
package io.syndesis.connector.rest.swagger.auth.oauth;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.syndesis.connector.rest.swagger.Configuration;

import org.apache.camel.Exchange;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OAuthRefreshTokenOnFailProcessor extends OAuthRefreshTokenProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthRefreshTokenOnFailProcessor.class);

    private Set<Integer> statusesToRefreshFor = new HashSet<>();

    OAuthRefreshTokenOnFailProcessor(final OAuthState state, final Configuration configuration) {
        super(state, configuration);

        final String statuses = configuration.stringOption("refreshTokenRetryStatuses");
        if (statuses != null) {
            statusesToRefreshFor = Stream.of(statuses.split("\\s*,\\s*"))
                .map(Integer::valueOf)
                .collect(Collectors.toSet());
        }
    }

    @Override
    public void process(final Exchange exchange) throws Exception {
        final Exception caught = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
        if (caught instanceof HttpOperationFailedException) {
            final HttpOperationFailedException httpFailure = (HttpOperationFailedException) caught;
            LOG.warn("Failed invoking the remote API, status: {} {}, response body: {}", httpFailure.getStatusCode(),
                httpFailure.getStatusText(), httpFailure.getResponseBody());

            if (!shouldTryRefreshingAccessCode(httpFailure)) {
                throw httpFailure;
            }

            // we don't check the return value as we will throw `httpFailure`
            // anyhow
            tryToRefreshAccessToken();

            // we need to throw the failure so that the exchange fails,
            // otherwise it might be considered successful and we do not perform
            // any retry, and that would lead to data inconsistencies
            throw httpFailure;
        }

        super.process(exchange);
    }

    boolean shouldTryRefreshingAccessCode(final HttpOperationFailedException httpFailure) {
        final int statusCode = httpFailure.getStatusCode();

        return statusesToRefreshFor.contains(statusCode);
    }
}
