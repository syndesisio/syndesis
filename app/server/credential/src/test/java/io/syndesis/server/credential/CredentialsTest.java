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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.autoconfigure.social.SocialProperties;
import org.springframework.social.connect.support.OAuth1ConnectionFactory;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth1.AuthorizedRequestToken;
import org.springframework.social.oauth1.OAuth1Operations;
import org.springframework.social.oauth1.OAuth1Parameters;
import org.springframework.social.oauth1.OAuth1Version;
import org.springframework.social.oauth1.OAuthToken;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CredentialsTest {

    private Credentials credentials;

    @Mock
    private DataManager dataManager;

    @Mock
    private EncryptionComponent encryptionComponent;

    @Mock
    private CredentialProviderLocator locator;

    private final SocialProperties properties = new SocialProperties() {
        // simple social properties just for test purposes
    };

    @Before
    @SuppressWarnings("unchecked")
    public void setupMocks() {
        credentials = new Credentials(locator, encryptionComponent, dataManager);

        properties.setAppId("appId");
        properties.setAppSecret("appSecret");

        when(encryptionComponent.encryptPropertyValues(anyMap(), anyMap())).then(invocation -> invocation.getArgument(0));
    }

    @Test
    public void shouldAcquireOAuth1aCredentials() {
        final OAuth1ConnectionFactory<?> oauth1 = mock(OAuth1ConnectionFactory.class);
        @SuppressWarnings("unchecked")
        final Applicator<OAuthToken> applicator = mock(Applicator.class);
        when(locator.providerWithId("providerId"))
            .thenReturn(new OAuth1CredentialProvider<>("providerId", oauth1, applicator));

        final OAuth1Operations operations = mock(OAuth1Operations.class);
        when(oauth1.getOAuthOperations()).thenReturn(operations);
        when(operations.getVersion()).thenReturn(OAuth1Version.CORE_10_REVISION_A);
        final OAuthToken token = new OAuthToken("value", "secret");
        when(operations.fetchRequestToken("https://syndesis.io/api/v1/credentials/callback", null)).thenReturn(token);

        final ArgumentCaptor<OAuth1Parameters> parameters = ArgumentCaptor.forClass(OAuth1Parameters.class);
        when(operations.buildAuthorizeUrl(eq("value"), parameters.capture()))
            .thenReturn("https://provider.io/oauth/authorize");

        final AcquisitionFlow acquisition = credentials.acquire("providerId", URI.create("https://syndesis.io/api/v1/"),
            URI.create("https://syndesis.io/ui#state"));

        final CredentialFlowState expectedFlowState = new OAuth1CredentialFlowState.Builder().providerId("providerId")
            .redirectUrl("https://provider.io/oauth/authorize").returnUrl(URI.create("https://syndesis.io/ui#state"))
            .token(token).build();

        final AcquisitionFlow expected = new AcquisitionFlow.Builder().type(Type.OAUTH1)
            .redirectUrl("https://provider.io/oauth/authorize").state(expectedFlowState).build();
        assertThat(acquisition).isEqualToIgnoringGivenFields(expected, "state");

        final Optional<CredentialFlowState> maybeState = acquisition.state();
        assertThat(maybeState).isPresent();
        final CredentialFlowState state = maybeState.get();
        assertThat(state).isEqualToIgnoringGivenFields(expectedFlowState, "key");
        assertThat(state.getKey()).isNotNull();

        final OAuth1Parameters oAuth1Parameters = parameters.getValue();
        assertThat(oAuth1Parameters.getCallbackUrl()).isNull();
    }

    @Test
    public void shouldAcquireOAuth1Credentials() {
        final OAuth1ConnectionFactory<?> oauth1 = mock(OAuth1ConnectionFactory.class);
        @SuppressWarnings("unchecked")
        final Applicator<OAuthToken> applicator = mock(Applicator.class);
        when(locator.providerWithId("providerId"))
            .thenReturn(new OAuth1CredentialProvider<>("providerId", oauth1, applicator));

        final OAuth1Operations operations = mock(OAuth1Operations.class);
        when(oauth1.getOAuthOperations()).thenReturn(operations);
        when(operations.getVersion()).thenReturn(OAuth1Version.CORE_10);
        final OAuthToken token = new OAuthToken("value", "secret");
        when(operations.fetchRequestToken(null, null)).thenReturn(token);

        final ArgumentCaptor<OAuth1Parameters> parameters = ArgumentCaptor.forClass(OAuth1Parameters.class);
        when(operations.buildAuthorizeUrl(eq("value"), parameters.capture()))
            .thenReturn("https://provider.io/oauth/authorize");

        final AcquisitionFlow acquisition = credentials.acquire("providerId", URI.create("https://syndesis.io/api/v1/"),
            URI.create("/ui#state"));

        final CredentialFlowState expectedFlowState = new OAuth1CredentialFlowState.Builder().providerId("providerId")
            .redirectUrl("https://provider.io/oauth/authorize").returnUrl(URI.create("/ui#state")).token(token).build();

        final AcquisitionFlow expected = new AcquisitionFlow.Builder().type(Type.OAUTH1)
            .redirectUrl("https://provider.io/oauth/authorize").state(expectedFlowState).build();
        assertThat(acquisition).isEqualToIgnoringGivenFields(expected, "state");

        final Optional<CredentialFlowState> maybeState = acquisition.state();
        assertThat(maybeState).isPresent();
        final CredentialFlowState state = maybeState.get();
        assertThat(state).isEqualToIgnoringGivenFields(expectedFlowState, "key");
        assertThat(state.getKey()).isNotNull();

        final OAuth1Parameters oAuth1Parameters = parameters.getValue();
        assertThat(oAuth1Parameters.getCallbackUrl()).isEqualTo("https://syndesis.io/api/v1/credentials/callback");
    }

    @Test
    public void shouldAcquireOAuth2Credentials() {
        final OAuth2ConnectionFactory<?> oauth2 = mock(OAuth2ConnectionFactory.class);
        @SuppressWarnings("unchecked")
        final Applicator<AccessGrant> applicator = mock(Applicator.class);
        when(locator.providerWithId("providerId"))
        .thenReturn(new OAuth2CredentialProvider<>("providerId", oauth2, applicator, Collections.emptyMap()));

        when(oauth2.getScope()).thenReturn("scope");
        when(oauth2.generateState()).thenReturn("state-token");
        final OAuth2Operations operations = mock(OAuth2Operations.class);
        when(oauth2.getOAuthOperations()).thenReturn(operations);
        final ArgumentCaptor<OAuth2Parameters> parameters = ArgumentCaptor.forClass(OAuth2Parameters.class);
        when(operations.buildAuthorizeUrl(parameters.capture())).thenReturn("https://provider.io/oauth/authorize");

        final AcquisitionFlow acquisition = credentials.acquire("providerId", URI.create("https://syndesis.io/api/v1/"),
            URI.create("/ui#state"));

        final CredentialFlowState expectedFlowState = new OAuth2CredentialFlowState.Builder().key("state-token")
            .providerId("providerId").redirectUrl("https://provider.io/oauth/authorize")
            .returnUrl(URI.create("/ui#state")).build();

        final AcquisitionFlow expected = new AcquisitionFlow.Builder().type(Type.OAUTH2)
            .redirectUrl("https://provider.io/oauth/authorize").state(expectedFlowState).build();
        assertThat(acquisition).isEqualTo(expected);

        final OAuth2Parameters capturedParameters = parameters.getValue();
        assertThat(capturedParameters.getRedirectUri()).isEqualTo("https://syndesis.io/api/v1/credentials/callback");
        assertThat(capturedParameters.getScope()).isEqualTo("scope");
        assertThat(capturedParameters.getState()).isEqualTo("state-token");
    }

    @Test
    public void shouldAcquireOAuth2CredentialsWithAdditionalQueryParameters() {
        final OAuth2ConnectionFactory<?> oauth2 = mock(OAuth2ConnectionFactory.class);
        @SuppressWarnings("unchecked")
        final Applicator<AccessGrant> applicator = mock(Applicator.class);
        final Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("q1", "v1");
        queryParameters.put("q2", "v2");
        when(locator.providerWithId("providerId"))
            .thenReturn(new OAuth2CredentialProvider<>("providerId", oauth2, applicator, queryParameters));

        when(oauth2.getScope()).thenReturn("scope");
        when(oauth2.generateState()).thenReturn("state-token");
        final OAuth2Operations operations = mock(OAuth2Operations.class);
        when(oauth2.getOAuthOperations()).thenReturn(operations);
        final ArgumentCaptor<OAuth2Parameters> parameters = ArgumentCaptor.forClass(OAuth2Parameters.class);
        when(operations.buildAuthorizeUrl(parameters.capture())).thenReturn("https://provider.io/oauth/authorize");

        final AcquisitionFlow acquisition = credentials.acquire("providerId", URI.create("https://syndesis.io/api/v1/"),
            URI.create("/ui#state"));

        final CredentialFlowState expectedFlowState = new OAuth2CredentialFlowState.Builder().key("state-token")
            .providerId("providerId").redirectUrl("https://provider.io/oauth/authorize")
            .returnUrl(URI.create("/ui#state")).build();

        final AcquisitionFlow expected = new AcquisitionFlow.Builder().type(Type.OAUTH2)
            .redirectUrl("https://provider.io/oauth/authorize").state(expectedFlowState).build();
        assertThat(acquisition).isEqualTo(expected);

        final OAuth2Parameters capturedParameters = parameters.getValue();
        assertThat(capturedParameters.getRedirectUri()).isEqualTo("https://syndesis.io/api/v1/credentials/callback");
        assertThat(capturedParameters.getScope()).isEqualTo("scope");
        assertThat(capturedParameters.getState()).isEqualTo("state-token");
        assertThat(capturedParameters.get("q1")).containsOnly("v1");
        assertThat(capturedParameters.get("q2")).containsOnly("v2");
    }

    @Test
    public void shouldApplyReceivedCredentialsToConnections() {
        final CredentialProvider credentialProvider = mock(CredentialProvider.class);

        when(locator.providerWithId("providerId")).thenReturn(credentialProvider);

        final CredentialFlowState flowState = new OAuth2CredentialFlowState.Builder().providerId("providerId")
            .returnUrl(URI.create("/ui#state")).code("code").state("state").build();

        final Connection connection = new Connection.Builder()
            .connector(new Connector.Builder().putProperty("key", new ConfigurationProperty.Builder().build()).build())
            .build();
        when(credentialProvider.applyTo(new Connection.Builder().createFrom(connection).isDerived(true).build(),
            flowState))
                .then(a -> new Connection.Builder().createFrom(a.getArgument(0))
                    .putConfiguredProperty("key", "value").build());

        final Connection finishedConnection = credentials.apply(connection, flowState);

        assertThat(finishedConnection).isNotNull();
        assertThat(finishedConnection.getConfiguredProperties()).contains(entry("key", "value"));
        assertThat(finishedConnection.isDerived()).isTrue();
    }

    @Test
    public void shouldFinishOAuth1Acquisition() {
        final OAuthToken token = new OAuthToken("value", "secret");

        final OAuth1ConnectionFactory<?> oauth1 = mock(OAuth1ConnectionFactory.class);
        final OAuth1Applicator applicator = new OAuth1Applicator(properties);
        when(locator.providerWithId("providerId"))
            .thenReturn(new OAuth1CredentialProvider<>("providerId", oauth1, applicator));

        final OAuth1Operations operations = mock(OAuth1Operations.class);
        when(oauth1.getOAuthOperations()).thenReturn(operations);

        final ArgumentCaptor<AuthorizedRequestToken> requestToken = ArgumentCaptor
            .forClass(AuthorizedRequestToken.class);
        final OAuthToken accessToken = new OAuthToken("tokenValue", "tokenSecret");
        @SuppressWarnings({"unchecked", "rawtypes"})
        final Class<MultiValueMap<String, String>> multimapType = (Class) MultiValueMap.class;
        when(operations.exchangeForAccessToken(requestToken.capture(), isNull(multimapType))).thenReturn(accessToken);

        applicator.setAccessTokenSecretProperty("accessTokenSecretProperty");
        applicator.setAccessTokenValueProperty("accessTokenValueProperty");
        applicator.setConsumerKeyProperty("consumerKeyProperty");
        applicator.setConsumerSecretProperty("consumerSecretProperty");

        final CredentialFlowState flowState = new OAuth1CredentialFlowState.Builder().providerId("providerId")
            .token(token).returnUrl(URI.create("/ui#state")).verifier("verifier").build();

        final CredentialFlowState finalFlowState = credentials.finishAcquisition(flowState,
            URI.create("https://www.example.com"));

        final AuthorizedRequestToken capturedRequestToken = requestToken.getValue();
        assertThat(capturedRequestToken.getValue()).isEqualTo("value");
        assertThat(capturedRequestToken.getSecret()).isEqualTo("secret");
        assertThat(capturedRequestToken.getVerifier()).isEqualTo("verifier");

        assertThat(finalFlowState)
            .isEqualTo(new OAuth1CredentialFlowState.Builder().createFrom(flowState).accessToken(accessToken).build());
    }

    @Test
    public void shouldFinishOAuth2Acquisition() {
        final OAuth2ConnectionFactory<?> oauth2 = mock(OAuth2ConnectionFactory.class);

        final OAuth2Applicator applicator = new OAuth2Applicator(properties);
        applicator.setAccessTokenProperty("accessTokenProperty");
        applicator.setClientIdProperty("clientIdProperty");
        applicator.setClientSecretProperty("clientSecretProperty");
        applicator.setRefreshTokenProperty("refreshTokenProperty");

        when(locator.providerWithId("providerId"))
        .thenReturn(new OAuth2CredentialProvider<>("providerId", oauth2, applicator, Collections.emptyMap()));
        final OAuth2Operations operations = mock(OAuth2Operations.class);
        when(oauth2.getOAuthOperations()).thenReturn(operations);

        final AccessGrant accessGrant = new AccessGrant("accessToken", "scope", "refreshToken", 1L);
        when(operations.exchangeForAccess("code", "https://syndesis.io/api/v1/credentials/callback", null))
        .thenReturn(accessGrant);

        final CredentialFlowState flowState = new OAuth2CredentialFlowState.Builder().providerId("providerId")
            .returnUrl(URI.create("/ui#state")).code("code").state("state").build();

        final CredentialFlowState finalFlowState = credentials.finishAcquisition(flowState,
            URI.create("https://syndesis.io/api/v1/"));

        assertThat(finalFlowState)
        .isEqualTo(new OAuth2CredentialFlowState.Builder().createFrom(flowState).accessGrant(accessGrant).build());
    }
}
