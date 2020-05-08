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

import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public class TableElement extends AbstractStatementObject {
    private Token nameToken;
    private Token[] datatypeTokens;
    private Token[] notNullTokens;
    private Token autoIncrementToken;
    private Token uniqueToken;
    private Token indexToken;
    private Token[] pkTokens;
    private Token[] defaultTokens;
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
            Token tkn = this.getTokens()[currentTknIndex];
            if (currentTknIndex >= tableBodylastIndex) {
                elementEnded = true;
                setLastTknIndex(tableBodylastIndex - 1);
            } else {
                count++;
                switch (count) {
                case 1: {
                    // first token should be kind == ID
                    if (tkn.kind == ID || tkn.kind == STRINGVAL) {
                        setNameToken(tkn);
                        if (getFirstTknIndex() == 0) {
                            setFirstTknIndex(getTokenIndex(tkn));
                        }
                    } else if (tkn.kind == PRIMARY) {
                        // LOOKING FOR "PRIMARY KEY (ID)" tokens
                        setFirstTknIndex(getTokenIndex(tkn));
                        currentTknIndex = parsePrimaryKeyTokens(currentTknIndex, tkn);
                        setPKElement(true);
                    } else if( isReservedKeywordToken(tkn)) {
                        this.analyzer.addException(
                            tkn, tkn,"Token: '" + tkn.image + "' is a reserved word and must be wrapped in double quotes \"\" ");
                    } else if( isNonReservedKeywordToken(tkn) ) {
                        setNameToken(tkn);
                        if (getFirstTknIndex() == 0) {
                            setFirstTknIndex(getTokenIndex(tkn));
                        }
                    } else {
                        Token firstToken = tkn;
                        Token lastToken = tkn;
                        this.analyzer.addException(firstToken, lastToken, "column name '" + tkn.image
                                + "' is invalid at: " + positionToString(getBeginPosition(tkn)));
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
                        if (isNextTokenOfKind(this.getTokens(), currentTknIndex, LPAREN)) {
                            Token[] bracketedTkns = getBracketedTokens(getTokens(), currentTknIndex + 1, LPAREN,
                                    RPAREN);
                            if (bracketedTkns.length > 0) {
                                // collect all the tokens in the datatype
                                for (Token dTypeTkn : bracketedTkns) {
                                    currentTknIndex++;
                                    dTypeTkns.add(dTypeTkn);
                                    if( dTypeTkn.kind != LPAREN &&
                                        dTypeTkn.kind != RPAREN &&
                                        dTypeTkn.kind != COMMA ) {
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
                        setDatatypeTokens(dTypeTkns.toArray(new Token[0]));
                    } else if (tkn.kind == NOT) {
                        // Check if exists
                        if (notNullTokens != null && notNullTokens.length > 0) {
                            logAlreadySetPropertyException(tkn, tkn, "NOT NULL");
                        } else {
                            // Check for NULL
                            if (hasAnotherToken(getTokens(), currentTknIndex)) {
                                List<Token> tmpTkns = new ArrayList<Token>();
                                tmpTkns.add(tkn);
                                currentTknIndex++;
                                tkn = getTokens()[currentTknIndex];
                                if (tkn.kind == NULL) {
                                    tmpTkns.add(tkn);
                                    setNotNullTokens(tmpTkns.toArray(new Token[0]));
                                }
                            }
                        }
                    } else if (tkn.kind == AUTO_INCREMENT) {
                        if (autoIncrementToken != null) {
                            logAlreadySetPropertyException(tkn, tkn, "AUTO_INCREMENT");
                        } else {
                            setAutoIncrementToken(tkn);
                        }
                    } else if (tkn.kind == PRIMARY) {
                        if (pkTokens != null && pkTokens.length > 0) {
                            logAlreadySetPropertyException(tkn, tkn, "PRIMARY KEY");
                        } else {
                            // Check for NULL
                            if (hasAnotherToken(getTokens(), currentTknIndex)) {
                                List<Token> tmpTkns = new ArrayList<Token>();
                                tmpTkns.add(tkn);
                                currentTknIndex++;
                                tkn = getTokens()[currentTknIndex];
                                if (tkn.kind == KEY) {
                                    tmpTkns.add(tkn);
                                    setPrimaryKeyTokens(tmpTkns.toArray(new Token[0]));
                                }
                            }
                        }
                    } else if (tkn.kind == DEFAULT_KEYWORD) {
                        if (defaultTokens != null && defaultTokens.length > 0) {
                            logAlreadySetPropertyException(tkn, tkn, "DEFAULT VALUE");
                        } else {
                            // Check for NULL
                            if (hasAnotherToken(getTokens(), currentTknIndex)) {
                                List<Token> tmpTkns = new ArrayList<Token>();
                                tmpTkns.add(tkn);
                                currentTknIndex++;
                                tkn = getTokens()[currentTknIndex];
                                if (tkn.kind == STRINGVAL) {
                                    tmpTkns.add(tkn);
                                    setDefaultTokens(tmpTkns.toArray(new Token[0]));
                                }
                            }
                        }
                    } else if (tkn.kind == INDEX) {
                        setIndexToken(tkn);
                    } else if (tkn.kind == UNIQUE) {
                        setUniqueToken(tkn);
                    } else if (tkn.kind == OPTIONS) {
                        if (getOptionClause() != null) {
                            logAlreadySetPropertyException(tkn, tkn, "OPTIONS(...)");
                        } else {
                            List<Token> optionsTkns = new ArrayList<Token>();
                            optionsTkns.add(tkn);

                            // Check for parens in case of string(), decimal() types.. etc
                            if (isNextTokenOfKind(getTokens(), currentTknIndex, LPAREN)) {
                                Token[] bracketedTkns = getBracketedTokens(getTokens(), currentTknIndex + 1, LPAREN,
                                        RPAREN);
                                if (bracketedTkns.length > 0) {
                                    for (Token dTypeTkn : bracketedTkns) {
                                        currentTknIndex++;
                                        optionsTkns.add(dTypeTkn);
                                    }
                                }
                            }

                            if (!optionsTkns.isEmpty()) {
                                TableElementOptionsClause options = new TableElementOptionsClause(analyzer);
                                options.setOptionsTokens(optionsTkns.toArray(new Token[0]));
                                setOptionClause(options);
                            }
                        }
                    } else if (tkn.kind == COMMA) {
                        setLastTknIndex(getTokenIndex(tkn));
                        elementEnded = true;
                    } else if(count == 2) {
                        this.analyzer.addException(tkn, tkn, "Invalid datatype: '"
                                + tkn.image + "' at: " + positionToString(getBeginPosition(tkn)));
                    } else{
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
            thisToken = getTokens()[currentIndex];
            if (thisToken.kind == KEY) {
                previousToken = thisToken;
                tmpTkns.add(thisToken);
                // Check for NULL and look for ( token
                if (hasAnotherToken(getTokens(), currentIndex)) {
                    currentIndex++;
                    thisToken = getTokens()[currentIndex];
                    if (thisToken.kind == LPAREN) {
                        previousToken = thisToken;
                        tmpTkns.add(thisToken);
                        // Check for NULL and look for ID token
                        if (hasAnotherToken(getTokens(), currentIndex)) {
                            currentIndex++;
                            thisToken = getTokens()[currentIndex];
                            if (thisToken.kind == ID) {
                                previousToken = thisToken;
                                tmpTkns.add(thisToken);
                                // Check for NULL and look for ) token
                                if (hasAnotherToken(getTokens(), currentIndex)) {
                                    currentIndex++;
                                    thisToken = getTokens()[currentIndex];
                                    if (thisToken.kind == RPAREN) {
                                        tmpTkns.add(thisToken);
                                        if (hasAnotherToken(getTokens(), currentIndex)) {
                                            thisToken = getTokens()[currentIndex + 1];
                                            if (thisToken.kind != RPAREN && thisToken.kind != COMMA) {
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
                                if (thisToken.kind == RPAREN || thisToken.kind == COMMA) {
                                    logIncompletePrimaryKeyException(previousToken, previousToken);
                                } else {
                                    logPrimaryKeyException(previousToken, thisToken);
                                }
                            }
                        } else {
                            logIncompletePrimaryKeyException(previousToken, previousToken);
                        }
                    } else {
                        if (thisToken.kind == RPAREN || thisToken.kind == COMMA) {
                            logIncompletePrimaryKeyException(previousToken, previousToken);
                        } else {
                            logPrimaryKeyException(previousToken, thisToken);
                        }
                    }
                } else {
                    logIncompletePrimaryKeyException(previousToken, previousToken);
                }
            } else {
                if (thisToken.kind == RPAREN || thisToken.kind == COMMA) {
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
                setPrimaryKeyTokens(tmpTkns.toArray(new Token[0]));
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

    public Token[] getDatatypeTokens() {
        return datatypeTokens;
    }

    public void setDatatypeTokens(Token[] datatypeTokens) {
        this.datatypeTokens = datatypeTokens;
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

    public Token[] getNotNullTokens() {
        return notNullTokens;
    }

    public void setNotNullTokens(Token[] notNullTokens) {
        this.notNullTokens = notNullTokens;
    }

    public Token[] getPrimaryKeyTokens() {
        return pkTokens;
    }

    public void setPrimaryKeyTokens(Token[] pkTokens) {
        this.pkTokens = pkTokens;
        setIsPrimaryKey(pkTokens != null);
    }

    public Token[] getDefaultTokens() {
        return defaultTokens;
    }

    public void setDefaultTokens(Token[] defaultTokens) {
        this.defaultTokens = defaultTokens;
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

    public boolean isDatatype(Token token) {
        for (int dType : DATATYPES) {
            if (token.kind == dType) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        boolean isInElement = isBetween(getFirstTknIndex(), getLastTknIndex(), position);
        if (isInElement) {
            Token tkn = this.analyzer.getTokenFor(position);

            // Check for last token == COMMA since comma is part of the table element token
            if (tkn.kind == COMMA && getTokens()[getLastTknIndex()].kind == COMMA
                    && getTokenIndex(tkn) == getLastTknIndex()) {
                // return the TableBody context to show new column definition item
                return new TokenContext(position, tkn, CONTEXT.TABLE_BODY, this);
            }

            if (tkn.kind == RPAREN) {
                return new TokenContext(position, tkn, CONTEXT.TABLE_ELEMENT, this);
            }

            if (optionsClause != null) {
                TokenContext context = optionsClause.getTokenContext(position);
                if (context != null) {
                    return context;
                }
                ;
            }
            return new TokenContext(position, tkn, CONTEXT.TABLE_ELEMENT, this);
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
            sb.append(" " + getTokens()[i]);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TableElement:   ").append(getNameToken());
        if (getDatatypeTokens() != null) {
            for (Token tkn : getDatatypeTokens()) {
                sb.append(" " + tkn.image);
            }
        }
        if (getNotNullTokens() != null) {
            for (Token tkn : getNotNullTokens()) {
                sb.append(" " + tkn.image);
            }
        }
        if (getAutoIncrementToken() != null) {
            sb.append(getAutoIncrementToken().image);
        }
        if (getIndexToken() != null) {
            sb.append(" " + getIndexToken().image);
        }
        if (getDefaultTokens() != null) {
            for (Token tkn : getDefaultTokens()) {
                sb.append(" " + tkn.image);
            }
        }
        if (getUniqueToken() != null) {
            sb.append(" " + getUniqueToken().image);
        }
        if (getPrimaryKeyTokens() != null) {
            for (Token tkn : getPrimaryKeyTokens()) {
                sb.append(" " + tkn.image);
            }
        }
        if (getOptionClause() != null) {
            for (Token tkn : getOptionClause().getOptionsTokens()) {
                sb.append(" " + tkn.image);
            }
        }
        sb.append("\n" + getFullTokenString());

        return sb.toString();
    }
}
