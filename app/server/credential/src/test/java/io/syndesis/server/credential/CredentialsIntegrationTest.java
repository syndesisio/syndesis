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

import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.util.EventBus;
import io.syndesis.server.credential.CredentialsIntegrationTest.TestConfiguration;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {CredentialConfiguration.class, TestConfiguration.class})
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@Configuration
@ExtendWith(SpringExtension.class)
public class CredentialsIntegrationTest {

    @Autowired
    private CredentialProviderLocator credentialProviderLocator;

    @Configuration
    public static class TestConfiguration {

        @Bean
        public TextEncryptor getTextEncryptor() {
            return Encryptors.noOpText();
        }

        @Bean
        public EncryptionComponent encryptionComponent() {
            return new EncryptionComponent(getTextEncryptor());
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

    @ParameterizedTest
    @ValueSource(strings = {"salesforce", "twitter"})
    public void shouldSupportResourceProviders(final String provider) {
        assertThat(credentialProviderLocator.providerWithId(provider)).isNotNull();
    }
}
