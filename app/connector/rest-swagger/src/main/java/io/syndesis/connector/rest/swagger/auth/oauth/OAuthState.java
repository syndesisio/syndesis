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

import io.syndesis.connector.rest.swagger.Configuration;

final class OAuthState {

    private final String clientId;

    private final String clientSecret;

    private volatile State state;

    private static final class State {
        private final String accessToken;

        private final long accessTokenExpiresAt;

        private final String refreshToken;

        private State(final String accessToken, final long accessTokenExpiresAt, final String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.accessTokenExpiresAt = accessTokenExpiresAt;
        }

        static State create(final String accessToken, final long accessTokenExpiresAt, final String refreshToken) {
            return new State(accessToken, accessTokenExpiresAt, refreshToken);
        }
    }

    private OAuthState(final String clientId, final String clientSecret, final String accessToken, final long accessTokenExpiresAt, final String refreshToken) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        state = State.create(accessToken, accessTokenExpiresAt, refreshToken);
    }

    String getAccessToken() {
        return state == null ? null : state.accessToken;
    }

    long getAccessTokenExpiresAt() {
        return state == null ? 0L : state.accessTokenExpiresAt;
    }

    String getClientId() {
        return clientId;
    }

    String getClientSecret() {
        return clientSecret;
    }

    String getRefreshToken() {
        return state == null ? null : state.refreshToken;
    }

    void update(final String accessToken, final long accessTokenExpiresAt, final String refreshToken) {
        state = State.create(accessToken, accessTokenExpiresAt, refreshToken == null ? state.refreshToken : refreshToken);
    }

    static OAuthState createFrom(final Configuration configuration) {
        return new OAuthState(configuration.stringOption("clientId"),
            configuration.stringOption("clientSecret"),
            configuration.stringOption("accessToken"),
            configuration.longOption("accessTokenExpiresAt"),
            configuration.stringOption("refreshToken"));
    }
}
