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
package io.syndesis.credential;

import io.syndesis.dao.manager.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

@Configuration
public class CredentialConfiguration {

    @Bean
    @Autowired()
    public CredentialProviderLocator credentialProviderLocator(Optional<List<CredentialProvider>> providers) {
        final CredentialProviderRegistry registry = new CredentialProviderRegistry();
        if ( providers.isPresent() ) {
            for (CredentialProvider provider : providers.get()) {
                registry.addCredentialProvider(provider);
            }
        }
        return registry;
    }

    @Bean
    public Credentials credentials(final CredentialProviderLocator connectionProviderLocator,
        final DataManager dataManager, final CacheManager cacheManager) {
        return new Credentials(connectionProviderLocator, dataManager, cacheManager);
    }

}
