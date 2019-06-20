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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.credential.AcquisitionMethod;
import io.syndesis.server.credential.AcquisitionResponse;
import io.syndesis.server.credential.AcquisitionResponse.State;
import io.syndesis.server.credential.CredentialFlowState;
import io.syndesis.server.credential.Credentials;
import io.syndesis.server.credential.OAuth2CredentialFlowState;
import io.syndesis.server.credential.Type;
import io.syndesis.server.endpoint.v1.state.ClientSideState;
import io.syndesis.server.runtime.BaseITCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.springframework.web.util.UriUtils.encode;

public class CredentialITCase extends BaseITCase {

    @Autowired
    ClientSideState clientSideState;

    @Test
    public void callbackErrorsShouldBeHandeled() {
        final String credentialKey = UUID.randomUUID().toString();
        final OAuth2CredentialFlowState flowState = new OAuth2CredentialFlowState.Builder().providerId("test-provider")
            .key(credentialKey).returnUrl(URI.create("/ui#state")).build();

        final HttpHeaders cookies = persistAsCookie(flowState);

        final ResponseEntity<Void> callbackResponse = http(HttpMethod.GET,
            "/api/v1/credentials/callback?denied=something", null, Void.class, null, cookies,
            HttpStatus.TEMPORARY_REDIRECT);

        assertThat(callbackResponse.getStatusCode()).as("Status should be temporarry redirect (307)")
            .isEqualTo(HttpStatus.TEMPORARY_REDIRECT);
        assertThat(callbackResponse.hasBody()).as("Should not contain HTTP body").isFalse();
        assertThat(callbackResponse.getHeaders().getLocation().toString()).matches(
            "http.?://localhost:[0-9]*/api/v1/ui#%7B%22connectorId%22:%22test-provider%22,%22message%22:%22Unable%20to%20update%20the%20state%20of%20authorization%22,%22status%22:%22FAILURE%22%7D");

        final List<String> receivedCookies = callbackResponse.getHeaders().get("Set-Cookie");
        assertThat(receivedCookies).hasSize(1);
        assertThat(receivedCookies.get(0)).isEqualTo("cred-o2-" + credentialKey
            + "=\"\"; path=/; secure; HttpOnly; Max-Age=0; Expires=Thu, 01-Jan-1970 00:00:00 GMT");
    }

    @After
    public void cleanupDatabase() {
        dataManager.delete(Connector.class, "test-provider");
        dataManager.delete(Connection.class, "test-connection");
    }

    @Before
    public void prepopulateDatabase() {
        final Connector provider = new Connector.Builder().id("test-provider")
            .putProperty("clientId", new ConfigurationProperty.Builder()
                .addTag(Credentials.CLIENT_ID_TAG)
                .build())
            .putProperty("clientSecret",
                new ConfigurationProperty.Builder()
                    .addTag(Credentials.CLIENT_SECRET_TAG)
                    .build())
            .putConfiguredProperty("clientId", "a-client-id")
            .putConfiguredProperty("clientSecret", "a-client-secret")
            .build();
        dataManager.create(provider);

        dataManager.create(new Connection.Builder()
            .id("test-connection")
            .connector(provider)
            .build());
    }

    @Test
    public void shouldApplyOAuthPropertiesToConnectionUpdates() {
        final OAuth2CredentialFlowState flowState = new OAuth2CredentialFlowState.Builder()
            .providerId("test-provider")
            .key("key")
            .accessGrant(new AccessGrant("token"))
            .build();

        final HttpHeaders cookies = persistAsCookie(flowState);

        final Connection newConnection = new Connection.Builder()
            .id("test-connection")
            .name("Test connection")
            .connectorId("test-provider")
            .build();

        final ResponseEntity<Void> connectionResponse = http(HttpMethod.PUT, "/api/v1/connections/test-connection",
            newConnection, Void.class, tokenRule.validToken(), cookies, HttpStatus.NO_CONTENT);
        assertThat(connectionResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        final Connection updatedConnection = dataManager.fetch(Connection.class, "test-connection");
        assertThat(updatedConnection.isDerived()).isTrue();
        assertThat(updatedConnection.getConfiguredProperties()).containsOnly(entry("accessToken", "token"),
            entry("clientId", "appId"), entry("clientSecret", "appSecret"));
    }

    @Test
    public void shouldApplyOAuthPropertiesToNewlyCreatedConnections() {
        final OAuth2CredentialFlowState flowState = new OAuth2CredentialFlowState.Builder()
            .providerId("test-provider")
            .key("key")
            .accessGrant(new AccessGrant("token"))
            .build();

        final HttpHeaders cookies = persistAsCookie(flowState);

        final Connection newConnection = new Connection.Builder().name("Test connection").connectorId("test-provider").build();
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

        final AcquisitionMethod salesforce = new AcquisitionMethod.Builder()
            .type(Type.OAUTH2)
            .label("test-provider")
            .icon("test-provider")
            .label("test-provider")
            .description("test-provider")
            .configured(true)
            .build();

        assertThat(acquisitionMethod).isEqualTo(salesforce);
    }

    @Test
    public void shouldReceiveCallbacksFromResourceProviders() {
        final OAuth2CredentialFlowState flowState = new OAuth2CredentialFlowState.Builder().providerId("test-provider")
            .key(UUID.randomUUID().toString()).returnUrl(URI.create("/ui#state")).build();

        final HttpHeaders cookies = persistAsCookie(flowState);

        final ResponseEntity<Void> callbackResponse = http(HttpMethod.GET,
            "/api/v1/credentials/callback?state=test-state&code=code", null, Void.class, null, cookies,
            HttpStatus.TEMPORARY_REDIRECT);

        assertThat(callbackResponse.getStatusCode()).as("Status should be temporarry redirect (307)")
            .isEqualTo(HttpStatus.TEMPORARY_REDIRECT);
        assertThat(callbackResponse.hasBody()).as("Should not contain HTTP body").isFalse();
        assertThat(callbackResponse.getHeaders().getLocation().toString()).matches(
            "http.?://localhost:[0-9]*/api/v1/ui#%7B%22connectorId%22:%22test-provider%22,%22message%22:%22Successfully%20authorized%20Syndesis's%20access%22,%22status%22:%22SUCCESS%22%7D");

        final List<String> receivedCookies = callbackResponse.getHeaders().get("Set-Cookie");
        assertThat(receivedCookies).hasSize(1);

        final OAuth2CredentialFlowState endingFlowState = clientSideState
            .restoreFrom(Cookie.valueOf(receivedCookies.get(0)), OAuth2CredentialFlowState.class);

        // AccessGrant does not implement equals/hashCode
        assertThat(endingFlowState).isEqualToIgnoringGivenFields(
            new OAuth2CredentialFlowState.Builder().createFrom(flowState).code("code").build(), "accessGrant");
        assertThat(endingFlowState.getAccessGrant()).isEqualToComparingFieldByField(new AccessGrant("token"));
    }

    private HttpHeaders persistAsCookie(final OAuth2CredentialFlowState flowState) {
        final NewCookie cookie = clientSideState.persist(flowState.persistenceKey(), "/", flowState);

        final HttpHeaders cookies = new HttpHeaders();
        cookies.add(HttpHeaders.COOKIE, cookie.toString());
        return cookies;
    }

}
