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

import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.SQLParserConstants;
import org.teiid.query.parser.Token;

import java.util.ArrayList;
import java.util.List;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public class FromClause extends AbstractStatementObject {
    private final List<TableSymbol> tableSymbols;

    public FromClause(DdlTokenAnalyzer analyzer) {
        super(analyzer);
        this.tableSymbols = new ArrayList<TableSymbol>();
    }

    public TableSymbol[] getTableSymbols() {
        return tableSymbols.toArray(new TableSymbol[0]);
    }

    public void addTableSymbol(TableSymbol tableSymbol) {
        this.tableSymbols.add(tableSymbol);
    }

    @Override
    protected void parseAndValidate() {
        processFromTokens();
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        boolean isInClause = isBetween(getFirstTknIndex(), getLastTknIndex(), position);
        if (isInClause) {
            Token tkn = this.analyzer.getTokenFor(position);

            if (isLastToken(tkn)) {
                if (tkn.kind == SQLParserConstants.COMMA) {
                    return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.FROM_CLAUSE, this);
                } else {
                    Token nextTkn = this.analyzer.getTokenAt(position);
                    if (nextTkn != null &&
                            nextTkn.kind == SQLParserConstants.WHERE &&
                            this.analyzer.getTokenAt(position) != null) {
                        return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.WHERE_CLAUSE, this);
                    }
                }
            } else {
                // Need to check each table element
                for (TableSymbol element : getTableSymbols()) {
                    TokenContext context = element.getTokenContext(position);
                    if (context != null) {
                        return context;
                    }
                }
            }
        }
        return null;
    }

    private void processFromTokens() {
        // Look for and FROM and WHERE tokens
        Token fromToken = findTokenByKind(SQLParserConstants.FROM);
        if (fromToken != null) {
            setFirstTknIndex(getTokenIndex(fromToken));
        } else {
            this.analyzer.addException(new DdlAnalyzerException(
                    "Parsing Exception:  there is no 'FROM' keyword in your query expression"));
            return;
        }

        // We have the FROM
        // Look for OPTIONAL >> WHERE

        Token whereToken = findTokenByKind(SQLParserConstants.WHERE);
        if (whereToken != null) {
            setLastTknIndex(getTokenIndex(whereToken) - 1);
        } else {
            Token orderToken = findTokenByKind(SQLParserConstants.ORDER);
            Token byToken = findTokenByKind(SQLParserConstants.BY);
            if( orderToken != null && byToken != null &&
                (getTokenIndex(byToken) == (getTokenIndex(orderToken)+1)) ) {
                setLastTknIndex(getTokenIndex(byToken) -1);
            } else {
                List<Token> tokens = getTokens();
                if (tokens.get(tokens.size() - 1).kind == SQLParserConstants.SEMICOLON) {
                    setLastTknIndex(tokens.size() - 2);
                } else {
                    setLastTknIndex(tokens.size() - 1);
                }
            }
        }

        // Parse TableSymbols
        // EXAMPLE: FROM Table1 AS t1, Table2 WHERE
        parseTableSymbols();

    }

    /**
     * Parse TableSymbols ||||||||||||||||| EXAMPLE: FROM Table1 AS t1, Table2 WHERE
     */
    private void parseTableSymbols() {
        boolean isDone = false;

        while (!isDone) {
            TableSymbol tableSymbol = new TableSymbol(super.analyzer, this);

            tableSymbol.parseAndValidate();

            if( tableSymbol.nextTokenIsInvalid() ) {
                isDone = true;
            } else if (tableSymbol.getLastTknIndex() > 0) {
                if (tableSymbol.getLastTknIndex() == getLastTknIndex()) {
                    isDone = true;
                }

                this.addTableSymbol(tableSymbol);
            }
        }
    }

}
