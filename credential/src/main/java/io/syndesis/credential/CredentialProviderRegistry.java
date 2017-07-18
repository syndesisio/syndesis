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

import org.springframework.social.connect.ConnectionFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class CredentialProviderRegistry implements CredentialProviderLocator {

    private final Map<String, CredentialProvider<?, ?>> providers = new ConcurrentHashMap<>();

    public <A, T> void addCredentialProvider(final CredentialProvider<A, T> credentialProvider) {
        providers.put(credentialProvider.id(), credentialProvider);
    }

    @Override
    public Applicator<?> getApplicator(final String id) {
        return providerWithId(id).applicator();
    }

    @Override
    public ConnectionFactory<?> getConnectionFactory(final String providerId) {
        return providerWithId(providerId).connectionFactory();
    }

    private CredentialProvider<?, ?> providerWithId(final String providerId) {
        final CredentialProvider<?, ?> providerWithId = providers.get(providerId);

        if (providerWithId == null) {
            throw new IllegalArgumentException("Unable to locate credential provider with id: " + providerId);
        }

        return providerWithId;
    }

}
