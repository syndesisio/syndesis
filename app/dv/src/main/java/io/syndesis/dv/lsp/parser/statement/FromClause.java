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

import java.util.List;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public class FromClause extends AbstractStatementObject {

    public FromClause(DdlTokenAnalyzer analyzer) {
        super(analyzer);
    }

    @Override
    protected void parseAndValidate() {
        processFromTokens();
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        boolean isInClause = isBetween(getFirstTknIndex(), getLastTknIndex(), position);
        if( isInClause ) {
            Token tkn = this.analyzer.getTokenFor(position);
            return new TokenContext(position, tkn, DdlAnalyzerConstants.CONTEXT.FROM_CLAUSE, this);
        }
        return null;
    }

    private void processFromTokens() {
        // Look for SELECT and FROM tokens
        Token fromToken = findTokenByKind(SQLParserConstants.FROM);
        if(fromToken != null ) {
            setFirstTknIndex(getTokenIndex(fromToken));
        } else {
            this.analyzer.addException(new DdlAnalyzerException("Parsing Exception:  there is no 'FROM' keyword in your query expression"));
            return;
        }

        // We have the FROM
        // Look for OPTIONAL  >> WHERE

        Token whereToken = findTokenByKind(SQLParserConstants.WHERE);
        if(whereToken != null ) {
            setLastTknIndex(getTokenIndex(whereToken)-1);
        } else {
            List<Token> tokens = getTokens();
            if( tokens.get(tokens.size()-1).kind == SQLParserConstants.SEMICOLON ) {
                setLastTknIndex(tokens.size()-2);
            } else {
                setLastTknIndex(tokens.size()-1);
            }
        }
    }
}
