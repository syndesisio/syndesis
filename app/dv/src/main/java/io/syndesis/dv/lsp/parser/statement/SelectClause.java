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

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.SQLParserConstants;
import org.teiid.query.parser.Token;

public class SelectClause extends AbstractStatementObject {
    private final List<SelectColumn> selectColumns;
    private boolean isAll;
    private boolean isDistinct;
    private boolean isStar;
    private final QueryExpression queryExpression;

    public SelectClause(DdlTokenAnalyzer analyzer, QueryExpression queryExpression) {
        super(analyzer);
        this.selectColumns = new ArrayList<SelectColumn>();
        this.queryExpression = queryExpression;
    }

    public QueryExpression getQueryExpression() {
        return this.queryExpression;
    }

    public SelectColumn[] getSelectColumns() {
        return selectColumns.toArray(new SelectColumn[0]);
    }

    public void addSelectColumn(SelectColumn selectColumn) {
        this.selectColumns.add(selectColumn);
    }

    @Override
    protected void parseAndValidate() {
        // Look for SELECT and FROM tokens
        Token selectToken = findTokenByKind(SQLParserConstants.SELECT);
        if (selectToken != null) {
            setFirstTknIndex(getTokenIndex(selectToken));
        } else {
            this.analyzer.addException(new DdlAnalyzerException("There is no 'SELECT' in your query expression"));
        }

        // We have the SELECT
        // Look for FROM

        Token fromToken = findTokenByKind(SQLParserConstants.FROM);
        if (fromToken != null) {
            setLastTknIndex(getTokenIndex(fromToken) - 1);
        } else {
            // Assume that the SELECT ends at either the last token before the semicolon
            // or the last token if semi-colon isn't there
            List<Token> tokens = getTokens();
            if (tokens.get(tokens.size() - 1).kind == SQLParserConstants.SEMICOLON) {
                setLastTknIndex(tokens.size() - 2);
            }
        }

        // Parse SelectColumn's
        processSelectTokens();
    }


     // SELECT ( ALL | DISTINCT )? ( <star> | ( <select sublist> ( <comma> <select sublist> )* ) )
    private void processSelectTokens() {
        // first token after SELECT
        int currentIndex = getFirstTknIndex() + 1;
        Token currentToken = getAnalyzer().getToken(currentIndex);
        if (currentToken.kind == SQLParserConstants.ALL) {
            isAll = true;
        } else if (currentToken.kind == SQLParserConstants.DISTINCT) {
            isDistinct = true;
        }

        // Look for ALL or DISTINCT and *
        // Parse SelectColumn's
        parseSelectColumns(currentIndex);
    }

    private void parseSelectColumns(int startingIndex) {
        boolean isDone = false;
        Token currentToken = getAnalyzer().getToken(startingIndex);

        // if next token is * , then there will be no defined columns
        if (currentToken.kind == SQLParserConstants.STAR) {
            this.isStar = true;
            isDone = true;
        }

        int nextFirstIndex = startingIndex;

        while (!isDone) {
            if (functionIsNext(nextFirstIndex)) {
                // Peek at next Token and see if it's Function or a
                SelectFunction selectFunction = new SelectFunction(super.analyzer, this);

                selectFunction.parseAndValidate();

                if (selectFunction.getLastTknIndex() > 0) {
                    if (selectFunction.getLastTknIndex() == getLastTknIndex()) {
                        isDone = true;
                    }

                    this.addSelectColumn(selectFunction);
                }
            } else {
                // Peek at next Token and see if it's Function or a
                SelectColumn selectColumn = new SelectColumn(super.analyzer, this);

                selectColumn.parseAndValidate();

                if (selectColumn.getLastTknIndex() > 0) {
                    if (selectColumn.getLastTknIndex() == getLastTknIndex()) {
                        isDone = true;
                    }

                    this.addSelectColumn(selectColumn);
                }
            }
        }
    }

    public boolean functionIsNext(int startIndex) {
        Token idToken = getAnalyzer().getToken(startIndex);
        Token parenToken = getAnalyzer().getToken(startIndex + 1);
        return parenToken != null && parenToken.kind == SQLParserConstants.LPAREN
            && FunctionHelper.getInstance().isFunctionName(idToken.image);
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        boolean isInClause = isBetween(getFirstTknIndex(), getLastTknIndex(), position);
        if (isInClause) {
            // Need to check each SELECT column
            for (SelectColumn element : getSelectColumns()) {
                TokenContext context = element.getTokenContext(position);
                if (context != null) {
                    return context;
                }
            }
            Token tkn = this.analyzer.getTokenFor(position);
            return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.SELECT_CLAUSE, this);
        }
        return null;
    }

    public boolean isAll() {
        return isAll;
    }

    public boolean isDistinct() {
        return isDistinct;
    }

    public boolean isStar() {
        return isStar;
    }
}
