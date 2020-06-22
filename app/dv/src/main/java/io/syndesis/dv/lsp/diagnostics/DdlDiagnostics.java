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

//import javax.swing.text.BadLocationException;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.TextDocumentItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syndesis.dv.lsp.TeiidDdlLanguageServer;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;
import io.syndesis.dv.lsp.parser.statement.CreateViewStatement;

public final class DdlDiagnostics {
    private static final Logger LOGGER = LoggerFactory.getLogger(DdlDiagnostics.class);

    @SuppressWarnings("FutureReturnValueIgnored")
    public static void publishDiagnostics(TextDocumentItem ddlDocument, TeiidDdlLanguageServer languageServer) {
        CompletableFuture.runAsync(() -> {
            LOGGER.debug("  >>> runAsync() STARTED");

            List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();

            DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(ddlDocument.getText());
            CreateViewStatement createStatement = new CreateViewStatement(analyzer);
            for (DdlAnalyzerException exception : createStatement.getExceptions()) {
                diagnostics.add(exception.getDiagnostic());
            }
            if( !diagnostics.isEmpty() ) {
                languageServer.getClient().publishDiagnostics(new PublishDiagnosticsParams(ddlDocument.getUri(), diagnostics));
            } else {
                languageServer.getClient().publishDiagnostics(new PublishDiagnosticsParams(ddlDocument.getUri(), Collections.emptyList()));
            }
            LOGGER.debug("  >>>  runAsync() FINISHIED ## diagnostics = " + diagnostics.size());
        });
    }

    public static List<Diagnostic> getCurrentDiagnostics(String ddlDocumentText) {
      return getCurrentDiagnostics(getCurrentExceptions(ddlDocumentText));
    }

    public static List<Diagnostic> getCurrentDiagnostics(List<DdlAnalyzerException> exceptions) {
        List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
      for (DdlAnalyzerException exception : exceptions) {
          diagnostics.add(exception.getDiagnostic());
          LOGGER.debug(diagnostics.toString());
      }
      return diagnostics;
    }

    public static List<DdlAnalyzerException> getCurrentExceptions(String ddlDocumentText) {
        CreateViewStatement createStatement =
                new CreateViewStatement(new DdlTokenAnalyzer(ddlDocumentText));
        return createStatement.getExceptions();
    }

    public static void clear(TextDocumentItem ddlDocument, TeiidDdlLanguageServer languageServer) {
        LOGGER.debug("clear diagnostics");
        languageServer.getClient().publishDiagnostics(new PublishDiagnosticsParams(ddlDocument.getUri(), Collections.emptyList()));
    }

    private DdlDiagnostics() {
        // utility class
    }
}
