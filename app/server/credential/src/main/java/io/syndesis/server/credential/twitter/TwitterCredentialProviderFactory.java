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
package io.syndesis.server.credential.twitter;

import io.syndesis.server.credential.CredentialProvider;
import io.syndesis.server.credential.CredentialProviderFactory;
import io.syndesis.server.credential.OAuth1Applicator;
import io.syndesis.server.credential.OAuth1CredentialProvider;
import io.syndesis.server.credential.UnconfiguredProperties;

import org.springframework.boot.autoconfigure.social.SocialProperties;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;

public final class TwitterCredentialProviderFactory implements CredentialProviderFactory {

    @Override
    public CredentialProvider create(final SocialProperties properties) {
        if (properties instanceof UnconfiguredProperties) {
            return new OAuth1CredentialProvider<>("twitter");
        }

        return createCredentialProvider(properties);
    }

    @Override
    public String id() {
        return "twitter";
    }

    static CredentialProvider createCredentialProvider(final SocialProperties properties) {
        final TwitterConnectionFactory twitter = new TwitterConnectionFactory(properties.getAppId(),
            properties.getAppSecret());
        final OAuth1Applicator applicator = new OAuth1Applicator(properties);
        applicator.setConsumerKeyProperty("consumerKey");
        applicator.setConsumerSecretProperty("consumerSecret");
        applicator.setAccessTokenSecretProperty("accessTokenSecret");
        applicator.setAccessTokenValueProperty("accessToken");

        return new OAuth1CredentialProvider<>("twitter", twitter, applicator);
    }

}
