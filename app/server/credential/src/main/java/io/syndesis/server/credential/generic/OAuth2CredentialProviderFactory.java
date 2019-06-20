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
package io.syndesis.server.credential.generic;

import io.syndesis.server.credential.CredentialProvider;
import io.syndesis.server.credential.CredentialProviderFactory;
import io.syndesis.server.credential.OAuth2Applicator;
import io.syndesis.server.credential.OAuth2ConnectorProperties;
import io.syndesis.server.credential.OAuth2CredentialProvider;
import io.syndesis.server.credential.UnconfiguredProperties;

import org.springframework.boot.autoconfigure.social.SocialProperties;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth2.GenericOAuth2ServiceProvider;
import org.springframework.social.oauth2.OAuth2ServiceProvider;
import org.springframework.social.oauth2.TokenStrategy;
import org.springframework.web.client.RestOperations;

public class OAuth2CredentialProviderFactory implements CredentialProviderFactory {

    @Override
    public CredentialProvider create(final SocialProperties properties) {
        if (properties instanceof UnconfiguredProperties) {
            return new OAuth2CredentialProvider<>("oauth2");
        }

        final OAuth2ConnectorProperties oauth2Properties = (OAuth2ConnectorProperties) properties;

        final String appId = oauth2Properties.getAppId();
        final String appSecret = oauth2Properties.getAppSecret();
        final String authorizationUrl = oauth2Properties.getAuthorizationUrl();
        final String authenticationUrl = oauth2Properties.getAuthenticationUrl();
        final String accessTokenUrl = oauth2Properties.getAccessTokenUrl();
        final boolean useParametersForClientCredentials = oauth2Properties.isUseParametersForClientCredentials();
        final TokenStrategy tokenStrategy = oauth2Properties.getTokenStrategy();
        final String scope = oauth2Properties.getScope();

        final OAuth2ServiceProvider<RestOperations> serviceProvider = new GenericOAuth2ServiceProvider(appId, appSecret, authorizationUrl,
            authenticationUrl, accessTokenUrl, useParametersForClientCredentials, tokenStrategy);

        final OAuth2ConnectionFactory<RestOperations> connectionFactory = new OAuth2ConnectionFactory<>("oauth2", serviceProvider, null);
        connectionFactory.setScope(scope);

        final OAuth2Applicator applicator = new OAuth2Applicator(properties);
        applicator.setAccessTokenProperty("accessToken");
        applicator.setAccessTokenExpiresAtProperty("accessTokenExpiresAt");

        applicator.setRefreshTokenProperty("refreshToken");
        applicator.setClientIdProperty("clientId");
        applicator.setClientSecretProperty("clientSecret");

        return new OAuth2CredentialProvider<>("oauth2", connectionFactory, applicator, oauth2Properties.getAdditionalQueryParameters());
    }

    @Override
    public String id() {
        return "oauth2";
    }

}
