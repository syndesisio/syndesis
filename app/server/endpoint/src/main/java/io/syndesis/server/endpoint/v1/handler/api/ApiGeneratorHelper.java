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
package io.syndesis.server.endpoint.v1.handler.api;

import java.io.IOException;

import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.api.generator.APIIntegration;
import io.syndesis.server.api.generator.ProvidedApiTemplate;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.endpoint.v1.handler.api.ApiHandler.APIFormData;

import okio.BufferedSource;
import okio.Okio;

public final class ApiGeneratorHelper {

    private static final String API_PROVIDER_CONNECTION_ID = "api-provider";
    private static final String API_PROVIDER_END_ACTION_ID = "io.syndesis:api-provider-end";
    private static final String API_PROVIDER_START_ACTION_ID = "io.syndesis:api-provider-start";

    public static APIIntegration generateIntegrationFrom(final APIFormData apiFormData, final DataManager dataManager, final APIGenerator apiGenerator) {
        Connection apiProviderConnection = dataManager.fetch(Connection.class, API_PROVIDER_CONNECTION_ID);
        if (apiProviderConnection == null) {
            throw new IllegalStateException("Cannot find api-provider connection with id: " + API_PROVIDER_CONNECTION_ID);
        }

        final String spec = getSpec(apiFormData);
        if (!apiProviderConnection.getConnector().isPresent()) {
            final Connector apiProviderConnector = dataManager.fetch(Connector.class, apiProviderConnection.getConnectorId());
            apiProviderConnection = new Connection.Builder().createFrom(apiProviderConnection).connector(apiProviderConnector).build();
        }

        final ProvidedApiTemplate template = new ProvidedApiTemplate(apiProviderConnection, API_PROVIDER_START_ACTION_ID, API_PROVIDER_END_ACTION_ID);

        return apiGenerator.generateIntegration(spec, template);
    }

    static String getSpec(final APIFormData apiFormData) {
        try (BufferedSource source = Okio.buffer(Okio.source(apiFormData.getSpecification()))) {
            return source.readUtf8();
        } catch (final IOException e) {
            throw SyndesisServerException.launderThrowable("Failed to read specification", e);
        }
    }
}
