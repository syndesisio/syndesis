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
package io.syndesis.dv.lsp.parser.statement;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.SQLParserConstants;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.Messages;
import io.syndesis.dv.lsp.codeactions.QuickFixFactory;
import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

@SuppressWarnings({"PMD.GodClass", "PMD.CyclomaticComplexity"})
public class TableElement extends AbstractStatementObject {
    private Token nameToken;
    private List<Token> datatypeTokens;
    private List<Token> notNullTokens;
    private Token autoIncrementToken;
    private Token uniqueToken;
    private Token indexToken;
    private List<Token> pkTokens;
    private List<Token> defaultTokens;
    private TableElementOptionsClause optionsClause;
    private int datatypeKind;
    private boolean isPKElement;
    private boolean isPrimaryKey;

    private TableBody tableBody;

    public TableElement(DdlTokenAnalyzer analyzer) {
        super(analyzer);
    }

    public TableElement(DdlTokenAnalyzer analyzer, TableBody tableBody) {
        super(analyzer);
        this.tableBody = tableBody;
    }

    @Override
    @SuppressWarnings("PMD") // TODO refactor
    protected void parseAndValidate() {
        int count = 0;
        int tableBodyFirstIndex = tableBody.getFirstTknIndex();
        int tableBodylastIndex = tableBody.getLastTknIndex();

        int startIndex = tableBodyFirstIndex;
        // check for previous table elements and reset the startIndex
        int nTableElements = tableBody.getTableElements().length;
        if (nTableElements > 0) {
            startIndex = tableBody.getTableElements()[nTableElements - 1].getLastTknIndex();
        }

        int currentTknIndex = startIndex + 1;
        boolean elementEnded = false;

        while (!elementEnded) {
            Token tkn = this.getTokens().get(currentTknIndex);
            if (currentTknIndex >= tableBodylastIndex) {
                elementEnded = true;
                setLastTknIndex(tableBodylastIndex - 1);
            } else {
                count++;
                switch (count) {
                case 1: {
                    // first token should be kind == ID
                    if (tkn.kind == SQLParserConstants.ID || tkn.kind == SQLParserConstants.STRINGVAL) {
                        setNameToken(tkn);
                        if (getFirstTknIndex() == 0) {
                            setFirstTknIndex(getTokenIndex(tkn));
                        }
                    } else if (tkn.kind == SQLParserConstants.PRIMARY) {
                        // LOOKING FOR "PRIMARY KEY (ID)" tokens
                        setFirstTknIndex(getTokenIndex(tkn));
                        currentTknIndex = parsePrimaryKeyTokens(currentTknIndex, tkn);
                        setPKElement(true);
                    } else if( isReservedKeywordToken(tkn)) {
                        DdlAnalyzerException exception = this.analyzer.addException(
                            tkn, tkn,
                            Messages.getString(Messages.Error.COLUMN_NAME_RESERVED_WORD, tkn.image));
                        exception.setErrorCode(
                                QuickFixFactory.DiagnosticErrorId.COLUMN_NAME_RESERVED_WORD.getErrorCode());
                        exception.setTargetedString(tkn.image);
                    } else if( isNonReservedKeywordToken(tkn) ) {
                        setNameToken(tkn);
                        if (getFirstTknIndex() == 0) {
                            setFirstTknIndex(getTokenIndex(tkn));
                        }
                        DdlAnalyzerException exception = this.analyzer.addException(
                                tkn, tkn,
                                Messages.getString(Messages.Error.COLUMN_NAME_NON_RESERVED_WORD, tkn.image));
                            exception.setErrorCode(QuickFixFactory.DiagnosticErrorId.COLUMN_NAME_NON_RESERVED_WORD.getErrorCode());
                            exception.setTargetedString(tkn.image);
                            exception.getDiagnostic().setSeverity(DiagnosticSeverity.Warning);
                    } else {
                        this.analyzer.addException(tkn, tkn, Messages.getString(Messages.Error.INVALID_COLUMN_NAME, tkn.image));;
                    }
                    currentTknIndex++;
                }
                    break;

                // Check any additional/optional tokens after the datatype
                default: {
                    if (count == 2 && isDatatype(tkn)) {
                        setDatatypeKind(tkn.kind);
                        List<Token> dTypeTkns = new ArrayList<Token>();
                        dTypeTkns.add(tkn);

                        // Check for parens in case of string(), decimal() types.. etc
                        if (isNextTokenOfKind(this.getTokens(), currentTknIndex, SQLParserConstants.LPAREN)) {
                            List<Token> bracketedTkns = getBracketedTokens(getTokens(), currentTknIndex + 1, SQLParserConstants.LPAREN,
                                SQLParserConstants.RPAREN);
                            if (bracketedTkns.size() > 0) {
                                // collect all the tokens in the datatype
                                for (Token dTypeTkn : bracketedTkns) {
                                    currentTknIndex++;
                                    dTypeTkns.add(dTypeTkn);
                                    if( dTypeTkn.kind != SQLParserConstants.LPAREN &&
                                        dTypeTkn.kind != SQLParserConstants.RPAREN &&
                                        dTypeTkn.kind != SQLParserConstants.COMMA ) {
                                        // Check for integer value
                                        try {
                                            Integer.parseUnsignedInt(dTypeTkn.image);
                                        } catch (NumberFormatException e) {
                                            this.analyzer.addException(dTypeTkn, dTypeTkn,
                                                    "value is not an integer");
                                        }
                                    }
                                }
                            }
                        }
                        // Add datatype tokens to the table element
                        setDatatypeTokens(dTypeTkns);
                    } else if (tkn.kind == SQLParserConstants.NOT) {
                        // Check if exists
                        if (notNullTokens != null && !notNullTokens.isEmpty()) {
                            logAlreadySetPropertyException(tkn, tkn, "NOT NULL");
                        } else {
                            // Check for NULL
                            if (hasAnotherToken(getTokens(), currentTknIndex)) {
                                List<Token> tmpTkns = new ArrayList<Token>();
                                tmpTkns.add(tkn);
                                currentTknIndex++;
                                tkn = getTokens().get(currentTknIndex);
                                if (tkn.kind == SQLParserConstants.NULL) {
                                    tmpTkns.add(tkn);
                                    setNotNullTokens(tmpTkns);
                                }
                            }
                        }
                    } else if (tkn.kind == SQLParserConstants.AUTO_INCREMENT) {
                        if (autoIncrementToken != null) {
                            logAlreadySetPropertyException(tkn, tkn, "AUTO_INCREMENT");
                        } else {
                            setAutoIncrementToken(tkn);
                        }
                    } else if (tkn.kind == SQLParserConstants.PRIMARY) {
                        if (pkTokens != null && !pkTokens.isEmpty()) {
                            logAlreadySetPropertyException(tkn, tkn, "PRIMARY KEY");
                        } else {
                            // Check for NULL
                            if (hasAnotherToken(getTokens(), currentTknIndex)) {
                                List<Token> tmpTkns = new ArrayList<Token>();
                                tmpTkns.add(tkn);
                                currentTknIndex++;
                                tkn = getTokens().get(currentTknIndex);
                                if (tkn.kind == SQLParserConstants.KEY) {
                                    tmpTkns.add(tkn);
                                    setPrimaryKeyTokens(tmpTkns);
                                }
                            }
                        }
                    } else if (tkn.kind == SQLParserConstants.DEFAULT_KEYWORD) {
                        if (defaultTokens != null && !defaultTokens.isEmpty()) {
                            logAlreadySetPropertyException(tkn, tkn, "DEFAULT VALUE");
                        } else {
                            // Check for NULL
                            if (hasAnotherToken(getTokens(), currentTknIndex)) {
                                List<Token> tmpTkns = new ArrayList<Token>();
                                tmpTkns.add(tkn);
                                currentTknIndex++;
                                tkn = getTokens().get(currentTknIndex);
                                if (tkn.kind == SQLParserConstants.STRINGVAL) {
                                    tmpTkns.add(tkn);
                                    setDefaultTokens(tmpTkns);
                                }
                            }
                        }
                    } else if (tkn.kind == SQLParserConstants.INDEX) {
                        setIndexToken(tkn);
                    } else if (tkn.kind == SQLParserConstants.UNIQUE) {
                        setUniqueToken(tkn);
                    } else if (tkn.kind == SQLParserConstants.OPTIONS) {
                        if (getOptionClause() != null) {
                            logAlreadySetPropertyException(tkn, tkn, "OPTIONS(...)");
                        } else {
                            List<Token> optionsTkns = new ArrayList<Token>();
                            optionsTkns.add(tkn);

                            // Check for parens in case of string(), decimal() types.. etc
                            if (isNextTokenOfKind(getTokens(), currentTknIndex, SQLParserConstants.LPAREN)) {
                                List<Token> bracketedTkns = getBracketedTokens(getTokens(), currentTknIndex + 1, SQLParserConstants.LPAREN,
                                    SQLParserConstants.RPAREN);
                                for (Token dTypeTkn : bracketedTkns) {
                                    currentTknIndex++;
                                    optionsTkns.add(dTypeTkn);
                                }
                            }

                            if (!optionsTkns.isEmpty()) {
                                TableElementOptionsClause options = new TableElementOptionsClause(analyzer);
                                options.setOptionsTokens(optionsTkns);
                                setOptionClause(options);
                            }
                        }
                    } else if (tkn.kind == SQLParserConstants.COMMA) {
                        setLastTknIndex(getTokenIndex(tkn));
                        elementEnded = true;
                    } else if(count == 2) {
                        DdlAnalyzerException exception = this.analyzer.addException(tkn, tkn, "Invalid datatype: '"
                                + tkn.image + "' at: " + positionToString(getBeginPosition(tkn)));
                        exception.setErrorCode(QuickFixFactory.DiagnosticErrorId.INVALID_DATATYPE.getErrorCode());
                        exception.setTargetedString(tkn.image);
                    } else {
                        // IF THERE IS EXTRA add exception with text of unknown TOKEN
                        Token firstToken = tkn;
                        Token lastToken = tkn;
                        this.analyzer.addException(firstToken, lastToken, "Invalid token in table element:  '"
                                + tkn.image + "' at: " + positionToString(getBeginPosition(tkn)));
                    }
                    // After datatype could be
                    // NOT NULL (2 tokens)
                    // AUTO_INCREMENT
                    // INDEX
                    // PRIMARY KEY (a, b)
                    // UNIQUE (a, b)
                    // default 'someValue'
                    // OPTIONS(....)
                    // ','
                    currentTknIndex++;
                }
                    break;

                }
            }
        }
    }

