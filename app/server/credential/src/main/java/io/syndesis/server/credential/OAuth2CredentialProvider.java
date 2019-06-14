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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import io.syndesis.common.model.connection.Connection;

import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;

public final class OAuth2CredentialProvider<S> extends BaseCredentialProvider {

    private final Map<String, List<String>> additionalQueryParameters;

    private final Applicator<AccessGrant> applicator;

    private boolean configured;

    private final OAuth2ConnectionFactory<S> connectionFactory;

    private final String id;

    public OAuth2CredentialProvider(final String id) {
        this(id, null, null, Collections.emptyMap(), false);
    }

    public OAuth2CredentialProvider(final String id, final OAuth2ConnectionFactory<S> connectionFactory, final Applicator<AccessGrant> applicator,
        final Map<String, String> additionalQueryParameters) {
        this(id, connectionFactory, applicator, additionalQueryParameters, true);
    }

    private OAuth2CredentialProvider(final String id, final OAuth2ConnectionFactory<S> connectionFactory, final Applicator<AccessGrant> applicator,
        final Map<String, String> additionalQueryParameters, final boolean configured) {
        this.id = id;
        this.connectionFactory = connectionFactory;
        this.applicator = applicator;
        this.configured = configured;
        this.additionalQueryParameters = additionalQueryParameters.entrySet().stream()
            .collect(Collectors.toMap(Entry::getKey, e -> Collections.singletonList(e.getValue())));
    }

    @Override
    public AcquisitionMethod acquisitionMethod() {
        return new AcquisitionMethod.Builder()
            .label(labelFor(id))
            .icon(iconFor(id))
            .type(Type.OAUTH2)
            .description(descriptionFor(id))
            .configured(configured)
            .build();
    }

    @Override
    public Connection applyTo(final Connection connection, final CredentialFlowState givenFlowState) {
        final OAuth2CredentialFlowState flowState = flowState(givenFlowState);

        return applicator.applyTo(connection, flowState.getAccessGrant());
    }

    @Override
    public CredentialFlowState finish(final CredentialFlowState givenFlowState, final URI baseUrl) {
        final OAuth2CredentialFlowState flowState = flowState(givenFlowState);

        final AccessGrant accessGrant = connectionFactory.getOAuthOperations().exchangeForAccess(flowState.getCode(), callbackUrlFor(baseUrl, EMPTY), null);

        return new OAuth2CredentialFlowState.Builder().createFrom(flowState).accessGrant(accessGrant).build();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public CredentialFlowState prepare(final String connectorId, final URI baseUrl, final URI returnUrl) {
        final OAuth2CredentialFlowState.Builder flowState = new OAuth2CredentialFlowState.Builder().returnUrl(returnUrl).providerId(id);

        final OAuth2Parameters parameters = new OAuth2Parameters();
        parameters.putAll(additionalQueryParameters);

        final String callbackUrl = callbackUrlFor(baseUrl, EMPTY);
        parameters.setRedirectUri(callbackUrl);

        final String scope = connectionFactory.getScope();
        parameters.setScope(scope);

        final String stateKey = connectionFactory.generateState();
        flowState.key(stateKey);
        parameters.add("state", stateKey);

        final OAuth2Operations oauthOperations = connectionFactory.getOAuthOperations();

        final String redirectUrl = oauthOperations.buildAuthorizeUrl(parameters);
        flowState.redirectUrl(redirectUrl);

        flowState.connectorId(connectorId);

        return flowState.build();
    }

    private static OAuth2CredentialFlowState flowState(final CredentialFlowState givenFlowState) {
        if (!(givenFlowState instanceof OAuth2CredentialFlowState)) {
            throw new IllegalArgumentException("Expected flow state to be OAUTH2, given: " + givenFlowState);
        }

        return (OAuth2CredentialFlowState) givenFlowState;
    }

}
