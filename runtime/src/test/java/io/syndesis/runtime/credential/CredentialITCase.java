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

import io.syndesis.credential.Acquisition;
import io.syndesis.credential.AcquisitionMethod;
import io.syndesis.credential.CredentialFlowState;
import io.syndesis.credential.CredentialProvider;
import io.syndesis.credential.CredentialProviderConfiguration;
import io.syndesis.credential.Credentials;
import io.syndesis.credential.OAuth2Applicator;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.runtime.Application;
import io.syndesis.runtime.BaseITCase;
import io.syndesis.runtime.credential.CredentialITCase.TestConfiguration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.social.SocialProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
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
    private CacheManager cache;

    public static class TestConfiguration {
        @Bean
        public static CredentialProviderConfiguration provider() {
            @SuppressWarnings("unchecked")
            final CredentialProvider<Object, AccessGrant> credentialProvider = mock(CredentialProvider.class);

            @SuppressWarnings("unchecked")
            final OAuth2ConnectionFactory<Object> connectionFactory = mock(OAuth2ConnectionFactory.class);
            when(connectionFactory.getProviderId()).thenReturn("test-provider");

            when(credentialProvider.connectionFactory()).thenReturn(connectionFactory);
            when(connectionFactory.generateState()).thenReturn("test-state");

            final OAuth2Operations operations = spy(new OAuth2Template("testClientId", "testClientSecret",
                "https://test/oauth2/authorize", "https://test/oauth2/token"));
            doReturn(new AccessGrant("token")).when(operations).exchangeForAccess(Matchers.anyString(),
                Matchers.anyString(), Matchers.any(MultiValueMap.class));

            when(connectionFactory.getOAuthOperations()).thenReturn(operations);

            final SocialProperties properties = new SocialProperties() {
            };
            properties.setAppId("appId");
            properties.setAppSecret("appSecret");

            final OAuth2Applicator applicator = new OAuth2Applicator(properties);
            applicator.setAccessTokenProperty("accessToken");
            applicator.setClientIdProperty("clientId");
            applicator.setClientSecretProperty("clientSecret");
            applicator.setRefreshTokenProperty("refreshToken");

            when(credentialProvider.applicator()).thenReturn(applicator);

            return configurer -> configurer.addCredentialProvider(credentialProvider);
        }
    }

    @After
    public void cleanupDatabase() {
        dataManager.delete(Connector.class, "test-provider");
        dataManager.delete(Connection.class, "test-connection");
        cache.getCache(Credentials.CACHE_NAME).evict("test-state");
    }

    @Before
    public void prepopulateDatabase() {
        final Connector provider = new Connector.Builder().id("test-provider").build();
        dataManager.create(provider);

        dataManager.create(new Connection.Builder().id("test-connection").connector(provider).build());
    }

    @Test
    public void shouldInitiateCredentialFlow() throws UnsupportedEncodingException {
        final ResponseEntity<Acquisition> acquisitionResponse = post("/api/v1/connections/test-connection/credentials",
            Collections.singletonMap("returnUrl", "/ui#state"), Acquisition.class, tokenRule.validToken(),
            HttpStatus.OK);

        assertThat(acquisitionResponse.hasBody()).as("Should present a acquisition response in the HTTP body").isTrue();

        final Acquisition acquisition = acquisitionResponse.getBody();

        assertThat(acquisition.getType()).isEqualTo(Acquisition.Type.REDIRECT);

        final String redirectUrl = acquisition.getUrl();
        assertThat(redirectUrl).as("Should redirect to Salesforce and contain the correct callback URL")
            .startsWith("https://test/oauth2/authorize?client_id=testClientId&response_type=code&redirect_uri=")
            .contains(encode("/api/v1/credentials/callback", "ASCII"));

        final MultiValueMap<String, String> params = UriComponentsBuilder.fromHttpUrl(redirectUrl).build()
            .getQueryParams();

        final String state = params.getFirst("state");

        assertThat(state).as("state parameter should be set").isNotEmpty();

        final CredentialFlowState credentialFlowState = cache.getCache(Credentials.CACHE_NAME).get(state,
            CredentialFlowState.class);

        final CredentialFlowState expected = new CredentialFlowState.Builder().key("test-state")
            .providerId("test-provider").connectionId("test-connection").returnUrl(URI.create("/ui#state")).build();

        assertThat(credentialFlowState).as("The flow state should be as expected").isEqualTo(expected);
    }

    @Test
    public void shouldProvideCredentialsApplicableTo() {
        final ResponseEntity<AcquisitionMethod> acquisitionMethodEntity = get(
            "/api/v1/connectors/test-provider/credentials", AcquisitionMethod.class, tokenRule.validToken(),
            HttpStatus.OK);

        assertThat(acquisitionMethodEntity.hasBody()).as("Should present a acquisition method in the HTTP body")
            .isTrue();

        final AcquisitionMethod acquisitionMethod = acquisitionMethodEntity.getBody();

        final AcquisitionMethod salesforce = new AcquisitionMethod.Builder().type(AcquisitionMethod.Type.OAUTH2)
            .label("test-provider").icon("test-provider").label("test-provider").description("test-provider").build();

        assertThat(acquisitionMethod).isEqualTo(salesforce);
    }

    @Test
    public void shouldReceiveCallbacksFromResourceProviders() {
        cache.getCache(Credentials.CACHE_NAME).put("test-state", new CredentialFlowState.Builder()
            .connectionId("test-connection").providerId("test-provider").returnUrl(URI.create("/ui#state")).build());

        final ResponseEntity<Void> callbackResponse = get("/api/v1/credentials/callback?state=test-state", Void.class,
            null, HttpStatus.TEMPORARY_REDIRECT);

        assertThat(callbackResponse.getStatusCode()).as("Status should be temporarry redirect (307)")
            .isEqualTo(HttpStatus.TEMPORARY_REDIRECT);
        assertThat(callbackResponse.hasBody()).as("Should not contain HTTP body").isFalse();
        assertThat(callbackResponse.getHeaders().getLocation().toString())
            .matches("http.?://localhost:[0-9]*/api/v1/ui#state");

        final Connection connection = dataManager.fetch(Connection.class, "test-connection");

        assertThat(connection.getConfiguredProperties()).containsOnly(entry("accessToken", "token"),
            entry("clientId", "appId"), entry("clientSecret", "appSecret"));
    }

}
