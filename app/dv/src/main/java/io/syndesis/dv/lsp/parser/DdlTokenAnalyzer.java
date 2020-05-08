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
import java.util.List;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.teiid.query.parser.JavaCharStream;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.completion.DdlCompletionConstants;

public class DdlTokenAnalyzer implements DdlAnalyzerConstants {

    private final String statement;
    private Token[] tokens = null;
    private final STATEMENT_TYPE statementType;

    private DdlTokenParserReport report;

    public DdlTokenAnalyzer(String statement) {
        super();
        this.statement = statement;
        init();
        this.statementType = getStatementType();
        this.report = new DdlTokenParserReport();
    }

    public String getStatement() {
        return this.statement;
    }

    private void init() {

        JavaCharStream jcs = new JavaCharStream(new StringReader(this.statement));
        TeiidDdlParserTokenManager token_source = new TeiidDdlParserTokenManager(jcs);

        List<Token> tokensList = new ArrayList<Token>();

        Token currentToken = token_source.getNextToken();

        if( currentToken != null ) {
            convertToken(currentToken);

            // Add current token to simple list
            tokensList.add(currentToken);

            boolean done = false;

            while ( !done ) {
                // Get next token
                currentToken = token_source.getNextToken();

                // Check if next token exists
                if( currentToken != null && (currentToken.image.length() > 0) ) {
                    convertToken(currentToken);
                    tokensList.add(currentToken);
                } else {
                    done = true;
                }
            }
        }

        this.tokens = tokensList.toArray(new Token[0]);
    }

    private void convertToken(Token token) {
        token.beginColumn--;
        token.endColumn--;
        token.beginLine--;
        token.endLine--;
    }

    public Token[] getTokens() {
        return this.tokens;
    }

    public Token getTokenFor(Position pos) {
        DdlTokenWalker walker = new DdlTokenWalker(this.tokens);
        Token token = walker.findToken(pos, this.statementType);
        //System.out.println("  Walker found Token = " + token + " At " + pos);
        return token;
    }

    protected String[] getDatatypesList() {
        return DATATYPE_LIST;
    }

    public String[] getNextWordsByKind(int kind) {
        return getNextWordsByKind(kind, false);
    }

    public String[] getNextWordsByKind(int kind, boolean isStatementId) {
        List<String> words = new ArrayList<String>();

        switch (kind) {
            case CREATE:
                words.add(getKeywordLabel(VIEW, true));
                words.add(getKeywordLabel(VIRTUAL, true));
//                words.add(getKeywordLabel(GLOBAL, true));
//                words.add(getKeywordLabel(FOREIGN, true));
//                words.add(getKeywordLabel(TABLE, true));
//                words.add(getKeywordLabel(TRIGGER, true));
//                words.add(getKeywordLabel(TEMPORARY, true));
//                words.add(getKeywordLabel(ROLE, true));
//                words.add(getKeywordLabel(SCHEMA, true));
//                words.add(getKeywordLabel(SERVER, true));
//                words.add(getKeywordLabel(DATABASE, true));
                words.add(getKeywordLabel(PROCEDURE, true));
            break;

            case GLOBAL:
                words.add(getKeywordLabel(TEMPORARY, true));
            break;

            case TEMPORARY:
                words.add(getKeywordLabel(TABLE, true));
            break;

            case FOREIGN:
                words.add(getKeywordLabel(TABLE, true));
                words.add(getKeywordLabel(TEMPORARY, true));
            break;

            case VIRTUAL:
                words.add(getKeywordLabel(VIEW, true));
                words.add(getKeywordLabel(PROCEDURE, true));
            break;

            case ID:
                if( isStatementId ) {
                    words.add(getKeywordLabel(LPAREN, false));
                }
            break;

            case SELECT:
                words.add(getKeywordLabel(STAR, true));
                break;

            default:
        }

        return stringListToArray(words);
    }

    private String[] stringListToArray(List<String> array) {
        return array.toArray(new String[array.size()]);
    }

    public STATEMENT_TYPE getStatementType() {
        // walk through start of token[] array and return the type
        if( tokens.length < 2 ) {
            return STATEMENT_TYPE.UNKNOWN_STATEMENT_TYPE;
        }

        if( isStatementType(tokens, CREATE_VIRTUAL_VIEW_STATEMENT) ) {
            return STATEMENT_TYPE.CREATE_VIRTUAL_VIEW_TYPE;
        }

        if( isStatementType(tokens, CREATE_VIEW_STATEMENT) ) {
            return STATEMENT_TYPE.CREATE_VIEW_TYPE;
        }

        if( isStatementType(tokens, CREATE_GLOBAL_TEMPORARY_TABLE_STATEMENT) ) {
            return STATEMENT_TYPE.CREATE_GLOBAL_TEMPORARY_TABLE_TYPE;
        }

        if( isStatementType(tokens, CREATE_FOREIGN_TEMPORARY_TABLE_STATEMENT) ) {
            return STATEMENT_TYPE.CREATE_FOREIGN_TEMPORARY_TABLE_TYPE;
        }

        if( isStatementType(tokens, CREATE_FOREIGN_TABLE_STATEMENT) ) {
            return STATEMENT_TYPE.CREATE_FOREIGN_TABLE_TYPE;
        }

        if( isStatementType(tokens, CREATE_TABLE_STATEMENT) ) {
            return STATEMENT_TYPE.CREATE_TABLE_TYPE;
        }

        return STATEMENT_TYPE.UNKNOWN_STATEMENT_TYPE;
    }

