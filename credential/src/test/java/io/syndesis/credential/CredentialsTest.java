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

import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import io.syndesis.credential.Acquisition.Type;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.connection.Connection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.autoconfigure.social.SocialProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.social.connect.ConnectionFactory;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.NativeWebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CredentialsTest {

    private static final MultiValueMap<String, String> NONE = new LinkedMultiValueMap<>();

    @Mock
    private CacheManager cacheManager;

    private Credentials credentials;

    @Mock
    private DataManager dataManager;

    @Mock
    private CredentialProviderLocator locator;

    private final SocialProperties properties = new SocialProperties() {
    };

    @Mock
    private NativeWebRequest request;

    @Mock
    private Cache state;

    @Before
    public void setupMocks() {
        when(cacheManager.getCache(Credentials.CACHE_NAME)).thenReturn(state);

        final HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(request.getNativeRequest(HttpServletRequest.class)).thenReturn(httpRequest);
        when(httpRequest.getRequestURL())
            .thenReturn(new StringBuffer("https://syndesis.io/api/v1/connections/providerId/credentials"));

        final Connection connection = new Connection.Builder().build();
        when(dataManager.fetch(Connection.class, "connectionId")).thenReturn(connection);

        credentials = new Credentials(locator, dataManager, cacheManager);

        properties.setAppId("appId");
        properties.setAppSecret("appSecret");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void shouldAcquireOAuth1aCredentials() {
        final OAuth1ConnectionFactory<?> oauth1 = mock(OAuth1ConnectionFactory.class);
        when((OAuth1ConnectionFactory) locator.getConnectionFactory("providerId")).thenReturn(oauth1);
        final OAuth1Operations operations = mock(OAuth1Operations.class);
        when(oauth1.getOAuthOperations()).thenReturn(operations);
        when(operations.getVersion()).thenReturn(OAuth1Version.CORE_10_REVISION_A);
        final OAuthToken token = new OAuthToken("value", "secret");
        when(operations.fetchRequestToken("https://syndesis.io/api/v1/credentials/callback", null)).thenReturn(token);

        final ArgumentCaptor<OAuth1Parameters> parameters = ArgumentCaptor.forClass(OAuth1Parameters.class);
        when(operations.buildAuthorizeUrl(eq("value"), parameters.capture()))
            .thenReturn("https://provider.io/oauth/authorize");

        final Acquisition acquisition = credentials.acquire("connectionId", "providerId", URI.create("/ui#state"),
            request);

        final Acquisition expected = new Acquisition.Builder().type(Type.REDIRECT)
            .url("https://provider.io/oauth/authorize").build();
        assertThat(acquisition).isEqualTo(expected);
        final OAuth1Parameters oAuth1Parameters = parameters.getValue();
        assertThat(oAuth1Parameters.getCallbackUrl()).isNull();
        final List<String> capturedState = oAuth1Parameters.get("state");
        assertThat(capturedState).hasSize(1);
        final String stateValue = capturedState.get(0);
        assertThat(stateValue).isNotEmpty();

        final CredentialFlowState flowState = new CredentialFlowState.Builder().key(stateValue)
            .connectionId("connectionId").providerId("providerId").returnUrl(URI.create("/ui#state")).token(token)
            .build();
        verify(state).put(stateValue, flowState);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void shouldAcquireOAuth1Credentials() {
        final OAuth1ConnectionFactory<?> oauth1 = mock(OAuth1ConnectionFactory.class);
        when((OAuth1ConnectionFactory) locator.getConnectionFactory("providerId")).thenReturn(oauth1);
        final OAuth1Operations operations = mock(OAuth1Operations.class);
        when(oauth1.getOAuthOperations()).thenReturn(operations);
        when(operations.getVersion()).thenReturn(OAuth1Version.CORE_10);
        final OAuthToken token = new OAuthToken("value", "secret");
        when(operations.fetchRequestToken(null, null)).thenReturn(token);

        final ArgumentCaptor<OAuth1Parameters> parameters = ArgumentCaptor.forClass(OAuth1Parameters.class);
        when(operations.buildAuthorizeUrl(eq("value"), parameters.capture()))
            .thenReturn("https://provider.io/oauth/authorize");

        final Acquisition acquisition = credentials.acquire("connectionId", "providerId", URI.create("/ui#state"),
            request);

        final Acquisition expected = new Acquisition.Builder().type(Type.REDIRECT)
            .url("https://provider.io/oauth/authorize").build();
        assertThat(acquisition).isEqualTo(expected);
        final OAuth1Parameters oAuth1Parameters = parameters.getValue();
        assertThat(oAuth1Parameters.getCallbackUrl()).isEqualTo("https://syndesis.io/api/v1/credentials/callback");
        final List<String> capturedState = oAuth1Parameters.get("state");
        assertThat(capturedState).hasSize(1);
        final String stateValue = capturedState.get(0);
        assertThat(stateValue).isNotEmpty();

        final CredentialFlowState flowState = new CredentialFlowState.Builder().key(stateValue)
            .connectionId("connectionId").providerId("providerId").returnUrl(URI.create("/ui#state")).token(token)
            .build();
        verify(state).put(stateValue, flowState);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void shouldAcquireOAuth2Credentials() {
        final OAuth2ConnectionFactory<?> oauth2 = mock(OAuth2ConnectionFactory.class);
        when((OAuth2ConnectionFactory) locator.getConnectionFactory("providerId")).thenReturn(oauth2);
        when(oauth2.getScope()).thenReturn("scope");
        when(oauth2.generateState()).thenReturn("state-token");
        final OAuth2Operations operations = mock(OAuth2Operations.class);
        when(oauth2.getOAuthOperations()).thenReturn(operations);
        final ArgumentCaptor<OAuth2Parameters> parameters = ArgumentCaptor.forClass(OAuth2Parameters.class);
        when(operations.buildAuthorizeUrl(parameters.capture())).thenReturn("https://provider.io/oauth/authorize");

        final Acquisition acquisition = credentials.acquire("connectionId", "providerId", URI.create("/ui#state"),
            request);

        final Acquisition expected = new Acquisition.Builder().type(Type.REDIRECT)
            .url("https://provider.io/oauth/authorize").build();
        assertThat(acquisition).isEqualTo(expected);

        final OAuth2Parameters capturedParameters = parameters.getValue();
        assertThat(capturedParameters.getRedirectUri()).isEqualTo("https://syndesis.io/api/v1/credentials/callback");
        assertThat(capturedParameters.getScope()).isEqualTo("scope");
        assertThat(capturedParameters.getState()).isEqualTo("state-token");

        final CredentialFlowState flowState = new CredentialFlowState.Builder().key("state-token")
            .connectionId("connectionId").providerId("providerId").returnUrl(URI.create("/ui#state")).build();
        verify(state).put("state-token", flowState);
    }

    @Test
    public void shouldCreateAcquisitionMethodFromConnectionFactory() {
        final ConnectionFactory<?> oauth1 = mock(OAuth1ConnectionFactory.class);
        when(oauth1.getProviderId()).thenReturn("provider1");
        final ImmutableAcquisitionMethod method1 = new AcquisitionMethod.Builder().description("provider1")
            .label("provider1").icon("provider1").type(AcquisitionMethod.Type.OAUTH1).build();
        assertThat(Credentials.acquisitionMethodFor(oauth1)).isEqualTo(method1);

        final ConnectionFactory<?> oauth2 = mock(OAuth2ConnectionFactory.class);
        when(oauth2.getProviderId()).thenReturn("provider2");
        final ImmutableAcquisitionMethod method2 = new AcquisitionMethod.Builder().description("provider2")
            .label("provider2").icon("provider2").type(AcquisitionMethod.Type.OAUTH2).build();
        assertThat(Credentials.acquisitionMethodFor(oauth2)).isEqualTo(method2);
    }

    @Test
    public void shouldDetermineAcquisitionMethodOfConnectionFactories() {
        final ConnectionFactory<?> oauth1 = mock(OAuth1ConnectionFactory.class);
        assertThat(Credentials.typeOf(oauth1)).isEqualTo(AcquisitionMethod.Type.OAUTH1);

        final ConnectionFactory<?> oauth2 = mock(OAuth2ConnectionFactory.class);
        assertThat(Credentials.typeOf(oauth2)).isEqualTo(AcquisitionMethod.Type.OAUTH2);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void shouldFinishOAuth1Acquisition() {
        final OAuthToken token = new OAuthToken("value", "secret");
        when(state.get("state", CredentialFlowState.class))
            .thenReturn(new CredentialFlowState.Builder().connectionId("connectionId").providerId("providerId")
                .token(token).returnUrl(URI.create("/ui#state")).build());

        final OAuth1ConnectionFactory<?> oauth1 = mock(OAuth1ConnectionFactory.class);
        when((OAuth1ConnectionFactory) locator.getConnectionFactory("providerId")).thenReturn(oauth1);
        final OAuth1Operations operations = mock(OAuth1Operations.class);
        when(oauth1.getOAuthOperations()).thenReturn(operations);

        when(request.getParameter("oauth_verifier")).thenReturn("verifier");

        final ArgumentCaptor<AuthorizedRequestToken> requestToken = ArgumentCaptor
            .forClass(AuthorizedRequestToken.class);
        final OAuthToken accessToken = new OAuthToken("tokenValue", "tokenSecret");
        @SuppressWarnings("unchecked")
        final Class<MultiValueMap<String, String>> multimapType = (Class) MultiValueMap.class;
        when(operations.exchangeForAccessToken(requestToken.capture(), isNull(multimapType))).thenReturn(accessToken);

        final OAuth1Applicator applicator = new OAuth1Applicator(properties);
        applicator.setAccessTokenSecretProperty("accessTokenSecretProperty");
        applicator.setAccessTokenValueProperty("accessTokenValueProperty");
        applicator.setConsumerKeyProperty("consumerKeyProperty");
        applicator.setConsumerSecretProperty("consumerSecretProperty");
        when((OAuth1Applicator) locator.getApplicator("providerId")).thenReturn(applicator);

        final URI uri = credentials.finishAcquisition("state", request);

        assertThat(uri).isEqualTo(URI.create("/ui#state"));
        final AuthorizedRequestToken capturedRequestToken = requestToken.getValue();
        assertThat(capturedRequestToken.getValue()).isEqualTo("value");
        assertThat(capturedRequestToken.getSecret()).isEqualTo("secret");
        assertThat(capturedRequestToken.getVerifier()).isEqualTo("verifier");

        final ArgumentCaptor<Connection> updatedConnection = ArgumentCaptor.forClass(Connection.class);
        verify(dataManager).update(updatedConnection.capture());

        final Connection capturedConnection = updatedConnection.getValue();
        final Connection expected = new Connection.Builder()
            .putConfiguredProperty("accessTokenSecretProperty", "tokenSecret")
            .putConfiguredProperty("accessTokenValueProperty", "tokenValue")
            .putConfiguredProperty("consumerKeyProperty", "appId")
            .putConfiguredProperty("consumerSecretProperty", "appSecret").build();
        assertThat(capturedConnection).isEqualToIgnoringGivenFields(expected, "lastUpdated");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void shouldFinishOAuth2Acquisition() {
        final OAuthToken token = new OAuthToken("value", "secret");
        when(state.get("state", CredentialFlowState.class))
            .thenReturn(new CredentialFlowState.Builder().connectionId("connectionId").providerId("providerId")
                .token(token).returnUrl(URI.create("/ui#state")).build());

        final OAuth2ConnectionFactory<?> oauth2 = mock(OAuth2ConnectionFactory.class);
        when((OAuth2ConnectionFactory) locator.getConnectionFactory("providerId")).thenReturn(oauth2);
        final OAuth2Operations operations = mock(OAuth2Operations.class);
        when(oauth2.getOAuthOperations()).thenReturn(operations);

        when(request.getParameter("code")).thenReturn("code");

        final OAuth2Applicator applicator = new OAuth2Applicator(properties);
        applicator.setAccessTokenProperty("accessTokenProperty");
        applicator.setClientIdProperty("clientIdProperty");
        applicator.setClientSecretProperty("clientSecretProperty");
        applicator.setRefreshTokenProperty("refreshTokenProperty");
        when((OAuth2Applicator) locator.getApplicator("providerId")).thenReturn(applicator);

        when(operations.exchangeForAccess("code", "https://syndesis.io/api/v1/credentials/callback", null))
            .thenReturn(new AccessGrant("accessToken", "scope", "refreshToken", 1L));

        final URI uri = credentials.finishAcquisition("state", request);

        assertThat(uri).isEqualTo(URI.create("/ui#state"));

        final ArgumentCaptor<Connection> updatedConnection = ArgumentCaptor.forClass(Connection.class);
        verify(dataManager).update(updatedConnection.capture());

        final Connection capturedConnection = updatedConnection.getValue();
        final Connection expected = new Connection.Builder().putConfiguredProperty("accessTokenProperty", "accessToken")
            .putConfiguredProperty("clientIdProperty", "appId")
            .putConfiguredProperty("clientSecretProperty", "appSecret")
            .putConfiguredProperty("refreshTokenProperty", "refreshToken").build();
        assertThat(capturedConnection).isEqualToIgnoringGivenFields(expected, "lastUpdated");
    }

    @Test
    public void shouldGenerateConnectUrl() {
        assertThat(Credentials.callbackUrlFor(request, NONE)).as("The computed callback URL is not as expected")
            .isEqualTo("https://syndesis.io/api/v1/credentials/callback");

        final MultiValueMap<String, String> some = new LinkedMultiValueMap<>();
        some.set("param1", "value1");
        some.set("param2", "value2");

        assertThat(Credentials.callbackUrlFor(request, some)).as("The computed callback URL is not as expected")
            .isEqualTo("https://syndesis.io/api/v1/credentials/callback?param1=value1&param2=value2");
    }
}
