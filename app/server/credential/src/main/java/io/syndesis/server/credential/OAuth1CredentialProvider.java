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

import java.net.URI;
import java.util.UUID;

import io.syndesis.common.model.connection.Connection;

import org.springframework.social.connect.support.OAuth1ConnectionFactory;
import org.springframework.social.oauth1.AuthorizedRequestToken;
import org.springframework.social.oauth1.OAuth1Operations;
import org.springframework.social.oauth1.OAuth1Parameters;
import org.springframework.social.oauth1.OAuth1Version;
import org.springframework.social.oauth1.OAuthToken;

public final class OAuth1CredentialProvider<A> extends BaseCredentialProvider {

    private final Applicator<OAuthToken> applicator;

    private boolean configured;

    private final OAuth1ConnectionFactory<A> connectionFactory;

    private final String id;

    public OAuth1CredentialProvider(final String id) {
        this(id, null, null, false);
    }

    public OAuth1CredentialProvider(final String id, final OAuth1ConnectionFactory<A> connectionFactory,
        final Applicator<OAuthToken> applicator) {
        this(id, connectionFactory, applicator, true);
    }

    private OAuth1CredentialProvider(final String id, final OAuth1ConnectionFactory<A> connectionFactory,
        final Applicator<OAuthToken> applicator, final boolean configured) {
        this.id = id;
        this.connectionFactory = connectionFactory;
        this.applicator = applicator;
        this.configured = configured;
    }

    @Override
    public AcquisitionMethod acquisitionMethod() {
        return new AcquisitionMethod.Builder()
            .label(labelFor(id))
            .icon(iconFor(id))
            .type(Type.OAUTH1)
            .description(descriptionFor(id))
            .configured(configured)
            .build();
    }

    @Override
    public Connection applyTo(final Connection connection, final CredentialFlowState givenFlowState) {
        final OAuth1CredentialFlowState flowState = flowState(givenFlowState);

        return applicator.applyTo(connection, flowState.getAccessToken());
    }

    @Override
    public CredentialFlowState finish(final CredentialFlowState givenFlowState, final URI baseUrl) {
        final OAuth1CredentialFlowState flowState = flowState(givenFlowState);

        final AuthorizedRequestToken requestToken = new AuthorizedRequestToken(flowState.getToken(),
            flowState.getVerifier());

        final OAuthToken accessToken = connectionFactory.getOAuthOperations().exchangeForAccessToken(requestToken,
            null);

        return new OAuth1CredentialFlowState.Builder().createFrom(flowState).accessToken(accessToken).build();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public CredentialFlowState prepare(final String connectorId, final URI baseUrl, final URI returnUrl) {
        final OAuth1CredentialFlowState.Builder flowState = new OAuth1CredentialFlowState.Builder().returnUrl(returnUrl)
            .providerId(id);

        final OAuth1Operations oauthOperations = connectionFactory.getOAuthOperations();
        final OAuth1Parameters parameters = new OAuth1Parameters();

        final String stateKey = UUID.randomUUID().toString();
        flowState.key(stateKey);

        final OAuthToken oAuthToken;
        final OAuth1Version oAuthVersion = oauthOperations.getVersion();

        if (oAuthVersion == OAuth1Version.CORE_10) {
            parameters.setCallbackUrl(callbackUrlFor(baseUrl, EMPTY));

            oAuthToken = oauthOperations.fetchRequestToken(null, null);
        } else if (oAuthVersion == OAuth1Version.CORE_10_REVISION_A) {
            oAuthToken = oauthOperations.fetchRequestToken(callbackUrlFor(baseUrl, EMPTY), null);
        } else {
            throw new IllegalStateException("Unsupported OAuth 1 version: " + oAuthVersion);
        }
        flowState.token(oAuthToken);

        final String redirectUrl = oauthOperations.buildAuthorizeUrl(oAuthToken.getValue(), parameters);
        flowState.redirectUrl(redirectUrl);

        flowState.connectorId(connectorId);

        return flowState.build();
    }

    private static OAuth1CredentialFlowState flowState(final CredentialFlowState givenFlowState) {
        if (!(givenFlowState instanceof OAuth1CredentialFlowState)) {
            throw new IllegalArgumentException("Expected flow state to be OAUTH1, given: " + givenFlowState);
        }

        return (OAuth1CredentialFlowState) givenFlowState;
    }

}
