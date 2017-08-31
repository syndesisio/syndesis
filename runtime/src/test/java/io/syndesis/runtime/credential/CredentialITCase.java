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
package io.syndesis.runtime.credential;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

import io.syndesis.credential.AcquisitionMethod;
import io.syndesis.credential.AcquisitionResponse;
import io.syndesis.credential.AcquisitionResponse.State;
import io.syndesis.credential.CredentialFlowState;
import io.syndesis.credential.CredentialProvider;
import io.syndesis.credential.CredentialProviderLocator;
import io.syndesis.credential.OAuth2Applicator;
import io.syndesis.credential.OAuth2CredentialFlowState;
import io.syndesis.credential.OAuth2CredentialProvider;
import io.syndesis.credential.Type;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.rest.v1.state.ClientSideState;
import io.syndesis.runtime.Application;
import io.syndesis.runtime.BaseITCase;
import io.syndesis.runtime.credential.CredentialITCase.TestConfiguration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.social.SocialProperties;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Template;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.web.util.UriUtils.encode;

@Import({Application.class, TestConfiguration.class})
public class CredentialITCase extends BaseITCase {

    @Autowired
    ClientSideState clientSideState;

    public static class TestConfiguration {

        public TestConfiguration(final CredentialProviderLocator locator) {
            locator.addCredentialProvider(provider());
        }

        public CredentialProvider provider() {
            @SuppressWarnings("unchecked")
            final OAuth2ConnectionFactory<Object> connectionFactory = mock(OAuth2ConnectionFactory.class);
            when(connectionFactory.generateState()).thenReturn("test-state");

            final SocialProperties properties = new SocialProperties() {
            };
            properties.setAppId("appId");
            properties.setAppSecret("appSecret");

            final OAuth2Applicator applicator = new OAuth2Applicator(properties);
            applicator.setAccessTokenProperty("accessToken");
            applicator.setClientIdProperty("clientId");
            applicator.setClientSecretProperty("clientSecret");
            applicator.setRefreshTokenProperty("refreshToken");

            final CredentialProvider credentialProvider = new OAuth2CredentialProvider<>("test-provider",
                connectionFactory, applicator);

            final OAuth2Operations operations = spy(new OAuth2Template("testClientId", "testClientSecret",
                "https://test/oauth2/authorize", "https://test/oauth2/token"));
            doReturn(new AccessGrant("token")).when(operations).exchangeForAccess(Matchers.anyString(),
                Matchers.anyString(), Matchers.any(MultiValueMap.class));

            when(connectionFactory.getOAuthOperations()).thenReturn(operations);

            return credentialProvider;
        }
    }

    @After
    public void cleanupDatabase() {
        dataManager.delete(Connector.class, "test-provider");
        dataManager.delete(Connection.class, "test-connection");
    }

    @Before
    public void prepopulateDatabase() {
        final Connector provider = new Connector.Builder().id("test-provider").build();
        dataManager.create(provider);

        dataManager.create(new Connection.Builder().id("test-connection").connector(provider).build());
    }

    @Test
    public void shouldApplyOAuthPropertiesToNewlyCreatedConnections() {
        final OAuth2CredentialFlowState flowState = new OAuth2CredentialFlowState.Builder().providerId("test-provider")
            .key("key").accessGrant(new AccessGrant("token")).build();

        final HttpHeaders cookies = persistAsCookie(flowState);

        final Connection newConnection = new Connection.Builder().name("Test connection").build();
        final ResponseEntity<Connection> connectionResponse = http(HttpMethod.POST, "/api/v1/connections",
            newConnection, Connection.class, tokenRule.validToken(), cookies, HttpStatus.OK);

        assertThat(connectionResponse.hasBody()).as("Should contain created connection").isTrue();

        final Connection createdConnection = connectionResponse.getBody();
        assertThat(createdConnection.isDerived()).isTrue();
        assertThat(createdConnection.getConfiguredProperties()).containsOnly(entry("accessToken", "token"),
            entry("clientId", "appId"), entry("clientSecret", "appSecret"));
    }

