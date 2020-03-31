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

	public void clearDiagnostics(TeiidDdlLanguageServer languageServer) {
		languageServer.getClient()
				.publishDiagnostics(new PublishDiagnosticsParams("someURI", new ArrayList<Diagnostic>(0)));
	}

	public void publishDiagnostics(TextDocumentItem ddlDocument, TeiidDdlLanguageServer languageServer) {
		try {
			languageServer.getClient()
				.publishDiagnostics(new PublishDiagnosticsParams(ddlDocument.getUri(), new ArrayList<Diagnostic>(0)));
		} catch (IllegalStateException ise) {
			LOGGER.info(" ===>>> DdlDiagnostics pusblishDiagnostics()  IllegalStateException occurred");
			if (ise.getMessage().contains("TEXT_FULL_WRITING")) {
				String msg = "java.lang.IllegalStateException: The remote endpoint was in state [TEXT_FULL_WRITING] which is an invalid state for called method";
				LOGGER.error(msg);
			} else {
				LOGGER.error(ise.getMessage(), ise);
			}
		}

		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();

		try {
			doBasicDiagnostics(ddlDocument, diagnostics);
			languageServer.getClient()
					.publishDiagnostics(new PublishDiagnosticsParams(ddlDocument.getUri(), diagnostics));
		} catch (BadLocationException e) {
			LOGGER.error("BadLocationException thrown doing doBasicDiagnostics() in DdlDiagnostics.", e);
		}
	}

	/**
	 * Do basic validation to check the no XML valid.
	 *
	 * @param ddlDocument
	 * @param diagnostics
	 * @param monitor
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
}