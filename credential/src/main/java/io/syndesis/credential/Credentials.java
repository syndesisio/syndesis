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

import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.connection.Connection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class Credentials {
    private final CredentialProviderLocator credentialProviderLocator;

    private final DataManager dataManager;

    @Autowired
    public Credentials(final CredentialProviderLocator credentialProviderLocator, final DataManager dataManager) {
        this.credentialProviderLocator = credentialProviderLocator;
        this.dataManager = dataManager;
    }

    public AcquisitionFlow acquire(final String connectionId, final String providerId, final URI baseUrl,
        final URI returnUrl) {

        final CredentialProvider credentialProvider = providerFor(providerId);

        final CredentialFlowState flowState = credentialProvider.prepare(baseUrl, returnUrl, connectionId);

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

    public URI finishAcquisition(final CredentialFlowState flowState, final URI baseUrl) {
        final String connectionId = flowState.getConnectionId();
        final Connection connection = dataManager.fetch(Connection.class, connectionId);

        final String providerId = flowState.getProviderId();
        final CredentialProvider credentialProvider = providerFor(providerId);

        final Connection updatedConnection = credentialProvider.finish(connection, flowState, baseUrl);

        dataManager.update(updatedConnection);

        return flowState.getReturnUrl();
    }

    protected CredentialProvider providerFor(final String providerId) {
        return credentialProviderLocator.providerWithId(providerId);
    }

}