    @Test
    public void shouldInitiateCredentialFlow() throws UnsupportedEncodingException {
        final ResponseEntity<AcquisitionResponse> acquisitionResponse = post(
            "/api/v1/connectors/test-provider/credentials", Collections.singletonMap("returnUrl", "/ui#state"),
            AcquisitionResponse.class, tokenRule.validToken(), HttpStatus.ACCEPTED);

        assertThat(acquisitionResponse.hasBody()).as("Should present a acquisition response in the HTTP body").isTrue();

        final AcquisitionResponse response = acquisitionResponse.getBody();
        assertThat(response.getType()).isEqualTo(Type.OAUTH2);

        final String redirectUrl = response.getRedirectUrl();
        assertThat(redirectUrl).as("Should redirect to Salesforce and containthe correct callback URL")
            .startsWith("https://test/oauth2/authorize?client_id=testClientId&response_type=code&redirect_uri=")
            .contains(encode("/api/v1/credentials/callback", "ASCII"));

        final MultiValueMap<String, String> params = UriComponentsBuilder.fromHttpUrl(redirectUrl).build()
            .getQueryParams();

        final String state = params.getFirst("state");

        assertThat(state).as("state parameter should be set").isNotEmpty();

        final State responseStateInstruction = response.state();
        assertThat(responseStateInstruction).as("acquisition response should contain the state instruction")
            .isNotNull();
        assertThat(responseStateInstruction.persist()).isEqualByComparingTo(State.Persist.COOKIE);
        assertThat(responseStateInstruction.spec()).isNotEmpty();

        final CredentialFlowState credentialFlowState = clientSideState
            .restoreFrom(Cookie.valueOf(responseStateInstruction.spec()), CredentialFlowState.class);

        final CredentialFlowState expected = new OAuth2CredentialFlowState.Builder().key("test-state")
            .providerId("test-provider").build();

        assertThat(credentialFlowState).as("The flow state should be as expected")
            .isEqualToIgnoringGivenFields(expected, "returnUrl");
        final URI returnUrl = credentialFlowState.getReturnUrl();
        assertThat(returnUrl).isNotNull();
        assertThat(returnUrl.isAbsolute()).isTrue();
        assertThat(returnUrl.getPath()).isEqualTo("/ui");
        assertThat(returnUrl.getFragment()).isEqualTo("state");
    }

    @Test
    public void shouldProvideCredentialsApplicableTo() {
        final ResponseEntity<AcquisitionMethod> acquisitionMethodEntity = get(
            "/api/v1/connectors/test-provider/credentials", AcquisitionMethod.class, tokenRule.validToken(),
            HttpStatus.OK);

        assertThat(acquisitionMethodEntity.hasBody()).as("Should present a acquisition method in the HTTP body")
            .isTrue();

        final AcquisitionMethod acquisitionMethod = acquisitionMethodEntity.getBody();

        final AcquisitionMethod salesforce = new AcquisitionMethod.Builder().type(Type.OAUTH2).label("test-provider")
            .icon("test-provider").label("test-provider").description("test-provider").build();

        assertThat(acquisitionMethod).isEqualTo(salesforce);
    }

    @Test
    public void shouldReceiveCallbacksFromResourceProviders() {
        final OAuth2CredentialFlowState flowState = new OAuth2CredentialFlowState.Builder().providerId("test-provider")
            .key(UUID.randomUUID().toString()).returnUrl(URI.create("/ui#state")).build();

        final HttpHeaders cookies = persistAsCookie(flowState);

        final ResponseEntity<Void> callbackResponse = http(HttpMethod.GET,
            "/api/v1/credentials/callback?state=test-state", null, Void.class, null, cookies,
            HttpStatus.TEMPORARY_REDIRECT);

        assertThat(callbackResponse.getStatusCode()).as("Status should be temporarry redirect (307)")
            .isEqualTo(HttpStatus.TEMPORARY_REDIRECT);
        assertThat(callbackResponse.hasBody()).as("Should not contain HTTP body").isFalse();
        assertThat(callbackResponse.getHeaders().getLocation().toString())
            .matches("http.?://localhost:[0-9]*/api/v1/ui#%7B%22connectorId%22:%22test-provider%22,%22message%22:%22Successfully%20authorized%20Syndesis's%20access%22,%22status%22:%22SUCCESS%22%7D");

        final List<String> receivedCookies = callbackResponse.getHeaders().get("Set-Cookie");
        assertThat(receivedCookies).hasSize(1);

        final OAuth2CredentialFlowState endingFlowState = clientSideState
            .restoreFrom(Cookie.valueOf(receivedCookies.get(0)), OAuth2CredentialFlowState.class);

        // AccessGrant does not implement equals/hashCode
        assertThat(endingFlowState).isEqualToIgnoringGivenFields(flowState, "accessGrant");
        assertThat(endingFlowState.getAccessGrant()).isEqualToComparingFieldByField(new AccessGrant("token"));
    }

    private HttpHeaders persistAsCookie(final OAuth2CredentialFlowState flowState) {
        final NewCookie cookie = clientSideState.persist(flowState.persistenceKey(), "/", flowState);

        final HttpHeaders cookies = new HttpHeaders();
        cookies.add(HttpHeaders.COOKIE, cookie.toString());
        return cookies;
    }

}
