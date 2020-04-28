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
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public class TableBody extends AbstractStatementObject {
    private Token[] tableBodyTokens;
    private List<TableElement> elements;
    private TableOptionsClause options;
    private boolean hasPrimaryKey;

    public TableBody(DdlTokenAnalyzer analyzer) {
        super(analyzer);
        this.elements = new ArrayList<TableElement>();
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

        tableBodyTokens = getBracketedTokens(getTokens(), 3, LPAREN, RPAREN);

        setFirstTknIndex(getTokenIndex(tableBodyTokens[0]));
        setLastTknIndex(getTokenIndex(tableBodyTokens[tableBodyTokens.length - 1]));

        // Process table body (i.e. columns definition)
        // Check for table body content
        if (getLastTknIndex() == getFirstTknIndex() + 1) {
            // NO TABLE BODY
            Token firstToken = getTokens()[getFirstTknIndex()];
            Token lastToken = getTokens()[getLastTknIndex()];
            this.analyzer.addException(firstToken, lastToken, "No columns defined in table body");
        } else {
            parseTableElements();
        }

        // Parse Table Options

        // Create index for Token after table body () but it may be NULL
        int iTkn = 3 + tableBodyTokens.length;
        // check if index == MAX INDEX
        if( iTkn > getTokens().length-1) { return; }

        Token nextTkn = getTokens()[iTkn];

        if (nextTkn.kind == OPTIONS) {
            List<Token> optionsTkns = new ArrayList<Token>();
            optionsTkns.add(nextTkn);

            // Check for parens in case of string(), decimal() types.. etc
            Token lastTkn = nextTkn;
            if (isNextTokenOfKind(getTokens(), iTkn, LPAREN)) {
                Token[] bracketedTkns = getBracketedTokens(getTokens(), iTkn + 1, LPAREN, RPAREN);
                if (bracketedTkns.length > 0) {
                    for (Token optionsTkn : bracketedTkns) {
                        iTkn++;
                        optionsTkns.add(optionsTkn);
                        lastTkn = optionsTkn;
                    }
                }
            }

            if (!optionsTkns.isEmpty()) {
                TableOptionsClause options = new TableOptionsClause(analyzer);
                options.setOptionsTokens(optionsTkns.toArray(new Token[0]));
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
    }

    public boolean isDatatype(Token token) {
        for (int dType : DATATYPES) {
            if (token.kind == dType) {
                return true;
            }
        }
        return false;
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
            return new TokenContext(position, tkn, CONTEXT.TABLE_BODY, this);
        }
        if (index == getLastTknIndex()) {
            return new TokenContext(position, tkn, CONTEXT.TABLE_BODY, this);
        }

        // Check options
        if (options != null) {
            return options.getTokenContext(position);
        }

        return new TokenContext(position, tkn, CONTEXT.TABLE_BODY, this);
    }
}
