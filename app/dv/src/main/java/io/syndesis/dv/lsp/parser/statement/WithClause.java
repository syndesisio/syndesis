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

import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.SQLParserConstants;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.Messages;
import io.syndesis.dv.lsp.codeactions.QuickFixFactory;
import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public class WithClause extends AbstractStatementObject {
    private final List<WithListElement> withListElements;

    public WithClause(DdlTokenAnalyzer analyzer) {
        super(analyzer);
        this.withListElements = new ArrayList<WithListElement>();
    }

    @Override
    protected void parseAndValidate() {
        // SAMPLE:
        //  with a (x, y, z) as (select e1, e2, e3 from pm1.g1) SELECT pm1.g2.e2, a.x from pm1.g2, a where e1 = x and z = 1 order by x
        //  WITH X (Y, Z) AS (SELECT 1, 2), a (x, y, z) as (select e1, e2, e3 from pm1.g1), .... SELECT
        //  WITH <with-list-element>, <with-list-element> SELECT
        //                    | REQUIRED----------->| OPTIONAL-QUERY-EXPR->|
        //  WithListElement:  nameOrId ( c1, c2,...) AS SELECT (column1, column2)
        Token withToken = findTokenByKind(SQLParserConstants.WITH);

        if( withToken == null ) {
            return;
        } else {
            setIndex(getTokenIndex(withToken));
        }

        // Parse SelectColumn's
        processWithListElements(withToken);

        if( withListElements.isEmpty() ) {
            setLastTknIndex(getFirstTknIndex());
            setIndex(getLastTknIndex());
        } else {
            WithListElement lastElement = withListElements.get(withListElements.size()-1);
            setLastTknIndex(lastElement.getLastTknIndex());
            setIndex(getLastTknIndex());
        }

    }

    private void processWithListElements(Token withToken) {
        int tknIndex = currentIndex();
        if (withToken != null) {
            setFirstTknIndex(tknIndex);
            incrementIndex();

            // 1) Find the next nameOrId token
            // 2) nextTkn == (
            // 3) get bracketed tokens and add them all
            // 4) check if next token is "," then repeat 1), 2) and 3)
            // 5) if nextTkn == SELECT, then we are finished
            // 6) set last Tkn to the last ")" token

            boolean isDone = false;
            while( !isDone) {
                // THIS CLAUSE IS REPEATED AND COMMA SEPARATED:
                //      a (x, y, z) as (select e1, e2, e3 from pm1.g1)
                Token firstTkn = getToken(currentIndex());
                if( firstTkn != null ) {
                    WithListElement element = new WithListElement(analyzer, firstTkn);
                    element.parseAndValidate();

                    if( element.getLastTknIndex() > 0 ) {
                        withListElements.add(element);
                        setLastTknIndex(element.getLastTknIndex());
                        isDone = element.getLastToken().kind != SQLParserConstants.COMMA;
                    } else {
                        isDone = true;
                    }

                } else {
                    isDone = true;
                }
                if (!isDone) {
                    incrementIndex();
                }
            }

            if( withListElements.isEmpty() ) {
                this.analyzer.addException(
                        withToken,
                        withToken,
                        Messages.getString(Messages.Error.NO_WITH_LIST_ELEMENTS, getLabel(SQLParserConstants.AS, true)))
                        .setErrorCode(
                        QuickFixFactory.DiagnosticErrorId.NO_WITH_LIST_ELEMENTS.getErrorCode());
            }
        }
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        boolean isInClause = isBetween(getFirstTknIndex(), getLastTknIndex(), position);
        if (isInClause) {
            // Need to check each SELECT column
            for (WithListElement element : this.withListElements) {
                TokenContext context = element.getTokenContext(position);
                if (context != null) {
                    return context;
                }
            }
            Token tkn = this.analyzer.getTokenFor(position);
            return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.WITH_CLAUSE, this);
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