    private boolean isStatementType(Token[] tkns, int[] statementTokens) {
        int iTkn = 0;
        for(int kind : statementTokens ) {
            // Check each token for kind
            if( tkns[iTkn].kind == kind) {
                if( ++iTkn == statementTokens.length) {
                    return true;
                }
                continue;
            };
            break;
        }
        return false;
    }

    public boolean allParensMatch(Token[] tkns) {
        return parensMatch(tkns, 0);
    }

    public DdlAnalyzerException checkAllParens() {
        return checkAllBrackets(LPAREN, RPAREN);
    }

    public DdlAnalyzerException checkAllBrackets(int leftBracketKind, int rightBracketKind) {
        int numUnmatchedParens = 0;
        DdlAnalyzerException exception = null;

        Position diagStartPosition = null;

        // TODO: Add logic to check for the scenarios like the first bracket

        for(int iTkn= 0; iTkn<tokens.length; iTkn++) {
            Token token = tokens[iTkn];
            if( token.kind == leftBracketKind)  {
                if( diagStartPosition == null ) {
                    diagStartPosition = new Position(token.beginLine, token.beginColumn);
                }
                numUnmatchedParens++;
            } else if( token.kind == rightBracketKind && diagStartPosition == null ) {
                diagStartPosition = new Position(token.beginLine, token.beginColumn);
                exception = new DdlAnalyzerException("Bracket at location " //$NON-NLS-1$
                        + getPositionString(token) + " does not properly match previous bracket"); //$NON-NLS-1$
                exception.getDiagnostic().setMessage(exception.getMessage());
                exception.getDiagnostic().setSeverity(DiagnosticSeverity.Error);
                exception.getDiagnostic().setRange(new Range(diagStartPosition, diagStartPosition));
            }

            if( exception != null ) {
                break;
            }

            if( token.kind == rightBracketKind) {
                numUnmatchedParens--;
            }

            // If the ## goes < 0 throw exception because they should be correctly nested
            //  VALID:  (  () () )
            //  INVALID (  )) () (
            //              ^ would occur here
            if( diagStartPosition != null && exception == null && numUnmatchedParens < 0 ) {
                Position diagEndPosition = new Position(token.endLine, token.beginLine);
                exception = new DdlAnalyzerException("Bracket at location " //$NON-NLS-1$
                        + getPositionString(token) + " does not properly match previous bracket"); //$NON-NLS-1$
                exception.getDiagnostic().setMessage(exception.getMessage());
                exception.getDiagnostic().setSeverity(DiagnosticSeverity.Error);
                exception.getDiagnostic().setRange(new Range(diagStartPosition, diagEndPosition));
            }
            if( exception != null ) {
                break;
            }
        }

        if( numUnmatchedParens != 0 ) {
            exception = new DdlAnalyzerException("Missing or mismatched brackets"); //$NON-NLS-1$
            exception.getDiagnostic().setMessage(exception.getMessage());
            exception.getDiagnostic().setSeverity(DiagnosticSeverity.Error);
            Token lastToken = tokens[tokens.length-1];
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

        for(int iTkn= 0; iTkn<tokens.length; iTkn++) {
            if( iTkn < startTokenId) {
                continue;
            }

            Token token = tkns[iTkn];
            if( token.kind == leftBracket) {
                numUnmatchedParens++;
            }
            if( token.kind == rightBracket) {
                numUnmatchedParens--;
            }
        }
        return numUnmatchedParens == 0;
    }

    public boolean parensMatch(Token[] tkns, int startTokenId) {
        return bracketsMatch(tkns, startTokenId, LPAREN, RPAREN);
    }

    public void addException(DdlAnalyzerException exception) {
        if (exception != null) {
            this.report.addException(exception);
        }
    }

    public void addException(
            Token startToken,
            Token endToken,
            String errorMessage) {
        Position startPosition = new Position(startToken.beginLine, startToken.beginColumn);
        Position endPosition = new Position(endToken.endLine, endToken.endColumn+1);
        DdlAnalyzerException exception =
                new DdlAnalyzerException(
                        DiagnosticSeverity.Error,
                        errorMessage,
                        new Range(startPosition, endPosition)); //$NON-NLS-1$);
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

    public void printTokens() {
        printTokens(this.tokens, null);
    }

    public Token getToken(int tokenIndex) {
        return this.tokens[tokenIndex];
    }

    public int getTokenIndex(Token token) {
        if (token == null)  {
            return 0;
        }

        int index = 0;
        for (Token tkn : getTokens()) {
            if (token.equals(tkn)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public String positionToString(Position position) {
        return "Line " + (position.getLine()+1) + " Column " + (position.getCharacter()+1);
    }

    private void printTokens(Token[] tkns, String headerMessage) {
        System.out.println(headerMessage);
        for (Token token : tkns) {
            System.out.println("  >> Token = " + token.image +
                    "\n\t   >> KIND = " + token.kind +
                    "\n\t   >> begins at ( " +
                    token.beginLine + ", " + token.beginColumn + " )" +
                    "\n\t   >> ends   at ( " +
                    token.endLine + ", " + token.endColumn + " )");

        }
    }

    public String[] getKeywordLabels(int[] keywordIds, boolean upperCase) {
        List<String> labels = new ArrayList<String>();

        for( int id: keywordIds) {
            labels.add(getKeywordLabel(id, upperCase));
        }

        return labels.toArray(new String[0]);
    }

    /*
     * The tokenImage[...] call is returning strings wrapped in double-quotes
     *
     * Need to return a simple string
     * @param tokenImageString string
     * @return string without double quotes
     */
    public String getKeywordLabel(int keywordId, boolean upperCase) {
        return DdlCompletionConstants.getLabel(keywordId, upperCase);
    }
}