    @SuppressWarnings("PMD.CyclomaticComplexity") // TODO refactor
    private int parsePrimaryKeyTokens(int startIndex, Token primaryToken) {
        int currentIndex = startIndex;
        // LOOKING FOR "PRIMARY KEY (ID)" tokens

        List<Token> tmpTkns = new ArrayList<Token>();
        tmpTkns.add(primaryToken);
        Token previousToken = primaryToken;
        Token thisToken = primaryToken;

        // Check for NULL and look for KEY token
        if (hasAnotherToken(getTokens(), currentIndex)) {
            currentIndex++;
            thisToken = getTokens().get(currentIndex);
            if (thisToken.kind == SQLParserConstants.KEY) {
                previousToken = thisToken;
                tmpTkns.add(thisToken);
                // Check for NULL and look for ( token
                if (hasAnotherToken(getTokens(), currentIndex)) {
                    currentIndex++;
                    thisToken = getTokens().get(currentIndex);
                    if (thisToken.kind == SQLParserConstants.LPAREN) {
                        previousToken = thisToken;
                        tmpTkns.add(thisToken);
                        // Check for NULL and look for ID token
                        if (hasAnotherToken(getTokens(), currentIndex)) {
                            currentIndex++;
                            thisToken = getTokens().get(currentIndex);
                            if (thisToken.kind == SQLParserConstants.ID) {
                                previousToken = thisToken;
                                tmpTkns.add(thisToken);
                                // Check for NULL and look for ) token
                                if (hasAnotherToken(getTokens(), currentIndex)) {
                                    currentIndex++;
                                    thisToken = getTokens().get(currentIndex);
                                    if (thisToken.kind == SQLParserConstants.RPAREN) {
                                        tmpTkns.add(thisToken);
                                        if (hasAnotherToken(getTokens(), currentIndex)) {
                                            thisToken = getTokens().get(currentIndex + 1);
                                            if (thisToken.kind != SQLParserConstants.RPAREN && thisToken.kind != SQLParserConstants.COMMA) {
                                                logPrimaryKeyException(thisToken, thisToken);
                                            }
                                        }
                                    } else {
                                        logPrimaryKeyException(previousToken, thisToken);
                                    }
                                } else {
                                    logIncompletePrimaryKeyException(previousToken, previousToken);
                                }
                            } else {
                                if (thisToken.kind == SQLParserConstants.RPAREN || thisToken.kind == SQLParserConstants.COMMA) {
                                    logIncompletePrimaryKeyException(previousToken, previousToken);
                                } else {
                                    logPrimaryKeyException(previousToken, thisToken);
                                }
                            }
                        } else {
                            logIncompletePrimaryKeyException(previousToken, previousToken);
                        }
                    } else {
                        if (thisToken.kind == SQLParserConstants.RPAREN || thisToken.kind == SQLParserConstants.COMMA) {
                            logIncompletePrimaryKeyException(previousToken, previousToken);
                        } else {
                            logPrimaryKeyException(previousToken, thisToken);
                        }
                    }
                } else {
                    logIncompletePrimaryKeyException(previousToken, previousToken);
                }
            } else {
                if (thisToken.kind == SQLParserConstants.RPAREN || thisToken.kind == SQLParserConstants.COMMA) {
                    logIncompletePrimaryKeyException(previousToken, previousToken);
                } else {
                    logPrimaryKeyException(previousToken, thisToken);
                }
            }
        } else {
            logIncompletePrimaryKeyException(previousToken, previousToken);
        }

        if (!tmpTkns.isEmpty()) {
            if (!tableBody.hasPrimaryKey()) {
                setPrimaryKeyTokens(tmpTkns);
            } else {
                // error on all tokens since PK has already been defined
                Token startToken = tmpTkns.get(0);
                Token endToken = tmpTkns.get(tmpTkns.size() - 1);
                this.analyzer.addException(startToken, endToken, "Invalid token in PRIMARY KEY element:  '"
                        + startToken.image + "' at: " + positionToString(getBeginPosition(startToken)));
            }
        }

        return currentIndex;
    }

