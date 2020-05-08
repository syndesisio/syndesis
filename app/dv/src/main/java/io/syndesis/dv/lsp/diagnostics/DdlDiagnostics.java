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
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.swing.text.BadLocationException;

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

    public void clearDiagnostics(String uri, TeiidDdlLanguageServer languageServer) {
        languageServer.getClient().publishDiagnostics(new PublishDiagnosticsParams(uri, new ArrayList<Diagnostic>(0)));
    }

    public void publishDiagnostics(TextDocumentItem ddlDocument, TeiidDdlLanguageServer languageServer) {
        doAsyncPublishDiagnostics(ddlDocument, languageServer);
    }

    /**
     * Do basic validation to check the no XML valid.
     *
     * @param ddlDocument
     * @param diagnostics
     * @throws BadLocationException
     */
    private void doBasicDiagnostics(TextDocumentItem ddlDocument, List<Diagnostic> diagnostics)
            throws BadLocationException {
        DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(ddlDocument.getText());
        CreateViewStatement createStatement = new CreateViewStatement(analyzer);
        for (DdlAnalyzerException exception : createStatement.getExceptions()) {
            diagnostics.add(exception.getDiagnostic());
            LOGGER.debug(diagnostics.toString());
        }
    }

    private void doAsyncPublishDiagnostics(TextDocumentItem ddlDocument, TeiidDdlLanguageServer languageServer) {
        LOGGER.debug("publishDiagnostics() STARTED ");

        String uri = ddlDocument.getUri();

        CompletableFuture.runAsync(() -> {
            clearDiagnostics(uri, languageServer);

            List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();

            boolean success = false;
            try {
                doBasicDiagnostics(ddlDocument, diagnostics);
                languageServer.getClient().publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
                success = true;
            } catch (BadLocationException e) {
                LOGGER.error("BadLocationException thrown doing doAsyncPublishDiagnostics() in DdlDiagnostics.", e);
            }

            if( !success ) {
                clearDiagnostics(uri, languageServer);
            }
        });
    }
}
