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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.Diagnostic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DdlDiagnosticsHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DdlDiagnosticsHelper.class);

    @SuppressWarnings("unused")
    private static final List<Diagnostic> EMPTY_DIAGNOSTIC_LIST = new ArrayList<>(0);
    private static final DdlDiagnosticsHelper INSTANCE = new DdlDiagnosticsHelper();
    /**
     * Holds last sent diagnostics for the purpose of clear-off when publishing new diagnostics.
     */
    @SuppressWarnings("unused")
    private Map<String, List<Diagnostic>> lastDiagnosticMap;

    public static DdlDiagnosticsHelper getInstance() {
        return INSTANCE;
    }

    private DdlDiagnosticsHelper() {
        this.lastDiagnosticMap = new HashMap<>();
    }

    /**
     * Compiles and publishes diagnostics for a source file.
     *
     * @param client     Language server client
     * @param context    LS context
     * @param lsDoc {@link LSDocumentIdentifierImpl}
     * @param docManager LS Document manager
     * @throws CompilationFailedException throws a LS compiler exception
     */
    public synchronized void compileAndSendDiagnostics() { // ExtendedLanguageClient client, LSContext context,
                                                       //LSDocumentIdentifier lsDoc, WorkspaceDocumentManager docManager)
            //throws CompilationFailedException {
        // Compile diagnostics
//        List<org.ballerinalang.util.diagnostic.Diagnostic> diagnostics = new ArrayList<>();
//        LSModuleCompiler.getBLangPackages(context, docManager, null, true, true, true);
//        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
//        if (compilerContext.get(DiagnosticListener.class) instanceof CollectDiagnosticListener) {
//            diagnostics = ((CollectDiagnosticListener) compilerContext.get(DiagnosticListener.class)).getDiagnostics();
//        }
//
//        Map<String, List<Diagnostic>> diagnosticMap = getDiagnostics(diagnostics, lsDoc);
//        // If the client is null, returns
//        if (client == null) {
//            return;
//        }
//
//        // Replace old entries with an empty list
//        lastDiagnosticMap.keySet().forEach((key) -> diagnosticMap.computeIfAbsent(key, value -> EMPTY_DIAGNOSTIC_LIST));
//
//        // Publish diagnostics
//        diagnosticMap.forEach((key, value) -> client.publishDiagnostics(new PublishDiagnosticsParams(key, value)));
        // Replace old map
//        lastDiagnosticMap = diagnosticMap;
//        LOGGER.info("DdlDiagnosticsHelper.compileAndSendDiagnostics() FINISHED");
    }

    /**
     * Returns diagnostics for this file.
     *
     * @param diagnostics  List of ballerina diagnostics
     * @param lsDocument project path
     * @return diagnostics map
     */
    protected Map<String, List<Diagnostic>> getDiagnostics() { //List<org.ballerinalang.util.diagnostic.Diagnostic> diagnostics,
                                               //LSDocumentIdentifier lsDocument) {
        Map<String, List<Diagnostic>> diagnosticsMap = new HashMap<>();
//        for (org.ballerinalang.util.diagnostic.Diagnostic diag : diagnostics) {
//            Path diagnosticRoot = lsDocument.getProjectRootPath();
//            final org.ballerinalang.util.diagnostic.Diagnostic.DiagnosticPosition position = diag.getPosition();
//            String moduleName = position.getSource().getPackageName();
//            String fileName = position.getSource().getCompilationUnitName();
//            if (lsDocument.isWithinProject()) {
//                diagnosticRoot = diagnosticRoot.resolve("src");
//            }
//            if (!".".equals(moduleName)) {
//                diagnosticRoot = diagnosticRoot.resolve(moduleName);
//            }
//            String fileURI = diagnosticRoot.resolve(fileName).toUri().toString() + "";
//            diagnosticsMap.putIfAbsent(fileURI, new ArrayList<>());
//
//            List<Diagnostic> clientDiagnostics = diagnosticsMap.get(fileURI);
//            int startLine = position.getStartLine() - 1; // LSP diagnostics range is 0 based
//            int startChar = position.getStartColumn() - 1;
//            int endLine = position.getEndLine() - 1;
//            int endChar = position.getEndColumn() - 1;
//
//            endLine = (endLine <= 0) ? startLine : endLine;
//            endChar = (endChar <= 0) ? startChar + 1 : endChar;
//
//            Range range = new Range(new Position(startLine, startChar), new Position(endLine, endChar));
//            Diagnostic diagnostic = new Diagnostic(range, diag.getMessage());
//            org.ballerinalang.util.diagnostic.Diagnostic.Kind diagnosticKind = diag.getKind();
//
//            // set diagnostic log kind
//            if (diagnosticKind.equals(org.ballerinalang.util.diagnostic.Diagnostic.Kind.ERROR)) {
//                diagnostic.setSeverity(DiagnosticSeverity.Error);
//            } else if (diagnosticKind.equals(org.ballerinalang.util.diagnostic.Diagnostic.Kind.WARNING)) {
//                diagnostic.setSeverity(DiagnosticSeverity.Warning);
//            }
//
//            clientDiagnostics.add(diagnostic);
//        }
        return diagnosticsMap;
    }
}
