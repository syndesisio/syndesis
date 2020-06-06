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
package io.syndesis.dv.lsp;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 */
public class TeiidDdlWorkspaceService implements WorkspaceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TeiidDdlWorkspaceService.class);
    private static final String VIRTUALIZATION_NAME_ID = "virtualiation-name";

    private String currentVirtualizationName;

    public String getCurrentVirtualizationName() {
        return currentVirtualizationName;
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {

        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {

        if (!(params.getSettings() instanceof JsonObject)) {
            return;
        }

        JsonObject settings = (JsonObject) params.getSettings();

        if (settings.has(VIRTUALIZATION_NAME_ID)) {
            JsonElement element = settings.get(VIRTUALIZATION_NAME_ID);
            String virtualizationName = element.getAsString();
            if (virtualizationName != null) {
                LOGGER.info("Setting language server metadata scope to the virtualization: " + virtualizationName);
                this.currentVirtualizationName = virtualizationName;
            }
        }
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        // nop
    }
}
