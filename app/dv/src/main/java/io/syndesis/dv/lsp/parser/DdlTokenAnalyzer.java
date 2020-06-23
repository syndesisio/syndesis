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
package io.syndesis.dv.lsp.parser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.teiid.query.parser.JavaCharStream;
import org.teiid.query.parser.SQLParserConstants;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.completion.DdlCompletionConstants;

@SuppressWarnings("PMD.GodClass")
public class DdlTokenAnalyzer {

    private final String statement;
    private final List<Token> tokens;
    private final DdlAnalyzerConstants.StatementType statementType;

    private final DdlTokenParserReport report;

    public DdlTokenAnalyzer(String statement) {
        super();
        this.statement = statement;
        tokens = init(statement);
        this.statementType = getStatementType();
        this.report = new DdlTokenParserReport();
    }

    public String getStatement() {
        return this.statement;
    }

    private static List<Token> init(final String statement) {

        JavaCharStream jcs = new JavaCharStream(new StringReader(statement));
        TeiidDdlParserTokenManager tokenSource = new TeiidDdlParserTokenManager(jcs);

        List<Token> tokensList = new ArrayList<Token>();

        Token currentToken = tokenSource.getNextToken();

        if (currentToken != null) {
            convertToken(currentToken);

            // Add current token to simple list
            tokensList.add(currentToken);

            boolean done = false;

            while (!done) {
                // Get next token
                currentToken = tokenSource.getNextToken();

                // Check if next token exists
                if (currentToken != null && (currentToken.image.length() > 0)) {
                    convertToken(currentToken);
                    tokensList.add(currentToken);
                } else {
                    done = true;
                }
            }
        }

        return tokensList;
    }

    private static void convertToken(Token token) {
        token.beginColumn--;
        token.endColumn--;
        token.beginLine--;
        token.endLine--;
    }

    public List<Token> getTokens() {
        return Collections.unmodifiableList(this.tokens);
    }

    public Token getTokenFor(Position pos) {
        DdlTokenWalker walker = new DdlTokenWalker(this.tokens);
        return walker.findToken(pos, this.statementType);
    }

    public boolean isPositionInToken(Position pos, Token tkn) {
        Token tknAt = getTokenAt(pos);
        return getTokenIndex(tknAt) == getTokenIndex(tkn);
    }

    /**
     * Token at the row, character position
     *
     * @param pos - position of the character
     * @return token - may be null
     */
    public Token getTokenAt(Position pos) {
        DdlTokenWalker walker = new DdlTokenWalker(this.tokens);
        Token targetToken = walker.findToken(pos, this.statementType);
        if (targetToken != null && (pos.getLine() > targetToken.beginLine || pos.getCharacter() > targetToken.endLine)) {
            int targetIndex = getTokenIndex(targetToken);
            if (targetIndex < getTokens().size() - 1) {
                return getToken(targetIndex + 1);
            }
            return null;
        }
        return targetToken;
    }

    protected String[] getDatatypesList() {
        return DdlAnalyzerConstants.DATATYPE_LIST;
    }

    public String[] getNextWordsByKind(int kind) {
        return getNextWordsByKind(kind, false);
    }

    public String[] getNextWordsByKind(int kind, boolean isStatementId) {
        List<String> words = new ArrayList<String>();

        switch (kind) {
            case SQLParserConstants.CREATE:
                words.add(getKeywordLabel(SQLParserConstants.VIEW, true));
                words.add(getKeywordLabel(SQLParserConstants.VIRTUAL, true));
                words.add(getKeywordLabel(SQLParserConstants.PROCEDURE, true));
                break;

            case SQLParserConstants.GLOBAL:
                words.add(getKeywordLabel(SQLParserConstants.TEMPORARY, true));
                break;

            case SQLParserConstants.TEMPORARY:
                words.add(getKeywordLabel(SQLParserConstants.TABLE, true));
                break;

            case SQLParserConstants.FOREIGN:
                words.add(getKeywordLabel(SQLParserConstants.TABLE, true));
                words.add(getKeywordLabel(SQLParserConstants.TEMPORARY, true));
                break;

            case SQLParserConstants.VIRTUAL:
                words.add(getKeywordLabel(SQLParserConstants.VIEW, true));
                words.add(getKeywordLabel(SQLParserConstants.PROCEDURE, true));
                break;

            case SQLParserConstants.ID:
                if (isStatementId) {
                    words.add(getKeywordLabel(SQLParserConstants.LPAREN, false));
                }
                break;

            case SQLParserConstants.SELECT:
                words.add(getKeywordLabel(SQLParserConstants.STAR, true));
                break;

            default:
                break;
        }

        return stringListToArray(words);
    }

