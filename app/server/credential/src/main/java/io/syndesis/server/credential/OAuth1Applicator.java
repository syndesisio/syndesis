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
import org.springframework.social.oauth1.OAuthToken;

public class OAuth1Applicator implements Applicator<OAuthToken> {

    private String accessTokenSecretProperty;

    private String accessTokenValueProperty;

    private final String consumerKey;

    private String consumerKeyProperty;

    private final String consumerSecret;

    private String consumerSecretProperty;

    public OAuth1Applicator(final SocialProperties properties) {
        consumerKey = properties.getAppId();
        consumerSecret = properties.getAppSecret();
    }

    @Override
    public Connection applyTo(final Connection connection, final OAuthToken token) {
        final Connection.Builder mutableConnection = new Connection.Builder().createFrom(connection)
            .lastUpdated(new Date());

        Applicator.applyProperty(mutableConnection, accessTokenValueProperty, token.getValue());
        Applicator.applyProperty(mutableConnection, accessTokenSecretProperty, token.getSecret());
        Applicator.applyProperty(mutableConnection, consumerKeyProperty, consumerKey);
        Applicator.applyProperty(mutableConnection, consumerSecretProperty, consumerSecret);

        return mutableConnection.build();
    }

    public void setAccessTokenSecretProperty(final String accessTokenSecretProperty) {
        this.accessTokenSecretProperty = accessTokenSecretProperty;
    }

    public void setAccessTokenValueProperty(final String accessTokenValueProperty) {
        this.accessTokenValueProperty = accessTokenValueProperty;
    }

    public void setConsumerKeyProperty(final String consumerKeyProperty) {
        this.consumerKeyProperty = consumerKeyProperty;
    }

    public void setConsumerSecretProperty(final String consumerSecretProperty) {
        this.consumerSecretProperty = consumerSecretProperty;
    }

}
