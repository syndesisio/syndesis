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

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.teiid.query.parser.SQLParserConstants;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

/**
 * A FunctionColumn represents a column reference in the SELECT clause
 *
 * Basic form is: func('1', col1) 1) coalesce(op1, op2, op3) 2) substr(string,
 * index, length) etc...
 */
public class SelectFunction extends SelectColumn {
    // NOTE that parameters may be various object types, depending on function
    // definition
    // Could be literal string, integer, etc... including a function
    List<Object> parameters;
    Token functionNameToken;

    public SelectFunction(DdlTokenAnalyzer analyzer, SelectClause selectClause) {
        super(analyzer, selectClause);
        this.parameters = new ArrayList<Object>();
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"}) // TODO refactor
    protected void parseAndValidate() {
        // Find starting index
        int selectClauselastIndex = getSelectClause().getLastTknIndex();

        int startIndex = getSelectClause().getFirstTknIndex() + 1;
        if (getSelectClause().isAll() || getSelectClause().isDistinct()) {
            startIndex++;
        }

        // check for previous table elements and reset the startIndex
        int nSelectColumns = getSelectClause().getSelectColumns().length;
        if (nSelectColumns > 0) {
            startIndex = getSelectClause().getSelectColumns()[nSelectColumns - 1].getLastTknIndex() + 1;
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
                if (tkn.kind == SQLParserConstants.ID) {
                    this.functionNameToken = tkn;
                    if (getFirstTknIndex() == 0) {
                        setFirstTknIndex(getTokenIndex(tkn));
                    }
                    currentTknIndex++;
                }
                // Check for alias (AS) token
                if (currentTknIndex < selectClauselastIndex) {
                    tkn = this.getTokens().get(currentTknIndex);
                    if (tkn.kind == SQLParserConstants.LPAREN) {
//                      int nParamTkns = handleFunctionParameters(tkn, currentTknIndex, selectClauselastIndex);
                        // Parse until we find a RPAREN or we hit the selectClauselastIndex
                        List<Token> functionParameterTkns = getBracketedTokens(getTokens(), currentTknIndex,
                                SQLParserConstants.LPAREN, SQLParserConstants.RPAREN);
                        // 1) check last index so it isn't past the SELECT clause
                        int nTkns = functionParameterTkns.size();
                        if (getTokenIndex(functionParameterTkns.get(nTkns - 1)) < selectClauselastIndex) {
                            // now count the parameters
                            for (Token paramTkn : functionParameterTkns) {
                                if (paramTkn.kind == SQLParserConstants.ID) {
                                    if (FunctionHelper.getInstance().isFunctionName(paramTkn.image)) {
                                        Position startPosition = new Position(paramTkn.beginLine, paramTkn.beginColumn);
                                        Position endPosition = new Position(paramTkn.endLine, paramTkn.endColumn + 1);
                                        DdlAnalyzerException exception = new DdlAnalyzerException(
                                                DiagnosticSeverity.Warning, "NESTED Functions are not yet supported",
                                                new Range(startPosition, endPosition));

                                        this.analyzer.addException(exception);
                                    } else {
                                        this.parameters.add(paramTkn.image);
                                    }
                                } else if (paramTkn.kind == SQLParserConstants.UNSIGNEDINTEGER) {
                                    this.parameters.add(paramTkn.image);
                                }
                            }
                        }
                        currentTknIndex = currentTknIndex + nTkns; // nParamTkns; // RPARENS token
                        tkn = this.getTokens().get(currentTknIndex);
                    }
                    if (tkn.kind == SQLParserConstants.AS) {
                        currentTknIndex++;
                        tkn = this.getTokens().get(currentTknIndex);
                        if (getTokenIndex(tkn) <= selectClauselastIndex) {
                            setAliasNameToken(tkn);
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
                } else {
                    setLastTknIndex(getTokenIndex(tkn));
                    columnEnded = true;
                }
            }
            currentTknIndex++;
        }
    }

//    private int handleFunctionParameters(Token inToken, int currentTknIndex, int selectClauselastIndex) {
//        // Parse until we find a RPAREN or we hit the selectClauselastIndex
//        List<Token> functionParameterTkns = getBracketedTokens(getTokens(), currentTknIndex,
//                SQLParserConstants.LPAREN, SQLParserConstants.RPAREN);
//        // 1) check last index so it isn't past the SELECT clause
//        int nTkns = functionParameterTkns.size();
//        if (getTokenIndex(functionParameterTkns.get(nTkns - 1)) < selectClauselastIndex) {
//            // now count the parameters
//            for (Token paramTkn : functionParameterTkns) {
//                if (paramTkn.kind == SQLParserConstants.ID) {
//                    if (FunctionHelper.getInstance().isFunctionName(paramTkn.image)) {
//                        Position startPosition = new Position(paramTkn.beginLine, paramTkn.beginColumn);
//                        Position endPosition = new Position(paramTkn.endLine, paramTkn.endColumn + 1);
//                        DdlAnalyzerException exception = new DdlAnalyzerException(
//                                DiagnosticSeverity.Warning, "NESTED Functions are not yet supported",
//                                new Range(startPosition, endPosition));
//
//                        this.analyzer.addException(exception);
//                    } else {
//                        this.parameters.add(paramTkn.image);
//                    }
//                } else if (paramTkn.kind == SQLParserConstants.UNSIGNEDINTEGER) {
//                    this.parameters.add(paramTkn.image);
//                }
//            }
//        }
//        return nTkns;
//    }

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
                return new TokenContext(position, aliasNamePrefixToken, DdlAnalyzerConstants.Context.SELECT_COLUMN,
                        this);
            }

            return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.SELECT_COLUMN, this);
        }

        return null;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public Token getFunctionNameToken() {
        return functionNameToken;
    }

}
