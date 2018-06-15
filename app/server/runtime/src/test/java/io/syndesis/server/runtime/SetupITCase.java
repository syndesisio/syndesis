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
package io.syndesis.server.runtime;

import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.credential.CredentialFlowState;
import io.syndesis.server.credential.CredentialProvider;
import io.syndesis.server.credential.CredentialProviderLocator;
import io.syndesis.server.credential.OAuth1CredentialFlowState;
import io.syndesis.server.credential.OAuth1CredentialProvider;
import io.syndesis.server.endpoint.v1.handler.setup.OAuthApp;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.social.oauth1.OAuthToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.awaitility.Awaitility.given;

/**
 * /setup/* related endpoint tests.
 */
public class SetupITCase extends BaseITCase {

    @JsonDeserialize
    public static class OAuthResult implements ListResult<OAuthApp> {

        private List<OAuthApp> items = new ArrayList<>();

        @Override
        public List<OAuthApp> getItems() {
            return items;
        }

        @Override
        public int getTotalCount() {
            return items.size();
        }

        public void setItems(final List<OAuthApp> items) {
            this.items = items;
        }
    }

    @Autowired
    protected CredentialProviderLocator locator;

    @Test
    public void getOauthApps() {
        final ResponseEntity<OAuthResult> result = get("/api/v1/setup/oauth-apps", OAuthResult.class);
        final List<OAuthApp> apps = result.getBody().getItems();
        assertThat(apps.size()).isEqualTo(4);

        final OAuthApp twitter = apps.stream().filter(x -> "twitter".equals(x.getId())).findFirst().get();
        assertThat(twitter.getId()).isEqualTo("twitter");
        assertThat(twitter.getName()).isEqualTo("Twitter");
        assertThat(twitter.getIcon()).startsWith("data:image/svg+xml;base64");
        assertThat(twitter.getClientId()).isNull();
        assertThat(twitter.getClientSecret()).isNull();

    }

    @Test
    public void shouldDeleteOAuthSettings() {
        final OAuthApp twitter = new OAuthApp();
        twitter.setClientId("test-id");
        twitter.setClientSecret("test-secret");

        put("/api/v1/setup/oauth-apps/twitter", twitter);

        given().ignoreExceptions().await().atMost(10, SECONDS).pollInterval(1, SECONDS).until(() -> {
            return locator.providerWithId("twitter") != null;
        });

        delete("/api/v1/setup/oauth-apps/twitter");

        given().ignoreExceptions().await().atMost(10, SECONDS).pollInterval(1, SECONDS).until(() -> {
            try {
                return locator.providerWithId("twitter") == null;
            } catch (final IllegalArgumentException e) {
                return e.getMessage().startsWith("No property tagged with `oauth-client-id` on connector");
            }
        });

        final Connector connector = dataManager.fetch(Connector.class, "twitter");

        assertThat(connector).isNotNull();
    }

    @Test
    public void updateOauthApp() {
        OAuthApp twitter = new OAuthApp();
        twitter.setClientId("test-id");
        twitter.setClientSecret("test-secret");

        http(HttpMethod.PUT, "/api/v1/setup/oauth-apps/twitter", twitter, null, tokenRule.validToken(), HttpStatus.NO_CONTENT);

        final ResponseEntity<OAuthResult> result = get("/api/v1/setup/oauth-apps", OAuthResult.class);
        final List<OAuthApp> apps = result.getBody().getItems();
        assertThat(apps.size()).isEqualTo(4);

        twitter = apps.stream().filter(x -> "twitter".equals(x.getId())).findFirst().get();
        assertThat(twitter.getId()).isEqualTo("twitter");
        assertThat(twitter.getName()).isEqualTo("Twitter");
        assertThat(twitter.getIcon()).startsWith("data:image/svg+xml;base64");
        assertThat(twitter.getClientId()).isEqualTo("test-id");
        assertThat(twitter.getClientSecret()).isEqualTo("test-secret");

        // Now that we have configured the app, we should be able to create the
        // connection factory.
        // The connection factory is setup async so we might need to wait a
        // little bit for it to register.
        given().ignoreExceptions().await().atMost(10, SECONDS).pollInterval(1, SECONDS).until(() -> {
            final CredentialProvider twitterCredentialProvider = locator.providerWithId("twitter");

            // preparing is something we could not do with a `null`
            // ConnectionFactory
            assertThat(twitterCredentialProvider).isNotNull().isInstanceOfSatisfying(OAuth1CredentialProvider.class, p -> {
                final Connection connection = new Connection.Builder().build();
                final CredentialFlowState flowState = new OAuth1CredentialFlowState.Builder().accessToken(new OAuthToken("value", "secret"))
                    .connectorId("connectorId").build();
                final Connection appliedTo = p.applyTo(connection, flowState);

                // test that the updated values are used
                assertThat(appliedTo.getConfiguredProperties()).contains(entry("consumerKey", "test-id"),
                    entry("consumerSecret", "test-secret"));
            });

            return true;
        });

    }
}
