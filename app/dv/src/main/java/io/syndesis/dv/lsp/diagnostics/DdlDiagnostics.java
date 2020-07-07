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
package io.syndesis.dv.lsp.diagnostics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.TextDocumentItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syndesis.dv.lsp.TeiidDdlLanguageServer;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;
import io.syndesis.dv.lsp.parser.statement.CreateViewStatement;

public class DdlDiagnostics {
    private static final Logger LOGGER = LoggerFactory.getLogger(DdlDiagnostics.class);
    private final TeiidDdlLanguageServer languageServer;

    private final Object diagnosticsLock = new Object();

    public DdlDiagnostics(TeiidDdlLanguageServer languageServer) {
        super();
        this.languageServer = languageServer;
    }

    /**
     * Clear diagnostics for a given document URI.
     *
     * @param uri - uri of the document
     */
    public void clearDiagnostics(String uri) {
        synchronized(diagnosticsLock) {
            this.languageServer.getClient().publishDiagnostics(new PublishDiagnosticsParams(uri, Collections.emptyList()));
        }
    }

    /**
     * Generate and publish diagnostics for target document.
     *
     * @param ddlDocument - document to diagnose
     */
    public boolean publishDiagnostics(TextDocumentItem ddlDocument) {
        doPublishDiagnostics(ddlDocument);
        return true;
    }

    /**
     * Performs actual parsing and diagnostics for a given ddl string
     *
     * @param documentText - text to process
     * @return list of language server {@link Diagnostic}s
     */
    public static List<Diagnostic> doBasicDiagnostics(String documentText) {
        DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(documentText);
        CreateViewStatement createStatement = new CreateViewStatement(analyzer);
        List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
        for (DdlAnalyzerException exception : createStatement.getExceptions()) {
            diagnostics.add(exception.getDiagnostic());
            LOGGER.debug(diagnostics.toString());
        }

        return diagnostics;
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    private void doPublishDiagnostics(TextDocumentItem ddlDocument) {
        List<Diagnostic> diagnostics = doBasicDiagnostics(ddlDocument.getText());
        CompletableFuture.runAsync(() -> {
            this.languageServer.getClient()
                    .publishDiagnostics(new PublishDiagnosticsParams(ddlDocument.getUri(), diagnostics));
        });
    }
}