    private void logPrimaryKeyException(Token startToken, Token endToken) {
        this.analyzer.addException(startToken, endToken, "Invalid token in PRIMARY KEY element:  '" + startToken.image
                + "' at: " + positionToString(getBeginPosition(startToken)));
    }

    private void logIncompletePrimaryKeyException(Token startToken, Token endToken) {
        this.analyzer.addException(startToken, endToken,
                "PRIMARY KEY element definition is incomplete\n\n expecting >>  PRIMARY KEY (columnName)");
    }

    private void logAlreadySetPropertyException(Token startToken, Token endToken, String property) {
        this.analyzer.addException(startToken, endToken, property + " already set on table element");
    }

    public Token getNameToken() {
        return nameToken;
    }

    public void setNameToken(Token nameToken) {
        this.nameToken = nameToken;
    }

    public List<Token> getDatatypeTokens() {
        return datatypeTokens;
    }

    public void setDatatypeTokens(List<Token> dTypeTkns) {
        this.datatypeTokens = dTypeTkns;
    }

    public TableElementOptionsClause getOptionClause() {
        return optionsClause;
    }

    public void setOptionClause(TableElementOptionsClause optionClause) {
        this.optionsClause = optionClause;
    }

    public int getDatatypeKind() {
        return datatypeKind;
    }

