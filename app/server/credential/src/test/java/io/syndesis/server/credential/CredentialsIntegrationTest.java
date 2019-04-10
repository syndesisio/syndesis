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

import io.syndesis.common.util.EventBus;
import io.syndesis.server.credential.CredentialsIntegrationTest.TestConfiguration;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.social.FacebookAutoConfiguration;
import org.springframework.boot.autoconfigure.social.LinkedInAutoConfiguration;
import org.springframework.boot.autoconfigure.social.SocialWebAutoConfiguration;
import org.springframework.boot.autoconfigure.social.TwitterAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
@ContextConfiguration(classes = {CredentialConfiguration.class, TestConfiguration.class})
@SpringBootTest
@EnableAutoConfiguration(exclude = {TwitterAutoConfiguration.class, FacebookAutoConfiguration.class,
    LinkedInAutoConfiguration.class, SocialWebAutoConfiguration.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@Configuration
public class CredentialsIntegrationTest {

    @Parameter(0)
    public static String PROVIDER;

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private CredentialProviderLocator credentialProviderLocator;

    @Configuration
    public static class TestConfiguration {

        @Bean
        public TextEncryptor getTextEncryptor() {
            return Encryptors.noOpText();
        }

        @Bean
        public DataManager dataManager() {
            final DataManager dataManager = mock(DataManager.class);

            final Connector salesforceConnector = mockConnector("salesforce");
            when(dataManager.fetch(Connector.class, "salesforce")).thenReturn(salesforceConnector);

            final Connector twitterConnector = mockConnector("twitter");
            when(dataManager.fetch(Connector.class, "twitter")).thenReturn(twitterConnector);

            return dataManager;
        }

        @Bean
        public EventBus eventBus() {
            return mock(EventBus.class);
        }

        static Connector mockConnector(final String id) {
            return new Connector.Builder().id(id)
                .putProperty("clientId", new ConfigurationProperty.Builder().addTag(Credentials.CLIENT_ID_TAG).build())
                .putProperty("clientSecret",
                    new ConfigurationProperty.Builder().addTag(Credentials.CLIENT_SECRET_TAG).build())
                .putConfiguredProperty("clientId", "a-client-id")
                .putConfiguredProperty("clientSecret", "a-client-secret").build();
        }
    }

    @Test
    public void shouldSupportResourceProviders() {
        assertThat(credentialProviderLocator.providerWithId(PROVIDER)).isNotNull();
    }

    @Parameters(name = "provider={0}")
    public static Iterable<String> resourceProviders() {
        return Arrays.asList("salesforce", "twitter");
    }
}
