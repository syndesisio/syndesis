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
package io.syndesis.server.dao.file;

import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.extension.converter.BinaryExtensionAnalyzer;
import io.syndesis.common.model.extension.Extension;

public class FileDataManager {
    private final DataManager dataManager;
    private final FileDAO extensionDataAccess;

    public FileDataManager(DataManager dataManager, FileDAO extensionDataAccess) {
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

    public Optional<InputStream> getExtensionIcon(String extensionId, String iconFile) {
        String id = getInstalledPhysicalId(extensionId);
        return BinaryExtensionAnalyzer.getDefault().getIcon(extensionDataAccess.read("/extensions/" + id), iconFile);
    }

    public String getExtensionIconMediaType(String iconFile) {
        return BinaryExtensionAnalyzer.getDefault().getIconMediaType(iconFile);
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

        String res = ids.iterator().next();
        if (res == null) {
            throw new IllegalStateException("Found empty id for extensionId=" + extensionId);
        }
        return res;
    }

    public FileDAO getExtensionDataAccess() {
        return extensionDataAccess;
    }
}
