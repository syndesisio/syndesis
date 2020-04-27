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

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeiidDdlLanguageServer implements LanguageServer, LanguageClientAware {

    private static CompletionOptions DEFAULT_COMPLETION_OPTIONS = new CompletionOptions(Boolean.TRUE, Arrays.asList(".", "@", "#", "*"));

    private static final Logger LOGGER = LoggerFactory.getLogger(TeiidDdlLanguageServer.class);

    private WorkspaceService workspaceService;
    private TeiidDdlTextDocumentService textDocumentService;

    private LanguageClient client;

    public TeiidDdlLanguageServer() {
        this.textDocumentService = new TeiidDdlTextDocumentService(this);
        this.workspaceService = new TeiidDdlWorkspaceService();
        LOGGER.debug("TeiidDdlLanguageServer()  doc and workspace services created");
    }

    /**
     * @return the textDocumentService
     */
    @Override
    public TeiidDdlTextDocumentService getTextDocumentService() {
        return this.textDocumentService;
    }

    /**
     * @return the workspaceService
     */
    @Override
    public WorkspaceService getWorkspaceService() {
        return this.workspaceService;
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        ServerCapabilities capabilities = createServerCapabilities();
        return CompletableFuture.completedFuture(new InitializeResult(capabilities));
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        LOGGER.debug("Shutting down Teiid DDL language server");
        return CompletableFuture.completedFuture(new Object());
    }

    @Override
    public void exit() {
    }

    private ServerCapabilities createServerCapabilities() {
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
        capabilities.setHoverProvider(Boolean.TRUE);
        capabilities.setDocumentHighlightProvider(Boolean.TRUE);
        capabilities.setDocumentSymbolProvider(Boolean.TRUE);
        // TODO: define capabilities, usually the first provided is completion
        capabilities.setCompletionProvider(DEFAULT_COMPLETION_OPTIONS); // new CompletionOptions(Boolean.TRUE, Arrays.asList(".","?","&", "\"", "=")));
        return capabilities;
    }

    public LanguageClient getClient() {
        return client;
    }
}
