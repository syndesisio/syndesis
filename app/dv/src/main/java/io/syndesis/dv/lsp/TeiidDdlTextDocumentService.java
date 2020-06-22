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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.ColorInformation;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentColorParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syndesis.dv.lsp.codeactions.QuickFixFactory;
import io.syndesis.dv.lsp.completion.providers.DdlCompletionProvider;
import io.syndesis.dv.lsp.diagnostics.DdlDiagnostics;

/**
 */
public class TeiidDdlTextDocumentService implements TextDocumentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TeiidDdlTextDocumentService.class);
    private final Map<String, TextDocumentItem> openedDocuments = new HashMap<>();
    private final TeiidDdlLanguageServer teiidLanguageServer;

    private final DdlCompletionProvider completionProvider;

    private static final boolean DO_PRINT_TO_CONSOLE = false;

    public TeiidDdlTextDocumentService(TeiidDdlLanguageServer teiidLanguageServer) {
        this.teiidLanguageServer = teiidLanguageServer;
        this.completionProvider = new DdlCompletionProvider(teiidLanguageServer.getMetadataService(),
                (TeiidDdlWorkspaceService) teiidLanguageServer.getWorkspaceService());
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
            CompletionParams completionParams) {
        String uri = completionParams.getTextDocument().getUri();
        logDebug("\ncompletion: {} URI = " + uri);
        TextDocumentItem doc = openedDocuments.get(uri);

        // get applicable completion items
        List<CompletionItem> items = completionProvider.getCompletionItems(doc.getText(),
                completionParams.getPosition());

        // if items exist, return them
        if (items != null && !items.isEmpty()) {
            return CompletableFuture.completedFuture(Either.forLeft(items));
        }

        // if items do no exist return empty results
        return CompletableFuture.completedFuture(Either.forLeft(Collections.emptyList()));
    }

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
        logDebug("\nresolveCompletionItem: {}");
        return CompletableFuture.completedFuture(unresolved);
    }

    @Override
    public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
        /*
         * logDebug("hover: {}", position.getTextDocument()); TextDocumentItem
         * textDocumentItem = openedDocuments.get(position.getTextDocument().getUri());
         * String htmlContent = new
         * HoverProcessor(textDocumentItem).getHover(position.getPosition()); Hover
         * hover = new Hover();
         * hover.setContents(Collections.singletonList((Either.forLeft(htmlContent))));
         * logDebug("hover: {}", position.getTextDocument()); Hover hover = new
         * Hover(); hover.setContents(Collections.singletonList((Either.
         * forLeft("HELLO HOVER WORLD!!!!"))));
         */
        Hover result = null;
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams position) {
        logDebug("\nsignatureHelp: {}");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
            TextDocumentPositionParams params) {
        logDebug("\ndefinition: {}");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams position) {
        logDebug("\ndocumentHighlight: {}");
        List<DocumentHighlight> result = new ArrayList<DocumentHighlight>();
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<List<ColorInformation>> documentColor(DocumentColorParams params) {
        return TextDocumentService.super.documentColor(params);
    }

    @Override
    public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(
            DocumentSymbolParams params) {
        logDebug("\ndocumentSymbol: {}");
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
        logDebug("\ncodeAction() Called");
        CodeActionContext context = params.getContext();
        String uri = params.getTextDocument().getUri();
        TextDocumentItem doc = openedDocuments.get(uri);
        if (context != null && (context.getOnly() == null || context.getOnly().contains(CodeActionKind.QuickFix))) {
            return CompletableFuture.supplyAsync(() -> {
                return QuickFixFactory.getInstance().getQuickFixCodeActions(params, doc);
            });
        } else {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
        logDebug("\ncodeLens: {}");
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
        logDebug("\nresolveCodeLens: {}");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
        logDebug("\nformatting: {}");
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
        logDebug("\nrangeFormatting: {}");
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
        logDebug("\nonTypeFormatting: {}");
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
        logDebug("\nrename: {}");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        TextDocumentItem textDocument = params.getTextDocument();
        logDebug("\ndidOpen: {}");
        openedDocuments.put(textDocument.getUri(), textDocument);
        DdlDiagnostics.publishDiagnostics(textDocument, teiidLanguageServer);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        logDebug("\ndidChange: {}");
        List<TextDocumentContentChangeEvent> contentChanges = params.getContentChanges();
        TextDocumentItem textDocument = openedDocuments.get(params.getTextDocument().getUri());
        if (!contentChanges.isEmpty()) {
            textDocument.setText(contentChanges.get(0).getText());
            DdlDiagnostics.publishDiagnostics(textDocument, teiidLanguageServer);
        }
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        logDebug("\ndidClose: {}");
        String uri = params.getTextDocument().getUri();

        /*
         * The rule observed by VS Code servers as explained in LSP specification is to
         * clear the Diagnostic when it is related to a single file.
         * https://microsoft.github.io/language-server-protocol/specification#
         * textDocument_publishDiagnostics
         *
         * clear diagnostics before removing document.
         */

        DdlDiagnostics.clear(getOpenedDocument(uri), teiidLanguageServer);

        openedDocuments.remove(uri);
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        logDebug("\ndidSave: {}");
    }

    public TextDocumentItem getOpenedDocument(String uri) {
        return openedDocuments.get(uri);
    }

    public Collection<TextDocumentItem> getAllOpenedDocuments() {
        return openedDocuments.values();
    }

    @SuppressWarnings("PMD.SystemPrintln")
    private static void logDebug(String msg) {
        if (DO_PRINT_TO_CONSOLE) {
            LOGGER.info(msg);
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(msg);
        }
    }
}
