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

import java.util.Date;

import io.syndesis.common.model.connection.Connection;

import org.springframework.boot.autoconfigure.social.SocialProperties;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;

/**
 * Applies given {@link AccessGrant} to mutable Connection (i.e.
 * {@link Connection.Builder}. This allows {@link ConnectionFactory}
 * implementations to apply the credentials to specific properties of the
 * {@link Connection}.
 * <p>
 * For instance setting {@code camel.component.salesforce.clientId} and
 * {@code camel.component.salesforce.clientSecret} properties on the Salesforce
 * connector.
 */
public class OAuth2Applicator implements Applicator<AccessGrant> {

    private String accessTokenExpiresAtProperty;

    private String accessTokenProperty;

    private String clientIdProperty;

    private String clientSecretProperty;

    private String refreshTokenProperty;

    private final SocialProperties socialProperties;

    public OAuth2Applicator(final SocialProperties socialProperties) {
        this.socialProperties = socialProperties;
    }

    /**
     * Default implementation that applies {@link SocialProperties} and
     * {@link AccessGrant} to {@link Connection.Builder}.
     */
    @Override
    public final Connection applyTo(final Connection connection, final AccessGrant accessGrant) {
        final Connection.Builder mutableConnection = new Connection.Builder().createFrom(connection).lastUpdated(new Date());

        Applicator.applyProperty(mutableConnection, clientIdProperty, socialProperties.getAppId());
        Applicator.applyProperty(mutableConnection, clientSecretProperty, socialProperties.getAppSecret());
        Applicator.applyProperty(mutableConnection, accessTokenProperty, accessGrant.getAccessToken());
        Applicator.applyProperty(mutableConnection, refreshTokenProperty, accessGrant.getRefreshToken());
        final Long expireTime = accessGrant.getExpireTime();
        Applicator.applyProperty(mutableConnection, accessTokenExpiresAtProperty, expireTime == null ? null : expireTime.toString());

        additionalApplication(mutableConnection, accessGrant);

        return mutableConnection.build();
    }

    public void setAccessTokenExpiresAtProperty(final String accessTokenExpiresAtProperty) {
        this.accessTokenExpiresAtProperty = accessTokenExpiresAtProperty;
    }

    public final void setAccessTokenProperty(final String accessTokenProperty) {
        this.accessTokenProperty = accessTokenProperty;
    }

    public final void setClientIdProperty(final String clientIdProperty) {
        this.clientIdProperty = clientIdProperty;
    }

    public final void setClientSecretProperty(final String clientSecretProperty) {
        this.clientSecretProperty = clientSecretProperty;
    }

    public final void setRefreshTokenProperty(final String refreshTokenProperty) {
        this.refreshTokenProperty = refreshTokenProperty;
    }

    protected void additionalApplication(final Connection.Builder mutableConnection, final AccessGrant accessGrant) {
        // subclass hook
    }

}
