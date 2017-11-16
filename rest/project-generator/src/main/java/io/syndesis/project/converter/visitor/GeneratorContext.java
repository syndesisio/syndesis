/*
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

package io.syndesis.project.converter.visitor;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.connector.catalog.ConnectorCatalog;
import io.syndesis.dao.extension.ExtensionDataManager;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.integration.model.Flow;
import io.syndesis.model.integration.Integration;
import io.syndesis.project.converter.ProjectGeneratorProperties;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = GeneratorContext.Builder.class)
public interface GeneratorContext {

    Integration getIntegration();
    Flow getFlow();
    ConnectorCatalog getConnectorCatalog();
    ProjectGeneratorProperties getGeneratorProperties();
    StepVisitorFactoryRegistry getVisitorFactoryRegistry();
    TarArchiveOutputStream getTarArchiveOutputStream();
    DataManager getDataManager();
    Optional<ExtensionDataManager> getExtensionDataManager();

    default void addTarEntry(String path, byte[] content) throws IOException {
        TarArchiveOutputStream tos = getTarArchiveOutputStream();
        TarArchiveEntry entry = new TarArchiveEntry(path);
        entry.setSize(content.length);
        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();
    }

    class Builder extends ImmutableGeneratorContext.Builder {
    }
}
