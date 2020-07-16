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

import java.util.List;

import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.SQLParserConstants;
import org.teiid.query.parser.Token;

import com.google.common.base.Splitter;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

/**
 * <p>
 * This is the representation of a FROM clause table symbol. The table symbol
 * has a name and an optional definition which defines the table symbol as
 * 'aliased'
 *
 * <p>
 * For example, if the original string contained a FROM clause such as "FROM
 * Table1 AS t1, Table2", there would be two TableSymbol's created. The first
 * would have name=t1, definition=Table1 and the second would have name=Table2,
 * definition=null.
 */

public class TableSymbol extends AbstractStatementObject {
    /** Definition of the symbol, may be null */
    private Token definitionToken;

    /** name referenced throughout the query expression */
    private Token nameToken;

    private final FromClause fromClause;

    private boolean nextTokenIsInvalid;

    public TableSymbol(DdlTokenAnalyzer analyzer, FromClause fromClause) {
        super(analyzer);
        this.fromClause = fromClause;
        this.nextTokenIsInvalid = false;
    }

    @Override
    protected void parseAndValidate() {
        if( isLastIndex()) { return; }

        incrementIndex(); // Should be at an ID or STRINGVAL

        if( isTokenOfKind(currentIndex(), SQLParserConstants.ID, SQLParserConstants.STRINGVAL)) {
            // Get name token and set first index
            Token tkn = getCurrentToken();
            setNameToken(tkn);
            setFirstTknIndex(currentIndex());
            setLastTknIndex(currentIndex());

            // Check for alias (AS) token
            if (hasNextIndex() && isNextTokenOfKind(currentIndex(), SQLParserConstants.AS)) {
                incrementIndex();
                if (hasNextIndex()) {
                    incrementIndex();
                    // get the alias name and save the actual name to definition token
                    if( isTokenOfKind(currentIndex(), SQLParserConstants.ID, SQLParserConstants.STRINGVAL)) {
                        Token aliasNameTkn = getCurrentToken();
                        Token nameToken = getNameToken();
                        setNameToken(aliasNameTkn);
                        setDefinitionToken(nameToken);
                        setLastTknIndex(currentIndex());
                    } else {
                        Token tmpTkn = getCurrentToken();
                        this.analyzer.addException(tmpTkn, tmpTkn, "Missing or invalid alias table name [" + tmpTkn.image + "]");
                        setLastTknIndex(currentIndex());
                    }
                }
            }

            if (hasNextIndex() && isNextTokenOfKind(currentIndex(), SQLParserConstants.COMMA, SQLParserConstants.SEMICOLON)) {
                incrementIndex();
                setLastTknIndex(currentIndex());
            }
        } else {
            Token tkn = getCurrentToken();
            this.analyzer.addException(tkn, tkn, "Invalid token: [" + tkn.image + "]");
            setNextTokenIsInvalid(true);
            decrementIndex();
        }
    }

    public boolean isLastTableSymbol() {
        // Check the following:
        // 1) Is the last token the LAST TOKEN == YES
        // 2) else If this table symbol ends with COMMA token == YES
        // 3) If next token is a WHERE or SEMICOLON clause == YES

        if( fromClause.getQueryExpression().isWithQuery ) {
            return isLastIndex() ||
                    isNextTokenOfKind(getLastTknIndex(),
                            SQLParserConstants.SEMICOLON,
                            SQLParserConstants.WHERE,
                            SQLParserConstants.RPAREN);
        }

        return isLastIndex() ||
                isNextTokenOfKind(getLastTknIndex(),
                        SQLParserConstants.SEMICOLON,
                        SQLParserConstants.WHERE);
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        boolean isInElement = isBetween(getFirstTknIndex(), getLastTknIndex(), position);
        if (isInElement) {
            Token tkn = this.analyzer.getTokenFor(position);
            if (isLastToken(tkn)) {
                if (tkn.kind == SQLParserConstants.COMMA) {
                    return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.FROM_CLAUSE, this);
                } else if (tkn.kind == SQLParserConstants.AS) {
                    return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.TABLE_SYMBOL_ID, this);
                }
            }

            return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.TABLE_SYMBOL, this);
        }

        return null;
    }

    public Token getDefinitionToken() {
        return definitionToken;
    }

    public void setDefinitionToken(Token definitionToken) {
        this.definitionToken = definitionToken;
    }

    public Token getNameToken() {
        return nameToken;
    }

    public void setNameToken(Token nameToken) {
        this.nameToken = nameToken;
    }

    public boolean isAliased() {
        return this.getDefinitionToken() != null;
    }

    /*
     * "<schema-name>.<table-name>"
     */
    public String getSchemaName() {
        if (getNameToken() != null) {
            String fullNameToken = getNameToken().image;
            if (isAliased()) {
                fullNameToken = getDefinitionToken().image;
            }

            List<String> parts = Splitter.on('.').splitToList(fullNameToken);
            if (parts.size() == 2) {
                return parts.get(0);
            }
        }

        return null;
    }

    /*
     * "<schema-name>.<table-name>"
     */
    public String getTableName() {
        if (getNameToken() != null) {
            String fullNameToken = getNameToken().image;
            if (isAliased()) {
                fullNameToken = getDefinitionToken().image;
            }

            List<String> parts = Splitter.on('.').splitToList(fullNameToken);
            if (parts.size() == 2) {
                return parts.get(1);
            }

            return parts.get(0);
        }

        return null;
    }

    public String getAlias() {
        if (isAliased()) {
            return getNameToken().image;
        }

        return null;
    }

    public boolean nextTokenIsInvalid() {
        return nextTokenIsInvalid;
    }

    public void setNextTokenIsInvalid(boolean isValid) {
        this.nextTokenIsInvalid = isValid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(75);

        for (int i=getFirstTknIndex(); i<getLastTknIndex()+1; i++) {
            append(getToken(i), sb);
            if (i < getLastTknIndex()-1) {
                sb.append(' ');
            }
        }

        return sb.toString();
    }

}
