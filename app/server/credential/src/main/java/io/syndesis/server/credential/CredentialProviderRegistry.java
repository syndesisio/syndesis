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

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.dao.manager.DataManager;

import org.springframework.boot.autoconfigure.social.SocialProperties;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ClassUtils;

final class CredentialProviderRegistry implements CredentialProviderLocator {

    private final Map<String, CredentialProviderFactory> credentialProviderFactories;

    private final DataManager dataManager;

    CredentialProviderRegistry(final DataManager dataManager) {
        this.dataManager = dataManager;

        credentialProviderFactories = SpringFactoriesLoader
            .loadFactories(CredentialProviderFactory.class, ClassUtils.getDefaultClassLoader()).stream()
            .collect(Collectors.toMap(CredentialProviderFactory::id, Function.identity()));
    }

    @Override
    public CredentialProvider providerWithId(final String providerId) {
        final Connector connector = dataManager.fetch(Connector.class, providerId);

        if (connector == null) {
            throw new IllegalArgumentException("Unable to find connector with id: " + providerId);
        }

        final String providerToUse = determineProviderFrom(connector);

        final CredentialProviderFactory credentialProviderFactory = credentialProviderFactories.get(providerToUse);
        if (credentialProviderFactory == null) {
            throw new IllegalArgumentException("Unable to locate credential provider factory with id: " + providerId);
        }

        final SocialProperties socialProperties = createSocialProperties(providerToUse, connector);
        final CredentialProvider providerWithId = credentialProviderFactory.create(socialProperties);

        if (providerWithId == null) {
            throw new IllegalArgumentException("Unable to locate credential provider with id: " + providerId);
        }

        return providerWithId;
    }

    private static SocialProperties createSocialProperties(final String provider, final Connector connector) {
        try {
            if ("oauth2".equals(provider)) {
                return new OAuth2ConnectorProperties(connector);
            }

            return new ConnectorSettings(connector);
        } catch (final IllegalArgumentException ignored) {
            return UnconfiguredProperties.INSTANCE;
        }
    }

    private static String determineProviderFrom(final Connector connector) {
        final Optional<String> authentication = connector.propertyTaggedWith(Credentials.AUTHENTICATION_TYPE_TAG);

        return authentication.orElse(connector.getId().get());
    }
}