    @SuppressWarnings("PMD.OptimizableToArrayCall") // false positive
    private static String[] stringListToArray(List<String> array) {
        return array.toArray(new String[array.size()]);
    }

    public final DdlAnalyzerConstants.StatementType getStatementType() {
        // walk through start of token[] array and return the type
        if (tokens.size() < 2) {
            return DdlAnalyzerConstants.StatementType.UNKNOWN_STATEMENT_TYPE;
        }

        if (isStatementType(tokens, DdlAnalyzerConstants.CREATE_VIRTUAL_VIEW_STATEMENT)) {
            return DdlAnalyzerConstants.StatementType.CREATE_VIRTUAL_VIEW_TYPE;
        }

        if (isStatementType(tokens, DdlAnalyzerConstants.CREATE_VIEW_STATEMENT)) {
            return DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE;
        }

        if (isStatementType(tokens, DdlAnalyzerConstants.CREATE_GLOBAL_TEMPORARY_TABLE_STATEMENT)) {
            return DdlAnalyzerConstants.StatementType.CREATE_GLOBAL_TEMPORARY_TABLE_TYPE;
        }

        if (isStatementType(tokens, DdlAnalyzerConstants.CREATE_FOREIGN_TEMPORARY_TABLE_STATEMENT)) {
            return DdlAnalyzerConstants.StatementType.CREATE_FOREIGN_TEMPORARY_TABLE_TYPE;
        }

        if (isStatementType(tokens, DdlAnalyzerConstants.CREATE_FOREIGN_TABLE_STATEMENT)) {
            return DdlAnalyzerConstants.StatementType.CREATE_FOREIGN_TABLE_TYPE;
        }

        if (isStatementType(tokens, DdlAnalyzerConstants.CREATE_TABLE_STATEMENT)) {
            return DdlAnalyzerConstants.StatementType.CREATE_TABLE_TYPE;
        }

        return DdlAnalyzerConstants.StatementType.UNKNOWN_STATEMENT_TYPE;
    }

    private static boolean isStatementType(List<Token> tokens, int... statementTokens) {
        if (tokens.size() < statementTokens.length) {
            return false;
        }

        for (int i = 0; i < statementTokens.length; i++) {
            if (tokens.get(i).kind != statementTokens[i]) {
                return false;
            }
        }

        return true;
    }

    public boolean allParensMatch(Token... tkns) {
        return parensMatch(tkns, 0);
    }

    public DdlAnalyzerException checkAllParens() {
        return checkAllBrackets(SQLParserConstants.LPAREN, SQLParserConstants.RPAREN);
    }