    public void setDatatypeKind(int datatypeKind) {
        this.datatypeKind = datatypeKind;
    }

    public List<Token> getNotNullTokens() {
        return notNullTokens;
    }

    public void setNotNullTokens(List<Token> tmpTkns) {
        this.notNullTokens = tmpTkns;
    }

    public List<Token> getPrimaryKeyTokens() {
        return pkTokens;
    }

    public void setPrimaryKeyTokens(List<Token> tmpTkns) {
        this.pkTokens = tmpTkns;
        setIsPrimaryKey(tmpTkns != null);
    }

    public List<Token> getDefaultTokens() {
        return defaultTokens;
    }

    public void setDefaultTokens(List<Token> tmpTkns) {
        this.defaultTokens = tmpTkns;
    }

    public Token getAutoIncrementToken() {
        return autoIncrementToken;
    }

    public void setAutoIncrementToken(Token autoIncrementToken) {
        this.autoIncrementToken = autoIncrementToken;
    }

    public Token getUniqueToken() {
        return uniqueToken;
    }

    public void setUniqueToken(Token uniqueToken) {
        this.uniqueToken = uniqueToken;
    }

    public Token getIndexToken() {
        return indexToken;
    }

    public void setIndexToken(Token indexToken) {
        this.indexToken = indexToken;
    }

