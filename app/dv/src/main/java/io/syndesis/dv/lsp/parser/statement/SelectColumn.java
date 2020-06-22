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

import com.google.common.base.Splitter;

import io.syndesis.dv.lsp.Messages;
import io.syndesis.dv.lsp.codeactions.QuickFixFactory;
import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

/*
 * Select column is a column name defined as
 * 1) a constant (string, integer, etc...)
 * 2) a columnName
 * 3) a tableName.columnName
 * 4) an aliased name of the form: tableName.columnName AS foobar
 * 5) an aliased function:  concat(name1, name2) AS foobar
 *    - where a function can be a complex function
 *    - EXAMPLE: concat(name1, concat(' ', name2)) AS foobar
 */
public class SelectColumn extends AbstractStatementObject {
    private Token nameToken;
    private Token aliasNameToken; // OPTIONAL
    private Token literalValueToken; // OPTIONAL

    private final SelectClause selectClause;

    public SelectColumn(DdlTokenAnalyzer analyzer, SelectClause selectClause) {
        super(analyzer);
        this.selectClause = selectClause;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"}) // TODO refactor
    protected void parseAndValidate() {
        // Find starting index
        int selectClauselastIndex = selectClause.getLastTknIndex();

        int startIndex = selectClause.getFirstTknIndex() + 1;
        if (selectClause.isAll() || selectClause.isDistinct()) {
            startIndex++;
        }

        // check for previous table elements and reset the startIndex
        int nSelectColumns = selectClause.getSelectColumns().length;
        if (nSelectColumns > 0) {
            startIndex = selectClause.getSelectColumns()[nSelectColumns - 1].getLastTknIndex() + 1;
        }

        int currentTknIndex = startIndex;
        boolean columnEnded = startIndex > selectClauselastIndex;

        while (!columnEnded) {
            // LOOKING FOR : Table1 AS t1
            Token tkn = this.getTokens().get(currentTknIndex);
            if (currentTknIndex > selectClauselastIndex) {
                columnEnded = true;
                setLastTknIndex(selectClauselastIndex - 1);
            } else {
                if (tkn.kind == SQLParserConstants.STRINGVAL || tkn.kind == SQLParserConstants.UNSIGNEDINTEGER) {
                    this.literalValueToken = tkn;
                    if (getFirstTknIndex() == 0) {
                        setFirstTknIndex(getTokenIndex(tkn));
                    }
                    currentTknIndex++;
                } else if (tkn.kind == SQLParserConstants.ID) {
                    this.nameToken = tkn;
                    if (getFirstTknIndex() == 0) {
                        setFirstTknIndex(getTokenIndex(tkn));
                    }
                    currentTknIndex++;
                    // CHECK IF unfinished aliased ID (i.e. t2. , as opposed to t2.xyz)
                    if (currentTknIndex <= selectClauselastIndex) {
                        Token extraTkn = this.getTokens().get(currentTknIndex);
                        if (extraTkn.kind == SQLParserConstants.PERIOD) {
                            setLastTknIndex(getTokenIndex(extraTkn));
                            currentTknIndex++;
                        }
                    }
                }
                // Check for alias (AS) token
                if (currentTknIndex < selectClauselastIndex) {
                    tkn = this.getTokens().get(currentTknIndex);
                    if (tkn.kind == SQLParserConstants.AS) {
                        currentTknIndex++;
                        tkn = this.getTokens().get(currentTknIndex);
                        if (getTokenIndex(tkn) <= selectClauselastIndex) {
                            Token columnNameToken = getNameToken();
                            this.nameToken = columnNameToken;
                            this.aliasNameToken = tkn;
                        }
                        if (currentTknIndex < selectClauselastIndex) {
                            currentTknIndex++;
                            tkn = this.getTokens().get(currentTknIndex);
                            if (tkn.kind == SQLParserConstants.COMMA) {
                                // Since there is a comma, another TableSymbol is expected
                                setLastTknIndex(getTokenIndex(tkn));
                                columnEnded = true;
                            } else {
                                setLastTknIndex(currentTknIndex - 1);
                                columnEnded = true;
                            }
                        } else {
                            setLastTknIndex(getTokenIndex(tkn));
                            columnEnded = true;
                        }
                    } else if (tkn.kind == SQLParserConstants.COMMA) {
                        // Since there is a comma, another TableSymbol is expected
                        setLastTknIndex(getTokenIndex(tkn));
                        columnEnded = true;
                    }
                } else if (tkn.kind == SQLParserConstants.COMMA) {
                    // looks like there's an extra comma which should be ignored
                    columnEnded = true;
                    setFirstTknIndex(0);
                    setLastTknIndex(0);
                    DdlAnalyzerException exception = this.analyzer.addException(
                            tkn,
                            tkn,
                            Messages.getString(Messages.Error.UNEXPECTED_COMMA));
                    exception.setErrorCode(
                            QuickFixFactory.DiagnosticErrorId.UNEXPECTED_COMMA.getErrorCode());
                } else {
                    setLastTknIndex(getTokenIndex(tkn));
                    columnEnded = true;
                }
            }
            currentTknIndex++;
        }
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        boolean isInObject = isBetween(getFirstTknIndex(), getLastTknIndex(), position);
        if (isInObject) {
            Token tkn = this.analyzer.getTokenFor(position);

            // Check for last token == COMMA since comma is part of the table element token
            if (tkn.kind == SQLParserConstants.COMMA
                    && getTokens().get(getLastTknIndex()).kind == SQLParserConstants.COMMA
                    && getTokenIndex(tkn) == getLastTknIndex()) {
                // return the From Clause context
                return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.SELECT_CLAUSE, this);
            } else if (tkn.kind == SQLParserConstants.PERIOD) {
                Token aliasNamePrefixToken = getTokens().get(getTokenIndex(tkn) - 1);
                // check previous token and look for valid table alias
                for (TableSymbol nextSymbol : getSelectClause().getQueryExpression().getFromClause()
                        .getTableSymbols()) {
                    if (nextSymbol.isAliased() && nextSymbol.getAlias().equalsIgnoreCase(aliasNamePrefixToken.image)) {
                        return new TokenContext(position, aliasNamePrefixToken,
                                DdlAnalyzerConstants.Context.TABLE_ALIAS, this);
                    }
                }
            }

            return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.SELECT_COLUMN, this);
        }

        return null;
    }

    public String getTableName() {
        String fullNameToken = getNameToken().image;

        List<String> parts = Splitter.on('.').splitToList(fullNameToken);
        if (parts.size() == 2) {
            return parts.get(0);
        }

        return null;
    }

    public String getColumnName() {
        String fullNameToken = getNameToken().image;

        List<String> parts = Splitter.on('.').splitToList(fullNameToken);
        if (parts.size() == 2) {
            return parts.get(1);
        }

        return parts.get(0);
    }

    public Token getNameToken() {
        return nameToken;
    }

    public void setAliasNameToken(Token tkn) {
        this.aliasNameToken = tkn;
    }

    public Token getAliasNameToken() {
        return aliasNameToken;
    }

    public Token getLiteralValueToken() {
        return literalValueToken;
    }

    public SelectClause getSelectClause() {
        return selectClause;
    }

}
