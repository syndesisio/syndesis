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

import io.syndesis.dv.lsp.Messages;
import io.syndesis.dv.lsp.codeactions.QuickFixFactory;
import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.SQLParserConstants;
import org.teiid.query.parser.Token;

public class QueryExpression extends AbstractStatementObject {
    SelectClause selectClause;
    FromClause fromClause;
    WhereClause whereClause;
    CreateViewStatement createViewStatement;
    boolean isWithQuery;

    public QueryExpression(DdlTokenAnalyzer analyzer, CreateViewStatement createViewStatement) {
        super(analyzer);
        this.createViewStatement = createViewStatement;
    }

    public QueryExpression(DdlTokenAnalyzer analyzer) {
        super(analyzer);
    }

    @Override
    protected void parseAndValidate() {
        // For now we're going to pull ALL the tokens from AS... on... except for the
        // ';' if it exists

        List<Token> tokens = getTokens();
        Token lastToken = tokens.get(tokens.size() - 1);
        if (tokens.get(tokens.size() - 1).kind == SQLParserConstants.SEMICOLON) {
            lastToken = tokens.get(tokens.size() - 2);
            setLastTknIndex(tokens.size() - 2);
        }
        // Assume the current token is AS

        Token firstToken = getCurrentToken();
        if (this.createViewStatement.getWithClause() == null
            && (firstToken == null || !isTokenOfKind(currentIndex(), SQLParserConstants.AS))) {
                this.analyzer.addException(
                        getCurrentToken(),
                        getCurrentToken(),
                        Messages.getString(Messages.Error.QUERY_EXPRESSION_MISSING_AS))
                        .setErrorCode(
                        QuickFixFactory.DiagnosticErrorId.QUERY_EXPRESSION_MISSING_AS.getErrorCode());
                return;
        }

        setFirstTknIndex(getTokenIndex(firstToken));
        setLastTknIndex(getTokenIndex(lastToken));

        // Process query expression clauses
        selectClause = new SelectClause(analyzer, this);
        selectClause.parseAndValidate();
        if( selectClause.getLastTknIndex() == 0 ) {
            selectClause = null;
        }

        fromClause = new FromClause(analyzer, this);
        fromClause.parseAndValidate();
        if( fromClause.getLastTknIndex() == 0 ) {
            fromClause = null;
        }

        whereClause = new WhereClause(analyzer, this);
        whereClause.parseAndValidate();
        if( whereClause.getLastTknIndex() == 0 ) {
            whereClause = null;
        }

        if( !isLastIndex()) {
            setLastTknIndex(getLastTknIndex());
        }
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        boolean isInElement = isBetween(getFirstTknIndex(), getLastTknIndex(), position);
        if (isInElement) {
            if (selectClause != null) {
                TokenContext theContext = selectClause.getTokenContext(position);
                if (theContext != null) {
                    return theContext;
                }
            }

            if (fromClause != null) {
                TokenContext theContext = fromClause.getTokenContext(position);
                if (theContext != null) {
                    return theContext;
                }
            }

            if (whereClause != null) {
                TokenContext theContext = whereClause.getTokenContext(position);
                if (theContext != null) {
                    return theContext;
                }
            } else {
                Token tkn = this.analyzer.getTokenFor(position);
                return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.WHERE_CLAUSE_START, this);
            }
            Token tkn = this.analyzer.getTokenFor(position);
            return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.QUERY_EXPRESSION, this);
        }

        return null;
    }

    public SelectClause getSelectClause() {
        return selectClause;
    }

    public FromClause getFromClause() {
        return fromClause;
    }

    public WhereClause getWhereClause() {
        return whereClause;
    }

    public CreateViewStatement getCreateViewStatement() {
        return createViewStatement;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(75);

        append(selectClause, sb);
        append(fromClause, sb);
        append(whereClause, sb);

        return sb.toString();
    }
}
