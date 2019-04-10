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

import io.syndesis.server.credential.TestCredentialProviderFactory.TestCredentialProvider;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CredentialProviderRegistryTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutUnregisteredProviders() {
        final DataManager dataManager = mock(DataManager.class);
        final CredentialProviderRegistry registry = new CredentialProviderRegistry(dataManager);

        registry.providerWithId("unregistered");
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
}
