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

public class SelectClause extends AbstractStatementObject {

    public SelectClause(DdlTokenAnalyzer analyzer) {
        super(analyzer);
    }

    @Override
    protected void parseAndValidate() {
        // Look for SELECT and FROM tokens
        Token selectToken = findTokenByKind(SQLParserConstants.SELECT);
        if(selectToken != null ) {
            setFirstTknIndex(getTokenIndex(selectToken));
        } else {
            this.analyzer.addException(new DdlAnalyzerException("There is no 'SELECT' in your query expression"));
        }

        // We have the SELECT
        // Look for FROM

        Token fromToken = findTokenByKind(SQLParserConstants.FROM);
        if(fromToken != null ) {
            setLastTknIndex(getTokenIndex(fromToken)-1);
        } else {
            // Assume that the SELECT ends at either the last token before the semicolon
            // or the last token if semi-colon isn't there
            List<Token> tokens = getTokens();
            if( tokens.get(tokens.size()-1).kind == SQLParserConstants.SEMICOLON ) {
                setLastTknIndex(tokens.size()-2);
            }
        }

        // populate the list of selectClauseTokens
//        List<Token> tkns = new ArrayList<Token>();
//        for( int i=getFirstTknIndex(); i<getLastTknIndex()-1; i++) {
//            tkns.add(getTokens()[i]);
//        }
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        boolean isInClause = isBetween(getFirstTknIndex(), getLastTknIndex(), position);
        if( isInClause ) {
            Token tkn = this.analyzer.getTokenFor(position);
            return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.SELECT_CLAUSE, this);
        }
        return null;
    }
}
