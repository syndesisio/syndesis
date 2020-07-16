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

import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public class WithQueryExpression extends QueryExpression {

    public WithQueryExpression(DdlTokenAnalyzer analyzer) {
        super(analyzer);
        this.isWithQuery = true;
    }

    @Override
    protected void parseAndValidate() {
        // For now we're going to pull ALL the tokens from AS... on... except for the
        // ';' if it exists

        Token firstToken = getToken(getFirstTknIndex());

        if (firstToken == null) {
            this.analyzer
                    .addException(new DdlAnalyzerException("There is no '(' to surround to your with query expression"));
            return;
        }

        setIndex(getFirstTknIndex());

        // Process query expression clauses
        selectClause = new SelectClause(analyzer, this);
        selectClause.parseAndValidate();

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
    }
}
