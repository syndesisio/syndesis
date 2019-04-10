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
package io.syndesis.server.credential;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.util.Json;

import org.springframework.social.oauth2.TokenStrategy;

public class OAuth2ConnectorProperties extends ConnectorSettings {

    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<Map<String,String>>() {
        // type token pattern
    };

    private final String accessTokenUrl;

    private final String authenticationUrl;

    private final String authorizationUrl;

    private final String scope;

    private final TokenStrategy tokenStrategy;

    private final boolean useParameters;

    private final Map<String, String> additionalQueryParameters;

    public OAuth2ConnectorProperties(final Connector connector) {
        super(connector);

        accessTokenUrl = requiredProperty(connector, Credentials.ACCESS_TOKEN_URL_TAG);
        authenticationUrl = optionalProperty(connector, Credentials.AUTHENTICATION_URL_TAG).orElse(null);
        authorizationUrl = requiredProperty(connector, Credentials.AUTHORIZATION_URL_TAG);
        tokenStrategy = optionalProperty(connector, Credentials.TOKEN_STRATEGY_TAG).map(TokenStrategy::valueOf)
            .orElse(TokenStrategy.AUTHORIZATION_HEADER);
        useParameters = optionalProperty(connector, Credentials.AUTHORIZE_USING_PARAMETERS_TAG).map(Boolean::valueOf).orElse(false);
        scope = optionalProperty(connector, Credentials.SCOPE_TAG).orElse(null);

        additionalQueryParameters = optionalProperty(connector, Credentials.ADDITIONAL_QUERY_PARAMETERS_TAG)
            .map(params -> readJsonMap(params))
            .orElse(Collections.emptyMap());
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

    public String getScope() {
        return scope;
    }

    public TokenStrategy getTokenStrategy() {
        return tokenStrategy;
    }

    public boolean isUseParametersForClientCredentials() {
        return useParameters;
    }

    public Map<String, String> getAdditionalQueryParameters() {
        return additionalQueryParameters;
    }

    private static Map<String, String> readJsonMap(String params) {
        try {
            return Json.reader().forType(MAP_TYPE).readValue(params);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
