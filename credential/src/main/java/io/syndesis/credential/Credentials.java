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

import java.net.URI;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.syndesis.model.connection.Connection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.social.SocialProperties;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

@Component
public final class Credentials {

    private final Map<String, CredentialProviderFactory> credentialProviderFactories;

    private final CredentialProviderLocator credentialProviderLocator;

    @Autowired
    public Credentials(final CredentialProviderLocator credentialProviderLocator) {
        this.credentialProviderLocator = credentialProviderLocator;

        credentialProviderFactories = SpringFactoriesLoader
            .loadFactories(CredentialProviderFactory.class, ClassUtils.getDefaultClassLoader()).stream()
            .collect(Collectors.toMap(CredentialProviderFactory::id, Function.identity()));
    }

    public AcquisitionFlow acquire(final String providerId, final URI baseUrl, final URI returnUrl) {

        final CredentialProvider credentialProvider = providerFor(providerId);

        final CredentialFlowState flowState = credentialProvider.prepare(baseUrl, returnUrl);

        return new AcquisitionFlow.Builder().type(flowState.type()).redirectUrl(flowState.getRedirectUrl())
            .state(flowState).build();
    }

    public AcquisitionMethod acquisitionMethodFor(final String providerId) {
        try {
            return providerFor(providerId).acquisitionMethod();
        } catch (final IllegalArgumentException ignored) {
            return AcquisitionMethod.NONE;
        }
    }

    public Connection apply(final Connection updatedConnection, final CredentialFlowState flowState) {
        final CredentialProvider credentialProvider = providerFrom(flowState);

        return credentialProvider.applyTo(updatedConnection, flowState);
    }

    public CredentialFlowState finishAcquisition(final CredentialFlowState flowState, final URI baseUrl) {
        final CredentialProvider credentialProvider = providerFrom(flowState);

        return credentialProvider.finish(flowState, baseUrl);
    }

    public void registerProvider(final String providerId, final SocialProperties properties) {
        final CredentialProviderFactory credentialProviderFactory = credentialProviderFactories.get(providerId);

        final CredentialProvider credentialProvider = credentialProviderFactory.create(properties);

        credentialProviderLocator.addCredentialProvider(credentialProvider);
    }

    /* default */ CredentialProvider providerFor(final String providerId) {
        return credentialProviderLocator.providerWithId(providerId);
    }

    /* default */ CredentialProvider providerFrom(final CredentialFlowState flowState) {
        final String providerId = flowState.getProviderId();

        return providerFor(providerId);
    }

}
