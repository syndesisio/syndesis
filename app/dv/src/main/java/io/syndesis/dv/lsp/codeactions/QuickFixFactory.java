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
package io.syndesis.dv.lsp.codeactions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonValue;

import io.syndesis.dv.lsp.Messages;
import io.syndesis.dv.lsp.diagnostics.DdlDiagnostics;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;

public final class QuickFixFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuickFixFactory.class);
    private static final QuickFixFactory INSTANCE = new QuickFixFactory();

    /**
     * The following enum list contains the IDs for potential quick fixes for
     * applicable errors/warnings from error codes set on the {@link DdlAnalyzerException}'s
     * {@link Diagnostic} code that may be set on each exception.
     */
    public enum DiagnosticErrorId {
        VIEW_NAME_IS_INVALID(Messages.Error.VIEW_NAME_IS_INVALID.name()),
        VIEW_NAME_RESERVED_WORD(Messages.Error.VIEW_NAME_RESERVED_WORD.name()),
        INVALID_DATATYPE(Messages.Error.INVALID_DATATYPE.name()),
        INVALID_COLUMN_NAME(Messages.Error.INVALID_COLUMN_NAME.name()),
        COLUMN_NAME_RESERVED_WORD(Messages.Error.COLUMN_NAME_RESERVED_WORD.name()),
        COLUMN_NAME_NON_RESERVED_WORD(Messages.Error.COLUMN_NAME_NON_RESERVED_WORD.name()),
        VIEW_NAME_NON_RESERVED_WORD(Messages.Error.VIEW_NAME_NON_RESERVED_WORD.name()),
        UNEXPECTED_COMMA(Messages.Error.UNEXPECTED_COMMA.name()),
        UNKNOWN_ID("UNKNOWN");

        private final String errorCode;

        DiagnosticErrorId(String codeMsg) {
            this.errorCode = codeMsg;
        }

        @JsonValue
        public String getErrorCode() {
            return errorCode;
        }
    };

    private QuickFixFactory() {
        // singleton
    }

    public static QuickFixFactory getInstance() {
        return INSTANCE;
    }

    public List<Either<Command, CodeAction>> getQuickFixCodeActions(CodeActionParams params, TextDocumentItem doc) {
        List<DdlAnalyzerException> currentExceptions = DdlDiagnostics.getCurrentExceptions(doc.getText());

        // Walk through diagnostics and create quick fixes
        List<Either<Command, CodeAction>> codeActions = new ArrayList<>();

        for(DdlAnalyzerException exception: currentExceptions) {
            // get enum error code (if exists)
            Diagnostic diagnostic = exception.getDiagnostic();
            String errorCode = diagnostic.getCode();
            Range range = diagnostic.getRange();
            LOGGER.debug("  diagnostic error CODE = " + errorCode);
            if( errorCode != null && range.equals(params.getRange())) {
                switch(getIdFromCode(errorCode)) {
                case VIEW_NAME_RESERVED_WORD:
                case COLUMN_NAME_RESERVED_WORD: {
                    WrapInDoubleQuotesFix quickFix = new WrapInDoubleQuotesFix();
                    List<CodeAction> actions = quickFix.createCodeActions(params, exception);
                    for(CodeAction action: actions) {
                        codeActions.add(Either.forRight(action));
                    }
                } break;
                case VIEW_NAME_NON_RESERVED_WORD:
                case COLUMN_NAME_NON_RESERVED_WORD: {
                    WrapInDoubleQuotesFix quickFix = new WrapInDoubleQuotesFix();
                    List<CodeAction> actions = quickFix.createCodeActions(params, exception);
                    for(CodeAction action: actions) {
                        codeActions.add(Either.forRight(action));
                    }
                } break;
                case INVALID_DATATYPE: {
                    InvalidDatatypeQuickFix quickFix = new InvalidDatatypeQuickFix();
                    List<CodeAction> actions = quickFix.createCodeActions(params, exception);
                    for(CodeAction action: actions) {
                        codeActions.add(Either.forRight(action));
                    }
                } break;
                case UNEXPECTED_COMMA: {
                    RemoveUnexpectedCommaQuickFix quickFix = new RemoveUnexpectedCommaQuickFix();
                    List<CodeAction> actions = quickFix.createCodeActions(params, exception);
                    for(CodeAction action: actions) {
                        codeActions.add(Either.forRight(action));
                    }
                } break;
                default:
                }
            }
        }
        return codeActions;
    }

    public static DiagnosticErrorId getIdFromCode(String errorCode) {
        for(DiagnosticErrorId id: DiagnosticErrorId.values()) {
            if( errorCode.equalsIgnoreCase(id.getErrorCode()) ) {
                return id;
            }
        }
        return DiagnosticErrorId.UNKNOWN_ID;
    }
}