    public static boolean isDatatype(Token token) {
        return DdlAnalyzerConstants.DATATYPES.contains(token.kind);
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        boolean isInElement = isBetween(getFirstTknIndex(), getLastTknIndex(), position);
        if (isInElement) {
            Token tkn = this.analyzer.getTokenFor(position);

            // Check for last token == COMMA since comma is part of the table element token
            if (tkn.kind == SQLParserConstants.COMMA && getTokens().get(getLastTknIndex()).kind == SQLParserConstants.COMMA
                    && getTokenIndex(tkn) == getLastTknIndex()) {
                // return the TableBody context to show new column definition item
                return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.TABLE_BODY, this);
            }

            if (tkn.kind == SQLParserConstants.RPAREN) {
                return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.TABLE_ELEMENT, this);
            }

            if (optionsClause != null) {
                TokenContext context = optionsClause.getTokenContext(position);
                if (context != null) {
                    return context;
                }
            }
            return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.TABLE_ELEMENT, this);
        }

        return null;
    }

    public boolean isPKElement() {
        return isPKElement;
    }

    public void setPKElement(boolean isPKElement) {
        this.isPKElement = isPKElement;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setIsPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    private String getFullTokenString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RAW TOKENS: ");
        for (int i = getFirstTknIndex(); i < getLastTknIndex() + 1; i++) {
            sb.append(' ').append(getTokens().get(i));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(75)
            .append("TableElement:   ").append(getNameToken());

        append(datatypeTokens, sb);
        append(notNullTokens, sb);
        append(autoIncrementToken, sb);
        append(indexToken, sb);
        append(defaultTokens, sb);
        append(uniqueToken, sb);
        append(pkTokens, sb);
        if (optionsClause != null) {
            append(optionsClause.getOptionsTokens(), sb);
        }
        sb.append('\n').append(getFullTokenString());

        return sb.toString();
    }

    private static void append(Token token, StringBuilder target) {
        if (token == null) {
            return;
        }

        target.append(' ').append(token.image);
    }

    private static void append(final Iterable<Token> tokens, final StringBuilder target) {
        if (tokens == null) {
            return;
        }

        for (Token token : tokens) {
            append(token, target);
        }
    }
}
