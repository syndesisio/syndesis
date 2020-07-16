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

import io.syndesis.dv.lsp.Messages;
import io.syndesis.dv.lsp.codeactions.QuickFixFactory;
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

        if ( hasNextIndex() && isNextTokenOfKind(currentIndex(), SQLParserConstants.ID)) {
            incrementIndex();
            this.functionNameToken = getCurrentToken();
            setFirstTknIndex(currentIndex());
            setLastTknIndex(currentIndex());
        }

        if ( hasNextIndex() && isNextTokenOfKind(currentIndex(), SQLParserConstants.LPAREN)) {
            List<Token> functionParameterTkns = getBracketedTokens(getTokens(), currentIndex()+1,
                    SQLParserConstants.LPAREN, SQLParserConstants.RPAREN);
            int nTkns = functionParameterTkns.size();
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
            setIndex(currentIndex() + nTkns);
            if (hasNextIndex() && isNextTokenOfKind(currentIndex(), SQLParserConstants.AS)) {
                incrementIndex();
                if (hasNextIndex()) {
                    incrementIndex();
                    // get the alias name and save the actual name to definition token
                    if( isTokenOfKind(currentIndex(), SQLParserConstants.ID)) {
                        Token aliasNameTkn = getCurrentToken();
                        setAliasNameToken(aliasNameTkn);
                        setLastTknIndex(currentIndex());
                    } else {
                        Token tmpTkn = getCurrentToken();
                        this.analyzer.addException(tmpTkn, tmpTkn, "Missing or invalid alias for column name [" + tmpTkn.image + "]");
                                setLastTknIndex(currentIndex());
                    }
                }

                if (hasNextIndex() && isNextTokenOfKind(currentIndex(), SQLParserConstants.COMMA, SQLParserConstants.SEMICOLON)) {
                    incrementIndex();
                }
                setLastTknIndex(currentIndex());
            } else if (hasNextIndex() && isNextTokenOfKind(currentIndex(),
                        SQLParserConstants.COMMA,
                        SQLParserConstants.SEMICOLON)) {
                incrementIndex();
                setLastTknIndex(currentIndex());
            }
            // Have not found valid next/expected token?
            // if != FROM or != COMMA or != ID/STRINGVAL/UNSIGNEDINTEGER
            if (hasNextIndex() && !isNextTokenOfKind(currentIndex(),
                    SQLParserConstants.FROM,
                    SQLParserConstants.ID,
                    SQLParserConstants.STRINGVAL,
                    SQLParserConstants.UNSIGNEDINTEGER)) {
                    Token nextTkn = getToken(nextIndex());
                    this.analyzer.addException(
                            nextTkn,
                            nextTkn,
                            Messages.getString(Messages.Error.INVALID_TOKEN, nextTkn.image))
                                .setErrorCode(QuickFixFactory.DiagnosticErrorId.INVALID_TOKEN.getErrorCode());
                    incrementIndex();
                    if (hasNextIndex() && !isNextTokenOfKind(currentIndex(),
                            SQLParserConstants.FROM)) {
                        this.analyzer.addException(
                                this.functionNameToken,
                                this.functionNameToken,
                                Messages.getString(Messages.Error.INVALID_COLUMN_MISSING_COMMA, this.functionNameToken.image))
                                    .setErrorCode(QuickFixFactory.DiagnosticErrorId.INVALID_COLUMN_MISSING_COMMA.getErrorCode());
                    }
            } else if (hasNextIndex() &&
                    (getCurrentToken().kind != SQLParserConstants.COMMA && !isNextTokenOfKind(currentIndex(),
                    SQLParserConstants.FROM))) {
                this.analyzer.addException(
                        this.functionNameToken,
                        this.functionNameToken,
                        Messages.getString(Messages.Error.INVALID_COLUMN_MISSING_COMMA, this.functionNameToken.image))
                            .setErrorCode(QuickFixFactory.DiagnosticErrorId.INVALID_COLUMN_MISSING_COMMA.getErrorCode());
            }
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
