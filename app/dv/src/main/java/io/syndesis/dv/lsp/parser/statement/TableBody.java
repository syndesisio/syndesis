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

import io.syndesis.dv.lsp.Messages;
import io.syndesis.dv.lsp.codeactions.QuickFixFactory;
import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.SQLParserConstants;
import org.teiid.query.parser.Token;

public class TableBody extends AbstractStatementObject {
    private final List<TableElement> elements;
    private TableOptionsClause options;
    private boolean hasPrimaryKey;
    CreateViewStatement createViewStatement;

    public TableBody(DdlTokenAnalyzer analyzer, CreateViewStatement createViewStatement) {
        super(analyzer);
        this.elements = new ArrayList<TableElement>();
        this.createViewStatement = createViewStatement;
    }

    public TableElement[] getTableElements() {
        return elements.toArray(new TableElement[0]);
    }

    public void addTableElement(TableElement tableElement) {
        this.elements.add(tableElement);
    }

    public TableOptionsClause getOptions() {
        return options;
    }

    public void setOptions(TableOptionsClause options) {
        this.options = options;
    }

    @Override
    protected void parseAndValidate() {
        List<Token> tableBodyTokens = getBracketedTokens(getTokens(), 3, SQLParserConstants.LPAREN, SQLParserConstants.RPAREN);

        setFirstTknIndex(getTokenIndex(tableBodyTokens.get(0)));
        setLastTknIndex(getTokenIndex(tableBodyTokens.get(tableBodyTokens.size() - 1)));

        // Process table body (i.e. columns definition)
        // Check for table body content
        if (getLastTknIndex() == getFirstTknIndex() + 1) {
            // NO TABLE BODY
            Token firstToken = getTokens().get(getFirstTknIndex());
            Token lastToken = getTokens().get(getLastTknIndex());
            this.analyzer.addException(firstToken, lastToken, "No columns defined in table body");
        } else {
            parseTableElements();
        }

        // Parse Table Options

        // Create index for Token after table body () but it may be NULL
        int iTkn = 3 + tableBodyTokens.size();
        // check if index == MAX INDEX
        if( iTkn > getTokens().size()-1) { return; }

        Token nextTkn = getTokens().get(iTkn);

        if (nextTkn.kind == SQLParserConstants.OPTIONS) {
            List<Token> optionsTkns = new ArrayList<Token>();
            optionsTkns.add(nextTkn);

            // Check for parens in case of string(), decimal() types.. etc
            Token lastTkn = nextTkn;
            if (isNextTokenOfKind(getTokens(), iTkn, SQLParserConstants.LPAREN)) {
                List<Token> bracketedTkns = getBracketedTokens(getTokens(), iTkn + 1, SQLParserConstants.LPAREN, SQLParserConstants.RPAREN);
                for (Token optionsTkn : bracketedTkns) {
                    iTkn++;
                    optionsTkns.add(optionsTkn);
                    lastTkn = optionsTkn;
                }
            }

            if (!optionsTkns.isEmpty()) {
                TableOptionsClause options = new TableOptionsClause(analyzer);
                options.setOptionsTokens(optionsTkns);
                setOptions(options);
                options.setFirstTknIndex(getTokenIndex(nextTkn));
                options.setLastTknIndex(getTokenIndex(lastTkn));
            }

        }
    }

    private void parseTableElements() {
        boolean isDone = false;

        while (!isDone) {
            TableElement tableElement = new TableElement(super.analyzer, this);

            tableElement.parseAndValidate();

            if (tableElement.getLastTknIndex() > 0) {
                if (tableElement.getLastTknIndex() == getLastTknIndex() - 1) {
                    isDone = true;
                }

                this.addTableElement(tableElement);
                if (tableElement.isPKElement() || tableElement.isPrimaryKey()) {
                    setHasPrimaryKey(true);
                }
            }
        }

        // Check to see if last TableElement ends with a ',' token and add exception
        // if it does
        if( getTableElements().length > 0 ) {
            TableElement lastElement = getTableElements()[getTableElements().length-1];
            if( lastElement.getLastToken().kind == SQLParserConstants.COMMA) {
                DdlAnalyzerException exception = this.analyzer.addException(
                        lastElement.getLastToken(),
                        lastElement.getLastToken(),
                        Messages.getString(Messages.Error.UNEXPECTED_COMMA));
                exception.setErrorCode(
                        QuickFixFactory.DiagnosticErrorId.UNEXPECTED_COMMA.getErrorCode());
            }
        }
    }

    public static boolean isDatatype(Token token) {
        return DdlAnalyzerConstants.DATATYPES.contains(token.kind);
    }

    public boolean hasPrimaryKey() {
        return hasPrimaryKey;
    }

    public void setHasPrimaryKey(boolean hasPrimaryKey) {
        this.hasPrimaryKey = hasPrimaryKey;
    }

    @Override
    protected TokenContext getTokenContext(Position position) {

        TokenContext context = null;
        // Need to check each table element
        for (TableElement element : getTableElements()) {
            context = element.getTokenContext(position);
            if (context != null) {
                return context;
            }
        }

        Token tkn = this.analyzer.getTokenFor(position);
        int index = getTokenIndex(tkn);
        if (index == getFirstTknIndex()) {
            return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.TABLE_BODY, this);
        }
        if (index == getLastTknIndex()) {
            return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.TABLE_BODY, this);
        }

        // Check options
        if (options != null) {
            return options.getTokenContext(position);
        }

        return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.TABLE_BODY, this);
    }

    public CreateViewStatement getCreateViewStatement() {
        return createViewStatement;
    }

}
