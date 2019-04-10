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
package io.syndesis.server.runtime.credential;

import java.util.Collections;

import io.syndesis.server.credential.CredentialProvider;
import io.syndesis.server.credential.CredentialProviderFactory;
import io.syndesis.server.credential.OAuth2Applicator;
import io.syndesis.server.credential.OAuth2CredentialProvider;

import org.mockito.ArgumentMatchers;
import org.springframework.boot.autoconfigure.social.SocialProperties;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Template;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class TestCredentialProviderFactory implements CredentialProviderFactory {

    @Override
    public CredentialProvider create(final SocialProperties properties) {
        @SuppressWarnings("unchecked")
        final OAuth2ConnectionFactory<Object> connectionFactory = mock(OAuth2ConnectionFactory.class);
        when(connectionFactory.generateState()).thenReturn("test-state");

        properties.setAppId("appId");
        properties.setAppSecret("appSecret");

        final OAuth2Applicator applicator = new OAuth2Applicator(properties);
        applicator.setAccessTokenProperty("accessToken");
        applicator.setClientIdProperty("clientId");
        applicator.setClientSecretProperty("clientSecret");
        applicator.setRefreshTokenProperty("refreshToken");

        final CredentialProvider credentialProvider = new OAuth2CredentialProvider<>("test-provider", connectionFactory,
            applicator, Collections.emptyMap());

        final OAuth2Operations operations = spy(new OAuth2Template("testClientId", "testClientSecret",
            "https://test/oauth2/authorize", "https://test/oauth2/token"));
        doReturn(new AccessGrant("token")).when(operations).exchangeForAccess(ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString(), ArgumentMatchers.isNull());

        when(connectionFactory.getOAuthOperations()).thenReturn(operations);

        return credentialProvider;
    }

    @Override
    public String id() {
        return "test-provider";
    }

}
