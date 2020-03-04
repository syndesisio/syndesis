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
package io.syndesis.server.endpoint.v1.handler.connection;

import java.util.Map;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.DynamicActionMetadata;
import io.syndesis.server.verifier.MetadataConfigurationProperties;

class MetadataCommandAction extends MetadataCommand<DynamicActionMetadata> {

    private final String metadataUrl;

    MetadataCommandAction(MetadataConfigurationProperties configuration,
                          final String connectorId,
                          final ConnectorAction action,
                          Map<String, String> parameters) {
        super(configuration, DynamicActionMetadata.class, parameters);
        this.metadataUrl = String.format("http://%s/api/v1/connectors/%s/actions/%s", configuration.getService(), connectorId, action.getId().get());
    }

    @Override
    protected DynamicActionMetadata getFallback() {
        return DynamicActionMetadata.NOTHING;
    }

    @Override
    protected String getMetadataURL() {
        return metadataUrl;
    }

}
