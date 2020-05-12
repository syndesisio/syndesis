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
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public class QueryExpression extends AbstractStatementObject {
    SelectClause selectClause;
    FromClause fromClause;
    WhereClause whereClause;

    public QueryExpression(DdlTokenAnalyzer analyzer) {
        super(analyzer);
    }

    @Override
    protected void parseAndValidate() {
        // For now we're going to pull ALL the tokens from AS... on... except for the ';' if it exists

        Token[] tokens = getTokens();
        Token lastToken = tokens[tokens.length-1];
        if( tokens[tokens.length-1].kind == SEMICOLON ) {
            lastToken = tokens[tokens.length-2];
            setLastTknIndex(tokens.length-2);
        }
        Token firstToken = findTokenByKind(AS);

        if( firstToken == null ) {
            this.analyzer.addException(new DdlAnalyzerException("There is no 'AS' to project to your query expression"));
        }

        setFirstTknIndex(getTokenIndex(firstToken));
        setLastTknIndex(getTokenIndex(lastToken));

        // Process table body (i.e. columns definition)
        selectClause = new SelectClause(analyzer);
        selectClause.parseAndValidate();

        fromClause = new FromClause(analyzer);
        fromClause.parseAndValidate();

        whereClause = new WhereClause(analyzer);
        whereClause.parseAndValidate();
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        Token tkn = this.analyzer.getTokenFor(position);

        if( selectClause != null ) {
            TokenContext theContext = selectClause.getTokenContext(position);
            if( theContext != null) {
                return theContext;
            }
        }

        if( fromClause != null ) {
            TokenContext theContext = fromClause.getTokenContext(position);
            if( theContext != null) {
                return theContext;
            }
        }

        if( whereClause != null ) {
            TokenContext theContext = whereClause.getTokenContext(position);
            if( theContext != null) {
                return theContext;
            }
        }

        return new TokenContext(position, tkn, CONTEXT.QUERY_EXPRESSION, this);
    }
}
