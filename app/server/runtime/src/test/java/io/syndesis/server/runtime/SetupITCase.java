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

import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.credential.AcquisitionMethod;
import io.syndesis.server.credential.CredentialFlowState;
import io.syndesis.server.credential.CredentialProvider;
import io.syndesis.server.credential.CredentialProviderLocator;
import io.syndesis.server.credential.Credentials;
import io.syndesis.server.credential.OAuth1CredentialFlowState;
import io.syndesis.server.credential.OAuth1CredentialProvider;
import io.syndesis.server.endpoint.v1.handler.setup.OAuthApp;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.social.oauth1.OAuthToken;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static java.util.concurrent.TimeUnit.SECONDS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.awaitility.Awaitility.given;

/**
 * /setup/* related endpoint tests.
 */
public class SetupITCase extends BaseITCase {

    static final ConfigurationProperty CLIENT_ID_PROPERTY = new ConfigurationProperty.Builder().addTag(Credentials.CLIENT_ID_TAG).build();

    static final ConfigurationProperty CLIENT_SECRET_PROPERTY = new ConfigurationProperty.Builder().addTag(Credentials.CLIENT_SECRET_TAG).build();

    @Autowired
    protected CredentialProviderLocator locator;

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

    @Test
    public void getOAuthApps() {
        final ResponseEntity<OAuthResult> result = get("/api/v1/setup/oauth-apps", OAuthResult.class);
        final List<OAuthApp> apps = result.getBody().getItems();
        assertThat(apps).isNotEmpty();

        final OAuthApp twitter = apps.stream().filter(x -> x.idEquals("twitter")).findFirst().get();
        assertThat(twitter.getId()).hasValue("twitter");
        assertThat(twitter.getName()).isEqualTo("Twitter");
        assertThat(twitter.getIcon()).startsWith("assets:");
        assertThat(twitter.propertyTaggedWith(Credentials.CLIENT_ID_TAG)).isNotPresent();
        assertThat(twitter.propertyTaggedWith(Credentials.CLIENT_SECRET_TAG)).isNotPresent();

    }

    @Test
    public void shouldDeleteOAuthSettings() {
        final OAuthApp twitter = new OAuthApp.Builder()//
            .putProperty("consumerKey", CLIENT_ID_PROPERTY)//
            .putProperty("consumerSecret", CLIENT_SECRET_PROPERTY)//
            .putConfiguredProperty("consumerKey", "test-id")//
            .putConfiguredProperty("consumerSecret", "test-secret")//
            .build();

        put("/api/v1/setup/oauth-apps/twitter", twitter);

        given().ignoreExceptions().await().atMost(10, SECONDS).pollInterval(1, SECONDS).until(() -> {
            final CredentialProvider twitterProvider = locator.providerWithId("twitter");

            final AcquisitionMethod acquisitionMethod = twitterProvider.acquisitionMethod();

            return acquisitionMethod.configured();
        });

        delete("/api/v1/setup/oauth-apps/twitter");

        given().ignoreExceptions().await().atMost(10, SECONDS).pollInterval(1, SECONDS).until(() -> {
            try {
                final CredentialProvider twitterProvider = locator.providerWithId("twitter");

                final AcquisitionMethod acquisitionMethod = twitterProvider.acquisitionMethod();

                return !acquisitionMethod.configured();
            } catch (final IllegalArgumentException e) {
                return e.getMessage().startsWith("No property tagged with `oauth-client-id` on connector");
            }
        });

        final Connector connector = dataManager.fetch(Connector.class, "twitter");

        assertThat(connector).isNotNull();
    }

    @Test
    public void updateOAuthApp() {
        final OAuthApp twitter = new OAuthApp.Builder()//
            .putProperty("consumerKey", CLIENT_ID_PROPERTY)//
            .putProperty("consumerSecret", CLIENT_SECRET_PROPERTY)//
            .putConfiguredProperty("consumerKey", "test-id")//
            .putConfiguredProperty("consumerSecret", "test-secret")//
            .build();

        http(HttpMethod.PUT, "/api/v1/setup/oauth-apps/twitter", twitter, null, tokenRule.validToken(), HttpStatus.NO_CONTENT);

        final ResponseEntity<OAuthResult> result = get("/api/v1/setup/oauth-apps", OAuthResult.class);
        final List<OAuthApp> apps = result.getBody().getItems();
        assertThat(apps).isNotEmpty();

        final OAuthApp updated = apps.stream().filter(x -> x.idEquals("twitter")).findFirst().get();
        assertThat(updated.getId()).hasValue("twitter");
        assertThat(updated.getName()).isEqualTo("Twitter");
        assertThat(updated.getIcon()).startsWith("assets:");
        assertThat(updated.propertyTaggedWith(Credentials.CLIENT_ID_TAG)).hasValue("test-id");
        assertThat(updated.propertyTaggedWith(Credentials.CLIENT_SECRET_TAG)).hasValue("test-secret");

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
