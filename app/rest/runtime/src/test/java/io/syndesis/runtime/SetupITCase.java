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
package io.syndesis.runtime;

import java.util.Arrays;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

import io.syndesis.credential.CredentialFlowState;
import io.syndesis.credential.CredentialProvider;
import io.syndesis.credential.CredentialProviderLocator;
import io.syndesis.credential.OAuth1CredentialFlowState;
import io.syndesis.credential.OAuth1CredentialProvider;
import io.syndesis.model.connection.Connection;
import io.syndesis.rest.v1.handler.setup.OAuthAppHandler;

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

    @Autowired
    protected CredentialProviderLocator locator;

    @Test
    public void getOauthApps() {

        ResponseEntity<OAuthAppHandler.OAuthApp[]> result = get("/api/v1/setup/oauth-apps", OAuthAppHandler.OAuthApp[].class);
        List<OAuthAppHandler.OAuthApp> apps = Arrays.asList(result.getBody());
        assertThat(apps.size()).isEqualTo(2);

        OAuthAppHandler.OAuthApp twitter = apps.stream().filter(x -> "twitter".equals(x.id)).findFirst().get();
        assertThat(twitter.id).isEqualTo("twitter");
        assertThat(twitter.name).isEqualTo("Twitter");
        assertThat(twitter.icon).isEqualTo("fa-twitter");
        assertThat(twitter.clientId).isNull();
        assertThat(twitter.clientSecret).isNull();

    }

    @Test
    public void updateOauthApp() {
        // Validate initial state assumptions.
        getOauthApps();

        OAuthAppHandler.OAuthApp twitter = new OAuthAppHandler.OAuthApp();
        twitter.clientId = "test-id";
        twitter.clientSecret = "test-secret";

        http(HttpMethod.PUT, "/api/v1/setup/oauth-apps/twitter", twitter, null, tokenRule.validToken(), HttpStatus.NO_CONTENT);


        ResponseEntity<OAuthAppHandler.OAuthApp[]> result = get("/api/v1/setup/oauth-apps", OAuthAppHandler.OAuthApp[].class);
        List<OAuthAppHandler.OAuthApp> apps = Arrays.asList(result.getBody());
        assertThat(apps.size()).isEqualTo(2);

        twitter = apps.stream().filter(x -> "twitter".equals(x.id)).findFirst().get();
        assertThat(twitter.id).isEqualTo("twitter");
        assertThat(twitter.name).isEqualTo("Twitter");
        assertThat(twitter.icon).isEqualTo("fa-twitter");
        assertThat(twitter.clientId).isEqualTo("test-id");
        assertThat(twitter.clientSecret).isEqualTo("test-secret");

        // Now that we have configured the app, we should be able to create the connection factory.
        // The connection factory is setup async so we might need to wait a little bit for it to register.
        given().ignoreExceptions().await()
            .atMost(10, SECONDS)
            .pollInterval(1, SECONDS)
            .until(() -> {
                final CredentialProvider twitterCredentialProvider = locator.providerWithId("twitter");

                // preparing is something we could not do with a `null` ConnectionFactory
                assertThat(twitterCredentialProvider).isNotNull()
                    .isInstanceOfSatisfying(OAuth1CredentialProvider.class, p -> {
                        final Connection connection = new Connection.Builder().build();
                        final CredentialFlowState flowState = new OAuth1CredentialFlowState.Builder()
                            .accessToken(new OAuthToken("value", "secret")).connectorId("connectorId").build();
                        final Connection appliedTo = p.applyTo(connection, flowState);

                        // test that the updated values are used
                        assertThat(appliedTo.getConfiguredProperties())
                            .contains(entry("consumerKey", "test-id"), entry("consumerSecret", "test-secret"));
                    }); 

                return true;
            });

    }

}
