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

import java.net.URI;
import java.util.Map;

import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class Credentials {

    public static final String ACCESS_TOKEN_TAG = "oauth-access-token";

    public static final String ACCESS_TOKEN_URL_TAG = "oauth-access-token-url";

    public static final String AUTHENTICATION_TYPE_TAG = "authentication-type";

    public static final String AUTHENTICATION_URL_TAG = "oauth-authentication-url";

    public static final String AUTHORIZATION_URL_TAG = "oauth-authorization-url";

    public static final String AUTHORIZE_USING_PARAMETERS_TAG = "oauth-authorize-using-parameters";

    public static final String CLIENT_ID_TAG = "oauth-client-id";

    public static final String CLIENT_SECRET_TAG = "oauth-client-secret";

    public static final String SCOPE_TAG = "oauth-scope";

    public static final String TOKEN_STRATEGY_TAG = "oauth-token-strategy";

    public static final String ADDITIONAL_QUERY_PARAMETERS_TAG = "oauth-additional-query-parameters";

    private final CredentialProviderLocator credentialProviderLocator;

    private final DataManager dataManager;

    private final EncryptionComponent encryptionComponent;

    @Autowired
    public Credentials(final CredentialProviderLocator credentialProviderLocator, final EncryptionComponent encryptionComponent,
        final DataManager dataManager) {
        this.credentialProviderLocator = credentialProviderLocator;
        this.encryptionComponent = encryptionComponent;
        this.dataManager = dataManager;
    }

    public AcquisitionFlow acquire(final String providerId, final URI baseUrl, final URI returnUrl) {

        final CredentialProvider credentialProvider = providerFor(providerId);

        final CredentialFlowState flowState = credentialProvider.prepare(providerId, baseUrl, returnUrl);

        return new AcquisitionFlow.Builder().type(flowState.type()).redirectUrl(flowState.getRedirectUrl()).state(flowState).build();
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

        final Connection withDerivedFlag = new Connection.Builder().createFrom(updatedConnection).isDerived(true).build();

        final Connection appliedConnection = credentialProvider.applyTo(withDerivedFlag, flowState);

        final Map<String, String> configuredProperties = appliedConnection.getConfiguredProperties();
        final Map<String, ConfigurationProperty> properties = updatedConnection.getConnector()
            .orElseGet(() -> dataManager.fetch(Connector.class, updatedConnection.getConnectorId())).getProperties();
        final Map<String, String> encryptedConfiguredProperties = encryptionComponent.encryptPropertyValues(configuredProperties,
            properties);

        return new Connection.Builder().createFrom(appliedConnection).configuredProperties(encryptedConfiguredProperties).build();
    }

    public CredentialFlowState finishAcquisition(final CredentialFlowState flowState, final URI baseUrl) {
        final CredentialProvider credentialProvider = providerFrom(flowState);

        return credentialProvider.finish(flowState, baseUrl);
    }

    CredentialProvider providerFor(final String providerId) {
        return credentialProviderLocator.providerWithId(providerId);
    }

    CredentialProvider providerFrom(final CredentialFlowState flowState) {
        final String connectorId = flowState.getConnectorId();

        return providerFor(connectorId);
    }

}
