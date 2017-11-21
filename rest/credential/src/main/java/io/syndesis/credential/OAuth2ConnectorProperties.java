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
package io.syndesis.credential;

import io.syndesis.model.connection.Connector;

import org.springframework.social.oauth2.TokenStrategy;

public class OAuth2ConnectorProperties extends ConnectorSettings {

    private final String accessTokenUrl;

    private final String authenticationUrl;

    private final String authorizationUrl;

    private final TokenStrategy tokenStrategy;

    private final boolean useParameters;

    public OAuth2ConnectorProperties(final Connector connector) {
        super(connector);

        accessTokenUrl = requiredProperty(connector, Credentials.ACCESS_TOKEN_URL_TAG);
        authenticationUrl = optionalProperty(connector, Credentials.AUTHENTICATION_URL_TAG).orElse(null);
        authorizationUrl = requiredProperty(connector, Credentials.AUTHORIZATION_URL_TAG);
        tokenStrategy = optionalProperty(connector, Credentials.TOKEN_STRATEGY_TAG).map(TokenStrategy::valueOf)
            .orElse(TokenStrategy.AUTHORIZATION_HEADER);
        useParameters = optionalProperty(connector, Credentials.AUTHORIZE_USING_PARAMETERS_TAG).map(Boolean::valueOf)
            .orElse(false);
    }

    public String getAccessTokenUrl() {
        return accessTokenUrl;
    }

    public String getAuthenticationUrl() {
        return authenticationUrl;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public TokenStrategy getTokenStrategy() {
        return tokenStrategy;
    }

    public boolean isUseParametersForClientCredentials() {
        return useParameters;
    }

}
