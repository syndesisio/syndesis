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

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public class WhereClause extends AbstractStatementObject {
    private final QueryExpression queryExpression;

    public WhereClause(DdlTokenAnalyzer analyzer, QueryExpression queryExpression) {
        super(analyzer);
        this.queryExpression = queryExpression;
    }

    public QueryExpression getQueryExpression() {
        return this.queryExpression;
    }

    @Override
    protected void parseAndValidate() {
        if( hasNextIndex() && isNextTokenOfKind(currentIndex(), SQLParserConstants.WHERE) ) {
            incrementIndex();
            setFirstTknIndex(currentIndex());
            setLastTknIndex(getTokenIndex(getTokens().get(getTokens().size() - 1)));

//            if (whereToken != null) {
//                setLastTknIndex(getTokenIndex(whereToken) - 1);
//            } else {
//                Token orderToken = findTokenByKind(SQLParserConstants.ORDER);
//                Token byToken = findTokenByKind(SQLParserConstants.BY);
//                if( orderToken != null && byToken != null &&
//                    (getTokenIndex(byToken) == (getTokenIndex(orderToken)+1)) ) {
//                    setLastTknIndex(getTokenIndex(byToken) -1);
//                }
//                else {
//                    List<Token> tokens = getTokens();
//                    if (tokens.get(tokens.size() - 1).kind == SQLParserConstants.SEMICOLON) {
//                        setLastTknIndex(tokens.size() - 2);
//                    }
//                }
//            }
        } else if( queryExpression instanceof WithQueryExpression ) {
            setLastTknIndex(0);
            setLastTknIndex(0);
        }
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        boolean isInClause = isBetween(getFirstTknIndex(), getLastTknIndex(), position);
        if (isInClause) {
            Token tkn = this.analyzer.getTokenFor(position);
            if (tkn.kind == SQLParserConstants.PERIOD) {
                Token aliasNamePrefixToken = getTokens().get(getTokenIndex(tkn) - 1);
                // check previous token and look for valid table alias
                for (TableSymbol nextSymbol : getQueryExpression().getFromClause().getTableSymbols()) {
                    if (nextSymbol.isAliased() && nextSymbol.getAlias().equalsIgnoreCase(aliasNamePrefixToken.image)) {
                        return new TokenContext(position, aliasNamePrefixToken,
                                DdlAnalyzerConstants.Context.WHERE_CLAUSE_TABLE_ALIAS, this);
                    }
                }
            }

            if (this.analyzer.isPositionInToken(position, tkn)) {
                return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.WHERE_CLAUSE, this);
            } else {
                return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.QUERY_EXPRESSION, this);
            }
        }
        return null;
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
