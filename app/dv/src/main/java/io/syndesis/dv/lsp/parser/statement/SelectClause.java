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

import io.syndesis.dv.lsp.Messages;
import io.syndesis.dv.lsp.codeactions.QuickFixFactory;
import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.SQLParserConstants;
import org.teiid.query.parser.Token;

@SuppressWarnings({"PMD.GodClass"})
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
        if( hasNextIndex() && isNextTokenOfKind(currentIndex(), SQLParserConstants.SELECT)) {
            incrementIndex();
            setFirstTknIndex(currentIndex());
            setLastTknIndex(currentIndex());
        } else {
            Token firstQETkn = queryExpression.getFirstToken();
            this.analyzer.addException(
                    firstQETkn,
                    firstQETkn,
                    Messages.getString(Messages.Error.NO_SELECT_CLAUSE_FOUND));
        }

        // We have the SELECT

        // Parse SelectColumn's
        processSelectTokens();

        // Check number of select columns versus number of table elements in table body
        if( getQueryExpression() instanceof WithQueryExpression ) {
            return;
        }

        if( getQueryExpression().getCreateViewStatement().getTableBody().getLastTknIndex() == 0 ) {
            return;
        }

        int nViewColumns = 0;
        for( TableElement element: getQueryExpression().getCreateViewStatement().getTableBody().getTableElements()) {
            if( !element.isConstraint()) {
                nViewColumns++;
            }
        }

        int nSelectColumns = getSelectColumns().length;
        if( allColumnsComplete() && !isStar && nViewColumns != nSelectColumns ) {
            Token firstToken = this.getFirstToken();
            Token lastToken = this.getLastToken();
            if( getSelectColumns().length > 0 ) {
                firstToken = getSelectColumns()[0].getFirstToken();
                lastToken = getSelectColumns()[getSelectColumns().length-1].getLastToken();
            }
            DdlAnalyzerException exception = this.analyzer.addException(
                    firstToken,
                    lastToken,
                    Messages.getString(Messages.Error.PROJECTED_SYMBOLS_VIEW_COLUMNS_MISMATCH,
                    nSelectColumns, nViewColumns));
            exception.setErrorCode(
                            QuickFixFactory.DiagnosticErrorId.PROJECTED_SYMBOLS_VIEW_COLUMNS_MISMATCH.getErrorCode());
        }
    }


     // SELECT ( ALL | DISTINCT )? ( <star> | ( <select sublist> ( <comma> <select sublist> )* ) )
    private void processSelectTokens() {
        // first token after SELECT
        if (hasNextIndex()) {
            if (isNextTokenOfKind(currentIndex(), SQLParserConstants.ALL)) {
                isAll = true;
                incrementIndex();
            } else if (isNextTokenOfKind(currentIndex(), SQLParserConstants.DISTINCT)) {
                isDistinct = true;
                incrementIndex();
            }
        }

        // Look for ALL or DISTINCT and *
        // Parse SelectColumn's
        // currentIndex should be the first column name/ID
        // incrementIndex();
        parseSelectColumns();
    }

    private void parseSelectColumns() {
        boolean isDone = false;

        if (hasNextIndex() && isNextTokenOfKind(currentIndex(), SQLParserConstants.STAR)) {
            this.isStar = true;
            isDone = true;
            incrementIndex();
        }

        while (!isDone) {
            if (functionIsNext(currentIndex())) {
                // Peek at next Token and see if it's Function or a
                SelectFunction selectFunction = new SelectFunction(super.analyzer, this);

                selectFunction.parseAndValidate();

                if (selectFunction.getLastTknIndex() > 0) {
                    if (selectFunction.getLastTknIndex() == getLastTknIndex()) {
                        isDone = true;
                    }

                    this.addSelectColumn(selectFunction);
                    setLastTknIndex(selectFunction.getLastTknIndex());
                    if (hasNextIndex() ) {
                        isDone = isNextTokenOfKind(currentIndex(), SQLParserConstants.FROM);
                    }
                } else {
                    isDone = true;
                }
            } else {
                // Peek at next Token and see if it's Function or a
                SelectColumn selectColumn = new SelectColumn(super.analyzer, this);

                selectColumn.parseAndValidate();
                if (selectColumn.getLastTknIndex() > 0) {
                    this.addSelectColumn(selectColumn);
                    setLastTknIndex(selectColumn.getLastTknIndex());
                    if (hasNextIndex() ) {
                        isDone = isTokenOfKind(currentIndex(), SQLParserConstants.FROM, SQLParserConstants.RPAREN);
                    } else {
                        isDone = true;
                    }
                } else {
                    isDone = true;
                }
            }

            if( isDone && !this.selectColumns.isEmpty() ) {
                // Check if last select column ends with COMMA token
                Token selectColumnEndTkn =
                        this.analyzer.getToken(this.selectColumns.get(this.selectColumns.size()-1).getLastTknIndex());
                if( selectColumnEndTkn.kind == SQLParserConstants.COMMA) {
                    this.analyzer.addException(
                            selectColumnEndTkn,
                            selectColumnEndTkn,
                            Messages.getString(Messages.Error.UNEXPECTED_COMMA))
                            .setErrorCode(
                            QuickFixFactory.DiagnosticErrorId.UNEXPECTED_COMMA.getErrorCode());
                }
            }
        }
    }

    public boolean functionIsNext(int startIndex) {
        if( hasAnotherToken(currentIndex()) && hasAnotherToken(currentIndex()+1) )  {
            Token idToken = getAnalyzer().getToken(startIndex+1);
            Token parenToken = getAnalyzer().getToken(startIndex + 2);
            return parenToken != null && parenToken.kind == SQLParserConstants.LPAREN
                && FunctionHelper.getInstance().isFunctionName(idToken.image);
        }

        return false;
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

    private boolean allColumnsComplete() {
        for( SelectColumn col: getSelectColumns()) {
            if( col.isIncomplete() ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256).append(" SELECT ");

        if( !selectColumns.isEmpty()) {
            for( SelectColumn column: selectColumns) {
                sb.append("").append(column);
            }
        } else if( isStar ) {
            sb.append("* ");
        }

        return sb.toString();
    }
}
