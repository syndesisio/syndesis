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

import io.syndesis.dv.lsp.Messages;
import io.syndesis.dv.lsp.codeactions.QuickFixFactory;
import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public class FromClause extends AbstractStatementObject {
    private final List<TableSymbol> tableSymbols;
    private final QueryExpression queryExpression;

    public FromClause(DdlTokenAnalyzer analyzer, QueryExpression queryExpression) {
        super(analyzer);
        this.tableSymbols = new ArrayList<TableSymbol>();
        this.queryExpression = queryExpression;
    }

    public TableSymbol[] getTableSymbols() {
        return tableSymbols.toArray(new TableSymbol[0]);
    }

    public void addTableSymbol(TableSymbol tableSymbol) {
        this.tableSymbols.add(tableSymbol);
    }

    public QueryExpression getQueryExpression() {
        return this.queryExpression;
    }

    @Override
    protected void parseAndValidate() {
        if (hasNextIndex() && isNextTokenOfKind(currentIndex(), SQLParserConstants.FROM)) {
            incrementIndex();
            setFirstTknIndex(currentIndex());
            setLastTknIndex(currentIndex());
        } else {
            this.analyzer.addWarning(
                    getCurrentToken(),
                    getCurrentToken(),
                    Messages.getString(Messages.Error.MISSING_FROM_KEYWORD))
                    .setErrorCode(
                    QuickFixFactory.DiagnosticErrorId.MISSING_FROM_KEYWORD.getErrorCode());

            return;
        }

        // Parse TableSymbols
        // EXAMPLE: FROM Table1 AS t1, Table2 WHERE
        parseTableSymbols();
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
                    } else {
                        // basically end of statement and outside the from clause... return NULL for query expression
                        return null;
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

    /**
     * Parse TableSymbols ||||||||||||||||| EXAMPLE: FROM Table1 AS t1, Table2 WHERE
     */
    private void parseTableSymbols() {
        boolean isDone = false;

        while (!isDone) {
            TableSymbol tableSymbol = new TableSymbol(super.analyzer, this);

            tableSymbol.parseAndValidate();

            if (tableSymbol.nextTokenIsInvalid()) {
                isDone = true;
            }  else {
                if (tableSymbol.getLastTknIndex() > 0) {
                    this.addTableSymbol(tableSymbol);
                    isDone = tableSymbol.isLastTableSymbol();
                } else {
                    isDone = true;
                }
                if (isDone && queryExpression instanceof WithQueryExpression &&
                        isNextTokenOfKind(currentIndex(), SQLParserConstants.RPAREN)) {
                    incrementIndex();
                }
            }
        }
        if( !tableSymbols.isEmpty() ) {
            setLastTknIndex(tableSymbols.get(tableSymbols.size()-1).getLastTknIndex());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append(" FROM ");

        if( !tableSymbols.isEmpty()) {
            for( TableSymbol symbol: tableSymbols) {
                sb.append("").append(symbol);
            }
        }

        return sb.toString();
    }

}
