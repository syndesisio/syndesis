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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.connector.rest.swagger.Configuration;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.runtime.util.SyndesisHeaderStrategy;

/**
 * Refreshes the OAuth token based on the expiry time of the token.
 */
class OAuthRefreshTokenProcessor implements Processor {

    /**
     * The number of milliseconds we try to refresh the access token before it
     * expires.
     */
    static final long AHEAD_OF_TIME_REFRESH_MILIS = 60 * 1000;

    private static final ObjectMapper JSON = new ObjectMapper();

    private static final Logger LOG = LoggerFactory.getLogger(OAuthRefreshTokenProcessor.class);

    Long expiresInOverride = ConnectorOptions.extractOptionAndMap(System.getenv(),
        "AUTHTOKEN_EXPIRES_IN_OVERRIDE", Long::valueOf, null);

    // Always refresh on (re)start
    final AtomicReference<Boolean> isFirstTime = new AtomicReference<>(Boolean.TRUE);

    final AtomicReference<String> lastRefreshTokenTried = new AtomicReference<>(null);

    final OAuthState state;

    private final String authorizationEndpoint;

    private final boolean authorizeUsingParameters;

    OAuthRefreshTokenProcessor(final OAuthState state, final Configuration configuration) {
        this.state = state;
        authorizationEndpoint = configuration.stringOption("authorizationEndpoint");
        authorizeUsingParameters = configuration.booleanOption("authorizeUsingParameters");
    }

    @Override
    public void process(final Exchange exchange) throws Exception {
        if (state.getRefreshToken() != null && (isFirstTime.get() || state.getAccessTokenExpiresAt() - AHEAD_OF_TIME_REFRESH_MILIS <= now())) {
            tryToRefreshAccessToken();
        }

        final Message in = exchange.getIn();
        in.setHeader("Authorization", "Bearer " + state.getAccessToken());

        SyndesisHeaderStrategy.whitelist(exchange, "Authorization");
    }

    CloseableHttpClient createHttpClient() {
        return HttpClientBuilder.create().build();
    }

    HttpUriRequest createHttpRequest() {
        final RequestBuilder builder = RequestBuilder.post(authorizationEndpoint);

        if (authorizeUsingParameters) {
            builder.addParameter("client_id", state.getClientId())
                .addParameter("client_secret", state.getClientSecret());
        }

        builder.addParameter("refresh_token", state.getRefreshToken())
            .addParameter("grant_type", "refresh_token");

        return builder.build();
    }

    long now() {
        return System.currentTimeMillis();
    }

    void processRefreshTokenResponse(final HttpEntity entity) throws IOException, JsonProcessingException {
        final JsonNode body = JSON.readTree(entity.getContent());

        if (body == null) {
            LOG.error("Received empty body while attempting to refresh access token via: {}", authorizationEndpoint);
            return;
        }

        final JsonNode accessToken = body.get("access_token");
        if (isPresentAndHasValue(accessToken)) {
            final String accessTokenValue = accessToken.asText();
            isFirstTime.set(Boolean.FALSE);
            LOG.info("Successful access token refresh");

            Long expiresInSeconds = null;
            if (expiresInOverride != null) {
                expiresInSeconds = expiresInOverride;
            } else {
                final JsonNode expiresIn = body.get("expires_in");
                if (isPresentAndHasValue(expiresIn)) {
                    expiresInSeconds = expiresIn.asLong();
                }
            }

            long accessTokenExpiresAt = 0;
            if (expiresInSeconds != null) {
                accessTokenExpiresAt = now() + expiresInSeconds * 1000;
            }

            final JsonNode refreshToken = body.get("refresh_token");
            String refreshTokenValue = null;
            if (isPresentAndHasValue(refreshToken)) {
                refreshTokenValue = refreshToken.asText();

                lastRefreshTokenTried.compareAndSet(refreshTokenValue, null);
            }

            state.update(accessTokenValue, accessTokenExpiresAt, refreshTokenValue);
        }
    }

    void tryToRefreshAccessToken() {
        final String currentRefreshToken = state.getRefreshToken();
        lastRefreshTokenTried.getAndUpdate(last -> {
            if (isFirstTime.get()) {
                return null;
            }

            if (last != null && last.equals(currentRefreshToken)) {
                return last;
            }

            return null;
        });

        if (!lastRefreshTokenTried.compareAndSet(null, currentRefreshToken)) {
            LOG.info("Already tried to refresh the access token with the current refresh token");
            return;
        }

        LOG.info("Trying to refresh the OAuth2 access token");

        try (CloseableHttpClient client = createHttpClient()) {
            final HttpUriRequest request = createHttpRequest();

            try (CloseableHttpResponse response = client.execute(request)) {
                final StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                    final HttpEntity entity = response.getEntity();
                    LOG.error("Unable to refresh the access token, received status: `{}`, response: `{}`", statusLine,
                        EntityUtils.toString(entity));
                    return;
                }

                final HttpEntity entity = response.getEntity();
                processRefreshTokenResponse(entity);
            }
        } catch (final IOException e) {
            LOG.error("Unable to refresh the access token", e);
        }
    }

    static boolean isPresentAndHasValue(final JsonNode node) {
        return node != null && !node.isNull() && !node.asText().isEmpty();
    }
}
