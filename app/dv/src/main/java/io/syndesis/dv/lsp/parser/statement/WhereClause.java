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

import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public class WhereClause extends AbstractStatementObject {

    public WhereClause(DdlTokenAnalyzer analyzer) {
        super(analyzer);
    }

    @Override
    protected void parseAndValidate() {
        // TODO Auto-generated method stub
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        boolean isInClause = isBetween(getFirstTknIndex(), getLastTknIndex(), position);
        if( isInClause ) {
            Token tkn = this.analyzer.getTokenFor(position);
            return new TokenContext(position, tkn, CONTEXT.WHERE_CLAUSE, this);
        }
        return null;
    }
}
