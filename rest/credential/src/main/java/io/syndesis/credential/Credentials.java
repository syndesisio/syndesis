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

import io.syndesis.model.connection.Connection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class Credentials {

    public static final String CLIENT_ID_TAG = "oauth-client-id";

    public static final String CLIENT_SECRET_TAG = "oauth-client-secret";

    private final CredentialProviderLocator credentialProviderLocator;

    @Autowired
    public Credentials(final CredentialProviderLocator credentialProviderLocator) {
        this.credentialProviderLocator = credentialProviderLocator;
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

        final Connection withDerivedFlag = new Connection.Builder().createFrom(updatedConnection).isDerived(true)
            .build();

        return credentialProvider.applyTo(withDerivedFlag, flowState);
    }

    public CredentialFlowState finishAcquisition(final CredentialFlowState flowState, final URI baseUrl) {
        final CredentialProvider credentialProvider = providerFrom(flowState);

        return credentialProvider.finish(flowState, baseUrl);
    }

    /* default */ CredentialProvider providerFor(final String providerId) {
        return credentialProviderLocator.providerWithId(providerId);
    }

    /* default */ CredentialProvider providerFrom(final CredentialFlowState flowState) {
        final String providerId = flowState.getProviderId();

        return providerFor(providerId);
    }

}
