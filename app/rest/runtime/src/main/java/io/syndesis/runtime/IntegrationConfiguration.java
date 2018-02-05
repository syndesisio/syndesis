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
package io.syndesis.runtime;


import java.io.InputStream;
import java.util.Optional;

import io.syndesis.dao.extension.ExtensionDataManager;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.dao.manager.EncryptionComponent;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.extension.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates and configures the integration bits.
 */
@Configuration
public class IntegrationConfiguration {

    @Bean
    @Autowired
    public IntegrationResourceManager integrationResourceManager(
            DataManager dataManager,
            EncryptionComponent encryptionComponent,
            ExtensionDataManager extensionDataManager) {

        return new IntegrationResourceManager() {
            @Override
            public Optional<Connector> loadConnector(String id) {
                return Optional.ofNullable(dataManager.fetch(Connector.class, id));
            }

            @Override
            public Optional<Extension> loadExtension(String id) {
                return Optional.ofNullable(extensionDataManager.getExtensionMetadata(id));
            }

            @Override
            public Optional<InputStream> loadExtensionBLOB(String id) {
                return Optional.ofNullable(extensionDataManager.getExtensionBinaryFile(id));
            }

            @Override
            public String decrypt(String encrypted) {
                return encryptionComponent.decrypt(encrypted);
            }
        };
    }
}
