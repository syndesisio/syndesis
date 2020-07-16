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

import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.SQLParserConstants;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.Messages;
import io.syndesis.dv.lsp.codeactions.QuickFixFactory;
import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

@SuppressWarnings({"PMD.NPathComplexity"})
public class WithListElement extends AbstractStatementObject {
    private TableBody tableBody;
    private WithQueryExpression queryExpression;
    private final Token firstToken;

    public WithListElement(DdlTokenAnalyzer analyzer, Token firstToken) {
        super(analyzer);
        this.firstToken = firstToken;
    }

    @Override
    protected void parseAndValidate() {
        setFirstTknIndex(currentIndex());
        setLastTknIndex(currentIndex());
        // FIRST token should be an ID
        if( firstToken.kind != SQLParserConstants.ID && firstToken.kind != SQLParserConstants.STRINGVAL) {
            this.analyzer.addException(
                    getCurrentToken(),
                    getCurrentToken(),
                    Messages.getString(Messages.Error.INVALID_TABLE_NAME, firstToken.image))
                    .setErrorCode(
                    QuickFixFactory.DiagnosticErrorId.INVALID_TABLE_NAME.getErrorCode());
        }

        // 1) Find the next nameOrId token
        // 2) LOOK for nextTkn == ( (i.e. TABLE BODY is OPTIONAL)
        // 3) get bracketed tokens and add them all
        // 4) check if next token is "," then repeat 1), 2) and 3)
        // 5) if nextTkn == SELECT, then we are finished
        // 6) set last Tkn to the last ")" token

        // THIS CLAUSE IS REPEATED AND COMMA SEPARATED:
        //      a (x, y, z) as (select e1, e2, e3 from pm1.g1)
        // PARSE TABLE BODY >>  ( x, y, z)
        // Bracketed tokens
        if (hasNextIndex() && isNextTokenOfKind(currentIndex(), SQLParserConstants.LPAREN)) {
            List<Token> bracketedTkns = getBracketedTokens(getTokens(), currentIndex() + 1, SQLParserConstants.LPAREN,
                    SQLParserConstants.RPAREN);
            if (bracketedTkns.isEmpty()) {
                setLastTknIndex(currentIndex() + 1);
                // TODO:  logIncompleteWithClauseException(nextTkn, getLastToken());
            }  else {
                incrementIndex();
                tableBody = new TableBody(analyzer, null);
                tableBody.parseAndValidate();
                setIndex(tableBody.getLastTknIndex());
            }
        }

        if (hasNextIndex()) {
            if( isNextTokenOfKind(currentIndex(), SQLParserConstants.AS)) {
                incrementIndex();
            } else {
                Token nextTkn = getToken(nextIndex());
                this.analyzer.addException(
                        nextTkn,
                        nextTkn,
                        Messages.getString(Messages.Error.INVALID_TOKEN_EXPECTING_XXX_, getLabel(SQLParserConstants.AS, true)))
                        .setErrorCode(
                        QuickFixFactory.DiagnosticErrorId.INVALID_TOKEN_EXPECTING_XXX_.getErrorCode());
            }
        }

        if (hasNextIndex() && isNextTokenOfKind(currentIndex(), SQLParserConstants.LPAREN)) {
            List<Token> bracketedTkns = getBracketedTokens(getTokens(), currentIndex() + 1, SQLParserConstants.LPAREN,
                    SQLParserConstants.RPAREN);
            if (bracketedTkns.isEmpty()) {
                setLastTknIndex(currentIndex() + 1);
                // TODO:  logIncompleteWithClauseException(nextTkn, getLastToken());
            }  else {
                queryExpression = new WithQueryExpression(analyzer);
                queryExpression.setFirstTknIndex(currentIndex() + 1);
                queryExpression.setLastTknIndex(currentIndex() + bracketedTkns.size());
                queryExpression.parseAndValidate();
                setIndex(queryExpression.getLastTknIndex());
            }
        } else {
            logIncompleteWithListElement(getToken(currentIndex()));
        }

        if (hasNextIndex() && isNextTokenOfKind(currentIndex(), SQLParserConstants.COMMA)) {
            incrementIndex();
            setLastTknIndex(currentIndex());
        } else {
            setLastTknIndex(currentIndex());
        }
    }

    private void logIncompleteWithListElement(Token tkn) {
        this.analyzer.addException(
                getFirstToken(),
                tkn,
                Messages.getString(Messages.Error.INCOMPLETE_WITH_LIST_ELEMENT))
                .setErrorCode(
                QuickFixFactory.DiagnosticErrorId.INCOMPLETE_WITH_LIST_ELEMENT.getErrorCode());
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        boolean isInClause = isBetween(getFirstTknIndex(), getLastTknIndex(), position);
        if (isInClause) {
            if( tableBody != null && isBetween(tableBody.getFirstTknIndex(),
                    tableBody.getLastTknIndex(), position) ) {
               // TABLE BODY
               return tableBody.getTokenContext(position);
            }
            if(queryExpression != null &&
                    isBetween(queryExpression.getFirstTknIndex(),
                    queryExpression.getLastTknIndex(), position) ) {
               // TABLE BODY
               return queryExpression.getTokenContext(position);
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
