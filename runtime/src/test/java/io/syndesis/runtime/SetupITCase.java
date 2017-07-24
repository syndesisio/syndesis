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

import io.syndesis.credential.CredentialProviderLocator;
import io.syndesis.credential.DefaultCredentialProvider;
import io.syndesis.rest.v1.handler.setup.OAuthAppHandler;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.social.connect.ConnectionFactory;

import java.util.Arrays;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.given;

/**
 * /setup/* related endpoint tests.
 */
public class SetupITCase extends BaseITCase {

    @Autowired
    protected CredentialProviderLocator locator;

    @Before
    public void resetDB() {
        super.resetDB();
    }

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
    public void updateOauthApp() throws InterruptedException {

        // Lets register a null connection factory for twitter, in case one was previously registered.
        locator.addCredentialProvider(new DefaultCredentialProvider<>("twitter", null, null));
        assertThat(locator.getConnectionFactory("twitter")).isNull();

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
            .until(() ->{

            ConnectionFactory<?>twitterConnectionFactory = locator.getConnectionFactory("twitter");
            assertThat(twitterConnectionFactory).isNotNull();
            return true;
        });

    }



}
