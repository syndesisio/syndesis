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

import io.syndesis.common.model.connection.Connection;

import org.junit.Test;
import org.springframework.boot.autoconfigure.social.SocialProperties;
import org.springframework.social.oauth1.OAuthToken;

import static org.assertj.core.api.Assertions.assertThat;

public class OAuth1ApplicatorTest {

    @Test
    public void shouldApplyTokens() {
        final SocialProperties properties = new SocialProperties() {
        };
        properties.setAppId("appId");
        properties.setAppSecret("appSecret");

        final OAuth1Applicator applicator = new OAuth1Applicator(properties);
        applicator.setAccessTokenSecretProperty("accessTokenSecretProperty");
        applicator.setAccessTokenValueProperty("accessTokenValueProperty");
        applicator.setConsumerKeyProperty("consumerKeyProperty");
        applicator.setConsumerSecretProperty("consumerSecretProperty");

        final Connection connection = new Connection.Builder().build();

        final Connection result = applicator.applyTo(connection, new OAuthToken("tokenValue", "tokenSecret"));

        final Connection expected = new Connection.Builder()
            .putConfiguredProperty("accessTokenSecretProperty", "tokenSecret")
            .putConfiguredProperty("accessTokenValueProperty", "tokenValue")
            .putConfiguredProperty("consumerKeyProperty", "appId")
            .putConfiguredProperty("consumerSecretProperty", "appSecret").build();

        assertThat(result).isEqualToIgnoringGivenFields(expected, "lastUpdated");
        assertThat(result.getLastUpdated()).isPresent();
    }
}