    @SuppressWarnings("PMD.NPathComplexity") // TODO refactor
    public DdlAnalyzerException checkAllBrackets(int leftBracketKind, int rightBracketKind) {
        int numUnmatchedParens = 0;
        DdlAnalyzerException exception = null;

        Position diagStartPosition = null;

        // TODO: Add logic to check for the scenarios like the first bracket

        for (int iTkn = 0; iTkn < tokens.size(); iTkn++) {
            Token token = tokens.get(iTkn);
            if (token.kind == leftBracketKind) {
                if (diagStartPosition == null) {
                    diagStartPosition = new Position(token.beginLine, token.beginColumn);
                }
                numUnmatchedParens++;
            } else if (token.kind == rightBracketKind && diagStartPosition == null) {
                diagStartPosition = new Position(token.beginLine, token.beginColumn);
                exception = new DdlAnalyzerException("Bracket at location " //$NON-NLS-1$
                        + getPositionString(token) + " does not properly match previous bracket"); //$NON-NLS-1$
                exception.getDiagnostic().setMessage(exception.getMessage());
                exception.getDiagnostic().setSeverity(DiagnosticSeverity.Error);
                exception.getDiagnostic().setRange(new Range(diagStartPosition, diagStartPosition));
            }

            if (exception != null) {
                break;
            }

            if (token.kind == rightBracketKind) {
                numUnmatchedParens--;
            }

            // If the ## goes < 0 throw exception because they should be correctly nested
            // VALID: ( () () )
            // INVALID ( )) () (
            // ^ would occur here
            if (diagStartPosition != null && numUnmatchedParens < 0) {
                Position diagEndPosition = new Position(token.endLine, token.beginLine);
                exception = new DdlAnalyzerException("Bracket at location " //$NON-NLS-1$
                        + getPositionString(token) + " does not properly match previous bracket"); //$NON-NLS-1$
                exception.getDiagnostic().setMessage(exception.getMessage());
                exception.getDiagnostic().setSeverity(DiagnosticSeverity.Error);
                exception.getDiagnostic().setRange(new Range(diagStartPosition, diagEndPosition));
            }
            if (exception != null) {
                break;
            }
        }

        if (numUnmatchedParens != 0) {
            exception = new DdlAnalyzerException("Missing or mismatched brackets"); //$NON-NLS-1$
            exception.getDiagnostic().setMessage(exception.getMessage());
            exception.getDiagnostic().setSeverity(DiagnosticSeverity.Error);
            Token lastToken = tokens.get(tokens.size() - 1);
            Position diagEndPosition = new Position(lastToken.endLine, lastToken.endColumn);
            exception.getDiagnostic().setRange(new Range(diagStartPosition, diagEndPosition));
        }

        return exception;
    }

    public String getPositionString(Token tkn) {
        return "( " + tkn.beginLine + ", " + tkn.beginColumn + " )"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public boolean bracketsMatch(Token[] tkns, int startTokenId, int leftBracket, int rightBracket) {
        int numUnmatchedParens = 0;

        for (int iTkn = 0; iTkn < tokens.size(); iTkn++) {
            if (iTkn < startTokenId) {
                continue;
            }

            Token token = tkns[iTkn];
            if (token.kind == leftBracket) {
                numUnmatchedParens++;
            }
            if (token.kind == rightBracket) {
                numUnmatchedParens--;
            }
        }
        return numUnmatchedParens == 0;
    }

    public boolean parensMatch(Token[] tkns, int startTokenId) {
        return bracketsMatch(tkns, startTokenId, SQLParserConstants.LPAREN, SQLParserConstants.RPAREN);
    }

    public void addException(DdlAnalyzerException exception) {
        if (exception != null) {
            this.report.addException(exception);
        }
    }

    public void addException(Token startToken, Token endToken, String errorMessage) {
        Position startPosition = new Position(startToken.beginLine, startToken.beginColumn);
        Position endPosition = new Position(endToken.endLine, endToken.endColumn + 1);
        DdlAnalyzerException exception = new DdlAnalyzerException(DiagnosticSeverity.Error, errorMessage,
                new Range(startPosition, endPosition)); // $NON-NLS-1$);
        this.addException(exception);
    }

    public List<DdlAnalyzerException> getExceptions() {
        return this.report.getExceptions();
    }

    public DdlTokenParserReport getReport() {
        return this.report;
    }

    public void logReport() {
        this.report.log();
    }

    public Token getToken(int tokenIndex) {
        return this.tokens.get(tokenIndex);
    }

    public int getTokenIndex(Token token) {
        if (token == null) {
            return 0;
        }

        int index = 0;
        for (Token tkn : getTokens()) {
            // TODO no equals/hashCode implemented in Token
            if (token.equals(tkn)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public String positionToString(Position position) {
        return "Line " + (position.getLine() + 1) + " Column " + (position.getCharacter() + 1);
    }

    public String[] getKeywordLabels(int[] keywordIds, boolean upperCase) {
        List<String> labels = new ArrayList<String>();

        for (int id : keywordIds) {
            labels.add(getKeywordLabel(id, upperCase));
        }

        return labels.toArray(new String[0]);
    }

    public String getKeywordLabel(int keywordId, boolean upperCase) {
        return DdlCompletionConstants.getLabel(keywordId, upperCase);
    }
}
