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
import io.syndesis.server.credential.TestCredentialProviderFactory.TestCredentialProvider;
import io.syndesis.server.dao.manager.DataManager;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CredentialProviderRegistryTest {

    @Test
    public void shouldComplainAboutUnregisteredProviders() {
        final DataManager dataManager = mock(DataManager.class);
        final CredentialProviderRegistry registry = new CredentialProviderRegistry(dataManager);

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> registry.providerWithId("unregistered"))
            .withMessage("Unable to find connector with id: unregistered");
    }

    @Test
    public void shouldFetchProvidersFromDataManager() {
        final DataManager dataManager = mock(DataManager.class);
        final CredentialProviderRegistry registry = new CredentialProviderRegistry(dataManager);

        final Connector connector = new Connector.Builder().id("test-provider")
            .putProperty("clientId", new ConfigurationProperty.Builder().addTag(Credentials.CLIENT_ID_TAG).build())
            .putProperty("clientSecret",
                new ConfigurationProperty.Builder().addTag(Credentials.CLIENT_SECRET_TAG).build())
            .putConfiguredProperty("clientId", "a-client-id").putConfiguredProperty("clientSecret", "a-client-secret")
            .build();
        when(dataManager.fetch(Connector.class, "test-provider")).thenReturn(connector);

        assertThat(registry.providerWithId("test-provider")).isInstanceOfSatisfying(TestCredentialProvider.class, p -> {
            assertThat(p.getProperties().getAppId()).isEqualTo("a-client-id");
            assertThat(p.getProperties().getAppSecret()).isEqualTo("a-client-secret");
        });
    }

    @Test
    public void shouldSupportDescriptiveAuthenticationTypes() {
        // authentication types from OpenAPI are generated in the form of
        // `type:id`, this is to distinguish different security constraints
        // of the same type, alas with different IDs
        final Connector connector = new Connector.Builder()
            .putProperty("authType", new ConfigurationProperty.Builder()
                .addTag(Credentials.AUTHENTICATION_TYPE_TAG)
                .build())
            .build();

        assertThat(CredentialProviderRegistry.determineProviderFrom(withAuthenticationType(connector, "oauth2"))).isEqualTo("oauth2");
        assertThat(CredentialProviderRegistry.determineProviderFrom(withAuthenticationType(connector, "oauth2:"))).isEqualTo("oauth2");
        assertThat(CredentialProviderRegistry.determineProviderFrom(withAuthenticationType(connector, "oauth2:id"))).isEqualTo("oauth2");
    }

    private static Connector withAuthenticationType(final Connector connector, String type) {
        return connector.builder()
            .putOrRemoveConfiguredPropertyTaggedWith(Credentials.AUTHENTICATION_TYPE_TAG, type)
            .build();
    }
}
