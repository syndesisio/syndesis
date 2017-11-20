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
package io.syndesis.dao.extension;

import java.io.InputStream;
import java.util.Set;
import javax.annotation.Nonnull;

import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.extension.Extension;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "features.filestore.enabled")
public class ExtensionDataManager {
    private final DataManager dataManager;
    private final ExtensionDataAccessObject extensionDataAccess;

    public ExtensionDataManager(DataManager dataManager, ExtensionDataAccessObject extensionDataAccess) {
        this.dataManager = dataManager;
        this.extensionDataAccess = extensionDataAccess;
    }

    public Extension getExtensionMetadata(String extensionId) {
        String id = getInstalledPhysicalId(extensionId);
        return dataManager.fetch(Extension.class, id);
    }

    public InputStream getExtensionBinaryFile(String extensionId) {
        String id = getInstalledPhysicalId(extensionId);
        return extensionDataAccess.read("/extensions/" + id);
    }

    // ==========================================================

    @Nonnull
    private String getInstalledPhysicalId(String extensionId) {
        Set<String> ids = dataManager.fetchIdsByPropertyValue(
            Extension.class,
            "extensionId", extensionId,
            "status", Extension.Status.Installed.name()
        );

        if (ids.isEmpty()) {
            throw new IllegalStateException("Installed extension for extensionId=" + extensionId + " not found");
        } else if (ids.size() > 1) {
            throw new IllegalStateException("Too many extensions found with extensionId=" + extensionId);
        }

        return ids.iterator().next();
    }
}
