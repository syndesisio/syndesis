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
package io.syndesis.credential.twitter;

import io.syndesis.credential.CredentialProviderLocator;
import io.syndesis.credential.DefaultCredentialProvider;
import io.syndesis.credential.OAuth1Applicator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.social.TwitterProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;

@Configuration
@ConditionalOnClass(TwitterConnectionFactory.class)
@ConditionalOnProperty(prefix = "spring.social.twitter", name = "app-id")
@EnableConfigurationProperties(TwitterProperties.class)
public class TwitterConfiguration {

    private final OAuth1Applicator applicator;
    private final TwitterConnectionFactory twitter;

    @Autowired
    protected TwitterConfiguration(final TwitterProperties properties, final CredentialProviderLocator locator) {
        twitter = new TwitterConnectionFactory(properties.getAppId(), properties.getAppSecret());
        applicator = new OAuth1Applicator(properties);
        applicator.setConsumerKeyProperty("consumerKey");
        applicator.setConsumerSecretProperty("consumerSecret");
        applicator.setAccessTokenSecretProperty("accessTokenSecret");
        applicator.setAccessTokenValueProperty("accessToken");

        locator.addCredentialProvider(new DefaultCredentialProvider<>("twitter", twitter, applicator));
    }

}
