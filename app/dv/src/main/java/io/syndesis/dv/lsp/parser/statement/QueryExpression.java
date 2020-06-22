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

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.SQLParserConstants;
import org.teiid.query.parser.Token;

public class QueryExpression extends AbstractStatementObject {
    SelectClause selectClause;
    FromClause fromClause;
    WhereClause whereClause;
    CreateViewStatement createViewStatement;

    public QueryExpression(DdlTokenAnalyzer analyzer, CreateViewStatement createViewStatement) {
        super(analyzer);
        this.createViewStatement = createViewStatement;
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
        Token firstToken = findTokenByKind(SQLParserConstants.AS);

        if (firstToken == null) {
            this.analyzer
                    .addException(new DdlAnalyzerException("There is no 'AS' to project to your query expression"));
            return;
        }

        setFirstTknIndex(getTokenIndex(firstToken));
        setLastTknIndex(getTokenIndex(lastToken));

        // Process table body (i.e. columns definition)
        selectClause = new SelectClause(analyzer, this);
        selectClause.parseAndValidate();

        fromClause = new FromClause(analyzer);
        fromClause.parseAndValidate();

        whereClause = new WhereClause(analyzer, this);
        whereClause.parseAndValidate();
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

}
