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

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public class CreateViewStatement extends AbstractStatementObject {
    private Token viewNameToken;
    private TableBody tableBody;
    private QueryExpression queryExpression;
    private final int numTokens;

    public CreateViewStatement(DdlTokenAnalyzer analyzer) {
        super(analyzer);

        tableBody = new TableBody(analyzer);
        queryExpression = new QueryExpression(analyzer);
        this.numTokens = this.analyzer.getTokens().length;

        parseAndValidate();
    }

    public String getStatement() {
        return this.analyzer.getStatement();
    }

    public String getViewName() {
        return viewNameToken.image;
    }

    public Token getViewNameToken() {
        return viewNameToken;
    }

    public TableBody getTableBody() {
        return tableBody;
    }

    public void setTableBody(TableBody tableBody) {
        this.tableBody = tableBody;
    }

    public QueryExpression getQueryExpression() {
        return queryExpression;
    }

    public void setQueryExpression(QueryExpression queryExpression) {
        this.queryExpression = queryExpression;
    }

    @Override
    protected void parseAndValidate() {
        // Check statement
        boolean prefixOK = true;

        // Check view name exists
        if( numTokens == 1) {
            setFirstTknIndex(0);
            setLastTknIndex(0);

            Token onlyToken = this.analyzer.getTokens()[0];
            prefixOK = !onlyToken.image.equalsIgnoreCase("CREATE");
            Position startPosition = new Position(onlyToken.beginLine, onlyToken.beginColumn);
            Position endPosition = new Position(onlyToken.endLine, onlyToken.endColumn+1);
            DdlAnalyzerException exception =
                    new DdlAnalyzerException(
                            DiagnosticSeverity.Error,
                            "CREATE VIEW STATEMENT is INCOMPLETE",
                            new Range(startPosition, endPosition)); //$NON-NLS-1$);

            this.analyzer.addException(exception);
            return;
        }

        if( prefixOK && numTokens == 2) {
            setFirstTknIndex(0);
            setLastTknIndex(1);

            Token firstToken = this.analyzer.getTokens()[0];
            Token lastToken = this.analyzer.getTokens()[0];
            prefixOK = !lastToken.image.equalsIgnoreCase("VIEW");
            this.analyzer.addException(
                    firstToken,
                    lastToken,
                    "CREATE VIEW STATEMENT is INCOMPLETE");

            return;
        }

        if(!prefixOK) {
            Token firstToken = this.analyzer.getTokens()[0];
            Token lastToken = this.analyzer.getTokens()[1];
            this.analyzer.addException(
                    firstToken,
                    lastToken,
                    "Statement must start with CREATE VIEW");
        }

        this.viewNameToken = getToken(2);
        Token token = getToken(2);
        if( token == null) {
            Token firstToken = getToken(2);
            Token lastToken = getToken(2);
            this.analyzer.addException(
                    firstToken,
                    lastToken,
                    "Valid view name is missing after : " + getToken(1));
        } else {
            if( token.kind == ID || token.kind == STRINGVAL ) {
                this.viewNameToken = token;
            } else {
                String msg = "View name '" + viewNameToken.image + "' is invalid ";
                if( isReservedKeywordToken(viewNameToken) ) {
                    msg = "View name '" + viewNameToken.image + "' is a reserved word and must be wrapped in double quotes \"\" ";
                }
                this.analyzer.addException(this.viewNameToken, this.viewNameToken, msg);
            }
        }

        setFirstTknIndex(0);
        setLastTknIndex(2);

        // Check brackets match
        if( !isOk(this.analyzer.checkAllParens()) ) {
            this.analyzer.addException(
                    getToken(3), getToken(numTokens-1), "All parenthesis DO NOT MATCH");
            this.analyzer.getReport().setParensMatch(false);
        }

        if( !isOk(this.analyzer.checkAllBrackets(LBRACE, RBRACE)) ) {
            this.analyzer.addException(new DdlAnalyzerException("All braces DO NOT MATCH"));
            this.analyzer.getReport().setBracesMatch(false);
        }

        // Check Table Body
        // If token[4] == '(' then search for
        if( numTokens < 4 ) {
            Token firstToken = getToken(0);
            Token lastToken = getToken(2);
            this.analyzer.addException(
                    firstToken,
                    lastToken,
                    "The CREATE VIEW... statement is incomplete");
        }

        if( numTokens > 3) {
            if( getTokenValue(3).equals("(") && this.analyzer.getReport().doParensMatch() ) {
                parseTableBody();
                queryExpression.parseAndValidate();
            } else {
                // Table Body is NOT required so check for token[3] == AS
                if( getToken(3).kind == AS) {
                    queryExpression.parseAndValidate();
                } else {
                    Token firstToken = getToken(3);
                    Token lastToken = getToken(numTokens-1);
                    this.analyzer.addException(
                            firstToken,
                            lastToken,
                            "The CREATE VIEW... statement is incomplete");
                }
            }
        }
    }

    private void parseTableBody() {
        tableBody.setFirstTknIndex(4);
        // Now parse each table element
        // Break up table body into TableElements based on finding a "comma"
        tableBody.parseAndValidate();
    }

    private boolean isOk(DdlAnalyzerException exception) {
        if( exception == null ) {
            return true;
        }

        this.analyzer.addException(exception);

        return false;
    }

    private String getTokenValue(int tokenIndex) {
        return getToken(tokenIndex).image;
    }

    private Token getToken(int tokenIndex) {
        return analyzer.getToken(tokenIndex);
    }

    public List<DdlAnalyzerException> getExceptions() {
        return this.analyzer.getExceptions();
    }

    @Override
    public TokenContext getTokenContext(Position position) {
        if( isBetween(getFirstTknIndex(), getLastTknIndex(), position) ) {
            // PREFIX token
            return new TokenContext(position, this.analyzer.getTokenFor(position), CONTEXT.PREFIX, this);
        } else if( isBetween(tableBody.getFirstTknIndex(),
                             tableBody.getLastTknIndex(), position) ) {
            // TABLE BODY
            return tableBody.getTokenContext(position);
        } else if( tableBody.getOptions() != null &&
                   isBetween(tableBody.getOptions().getFirstTknIndex(),
                             tableBody.getOptions().getLastTknIndex()+1, position) ) {
            // TABLE OPTIONS
            return tableBody.getOptions().getTokenContext(position);
        } else if( isBetween(queryExpression.getFirstTknIndex(),
                             queryExpression.getLastTknIndex(), position) ) {
            return queryExpression.getTokenContext(position);
        }

        return new TokenContext(position, null, CONTEXT.NONE_FOUND, this);
    }
}
